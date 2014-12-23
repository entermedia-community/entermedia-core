/*
 * Created on Nov 16, 2004
 */
package com.openedit.error;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.openedit.WebPageRequest;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class CompoundErrorHandler implements ErrorHandler
{
	protected List fieldErrorHandlers;
	
	public boolean handleError( Throwable inException, WebPageRequest inContext )
	{
		//WebPageRequest inContext = inPageStreamer.getContext();
		inContext.putPageValue( "oe-exception", inException );
		for ( Iterator iter = getErrorHandlers().iterator(); iter.hasNext(); )
		{
			ErrorHandler errorHandler = (ErrorHandler) iter.next();
			if ( errorHandler.handleError( inException, inContext ) )
			{
				return true;
			}
		}
		return false;
	}
	
	public void addErrorHandler( ErrorHandler inErrorHandler )
	{
		getErrorHandlers().add( inErrorHandler );
	}
	public List getErrorHandlers()
	{
		if (fieldErrorHandlers == null)
		{
			fieldErrorHandlers = new ArrayList();
		}
		return fieldErrorHandlers;
	}

	public void setErrorHandlers( List errorHandlers )
	{
		fieldErrorHandlers = errorHandlers;
	}
}
