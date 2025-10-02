/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package org.openedit.error;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.OpenEditException;
import org.openedit.WebPageRequest;
import org.openedit.generators.Output;
import org.openedit.page.Page;
import org.openedit.page.PageRequestKeys;
import org.openedit.page.PageStreamer;


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
	public boolean handleError(Throwable error, WebPageRequest context ) 
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
				Page content = context.getPage();

				if( exception instanceof ContentNotAvailableException)
				{
					try
					{
						context.getResponse().setStatus(404);
						ContentNotAvailableException ex = (ContentNotAvailableException) exception;
						String pathWithError = ex.getPathWithError();
						context.putPageValue("errorpath", pathWithError);

						String errorpagepath = content.getProperty("error404page");
						
						if (errorpagepath == null)
						{
							errorpagepath = "/system/error404page.html";
						}
						return renderErrorPage(context, errorpagepath);
					}
					catch( Exception ex)
					{
						//ignored
						log.debug("Ignored:" + ex);
					}
				}
				else
				{
					log.error("Got an error: " ,error);
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
				String pathWithError = exception.getPathWithError();
				if( pathWithError == null)
				{
					pathWithError = context.getPage().getPath();
					exception.setPathWithError(pathWithError);
					
				}
				context.putPageValue("editPath", exception.getPathWithError());
				context.putPageValue("oe-exception", exception); //must be a top level thing since we create a new context
				log.error("error on "  + exception.getPathWithError(),error);
				
				//exception.getPathWithError()
				//Page content = pages.getPage();
				String errorpagepath = content.getProperty("errorpage");
				
				if (errorpagepath == null)
				{
					errorpagepath = "/system/errorpage.html";
				}
				return renderErrorPage(context, errorpagepath);
			} 
			catch ( Exception ex)
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


	protected boolean renderErrorPage(WebPageRequest context, String errorpagepath) throws IOException
	{
		PageStreamer pages = (PageStreamer)context.getPageValue(PageRequestKeys.PAGES);

		Page errorPage = pages.getPage(errorpagepath);
		
		if( !errorPage.exists() )
		{
			log.error("No error page found" + errorPage.getPath());
			return false;
		}
		else
		{
			if( context.getResponse() != null)
			{
				context.getResponse().setContentType( "text/html; charset=UTF-8" );
			}
			pages.include( errorPage);
			context.setHasRedirected(true);
		}
		return true;
	}
	

}
