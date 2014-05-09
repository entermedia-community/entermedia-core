package com.openedit.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openedit.OpenEditException;

public class ExecutorManager
{
	private static final Log log = LogFactory.getLog(ExecutorManager.class);
	
	protected ExecutorService fieldSharedExecutor; //will be unlimited
	protected Integer fieldThreadCount;
	
	public Integer getThreadCount()
	{
		if (fieldThreadCount == null)
		{
			fieldThreadCount = Runtime.getRuntime().availableProcessors();
			if( fieldThreadCount > 2 )
			{
				fieldThreadCount--;
			}
			fieldThreadCount = Math.max(fieldThreadCount, 4); //minimum of 4 threads
			fieldThreadCount = 2;
		}

		return fieldThreadCount;
	}

	public void setThreadCount(Integer inThreadCount)
	{
		fieldThreadCount = inThreadCount;
	}
	
	public ExecutorService createExecutor(int startThreads, int maxThreads) 
	{
		return createExecutor(startThreads, maxThreads, "Custom");
	}
	public ExecutorService createExecutor(int startThreads, int maxThreads, String inPrefix) 
	{
		DefaultThreadFactory factory = new DefaultThreadFactory(inPrefix);
		
		ScalingQueue queue = new ScalingQueue();
	    ThreadPoolExecutor executor = new ScalingThreadPoolExecutor(startThreads, maxThreads, 10L, TimeUnit.MINUTES, queue,factory);
	    executor.setRejectedExecutionHandler(new ForceQueuePolicy());
	    queue.setThreadPoolExecutor(executor);
		
//		ThreadPoolExecutor executer = new ThreadPoolExecutor(min, max,
//                10L, TimeUnit.MINUTES,
//                new LinkedBlockingQueue<Runnable>(),
//               factory,
//                new ThreadPoolExecutor.AbortPolicy());
//		executer.allowCoreThreadTimeOut(true);
		return executor;

	}
	
	class ForceQueuePolicy implements RejectedExecutionHandler {
	    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
	        try {
	            executor.getQueue().put(r);
	        } catch (InterruptedException e) {
	            //should never happen since we never wait
	            throw new RejectedExecutionException(e);
	        }
	    }
	}
	
	public ExecutorService getSharedExecutor()
	{
		if (fieldSharedExecutor == null)
		{
			fieldSharedExecutor =  createExecutor(2, Integer.MAX_VALUE, "Unlimited");
			//fieldSharedExecutor =  createExecutor(2, 2, "Unlimited");
		}
		return fieldSharedExecutor;
	}
	
	public void execute(Runnable inRunnable)
	{
		getSharedExecutor().execute(inRunnable);
	}
	public void waitForIt(ExecutorService inExec)
	{
		inExec.shutdown();
		try
		{
			inExec.awaitTermination(30L, TimeUnit.MINUTES);
		}
		catch (InterruptedException e)
		{
			throw new OpenEditException(e);
		}
		log.debug("Exec completed");
	}
	
	
	public class ScalingQueue extends LinkedBlockingQueue {
		/**
		The executor this Queue belongs to
		     */
		    private ThreadPoolExecutor executor;

		/**
		Creates a TaskQueue with a capacity of
			{@link Integer#MAX_VALUE}.
		     */
		    public ScalingQueue() {
		        super();
		    }

		/**
		Creates a TaskQueue with the given (fixed) capacity.
		     *
			@param capacity the capacity of this queue.
		     */
		    public ScalingQueue(int capacity) {
		        super(capacity);
		    }

		/**
		Sets the executor this queue belongs to.
		     */
		    public void setThreadPoolExecutor(ThreadPoolExecutor executor) {
		        this.executor = executor;
		    }

		/**
		Inserts the specified element at the tail of this queue if there is at
			least one available thread to run the current task. If all pool threads
			are actively busy, it rejects the offer.
		     *
			@param o the element to add.
			@return true if it was possible to add the element to this
			queue, else false
			@see ThreadPoolExecutor#execute(Runnable)
		     */
		    public boolean offer(Object o) {
		        int allWorkingThreads = executor.getActiveCount() + super.size();
		        return allWorkingThreads < executor.getPoolSize() && super.offer(o);
		    }		}
	
	

public class ScalingThreadPoolExecutor extends ThreadPoolExecutor {
    
    /**
     * number of threads that are actively executing tasks
     */
    private final AtomicInteger activeCount = new AtomicInteger();

    public ScalingThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                                     long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    @Override
    public int getActiveCount() {
        return activeCount.get();
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        activeCount.incrementAndGet();
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        activeCount.decrementAndGet();
    }
}

static class DefaultThreadFactory implements ThreadFactory {
    static final AtomicInteger poolNumber = new AtomicInteger(1);
    final ThreadGroup group;
    final AtomicInteger threadNumber = new AtomicInteger(1);
    final String namePrefix;

    DefaultThreadFactory(String inPrefix) 
    {
        SecurityManager s = System.getSecurityManager();
        group = (s != null)? s.getThreadGroup() :
                             Thread.currentThread().getThreadGroup();
        namePrefix = inPrefix + "pool-" +
                      poolNumber.getAndIncrement() +
                     "-thread-";
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                              namePrefix + threadNumber.getAndIncrement(),
                              0);
       //t.setDaemon(true);
        // if (t.isDaemon())
       //     t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}


}
