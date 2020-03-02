package cs455.scaling.server;

import java.util.*;

public class ThreadPoolManager {
    
    private LinkedList<Runnable> queue;
    private final ThreadPool threadPool;
    private final int batchSize;
    private final long batchTime;
    private final Timer timer;
    
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
            
            if (queue.size() == 1) {
                timer.schedule(new BatchPushTimerTask(), batchTime);
            }
            
            if (queue.size() == batchSize) {
                timer.cancel();
                threadPool.queueTasks(queue);
                queue = new LinkedList<>();
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
                }
            }
        }
        
    }

}
