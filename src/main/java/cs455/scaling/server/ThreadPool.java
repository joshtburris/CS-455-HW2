package cs455.scaling.server;

import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPool {

    private final LinkedBlockingQueue<Runnable> queue;
    private final WorkerThread[] workerThreads;
    
    /**
     * ThreadPool Constructor that initializes a number of Thread's equal to the <code>size</code> parameter. You are
     * then able to queue tasks by calling the <code>queueTask(Runnable)</code> method, which will then be completed
     * by the next available thread in the pool.
     * @param size
     */
    public ThreadPool(int size) {
        queue = new LinkedBlockingQueue<>();
        workerThreads = new WorkerThread[size];
        
        for (int i = 0; i < size; ++i) {
            workerThreads[i] = new WorkerThread();
            workerThreads[i].start();
        }
    }
    
    /**
     * This is a blocking method that will only return when the task has been added to the queue.
     * @param task
     */
    public void queueTask(Runnable task) {
        // Add the task to the queue with put() because this will block until there is space in the queue.
        try {
            queue.put(task);
        } catch (InterruptedException ie) {
            System.out.println("InterruptedException: Unable to add task to the queue.");
        }
    }
    
    /**
     * This method essentially just calls <code>interrupt()</code> on all of the threads in the pool.
     */
    public void interruptAll() {
        for (int i = 0; i < workerThreads.length; ++i) {
            workerThreads[i].interrupt();
        }
    }
    
    private class WorkerThread extends Thread {
    
        @Override public void run() {
            
            // We want this to be a continuously running thread that polls from the queue and then runs the task.
            while (!isInterrupted()) {
                
                // Calling queue.take() will block until there is something in the queue to poll.
                // Once we have successfully polled from the queue we are free to run that task, AKA task.run().
                Runnable task;
                try {
                    task = queue.take();
                    task.run();
                } catch (InterruptedException ie) {
                    // Do nothing. This will happen if the thread pool is closed and we want to shut down gracefully.
                }
                
            }
        }
    }

}
