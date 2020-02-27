package cs455.scaling.server;

import cs455.scaling.util.ThroughputStatistics;

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
    
    public Server(int portnum, int threadPoolSize, int batchSize, int batchTime) {
        this.portnum = portnum;
        this.threadPoolSize = threadPoolSize;
        this.batchSize = batchSize;
        this.batchTime = batchTime;
        stats = new ThroughputStatistics();
        threadPool = new ThreadPool(threadPoolSize);
        
        
    }
    
    // TODO
    @Override public void run() {
    
    
    
    }
    
    private class PrintHello implements Runnable {
        int i;
        public PrintHello(int i) {
            this.i = i;
        }
        @Override
        public void run() {
            System.out.println("Hello "+ i);
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
