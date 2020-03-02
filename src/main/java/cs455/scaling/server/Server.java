package cs455.scaling.server;

import cs455.scaling.util.Constants;
import cs455.scaling.util.ThroughputStatistics;

import java.io.IOException;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

/**
 * There is exactly one server node in the system. The server node provides the following functions:
 *      A. Accepts incoming network connections from the clients.
 *      B. Accepts incoming traffic from these connections.
 *      C. Groups data from the clients together into batches.
 *      D. Replies to clients by sending back a hash code for each message received.
 *      E. The server performs functions A, B, C, and D by relying on the thread pool.
 */
public class Server implements Node {
    
    private final int batchSize, batchTime;
    private final ThreadPool threadPool;
    private final ThroughputStatistics stats;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    
    public Server(int portnum, int threadPoolSize, int batchSize, int batchTime) {
        this.batchSize = batchSize;
        this.batchTime = batchTime;
        stats = new ThroughputStatistics();
        threadPool = new ThreadPool(threadPoolSize);
    
        try {
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
        } catch (IOException ioe) {
            System.out.println("IOException: Server may not have been given ");
        }
        
        
        while (serverChannel != null && !serverChannel.socket().isBound()) {
            try {
                serverChannel.socket().bind(new InetSocketAddress(portnum));
                serverChannel.configureBlocking(false);
                serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    
                String[] address = serverChannel.getLocalAddress().toString().split(":");
                System.out.println("Server was given port: " + address[address.length - 1]);
            } catch (IOException ioe) {
                ++portnum;
            }
        }
        
    }
    
    public void start() {
    
        while (true) {
            
            try {
                if (selector.selectNow() == 0) continue;
            } catch (IOException ioe) {
                continue;
            }
    
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                
                if (key.isAcceptable() && key.attachment() == null) {
                    
                    RegisterClientTask task = new RegisterClientTask(selector, serverChannel, key);
                    key.attach(task);
                    threadPool.queueTask(task);
                    
                } else if (key.isReadable() && key.attachment() == null) {
    
                    ReadMessageTask task = new ReadMessageTask(key);
                    key.attach(task);
                    threadPool.queueTask(task);
                    
                }
                
                iter.remove();
            }
        
        }
    
    }
    
    private class RegisterClientTask implements Runnable {
        
        Selector selector;
        ServerSocketChannel serverChannel;
        SelectionKey key;
    
        public RegisterClientTask(Selector selector, ServerSocketChannel serverChannel, SelectionKey key) {
            this.selector = selector;
            this.serverChannel = serverChannel;
            this.key = key;
        }
    
        @Override public void run() {
            try {
                
                SocketChannel client = serverChannel.accept();
                client.configureBlocking(false);
                client.register(selector, SelectionKey.OP_READ);
                key.attach(null);
                
            } catch (Exception ioe) {
                System.out.println("IOException: Unable to accept new client from the ServerSocketChannel.");
            }
        }
    }
    
    private class ReadMessageTask implements Runnable {
        
        final ByteBuffer buffer;
        final SelectionKey key;
        final SocketChannel client;
        
        public ReadMessageTask(SelectionKey key) {
            this.buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
            this.key = key;
            this.client = (SocketChannel) key.channel();
        }
        
        @Override public void run() {
            try {
                
                int len, bytesRead = 0;
                while (buffer.hasRemaining() && bytesRead != -1) {
                    bytesRead = client.read(buffer);
                }
                
                buffer.flip();
                
                while (buffer.hasRemaining()) {
                    client.write(buffer);
                }
                
                key.attach(null);
                buffer.clear();
                
            } catch (IOException ioe) {
                System.out.println("IOException: Unable to write buffer to client.");
            }
        }
    }
    
    // TODO
    public void printAndResetStatistics() {
        stats.printAndResetStatistics();
    }
    
    public static void main(String[] args) {
    
        // Extract our required arguments and assign them to the correct variables to be used in the server constructor.
        int portnum, threadPoolSize, batchSize, batchTime;
        try {
            
            portnum = Integer.parseInt(args[0]);
            threadPoolSize = Integer.parseInt(args[1]);
            batchSize = Integer.parseInt(args[2]);
            batchTime = Integer.parseInt(args[3]);
            
        } catch (Exception e) {
            System.out.println("ERROR: Given arguments were incorrect.");
            return;
        }
    
        // Initialize the server with our arguments that we just parsed.
        Server server = new Server(portnum, threadPoolSize, batchSize, batchTime);
    
        // Create and start a new timer task to call the server to print and reset it's statistics every 20 seconds.
        Timer timer = new Timer();
        //timer.scheduleAtFixedRate(new PrintStatsTask(server), 20000L, 20000L);
        
        // Meanwhile, we are going to be running the server on this main thread so call it's start() method.
        server.start();
    
    }
    
    private static class PrintStatsTask extends TimerTask {
    
        private final Server server;
        
        public PrintStatsTask(Server server) {
            this.server = server;
        }
        
        @Override public void run() {
            server.printAndResetStatistics();
            System.out.println();
        }
    }
    
}
