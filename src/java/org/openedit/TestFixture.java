/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package org.openedit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringWriter;
import java.util.Map;

import org.openedit.data.SearcherManager;
import org.openedit.page.Page;
import org.openedit.page.PageRequestKeys;
import org.openedit.page.manage.PageManager;
import org.openedit.profile.UserProfile;
import org.openedit.servlet.OpenEditEngine;
import org.openedit.users.User;
import org.openedit.users.UserManager;
import org.openedit.util.PathUtilities;
import org.openedit.web.Browser;


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
			SearcherManager manager = (SearcherManager)getModuleManager().getBean("searcherManager");
			UserProfile profile = (UserProfile)manager.getData("entermedia/catalogs/testcatalog", "userprofile","admin");
			context.putPageValue( "userprofile", profile );
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
		String path = parts[0];
		
		context.putPageValue("path", path);

		Page dynamicpage = getPageManager().getPage(path);
		context.putPageValue( PageRequestKeys.PAGE, dynamicpage);
		context.putPageValue( PageRequestKeys.CONTENT, dynamicpage);
		context.putPageValue( PageRequestKeys.USER, getUserManager().getUser("admin"));

		User admin = getUserManager().getUser("admin");
		if ( admin != null )
		{
			context.putPageValue( PageRequestKeys.USER, admin );
			String catid = dynamicpage.get("catalogid");
			context.putSessionValue(catid + "user",admin);
		}

		

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
