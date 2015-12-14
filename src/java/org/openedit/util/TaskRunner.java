package org.openedit.util;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TaskRunner
{
	protected Object fieldNotify = new Object();
	public Object getNotify()
	{
		return fieldNotify;
	}
	protected Timer fieldQueue;
	private static final Log log = LogFactory.getLog(TaskRunner.class);
	
	protected int fieldCount;
	protected boolean fieldHasHitMax;
	protected int fieldMaxQueueSize = 200;
	
	public Timer getQueue()
	{
		if( fieldQueue == null)
		{
			fieldQueue = new Timer(true);
		}
		return fieldQueue;
	}

	public void setQueue(Timer inQueue)
	{
		fieldQueue = inQueue;
	}
	protected void reduce()
	{
		fieldCount--;
		if( fieldHasHitMax && fieldCount == 0)
		{
			log.info("Image Queue is now empty");
			fieldHasHitMax = false;
		}
		synchronized (getNotify())
		{
			getNotify().notifyAll();
		}

	}
	protected void increase()
	{
		fieldCount++;
	}
	public int getCount()
	{
		return fieldCount;
	}
	
	public void add(final Runnable inTask)
	{		
		if(( fieldCount > getMaxQueueSize())) //We dont want this queue to get too big in case they cancel 
		{
			fieldHasHitMax = true;
			inTask.run();
			return;
		}
		increase();
		log.debug("Adding " + inTask);
		TimerTask task = new TimerTask()
		{
			public void run()
			{
				try
				{
					log.debug("Running " + inTask);
					inTask.run();
				}
				catch ( Throwable ex)
				{
					log.error("task failed " + ex.getMessage() + " on  " + inTask );
				}
				reduce();
			}
		};
		getQueue().schedule(task, 0);
	}

	public int getMaxQueueSize()
	{
		return fieldMaxQueueSize;
	}

	public void setMaxQueueSize(int inMaxQueueSize)
	{
		fieldMaxQueueSize = inMaxQueueSize;
	}
}
