/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package com.openedit.error;

import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openedit.OpenEditException;
import com.openedit.WebPageRequest;
import com.openedit.generators.Output;
import com.openedit.page.Page;
import com.openedit.page.PageRequestKeys;
import com.openedit.page.PageStreamer;


/**
 * DOCUMENT ME!
 *
 * @author cburkey
 */
public class HtmlErrorHandler implements ErrorHandler
{
	protected String fieldPathToErrorFile;
	private static final Log log = LogFactory.getLog(HtmlErrorHandler.class);


	/**
	 * @see org.jpublish.ErrorHandler#handleError(JPublishError)
	 */
	public boolean handleError(Exception error, WebPageRequest context ) 
	{
		OpenEditException exception = null;
		if (context != null)
		{
			try
			{
				if ( !(error instanceof OpenEditException))
				{
					exception = new OpenEditException(error); //we need the toStacktrace method
				}
				else
				{
					exception = (OpenEditException)error;
				}
				if( !context.hasRedirected() && context.getResponse() != null)
				{
					try
					{
						context.getResponse().setStatus(500);
					}
					catch( Exception ex)
					{
						//ignored
						log.debug("Ignored:" + ex);
					}
				}
				error.printStackTrace();
				String pathWithError = exception.getPathWithError();
				if( pathWithError == null)
				{
					pathWithError = context.getPage().getPath();
					exception.setPathWithError(pathWithError);
					
				}
				context.putPageValue("editPath", exception.getPathWithError());
				context.putPageValue("oe-exception", exception); //must be a top level thing since we create a new context
				PageStreamer pages = (PageStreamer)context.getPageValue(PageRequestKeys.PAGES);
				
				Page content = pages.getPage(exception.getPathWithError());
				String errorpagepath = content.getProperty("errorpage");
				
				Page errorPage = null;
				if (errorpagepath!=null)
				{
					errorPage = pages.getPage(errorpagepath);
				}
				if( errorPage==null||!errorPage.exists() )
				{
					errorPage = pages.getPage("/system/errorpage.html");
					log.error("No error page found" + errorPage.getPath());
					return false;
				}
				else
				{
					Writer out = context.getWriter();
					errorPage.generate(context,new Output(out, null));
					out.flush();
				}
			} catch ( Exception ex)
			{
				//Do not throw an error here is it will be infinite
				log.error( ex);
				ex.printStackTrace();
				try
				{
					context.getWriter().write("Check error logs: " + ex);
					//throw new OpenEditRuntimeException(ex);
				}
				catch (Throwable ex1)
				{
					log.error( ex1 );
				}
			}
			return true;
		}
		return false;
	}
	

}
