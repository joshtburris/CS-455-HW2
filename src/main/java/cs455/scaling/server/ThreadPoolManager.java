package cs455.scaling.server;

import java.util.*;

public class ThreadPoolManager {
    
    private LinkedList<Runnable> queue;
    private final ThreadPool threadPool;
    private final int batchSize;
    private final long batchTime;
    private Timer timer = new Timer();
    private final Object lock = new Object();
    
    public ThreadPoolManager(int threadPoolSize, int batchSize, long batchTime) {
        this.batchSize = batchSize;
        this.batchTime = batchTime;
        queue = new LinkedList<>();
        threadPool = new ThreadPool(threadPoolSize);
    }
    
    public void queueTask(Runnable task, boolean priority) {
        synchronized (lock) {
        
            queue.offer(task);
        
            if (queue.size() == 1 && batchSize > 1) {
                timer.schedule(new BatchPushTimerTask(), batchTime);
            }
        
            if (queue.size() >= batchSize || priority) {
                pushQueue();
            }
        }
    }
    
    public void queueTask(Runnable task) {
        queueTask(task, false);
    }
    
    private void pushQueue() {
        synchronized (lock) {
            threadPool.queueTasks(queue);
            queue = new LinkedList<>();
    
            if (batchSize > 1) {
                timer.cancel();
                timer = new Timer();
            }
        }
    }
    
    private class BatchPushTimerTask extends TimerTask {
        
        @Override public void run() {
            synchronized (lock) {
                if (queue.size() > 0) {
                    pushQueue();
                }
            }
        }
        
    }

}
