package com.openedit.util;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProcessContainer
{
	private static final Log log = LogFactory.getLog(ProcessContainer.class);
	protected int fieldReturnCode;
	protected Process fieldProcess;
	protected Exception fieldException;
	protected Thread fieldThread;
	protected boolean fieldFinished;
	protected String fieldErrors;
	protected String fieldStandardOut;
	protected List fieldCommands;
	
	public int getReturnCode()
	{
		return fieldReturnCode;
	}
	public void setReturnCode( int returnCode )
	{
		fieldReturnCode = returnCode;
	}
	public String getErrors()
	{
		return fieldErrors;
	}
	public void setErrors( String errors )
	{
		fieldErrors = errors;
	}
	public String getStandardOut()
	{
		return fieldStandardOut;
	}
	public void setStandardOut( String standardOut )
	{
		fieldStandardOut = standardOut;
	}
	public Process getProcess()
	{
		return fieldProcess;
	}
	public void setProcess( Process process )
	{
		fieldProcess = process;
	}
	public Exception getException()
	{
		return fieldException;
	}
	public void setException( Exception exception )
	{
		fieldException = exception;
	}
	public Thread getThread()
	{
		if ( fieldThread == null )
		{
			fieldThread = createExecThread();
		}
		return fieldThread;
	}

	public void terminate()
	{
		if( !isFinished() )
		{
			log.debug( "Terminating " + getCommands().get(0) );
			getProcess().destroy();
			getThread().getState();
			setFinished( true );
		}
	}
	
	public boolean isFinished()
	{
		return fieldFinished;
	}
	public void setFinished( boolean finished )
	{
		fieldFinished = finished;
	}
	
	protected Thread createExecThread()
	{
		
		Thread execThread = new Thread( new Runnable( ){
		
			public void run()
			{
				try
				{

					Process proc = Runtime.getRuntime().exec(commandsAsArray());
					setProcess( proc );

					InputStreamHandler errors = createStreamHandler( proc.getErrorStream() );
					
					InputStreamHandler standardOut = createStreamHandler( proc.getInputStream() );
					
					errors.start();
					standardOut.start();
					
					setReturnCode( proc.waitFor() );
					setErrors( errors.getBuffer().toString() );
					setStandardOut( standardOut.getBuffer().toString() );
				}
				catch ( Exception e )
				{
					log.error( e );
				}
				finally
				{
					terminate();
				}
			}

			protected InputStreamHandler createStreamHandler( InputStream inStream )
			{
				InputStreamHandler errors = new InputStreamHandler();
				errors.setStream( inStream );
				errors.setCommand( commandsAsArray()[0] );
				return errors;
			}
		 
		});
		return execThread;
	}
	public List getCommands()
	{
		return fieldCommands;
	}
	public void setCommands( List commands )
	{
		fieldCommands = commands;
	}
	
	protected String[] commandsAsArray()
	{
		return (String[]) getCommands().toArray(new String[getCommands().size()]);
	}
}
