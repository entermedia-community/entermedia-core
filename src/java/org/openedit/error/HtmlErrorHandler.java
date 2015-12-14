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
				if( exception instanceof ContentNotAvailableException)
				{
					try
					{
						context.getResponse().setStatus(404);
						return true;
					}
					catch( Exception ex)
					{
						//ignored
						log.debug("Ignored:" + ex);
					}
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
				
				//exception.getPathWithError()
				//Page content = pages.getPage();
				Page content = context.getPage();
				String errorpagepath = content.getProperty("errorpage");
				
				Page errorPage = null;
				
				if (errorpagepath == null)
				{
					errorpagepath = "/system/errorpage.html";
				}
				errorPage = pages.getPage(errorpagepath);
				
				if( !errorPage.exists() )
				{
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
