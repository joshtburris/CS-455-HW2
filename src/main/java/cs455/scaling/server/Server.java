package cs455.scaling.server;

import cs455.scaling.util.*;

import java.io.IOException;
import java.net.*;
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
public class Server {
    
    private final ThreadPoolManager threadPoolManager;
    public final ThroughputStatistics stats;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    
    public Server(int portnum, int threadPoolSize, int batchSize, long batchTime) {
        stats = new ThroughputStatistics();
        threadPoolManager = new ThreadPoolManager(threadPoolSize, batchSize, batchTime);
    
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
                    
                    RegisterClientTask task = new RegisterClientTask(selector, serverChannel, key, stats);
                    key.attach(task);
                    threadPoolManager.queueTask(task, true);
                    
                } else if (key.isReadable() && key.attachment() == null) {
    
                    ReadMessageTask task = new ReadMessageTask(key, stats);
                    key.attach(task);
                    threadPoolManager.queueTask(task);
                    
                }
                
                iter.remove();
            }
        
        }
    
    }
    
    private class RegisterClientTask implements Runnable {
        
        private final Selector selector;
        private final ServerSocketChannel serverChannel;
        private final SelectionKey key;
        private final ThroughputStatistics stats;
    
        public RegisterClientTask(Selector selector, ServerSocketChannel serverChannel, SelectionKey key,
                                  ThroughputStatistics stats) {
            this.selector = selector;
            this.serverChannel = serverChannel;
            this.key = key;
            this.stats = stats;
        }
    
        @Override public void run() {
            try {
                
                SocketChannel client = serverChannel.accept();
                client.configureBlocking(false);
                client.register(selector, SelectionKey.OP_READ);
                key.attach(null);
                stats.registerNewClient(client.getRemoteAddress().toString());
                
            } catch (Exception ioe) {
                System.out.println("IOException: Unable to accept new client from the ServerSocketChannel, or " +
                        "socketChannel could not get remote address.");
            }
        }
    }
    
    private class ReadMessageTask implements Runnable {
        
        private final SelectionKey key;
        private final SocketChannel socketChannel;
        private final MessageStream messageStream;
        private final ThroughputStatistics stats;
        
        public ReadMessageTask(SelectionKey key, ThroughputStatistics stats) {
            this.key = key;
            this.socketChannel = (SocketChannel) key.channel();
            this.messageStream = new MessageStream(socketChannel);
            this.stats = stats;
        }
        
        @Override public void run() {
            
            byte[] byteArray = messageStream.readByteArray();
            String hash = Hashing.SHA1FromBytes(byteArray);
            
            messageStream.writeString(hash);
            key.attach(null);
            
            try {
                stats.incrementNumMessages(socketChannel.getRemoteAddress().toString());
            } catch (IOException ioe) {
                System.out.println("IOException: socketChannel could not get remote address.");
            }
            
        }
        
    }
    
    public static void main(String[] args) {
    
        // Extract our required arguments and assign them to the correct variables to be used in the server constructor.
        int portnum, threadPoolSize, batchSize;
        long batchTime;
        try {
            
            portnum = Integer.parseInt(args[0]);
            threadPoolSize = Integer.parseInt(args[1]);
            batchSize = Integer.parseInt(args[2]);
            batchTime = Long.parseLong(args[3]);
            
            if (portnum < 1024 || portnum > 65535 || threadPoolSize < 1 || batchSize < 1 || batchTime < 1)
                throw new Exception();
            
        } catch (Exception e) {
            System.out.println("ERROR: Given arguments were unusable.");
            return;
        }
    
        // Initialize the server with our arguments that we just parsed.
        Server server = new Server(portnum, threadPoolSize, batchSize, batchTime);
    
        // Create and start a new timer task to call the server to print and reset it's statistics every 20 seconds.
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new PrintStatsTimerTask(server.stats), 20000L, 20000L);
        
        // Meanwhile, we are going to be running the server on this main thread so call it's start() method.
        server.start();
    
    }
    
    private static class PrintStatsTimerTask extends TimerTask {
    
        private final ThroughputStatistics stats;
        
        public PrintStatsTimerTask(ThroughputStatistics stats) {
            this.stats = stats;
        }
        
        @Override public void run() {
            stats.printAndReset();
            System.out.println();
        }
    }
    
}
