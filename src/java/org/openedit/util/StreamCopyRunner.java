/*
 * Copyright 2013 John Leacox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package org.openedit.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StreamCopyRunner implements Closeable, Runnable
{
	private static final Log log = LogFactory.getLog(StreamCopyRunner.class);

	protected ExecutorManager fieldExecutorManager;
	BufferedReader reader;
	protected Thread parentThread = null;
	StringBuffer savedoutbuffer; 
	String lastresult;
	public StreamCopyRunner(BufferedReader inReader, ExecutorManager inManager)
	{
		reader = inReader;
		fieldExecutorManager = inManager;
	}

	public ExecutorManager getExecutorManager()
	{
		return fieldExecutorManager;
	}

	public void setExecutorManager(ExecutorManager inExecutorManager)
	{
		fieldExecutorManager = inExecutorManager;
	}

	/**
	 * Starts gobbling the input stream.
	 */
	public void startCopy()
	{
		getExecutorManager().execute(this);
	}

	@Override
	public void run()
	{
		synchronized (this)
		{
			notify();
		}
		parentThread = Thread.currentThread();
		savedoutbuffer = new StringBuffer();
		String line = null;
		try
		{
			//log.info("Starting to read input" + parentThread.isInterrupted());
			while (!parentThread.isInterrupted() && (line = reader.readLine()) != null)
			{
				//log.info("Read oneline:" + line);
				if( line.isEmpty())
				{
					//log.info("Finish.Before");
					synchronized (StreamCopyRunner.this)
					{
						lastresult = savedoutbuffer.toString();
						savedoutbuffer = new StringBuffer();
						notifyAll();
					}
					//log.info("Finish.After");
				}
				savedoutbuffer.append(line);
				//log.info("Goind to read next line");
			}
		}
		catch (Exception e)
		{
			log.debug("Failed to read stream", e);
		}
		//log.info("Cloasing stream read");
	}
	public String getNextResult()
	{
		return getNextResult(-1);
	}
	public String getNextResult(long timeout)
	{
		if( lastresult != null)
		{
			String toreturn = lastresult;
			lastresult = null;
			return toreturn;
		}
		//log.info("getting next result.pre");
		synchronized (StreamCopyRunner.this)
		{
			try
			{
				//log.info("getting next result.wait");
				if( timeout == -1)
				{
					StreamCopyRunner.this.wait();
				}
				else
				{
					StreamCopyRunner.this.wait(timeout);
				}
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String toreturn = lastresult;
			lastresult = null;
			return toreturn;
		}
	}
	

	@Override
	public void close() throws IOException
	{
		FileUtils.safeClose(reader);
		if (parentThread != null)
		{
			parentThread.interrupt();
			//Do we need to destry this thread?
			synchronized (StreamCopyRunner.this)
			{
				notifyAll();
			}

		}
	}
}
