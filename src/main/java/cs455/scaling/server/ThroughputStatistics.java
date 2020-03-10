package cs455.scaling.server;

import java.util.*;
import java.util.Map.Entry;

public class ThroughputStatistics {
    
    private final TreeMap<String, Integer> map;
    
    public ThroughputStatistics() {
        map = new TreeMap<>();
    }
    
    public void incrementNumMessages(String client) {
        synchronized (map) {
            Integer get = map.get(client);
            map.put(client, (get != null) ? get + 1 : 1);
        }
    }
    
    public void registerNewClient(String client) {
        synchronized (map) {
            map.put(client, 0);
        }
    }
    
    public void printAndReset() {
        
        // Create temporary variables for our atomic ones.
        int x = 0, y;
        double mean, stdDev = 0.0;
        
        synchronized (map) {
            for (Integer i : map.values()) {
                x += i;
            }
            
            y = map.size();
            mean = (y != 0) ? (double)x / (double)y : 0;
            
            for (Entry<String, Integer> e : map.entrySet()) {
                stdDev += Math.pow(e.getValue().doubleValue() - mean, 2);
                map.put(e.getKey(), 0);
            }
        }
        
        stdDev = (y != 0 && y != 1) ? Math.sqrt( (1.0/((double)y-1.0)) * stdDev ) : 0.0;
        
        System.out.print("["+ new Date().toString() +"] ");
        System.out.print("Server Throughput: "+ x +" message(s), ");
        System.out.print("Active Client Connections: "+ y +", ");
        System.out.format("Mean Per-client Throughput: %.3f message(s), ", mean);
        System.out.format("Standard Deviation of Per-client Throughput: %.3f message(s)\n", stdDev);
    }

}
