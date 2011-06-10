package com.openedit.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Pump an input stream, used by the Exec class.
 * 
 *
 */
public class InputStreamHandler extends Thread
{

	private static final Log log = LogFactory.getLog( InputStreamHandler.class );

	protected InputStream fieldStream;

	protected StringBuffer fieldBuffer;
	protected String fieldCommand = "";

	/**
	 * @return Returns the buffer.
	 */
	public StringBuffer getBuffer()
	{
		if ( fieldBuffer == null )
		{
			fieldBuffer = new StringBuffer();
		}
		return fieldBuffer;
	}

	/**
	 * @param inBuffer The buffer to set.
	 */
	public void setBuffer( StringBuffer inBuffer )
	{
		fieldBuffer = inBuffer;
	}

	/**
	 * @return Returns the stream.
	 */
	public InputStream getStream()
	{
		return fieldStream;
	}

	/**
	 * @param inStream The stream to set.
	 */
	public void setStream( InputStream inStream )
	{
		fieldStream = inStream;
	}

	public void run()
	{
		try
		{
			InputStreamReader isr = new InputStreamReader( getStream() );
			BufferedReader reader = new BufferedReader( isr );
			String line = null;
			while ( ( line = reader.readLine() ) != null )
			{
				String outputLine = getCommand() + ">" + line + "\n"; 
				log.debug( outputLine );
				getBuffer().append( outputLine );

			}
		}
		catch ( IOException e )
		{
			log.error( e );
		}

	}

	public String getCommand()
	{
		return fieldCommand;
	}

	public void setCommand( String command )
	{
		fieldCommand = command;
	}
}
