/*
 Copyright (c) 2003 eInnovation Inc. All rights reserved

 This library is free software; you can redistribute it and/or modify it under the terms
 of the GNU Lesser General Public License as published by the Free Software Foundation;
 either version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU Lesser General Public License for more details.
 */

package org.openedit.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.BaseWebServer;
import org.openedit.WebServer;

public class OpenEditFilter implements Filter
{
	private static final Log log = LogFactory.getLog(OpenEditFilter.class);

	private OpenEditEngine fieldEngine;

	public void destroy()
	{
		//We try to shutdown in three ways 1. Filter.destroy() 2. Web.xml ShutdownListener.java 3. BaseWebServer.initialize() shutdown hook
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
			request.getSession(true).setAttribute("moduleManager", getEngine().getModuleManager());  //This is needed so we can save the moduleManager
			chain.doFilter(request, response); // Just continue chain.
			return;
		}
		if (path.startsWith("/entermedia/servlets")) 
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
		String rootPath = null;
		ServletContext servletContext = null;
		if( inConfig != null)
		{
			servletContext = inConfig.getServletContext();		
			log.info("grabbed context: " + servletContext);
			rootPath = inConfig.getInitParameter("oe.root.path");
	
			if( rootPath == null)
			{
				rootPath = servletContext.getRealPath("/");
			}
		}
		if( rootPath == null)
		{
			rootPath = System.getProperty("oe.root.path");
		}

		BaseWebServer server = new BaseWebServer();  //Singleton?
		server.setServletContext(servletContext);
		server.setRootDirectory(new File(rootPath));
		if( servletContext != null)
		{
			server.setNodeId(servletContext.getInitParameter("entermedianodeid"));
		}
		server.initialize();
		server.getBeanLoader().registerSingleton("WebServer",this);
		if( servletContext != null)
		{
			servletContext.setAttribute(BaseWebServer.class.getName(), server); //TODO: Why is this here?
			servletContext.setAttribute(WebServer.class.getName(), server); //TODO: Why is this here?
		}
		fieldEngine = server.getOpenEditEngine();
		server.finalizeStartup();
		
		
		
		
		
	}

	protected OpenEditEngine getEngine()
	{
		return fieldEngine;
	}
}
