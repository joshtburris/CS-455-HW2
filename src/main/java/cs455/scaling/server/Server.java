package cs455.scaling.server;

public class Server implements Runnable {
    
    private int portnum, threadPoolSize, batchSize, batchTime;
    
    public Server(int portnum, int threadPoolSize, int batchSize, int batchTime) {
        this.portnum = portnum;
        this.threadPoolSize = threadPoolSize;
        this.batchSize = batchSize;
        this.batchTime = batchTime;
    }
    
    @Override public void run() {
    
    }
    
    public void printAndResetStatistics() {
    
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
            server.printAndResetStatistics();
            System.out.println();
            try {
                Thread.sleep(20 * 1000);
            } catch (InterruptedException ie) {
                System.out.println(ie.getMessage());
            }
        }
    
    }
    
}
