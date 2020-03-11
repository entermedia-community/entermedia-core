/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/
package org.openedit.modules.edit;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.OpenEditException;
import org.openedit.WebPageRequest;
import org.openedit.error.ContentNotAvailableException;
import org.openedit.modules.BaseModule;
import org.openedit.page.Page;
import org.openedit.page.PageRequestKeys;
import org.openedit.page.PageStreamer;
import org.openedit.page.manage.PageManager;
import org.openedit.repository.ReaderItem;
import org.openedit.repository.filesystem.StringItem;
import org.openedit.users.User;
import org.openedit.util.PathUtilities;
import org.openedit.util.URLUtilities;
/**
 * This module provides the page editing functionality, and several actions to support it.
 *
 * @author Eric Galluzzo
 */
public class BaseEditorModule extends BaseModule
{
	private static final String ERROR404_HTML = "/error404.html";
	private static Log log = LogFactory.getLog(BaseEditorModule.class);
	protected List fieldWelcomeFiles;
	public List getWelcomeFiles()
	{
		return fieldWelcomeFiles;
	}

	public void setWelcomeFiles(List inWelcomeFiles)
	{
		fieldWelcomeFiles = inWelcomeFiles;
	}

	protected String normalizePath(String inPath)
	{
		String path = inPath;

		if ((path != null) && !path.startsWith("/"))
		{
			path = "/" + path;
		}
		path = path.replaceAll("\\.draft\\.", ".");
		return path;
	}
	
	public void writeContent( WebPageRequest inContext ) throws OpenEditException
	{
		String path = inContext.getRequiredParameter("editPath");
		String content = inContext.getRequestParameter( "content");
		User user = inContext.getUser();
		if (user == null)
		{
			throw new OpenEditException("User must be logged in system before you can save");
		}
		log.debug("Writing content to path " + path);
		try
		{
			Page page = getPageManager().getPage(path);
			if ( page.isDraft() && !page.exists() )
			{
				//make sure we save the original copy first
				String opath = path.replaceAll("\\.draft\\.", ".");
				Page orig = getPageManager().getPage(opath);
				if( orig.exists() ) //it does not exist if we are in a translation
				{
					ReaderItem revision = new ReaderItem( page.getPath(), orig.getReader() ,orig.getCharacterEncoding());
					revision.setAuthor(user.getUserName());
					revision.setMessage("Original");
					revision.setLastModified(orig.getContentItem().lastModified());
					page.setContentItem(revision);
					getPageManager().copyPage(orig, page);
				}
			}
			StringItem revision = new StringItem( page.getPath(), content ,page.getCharacterEncoding());
			revision.setAuthor( user.getUserName() );
			String message = inContext.getRequestParameter("message");
			if  ( message == null || message.equalsIgnoreCase("reason for your change"))
			{
				message =  "edited online";
				//revision.setMessage( message );
			}
			revision.setMessage( message );
			page.setContentItem( revision );
			getPageManager().putPage( page );
			//releaseEditLock( inContext );	
		}
		catch (Exception ex)
		{
			throw new OpenEditException(ex);
		}
	}
	
	/*
	 * @deprecated remove in OE 5.0
	 */
	public void checkExist(WebPageRequest inReq) throws Exception
	{
		check404(inReq);
	}
	public void check404(WebPageRequest inReq) throws Exception
	{
		PageManager pageManager = getPageManager();
		boolean exist = inReq.getPage().exists(); 
		if (  exist )
		{
			Page page = inReq.getPage();
			//If link does not exists. Then put a real welcome page on there so that fallback will work
			if ( page.isFolder() )
		    {
				String isVirtual = inReq.getPage().get("virtual");
				if ( Boolean.parseBoolean(isVirtual))
				{
					return;
				}
		    	//Loop over the various starting pages. 
		    	page = findWelcomePage(page); 
		    	inReq.redirect(page.getPath() );
				
	    	}
			return;
		}
	
		
		PageStreamer streamer = inReq.getPageStreamer();
		if(streamer != null)
		{
			streamer.getWebPageRequest().putPageValue("pathNotFound",inReq.getPath());
		}
		String isVirtual = inReq.getPage().get("virtual");
		if ( Boolean.parseBoolean(isVirtual))
		{
			return;
		}
		
		URLUtilities utils = (URLUtilities)inReq.getPageValue(PageRequestKeys.URL_UTILITIES);

		if( utils != null)
		{
			//redirecting only works relative to a webapp
			if(streamer != null)
			{
				streamer.getWebPageRequest().putPageValue("forcedDestinationPath", utils.requestPathWithArgumentsNoContext() );
			}
		}
		
		if ( inReq.getContentPage().getPath().equals( inReq.getPath()))
		{
			if ( inReq.getPage().isHtml() &&  inReq.isEditable() )
			{
				String path = inReq.findValue("404wizardpage");
				if( path == null)
				{
					path = "/system/nopagefound.html";
				}
				Page wizard = pageManager.getPage(path);
				if ( wizard.exists() )
				{
					inReq.getPageStreamer().include( wizard);
					inReq.setHasRedirected(true);
					return;
				}
			}
			//log.info( "Could not use  add page wizard. 404 error on: " + inReq.getPath() );
			String errorpage = inReq.getContentPage().getProperty("error404"); //"/error404.html";
			errorpage = errorpage !=null ? errorpage : ERROR404_HTML;
			Page p404 = pageManager.getPage(errorpage);
			if( !p404.exists() )
			{
				//p404 = pageManager.getPage("/system/error404page.html");
				throw new ContentNotAvailableException("Content missing " +  inReq.getPath(), inReq.getPath());
			}
			if ( p404.exists() )
			{
				HttpServletResponse response = inReq.getResponse();
				if ( response != null)
				{
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				}
				inReq.putProtectedPageValue("content", p404);
				//inReq.forward(p404.getPath());
				return;
			}
		}
		else
		{
			inReq.getWriter().write("404 on " + inReq.getPath());
			inReq.getWriter().flush();
			inReq.setHasRedirected(true);
			
		}
	}
	
	protected boolean createDraft(Page inEditPage, WebPageRequest inReq)
	{
		User user = inReq.getUser();
		if( !inEditPage.getPath().endsWith(".html"))
		{
			return false;
		}
		//REMOVE THIS OPTION IN OE 6.0
		//This is a dumb option. It just confuses everyone 		//For draft mode unless the user has directedits permission
		String prop = inEditPage.get("oe.edit.directedits"); //allow direct editing but it is optional
		if( prop != null && Boolean.parseBoolean(prop))
		{
			return false;
		}
		prop = inEditPage.get("oe.edit.draftedits"); //turns on or off the feature 
		if( prop != null && !Boolean.parseBoolean(prop))
		{
			return false;
		}
		//check the .xconf and request parameters
		prop = inReq.findValue("oe.edit.draftedits");
		if( prop != null && !Boolean.parseBoolean(prop))
		{
			return false;
		}
		if( inEditPage.isDraft())
		{
			return false;
		}
		
		if ( !user.hasProperty("oe.edit.draftmode" ))
		{
			Boolean can = (Boolean)inReq.getPageValue("canopeneditdirectedit");
			if ( user.hasPermission("oe.edit.directedits") || can )
			{
				//do nothing since they have permission to be direct editing
				//or this file has a special property
			}
			else
			{
				user.setProperty("oe.edit.draftmode", "true");
			}
		}		
		if( user.hasProperty("oe.edit.draftmode") )
		{
			return true;
		}

		return false;
	}
	protected String findPathForMode(WebPageRequest inContext)
	{
		String path = inContext.getRequestParameter( "editPath" );
			//See what page we should pickup. Perhaps .draft
			if( inContext.getUser().hasProperty("oe.edit.draftmode") && path.indexOf(".draft.") == -1)
			{
				String root = PathUtilities.extractPagePath(path);
				String p = root + ".draft." + PathUtilities.extractPageType(path);
				return p;
			}
		return path;
	}
	public void redirectToOriginal(WebPageRequest inReq )
	{
		String editPath = inReq.getRequestParameter("editPath");
		String orig = inReq.getRequestParameter("origURL");
		if( orig != null)
		{
			if ( orig.indexOf("?") == -1 && editPath != null)
			{
				inReq.redirect(orig + "?path=" + editPath + "&cache=false");
			}
			else
			{
				inReq.redirect(orig);
			}
		}
		else
		{
			log.error("No origURL specified");
		}
		//orig ?path=dfdsf
	}
	/**
	 * This must be called as a path-action
	 * @param inReq
	 * @throws Exception
	 */
	public void forceDownload(WebPageRequest inReq) throws Exception
	{
		if( inReq.getResponse() != null)
		{
			Page content = inReq.getContentPage();
			String filename = content.getName(); 
			//filename = URLEncoder.encode(filename,content.getCharacterEncoding());
			inReq.getResponse().setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
		}
	}

	protected Page findWelcomePage(Page inDirectory) throws OpenEditException
	{
		String dir = inDirectory.getPath();
		if (!dir.endsWith("/"))
     	{
     		dir =  dir + "/";
     	}
		for (Iterator iterator = getWelcomeFiles().iterator(); iterator.hasNext();)
		{
			String index = (String) iterator.next();
	    	if( getPageManager().getRepository().doesExist( dir + index))
		    {
	    		return getPageManager().getPage(dir + index,true);
		    }
		}
		return getPageManager().getPage( dir + "index.html",true);
	}
}
