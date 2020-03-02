package cs455.scaling.server;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPool {

    private final LinkedBlockingQueue<LinkedList<Runnable>> queue;
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
     * This is a non-blocking method that will return whether or not the task has been added to the queue.
     * @param tasks
     * @return true if the task was added to the queue, false otherwise.
     */
    public boolean queueTasks(LinkedList<Runnable> tasks) {
        // Add the task to the queue with add() because this will not block but will tell you if your task was
        // added successfully.
        if (tasks.size() == 0)
            return false;
        return queue.add(tasks);
    }
    
    private class WorkerThread extends Thread {
    
        @Override public void run() {
            
            // We want this to be a continuously running thread that polls from the queue and then runs the task.
            while (!isInterrupted()) {
                
                // Calling queue.take() will block until there is something in the queue to poll.
                // Once we have successfully polled from the queue we are free to run that list of tasks,
                // AKA task.run().
                try {
                    for (Runnable task : queue.take()) {
                        task.run();
                    }
                } catch (InterruptedException ie) {
                    // Do nothing. This will happen if the thread pool is closed and we want to shut down gracefully.
                }
                
            }
        }
    }

}
