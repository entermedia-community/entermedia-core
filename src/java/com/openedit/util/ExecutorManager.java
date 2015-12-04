package com.openedit.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import com.openedit.Shutdownable;

public class ExecutorManager implements Shutdownable
{
	private static final Log log = LogFactory.getLog(ExecutorManager.class);
	
	protected Integer fieldThreadCount;
	protected Map fieldExecutors;
	
	protected Map getExecutors()
	{
		if (fieldExecutors == null)
		{
			fieldExecutors = new HashMap();
		}

		return fieldExecutors;
	}

	

	public void setThreadCount(Integer inThreadCount)
	{
		fieldThreadCount = inThreadCount;
	}
	
	public ExecutorService getExecutor(String inType)
	{
		ExecutorService exec = (ExecutorService)getExecutors().get(inType);
		if( exec == null)
		{
			synchronized (this)
			{
				exec = (ExecutorService)getExecutors().get(inType);
				if( exec != null)
				{
					return exec;
				}
				if( inType.equals("unlimited"))
				{
					exec =  createExecutor(2, Integer.MAX_VALUE, "Unlimited");
				}
				else if( inType.equals("conversions"))
				{
					int max =  Runtime.getRuntime().availableProcessors();
					if( max > 20)
					{
						max = 15; //Disk IO gets crazy
					}
					else if( max < 4)
					{
						max = 4;
					}
					max--;
					exec =  createExecutor(max, max); //Max does not seem to work as advertised
				}
				else if( inType.equals("importing"))
				{
					exec =  createExecutor(10, 10, "Importing");
				}
				getExecutors().put(inType, exec);
			}
		}
		return exec;
	}
	public void execute(String inType, List<Runnable> inTasks)
	{
		Collection<Callable<Object>> runnow = new ArrayList<Callable<Object>>(inTasks.size());
	
		for (Runnable runner: inTasks)
		{ 
			runnow.add(Executors.callable(runner)); 
		}
		try
		{
			getExecutor(inType).invokeAll(runnow);
		}
		catch( Throwable ex)
		{
			throw new OpenEditException(ex);
		}
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
	public void execute(String inType, Runnable inRunnable)
	{
		getExecutor(inType).execute(inRunnable);
	}
	public void execute(Runnable inRunnable)
	{
		execute("unlimited",inRunnable);
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

@Override
public void shutdown()
{
	// TODO Auto-generated method stub
	for (Iterator iterator = getExecutors().keySet().iterator(); iterator.hasNext();)
	{
		String type = (String) iterator.next();
		ExecutorService exec = (ExecutorService)getExecutors().get(type);
		try
		{
			exec.shutdown();
			exec.awaitTermination(10L, TimeUnit.SECONDS);
		}
		catch (Throwable e)
		{
			//	throw new OpenEditException(e);
		}
		log.debug("Exec shut down");
	}
}


}
