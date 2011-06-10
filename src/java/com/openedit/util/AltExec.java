/*
 * Created on May 4, 2006
 */
package com.openedit.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openedit.OpenEditException;

public class AltExec
{
	private static final Log log = LogFactory.getLog(AltExec.class);
	protected String fieldErrorOutput;
	protected String fieldStandardOutput;
	protected int timelimit; // an optional timelimit on the exececution time
	protected boolean fieldTimeLimited = false;// set a time limit for this process to complete

	public int getTimelimit()
	{
		return timelimit;
	}

	public void setTimelimit(int inTimelimit)
	{
		setTimeLimited( inTimelimit > 0 );
		timelimit = inTimelimit;
	}

	public boolean isTimeLimited()
	{
		return fieldTimeLimited;
	}

	public void setTimeLimited(boolean inTimeLimited)
	{
		fieldTimeLimited = inTimeLimited;
	}

	public boolean runExec(List args) throws OpenEditException
	{
		setErrorOutput(null);
		setStandardOutput(null);
		long start = System.currentTimeMillis();
		final ProcessContainer container = new ProcessContainer();
		container.setCommands( args );
		try
		{
			log.info("Running: " + args);
			
			
			Thread execThread = container.getThread();
			
			if( isTimeLimited() )
			{
				// wait for notification from the execThread
				Thread timerThread = createTimerThread( container );
				timerThread.start();
				execThread.start();
				synchronized ( container )
				{
					container.wait();
				}
				
			}
			else
			{
				execThread.start();
				execThread.join();
			}
		}
		catch (Exception ex)
		{
			throw new OpenEditException(ex);
		}
		setStandardOutput( container.getStandardOut() );
		setErrorOutput( container.getErrors() );
		long duration = System.currentTimeMillis() - start;
		log.info( "Exec process " + args + " exiting with return code " + container.getReturnCode() + " in " + duration + "ms" );
		return container.getReturnCode() == 0;
	}

	protected Thread createTimerThread( final ProcessContainer container )
	{
		Thread timerThread = new Thread( new Runnable( )
		{
			public void run()
			{
				boolean timedOut = false;
				int count = 0;
				try
				{
					log.info( "Starting " + getTimelimit() + "ms process timer..." );
					// Neat... if the increment is too small (<10ms), the timer
					// accuracy suffers because this thread does not permit the process
					// thread time to execute.
					int increment = 100;
					
					while ( !container.isFinished() && count < getTimelimit() )
					{
						count = count + increment;
						Thread.sleep( increment );
					}
					timedOut = count >= getTimelimit();
				}
				catch ( InterruptedException e )
				{
					log.error( e );
				}
				finally
				{
					if ( timedOut )
					{
						log.info( "Process timed out, terminating... "  );
					}
					container.terminate();
					synchronized ( container )
					{
						container.notify();
					}
					if ( !timedOut )
					{
						log.info( "Exiting timer thread in " + count + "ms" );
					}
				}
			}

		} );
		
		return timerThread;
	}

	public String getErrorOutput()
	{
		return fieldErrorOutput;
	}

	public void setErrorOutput(String inErrorOutput)
	{
		fieldErrorOutput = inErrorOutput;
	}

	public String getStandardOutput()
	{
		return fieldStandardOutput;
	}

	public void setStandardOutput(String inStandardOutput)
	{
		fieldStandardOutput = inStandardOutput;
	}

	

	
}
