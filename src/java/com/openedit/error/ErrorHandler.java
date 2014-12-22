package com.openedit.error;

import com.openedit.WebPageRequest;


/**
 * 
 * @author Matt Avery, mavery@einnovation.com
 */
public interface ErrorHandler
{

	/**
	 * Handle the error.
	 * Return if consumed
	 * @param error The error
	 */
	public boolean handleError(Throwable inException, WebPageRequest inPageStreamer );

}
