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


import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A {@link Closeable} wrapper for {@link Process} for running a native process.
 * 
 * <p>
 * This wrapper provides some additional functionality around {@code Process} for some of the common pitfalls with using
 * {@code Process} directly.
 * 
 * <ul>
 * 
 * <li>It implements {@link Closeable}. The {@link #close()} method will make sure that all of the processes' streams
 * are closed, and if the {@code keepProcess} flag was not set, the process is destroyed via {@link Process#destroy()}.</li>
 * 
 * <li>It provides the {@link #waitFor(int)} method that invokes {@link Process#waitFor()} with a timeout period. If the
 * process execution takes longer than the timeout, then the thread is interrupted. This method also makes sure that the
 * thread interrupt flag is cleared</li>
 * 
 * </ul>
 * 
 * <p>
 * Here is a basic example of using this class:
 * 
 * <pre>
 * {@code
 * FinalizedProcessBuilder pb = new FinalizedProcessBuilder("myCommand", "myArg");
 * FinalizedProcess process = pb.start();
 * try {
 *   int returnVal = process.waitFor(5000);
 * } finally {
 *   process.close();
 * }}
 * </pre>
 * 
 * <p>
 * If running on Java 7, try-with-resources can be used:
 * 
 * <pre>
 * {@code
 * FinalizedProcessBuilder pb = new FinalizedProcessBuilder("myCommand", "myArg");
 * try (FinalizedProcess process = pb.start()) {
 * 		int returnVal = process.waitFor(5000);
 * }}
 * </pre>
 * 
 * @author John Leacox
 * @see Process
 * @see FinalizedProcessBuilder
 * @see ProcessBuilder
 * 
 */
public class FinalizedProcess implements Closeable {
	private final Process process;
	private final boolean keepProcess;
	private final Set<StreamGobbler> streamGobblers;

	FinalizedProcess(Process process, boolean keepProcess, Set<StreamGobbler> streamGobblers) {
		if (process == null) {
			throw new NullPointerException("process: null");
		}

		this.process = process;
		this.keepProcess = keepProcess;
		this.streamGobblers = streamGobblers;
	}

	/**
	 * Kills the subprocess. The subprocess represented by this {@code FinalizedProcess} object is forcibly terminated.
	 */
	public void destroy() {
		process.destroy();
	}
	
	public void destroyForcibly() {
		process.destroyForcibly();
	}

	/**
	 * Returns the exit value for the subprocess.
	 * 
	 * @return the exit value of the subprocess represented by this {@code FinalizedProcess} object. By convention, the
	 *         value {@code 0} indicates normal termination.
	 * @throws IllegalThreadStateException
	 *             if the subprocess represented by this {@code FinalizedProcess} object has not yet terminated
	 */
	public int exitValue() {
		return process.exitValue();
	}

	/**
	 * Returns the input stream connected to the error output of the subprocess. The stream obtains data piped from the
	 * error output of the process represented by this {@code FinalizedProcess} object.
	 * 
	 * <p>
	 * If the standard error of the subprocess has been redirected using
	 * {@link FinalizedProcessBuilder#redirectErrorStream(boolean)} then this method will return a null input
	 * stream</a>.
	 * 
	 * <p>
	 * Implementation note: It is a good idea for the returned input stream to be buffered.
	 * 
	 * @return the input stream connected to the error output of the subprocess
	 */
	public InputStream getErrorStream() {
		return process.getErrorStream();
	}

	/**
	 * Returns the input stream connected to the normal output of the subprocess. The stream obtains data piped from the
	 * standard output of the process represented by this {@code FinalizedProcess} object.
	 * 
	 * <p>
	 * If the standard error of the subprocess has been redirected using
	 * {@link FinalizedProcessBuilder#redirectErrorStream(boolean)} then the input stream returned by this method will
	 * receive the merged standard output and the standard error of the subprocess.
	 * 
	 * <p>
	 * Implementation note: It is a good idea for the returned input stream to be buffered.
	 * 
	 * @return the input stream connected to the normal output of the subprocess
	 */
	public InputStream getInputStream() {
		return process.getInputStream();
	}

	/**
	 * Returns the output stream connected to the normal input of the subprocess. Output to the stream is piped into the
	 * standard input of the process represented by this {@code FinalizedProcess} object.
	 * 
	 * <p>
	 * Implementation note: It is a good idea for the returned output stream to be buffered.
	 * 
	 * @return the output stream connected to the normal input of the subprocess
	 */
	public OutputStream getOutputStream() {
		return process.getOutputStream();
	}

	/**
	 * Causes the current thread to wait, if necessary, until the process represented by this {@code FinalizedProcess}
	 * object has terminated. This method returns immediately if the subprocess has already terminated. If the
	 * subprocess has not yet terminated, the calling thread will be blocked until the subprocess exits. If the
	 * subprocess execution takes longer than the specified {@code timeoutMilliseconds}, then the blocked thread will be
	 * interrupted.
	 * 
	 * @param timeoutMilliseconds
	 *            time, in milliseconds, to wait on the subprocess blocking thread before timing out. (must be greater
	 *            than 0)
	 * @return the exit value of the subprocess represented by this {@code Process} object. By convention, the value
	 *         {@code 0} indicates normal termination.
	 * @throws IllegalArgumentException
	 *             if timeoutMilliseconds is negative or zero.
	 * @throws InterruptedException
	 *             if the subprocess execution times out or the current thread is interrupted by another thread while it
	 *             is waiting.
	 */
	public int waitFor(long timeout) throws InterruptedException {
		if (timeout <= 0) {
			throw new IllegalArgumentException("timeoutMilliseconds: <= 0");
		}
        TimeUnit unit = TimeUnit.MILLISECONDS;
		
	      long startTime = System.nanoTime();
	        long rem = unit.toNanos(timeout);
	        do {
	            try {
	                return exitValue();
	            } catch(IllegalThreadStateException ex) {
	                if (rem > 0)
	                    Thread.sleep(
	                        Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100));
	            }
	            rem = unit.toNanos(timeout) - (System.nanoTime() - startTime);
	        } while (rem > 0);
	        return 124;
	}

	@Override
	public void close() throws IOException {
		if (streamGobblers != null) {
			for (StreamGobbler gobbler : streamGobblers) {
				try {
					gobbler.close();
				} catch (IOException e) {
				}
			}
		}

		if (process != null) {
			try {
				if (process.getErrorStream() != null) {
					process.getErrorStream().close();
				}
			} catch (IOException e) {
			}

			try {
				if (process.getInputStream() != null) {
					process.getInputStream().close();
				}
			} catch (IOException e) {
			}

			try {
				if (process.getOutputStream() != null) {
					process.getOutputStream().close();
				}
			} catch (IOException e) {
			}

			if (!keepProcess) {
				process.destroy();
			}
		}
	}

//	private static class InterruptTimerTask extends TimerTask {
//		private final Thread thread;
//
//		public InterruptTimerTask(Thread t) {
//			this.thread = t;
//		}
//
//		@Override
//		public void run() {
//			thread.interrupt();
//		}
//	}
	public String getErrorOutputs()
	{
		StringBuffer output = new StringBuffer();
		for( StreamGobbler stream : streamGobblers)
		{
			if( stream.isErrorStream() )
			{
				output.append(stream.getOutput());
				output.append("\n");
			}
		}
		return output.toString();
	}
	public String getAllOutputs()
	{
		StringBuffer output = new StringBuffer();
		for( StreamGobbler stream : streamGobblers)
		{
			output.append(stream.getOutput());
			output.append("\n");
		}
		return output.toString();
	}
	public String getStandardOutputs()
	{
		StringBuffer output = new StringBuffer();
		for( StreamGobbler stream : streamGobblers)
		{
			if( !stream.isErrorStream() )
			{
				output.append(stream.getOutput());
				output.append("\n");
			}
		}
		return output.toString();
	}
}
