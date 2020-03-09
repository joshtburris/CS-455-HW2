package cs455.scaling.client;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class MessagingStatistics {
    
    private final AtomicInteger totalSent = new AtomicInteger(0);
    private final AtomicInteger totalReceived = new AtomicInteger(0);
    
    public MessagingStatistics() { }
    
    public void incrementNumSent() {
        totalSent.incrementAndGet();
    }
    
    public void incrementNumReceived() {
        totalReceived.incrementAndGet();
    }
    
    public void print() {
        System.out.print("["+ new Date().toString() +"] ");
        System.out.print("Total Sent Count: "+ totalSent.get() +", ");
        System.out.println("Total Received Count: "+ totalReceived.get());
    }
    
}
