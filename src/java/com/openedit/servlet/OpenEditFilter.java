/*
 Copyright (c) 2003 eInnovation Inc. All rights reserved

 This library is free software; you can redistribute it and/or modify it under the terms
 of the GNU Lesser General Public License as published by the Free Software Foundation;
 either version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU Lesser General Public License for more details.
 */

package com.openedit.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.openedit.BaseWebServer;

public class OpenEditFilter implements Filter
{
	private OpenEditEngine fieldEngine;

	public void destroy()
	{
		getEngine().shutdown();
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param inRequest
	 * @param inResponse
	 * @param chain
	 *
	 * @throws IOException
	 * @throws ServletException
	 */
	public void doFilter(ServletRequest inRequest, ServletResponse inResponse, FilterChain chain)
		throws IOException, ServletException
	{
		HttpServletRequest request = (HttpServletRequest) inRequest;

		HttpServletResponse response = (HttpServletResponse) inResponse;

		String path = request.getRequestURI();
		if (path.startsWith("/entermedia/services/websocket")) 
		{
			chain.doFilter(request, response); // Just continue chain.
			return;
		}

		if (getEngine() == null)
		{
			response
				.getWriter()
				.print(
					"<html>Open Edit Server is not initialized, please check the logs for errors</html>");

			return;
		}
		request.setAttribute("servletchain", chain); //This is used by the ServletChainGenerator to call doFilter at the right time
		getEngine().render(request, response);
	}
	
	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig inConfig) throws ServletException
	{
		ServletContext servletContext = inConfig.getServletContext();		

		String rootPath = inConfig.getInitParameter("oe.root.path");

		if( rootPath == null)
		{
			rootPath = servletContext.getRealPath("/");
		}
		if( rootPath == null)
		{
			rootPath = System.getProperty("oe.root.path");
		}

		BaseWebServer server = new BaseWebServer();
		server.setRootDirectory(new File(rootPath));
		server.setNodeId(servletContext.getInitParameter("entermedianodeid"));
		server.initialize();
		servletContext.setAttribute(server.getClass().getName(), server); //TODO: Why is this here?
		fieldEngine = server.getOpenEditEngine();
		
	}

	protected OpenEditEngine getEngine()
	{
		return fieldEngine;
	}
}
