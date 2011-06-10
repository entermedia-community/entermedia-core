/*
 Copyright (c) 2003 eInnovation Inc. All rights reserved

 This library is free software; you can redistribute it and/or modify it under the terms
 of the GNU Lesser General Public License as published by the Free Software Foundation;
 either version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU Lesser General Public License for more details.
 */

package com.openedit.generators;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openedit.OpenEditException;
import com.openedit.WebPageRequest;
import com.openedit.page.Page;

/**
 * This repository implements a JSP compiler using the {@link NamedDispatcher} class in the
 * standard servlet API.
 */
public class FilterGenerator extends BaseGenerator
{
	protected static final Log log = LogFactory.getLog(FilterGenerator.class);

	public void generate(WebPageRequest inContext, Page inPage, Output inOut)
		throws OpenEditException
	{
		HttpServletRequest httpRequest = inContext.getRequest();
		HttpServletResponse httpResponse = inContext.getResponse();
		Page page = (Page) inContext.getPageValue("page");

		//load the standard context objects
		httpRequest.setAttribute("context", inContext);
		httpRequest.setAttribute("pages", inContext.getPageStreamer());
		httpRequest.setAttribute("content", inContext.getContentPage());

		/* I commented this out since it conflics with existing JSP pages. Blojsom has its own "page" variable, get stuff from the context
		 for (Iterator iter = inContext.getPageMap().entrySet().iterator(); iter.hasNext();)
		 {
		 Map.Entry entry = (Map.Entry) iter.next();
		 String key = (String) entry.getKey();
		 httpRequest.setAttribute(key, inContext.getPageValue(key));
		 }
		 */

		DecoratedServletResponse response = new DecoratedServletResponse(httpResponse,
			new DecoratedServletOutputStream(inOut));

		DecoratedServletRequest newrequest = new DecoratedServletRequest(httpRequest);

		String requestPath = page.getContentItem().getActualPath();
		newrequest.setrequestURI(requestPath);
		try
		{
			inOut.getWriter().flush();
			//URLUtilities urls = (URLUtilities)inContext.getPageValue(PageRequestKeys.URL_UTILITIES);
			if( requestPath.endsWith(".jsp") )   
			{
				//A dispatcherallow us to use the base directory and draft modes
				RequestDispatcher jspDispatcher = httpRequest.getRequestDispatcher( requestPath );	
				jspDispatcher.include(newrequest, response); //This only applies for jsp pages.
			}
			else
			{
				FilterChain chain = (FilterChain) httpRequest.getAttribute("servletchain");
				chain.doFilter(newrequest, response); //This will capture output to our existing output class
			}
			inOut.getWriter().flush();
		}
		catch (ServletException ex)
		{
			throw new OpenEditException(ex);
		}
		catch (IOException ex)
		{
			throw new OpenEditException(ex);
		}
	}

	/**
	 * Stub implementation of HttpServletResponse
	 */
	class DecoratedServletResponse extends HttpServletResponseWrapper
	{
		DecoratedServletOutputStream output;

		public DecoratedServletResponse(HttpServletResponse response,
			DecoratedServletOutputStream inOut)
		{
			super(response);
			this.output = inOut;
		}

		//Keep this stream open since we want to keep feeding data to the stream
		public boolean isCommitted()
		{
			return false;
		}

		public void setContentLength(int len)
		{
		}

		public void setContentType(java.lang.String type)
		{
		}

		//this is for text
		public PrintWriter getWriter()
		{
			PrintWriter out = new PrintWriter(output.getOutput().getWriter())
			{
				public void close()
				{
					//ignore closes
				}
			};
			return out;
		}

		//This is binary
		public ServletOutputStream getOutputStream() throws IOException
		{
			return output;
		}

		public void reset()
		{
		}

		public void resetBuffer()
		{
		}

	}

	class DecoratedServletRequest extends HttpServletRequestWrapper
	{
		protected String fieldRequestURI;

		public DecoratedServletRequest(HttpServletRequest inRequest)
		{
			super(inRequest);
		}

		public void setrequestURI(String inUri)
		{
			fieldRequestURI = inUri;
		}
		
		public String getRequestURI()
		{
			if (fieldRequestURI != null)
			{
				return fieldRequestURI;
			}
			return super.getRequestURI();
		}
		
	}

	class DecoratedServletOutputStream extends ServletOutputStream
	{
		Output output;

		public DecoratedServletOutputStream(Output inOut)
		{
			this.output = inOut;
		}

		public void write(int b) throws java.io.IOException
		{
			// This method is not used but has to be implemented
			//this.writer.write(b);
			output.getStream().write(b);
		}

		public void write(byte b[]) throws IOException
		{
			write(b, 0, b.length);
		}

		public void write(byte b[], int off, int len) throws IOException
		{
			//System.out.println("writing...");
			output.getStream().write(b, off, len);
		}

		public void close() throws IOException
		{
			//super.close();
		}

		public void flush() throws IOException
		{
			output.getStream().flush();
		}

		public Output getOutput()
		{
			return output;
		}
	}

}
