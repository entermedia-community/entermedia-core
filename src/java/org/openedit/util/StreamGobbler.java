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
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An object that consumes an {@link InputStream} on a daemon thread to prevent
 * the stream from blocking.
 * 
 * <p>
 * The stream can optionally be output to a logger at an INFO level. The gobbler
 * thread will run until the stream is empty or until this {@code StreamGobbler}
 * is closed.
 * 
 * @author John Leacox
 * 
 */
public class StreamGobbler implements Closeable, Runnable
{
	private static final Log log = LogFactory.getLog(StreamGobbler.class);

	protected ExecutorManager fieldExecutorManager;
	private final InputStream inputStream;
	private final boolean isLoggingEnabled;
	protected Thread parentThread = null;
	protected String fieldOutput = null;
	protected boolean fieldErrorStream;

	public boolean isErrorStream()
	{
		return fieldErrorStream;
	}

	public void setErrorStream(boolean inIsErrorStream)
	{
		fieldErrorStream = inIsErrorStream;
	}

	public StreamGobbler(InputStream inputStream, boolean enableLogging)
	{
		this.inputStream = inputStream;
		this.isLoggingEnabled = enableLogging;

		//setName("StreamGobbler");
		//setDaemon(true);
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
	public void gobble()
	{
		getExecutorManager().execute(this);
	}

	@Override
	public void run()
	{
		parentThread = Thread.currentThread();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		String line = null;
		try
		{
			StringBuffer writer = null;
			if (isLoggingEnabled)
			{
				writer = new StringBuffer();
			}
			while (!Thread.currentThread().isInterrupted() && (line = br.readLine()) != null)
			{
				if (isLoggingEnabled)
				{
					writer.append(line);
					writer.append('\n');
					if (writer.length() > 1000000) //Dont let this buffer get more than 100k of memory
					{
						String cut = writer.substring(writer.length() - 700000, writer.length());
						writer = new StringBuffer(cut);
					}
				}
			}
			if (isLoggingEnabled)
			{
				fieldOutput = writer.toString();
			}
		}
		catch (IOException e)
		{
			if (isLoggingEnabled)
			{
				log.debug("Failed to gobble stream", e);
				log.info("Failed to gobble stream");
				
			}
		}
	}

	public String getOutput()
	{
		return fieldOutput;
	}

	@Override
	public void close() throws IOException
	{
		if (parentThread != null)
		{
			parentThread.interrupt();
			//Do we need to destry this thread?

		}
		inputStream.close();
	}
}
