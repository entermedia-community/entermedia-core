package com.openedit.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
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
	
	protected ExecutorService fieldSharedExecutor;
	protected Integer fieldThreadCount;
	
	public Integer getThreadCount()
	{
		if (fieldThreadCount == null)
		{
			fieldThreadCount = Runtime.getRuntime().availableProcessors();
		}

		return fieldThreadCount;
	}

	public void setThreadCount(Integer inThreadCount)
	{
		fieldThreadCount = inThreadCount;
	}

	public ExecutorService createExecutor() 
	{
			//fieldExecutor = Executors.newCachedThreadPool();
			//fieldExecutor = Executors.newFixedThreadPool(8);
			int minimum = getThreadCount();
			
			DefaultThreadFactory factory = new DefaultThreadFactory();
			
			return new ThreadPoolExecutor(minimum, minimum,
                    10L, TimeUnit.MINUTES,
                    new LinkedBlockingQueue<Runnable>(),
                   factory,
                    new ThreadPoolExecutor.CallerRunsPolicy());
	}
	
	
	   static class DefaultThreadFactory implements ThreadFactory {
	        static final AtomicInteger poolNumber = new AtomicInteger(1);
	        final ThreadGroup group;
	        final AtomicInteger threadNumber = new AtomicInteger(1);
	        final String namePrefix;

	        DefaultThreadFactory() 
	        {
	            SecurityManager s = System.getSecurityManager();
	            group = (s != null)? s.getThreadGroup() :
	                                 Thread.currentThread().getThreadGroup();
	            namePrefix = "pool-" +
	                          poolNumber.getAndIncrement() +
	                         "-thread-";
	        }

	        public Thread newThread(Runnable r) {
	            Thread t = new Thread(group, r,
	                                  namePrefix + threadNumber.getAndIncrement(),
	                                  0);
	           t.setDaemon(true);
	            // if (t.isDaemon())
	           //     t.setDaemon(false);
	            if (t.getPriority() != Thread.NORM_PRIORITY)
	                t.setPriority(Thread.NORM_PRIORITY);
	            return t;
	        }
	    }
	
	
	public ExecutorService getSharedExecutor()
	{
		if (fieldSharedExecutor == null)
		{
			fieldSharedExecutor = createExecutor();
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
}
