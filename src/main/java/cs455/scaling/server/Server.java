package cs455.scaling.server;

import cs455.scaling.util.ThroughputStatistics;

import java.io.IOException;
import java.net.*;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * There is exactly one server node in the system. The server node provides the following functions:
 *      A. Accepts incoming network connections from the clients.
 *      B. Accepts incoming traffic from these connections.
 *      C. Groups data from the clients together into batches.
 *      D. Replies to clients by sending back a hash code for each message received.
 *      E. The server performs functions A, B, C, and D by relying on the thread pool.
 */
public class Server implements Node, Runnable {
    
    private final int portnum, threadPoolSize, batchSize, batchTime;
    private final ThreadPool threadPool;
    private final ThroughputStatistics stats;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    
    public Server(int portnum, int threadPoolSize, int batchSize, int batchTime) {
        this.portnum = portnum;
        this.threadPoolSize = threadPoolSize;
        this.batchSize = batchSize;
        this.batchTime = batchTime;
        stats = new ThroughputStatistics();
        threadPool = new ThreadPool(threadPoolSize);
    
        
        try {
    
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.socket().bind(new InetSocketAddress(portnum));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    
            
            
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
        
    }
    
    // TODO
    @Override public void run() {
    
        while (true) {
            
            try {
                if (selector.selectNow() == 0) continue;
            } catch (IOException ioe) {
                continue;
            }
    
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                if (key.isAcceptable()) {
                    
                    try {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        System.out.println(socketChannel.socket().getInetAddress().getHostName());
                        System.out.println(socketChannel.socket().getLocalSocketAddress().toString());
                        key.attach(null);
                    } catch (IOException ioe) {
                    
                    }
                
                } else if (key.isReadable()) {
                
                
                
                }
                iter.remove();
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
        
        // Initialize the server with our arguments that we just parsed, pass the server instance to a new thread
        // constructor, and call start() on the thread. This will spawn a new thread to take care of our server.
        Server server = new Server(portnum, threadPoolSize, batchSize, batchTime);
        Thread t = new Thread(server);
        t.start();
    
        // Meanwhile this thread is going to be printing server throughput statistics every 20 seconds.
        while (true) {
            try {
                Thread.sleep(20 * 1000);
            } catch (InterruptedException ie) {
                System.out.println(ie.getMessage());
            }
            
            server.printAndResetStatistics();
            System.out.println();
        }
    
    }
    
}
