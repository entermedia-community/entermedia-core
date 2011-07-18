/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package com.openedit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

import com.openedit.page.Page;
import com.openedit.page.PageRequestKeys;
import com.openedit.page.manage.PageManager;
import com.openedit.servlet.OpenEditEngine;
import com.openedit.users.User;
import com.openedit.users.UserManager;
import com.openedit.util.PathUtilities;
import com.openedit.web.Browser;


/**
 * This class is a text fixture for JPublish/Open Edit.
 *
 * @author Eric Galluzzo
 */
public class TestFixture
{
	protected String fieldPath = null;
	protected BaseWebServer fieldWebServer;	

	/**
	 * Constructor for TestFixture.
	 */
	public TestFixture()
	{
		super();
	}

	/**
	 * @return
	 */
	public String getPath()
	{
		return fieldPath;
	}

	/**
	 * @param inPath
	 */
	public void setPath(String inPath)
	{
		fieldPath = inPath;
	}



	public WebPageRequest createPageRequest() throws OpenEditException
	{
		BaseWebPageRequest context = new TestWebPageRequest();

		Browser browser = new Browser("Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)");

		Page page = getPageManager().getPage("/index.html");
		context.putPageValue( PageRequestKeys.PAGE, page);
		context.putPageValue( PageRequestKeys.CONTENT, page);
		context.putPageValue( PageRequestKeys.BROWSER, browser);
		context.putPageValue( PageRequestKeys.HOME, "");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		context.putPageValue( PageRequestKeys.OUTPUT_STREAM, out);
		context.putPageValue( PageRequestKeys.OUTPUT_WRITER, new StringWriter());
		User admin = getUserManager().getUser("admin");
		//FileSystemUserManager userManager = (FileSystemUserManager)getUserManager();
		//System.out.println( "Group directory: " + userManager.getGroupDirectory().getAbsolutePath() );
		//System.out.println( "User directory: " + userManager.getUserDirectory().getAbsolutePath() );
		if ( admin != null )
		{
			context.putPageValue( PageRequestKeys.USER, admin );
		}
		context.putPageValue("username", "admin");

		//URLUtilities util = (URLUtilities) inContext.getPageValue( "url_util" );
		context.putPageValue(PageRequestKeys.WEB_SERVER_PATH,"http://localhost:8080");
		//SessionTool sessionTool = new SessionTool( context, getModuleManager() );
		//context.putPageValue( PageRequestKeys.CLASSTOOL, sessionTool );

		getEngine().createPageStreamer( page,  context);

		return context;
	}
	
	public WebPageRequest createPageRequest(String inPath)
		throws OpenEditException
	{
		WebPageRequest context = (WebPageRequest) createPageRequest();

		String[] parts = inPath.split("[?]");
		if( parts.length > 1)
		{
			Map args = PathUtilities.extractArguments(parts[1]);
			context.putAllRequestParameters(args);
		}
		context.putPageValue("path", inPath);

		Page dynamicpage = getPageManager().getPage(inPath);
		context.putPageValue( PageRequestKeys.PAGE, dynamicpage);
		context.putPageValue( PageRequestKeys.CONTENT, dynamicpage);
		context.putPageValue( PageRequestKeys.USER, getUserManager().getUser("admin"));
		getEngine().createPageStreamer( dynamicpage,  context);

		return context;
	}

	public OpenEditEngine getEngine()
	{
		return getWebServer().getOpenEditEngine();
	}
	public ModuleManager getModuleManager()
	{
		return getWebServer().getModuleManager();
	}
	
	public PageManager getPageManager()
	{
		return getWebServer().getPageManager();
	}
	public UserManager getUserManager()
	{
		return getWebServer().getUserManager();
	}
	public WebServer getWebServer()
	{
		if (fieldWebServer == null)
		{
			fieldWebServer = new BaseWebServer();
			String rootPath = System.getProperty("oe.root.path");
			if ( rootPath == null )
			{
				File found = new File( "resources/test");
				if( found.exists())
				{
					rootPath = "resources/test";
				}
				else
				{
					rootPath = "webapp";
				}
			}
			fieldWebServer.setRootDirectory(new File( rootPath).getAbsoluteFile());
			fieldWebServer.initialize();
		}

		return fieldWebServer;
	}
}
