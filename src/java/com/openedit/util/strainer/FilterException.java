package com.openedit.util.strainer;

import com.openedit.OpenEditRuntimeException;

/**
 * This is the superclass of all exceptions thrown by {@link Filter}s.
 * 
 * @author Eric Galluzzo
 */
public class FilterException extends OpenEditRuntimeException
{
	public FilterException()
	{
		super();
	}
	
	public FilterException( Throwable t )
	{
		super( t );
	}
	
	public FilterException( String s, Throwable t )
	{
		super( s, t );
	}
	
	public FilterException( String s )
	{
		super( s );
	}
}