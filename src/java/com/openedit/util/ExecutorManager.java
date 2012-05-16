package com.openedit.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openedit.OpenEditException;

public class ExecutorManager
{
	private static final Log log = LogFactory.getLog(ExecutorManager.class);
	
	protected ExecutorService fieldSharedExecutor;
	protected Integer fieldThreadCount = 8;
	
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
			int minimum = 2;
			
			return new ThreadPoolExecutor(minimum, getThreadCount(),
                    10L, TimeUnit.MINUTES,
                    new LinkedBlockingQueue<Runnable>());
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
		log.info("Exec completed");
	}
}
