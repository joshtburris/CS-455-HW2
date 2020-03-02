package cs455.scaling.server;

import java.util.*;

public class ThreadPoolManager {
    
    private LinkedList<Runnable> queue;
    private final ThreadPool threadPool;
    private final int batchSize;
    private final long batchTime;
    private Timer timer;
    
    public ThreadPoolManager(int threadPoolSize, int batchSize, long batchTime) {
        this.batchSize = batchSize;
        this.batchTime = batchTime;
        queue = new LinkedList<>();
        threadPool = new ThreadPool(threadPoolSize);
        timer = new Timer();
    }
    
    public void queueTask(Runnable task) {
        synchronized (queue) {
            
            queue.offer(task);
            
            if (queue.size() == 1 && batchSize > 1) {
                timer.schedule(new BatchPushTimerTask(), batchTime);
            }
            
            if (queue.size() >= batchSize) {
                timer.cancel();
                threadPool.queueTasks(queue);
                queue = new LinkedList<>();
                timer = new Timer();
                System.out.println("Queue was pushed to thread pool and timer was reset.");
            }
        }
    }
    
    private class BatchPushTimerTask extends TimerTask {
        
        @Override public void run() {
            synchronized (queue) {
                if (queue.size() > 0) {
                    threadPool.queueTasks(queue);
                    queue = new LinkedList<>();
                    timer.cancel();
                    timer = new Timer();
                    System.out.println("Queue was pushed to thread pool because it exceeded its allotted batch time.");
                }
            }
        }
        
    }

}
