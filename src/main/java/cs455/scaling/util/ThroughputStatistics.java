package cs455.scaling.util;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class ThroughputStatistics {
    
    private final AtomicInteger numMessages = new AtomicInteger(0);
    private final AtomicInteger numClientConnections = new AtomicInteger(0);
    
    public void printAndResetStatistics() {
        
        // Create temporary variables for our atomic ones.
        final double x = numMessages.getAndSet(0),
                y = numClientConnections.getAndSet(0),
                mean = x / 1,
                stdDev = (1 - mean);
        
        System.out.print("["+ new Date().toString() +"] ");
        System.out.print("Server Throughput: "+ x +" message(s), ");
        System.out.print("Active Client Connections: "+ y +" ");
        System.out.format("Mean Per-client Throughput: %.3f message(s), ", mean);
        System.out.format("Standard Deviation of Per-client Throughput: %.3f message(s)\n", stdDev);
    }

}
