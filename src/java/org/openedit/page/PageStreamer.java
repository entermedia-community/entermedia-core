/*
 * Created on Oct 19, 2004
 */
package org.openedit.page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.OpenEditException;
import org.openedit.WebPageRequest;
import org.openedit.generators.Output;
import org.openedit.page.manage.PageManager;
import org.openedit.profile.UserProfile;
import org.openedit.servlet.OpenEditEngine;
import org.openedit.users.User;
import org.openedit.util.PathUtilities;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class PageStreamer
{
	private static final Log log = LogFactory.getLog(PageStreamer.class);
	protected OpenEditEngine fieldEngine;
	protected Output fieldOutput;
	
	protected WebPageRequest fieldWebPageRequest;
	protected Map fieldWebPageRequestedCount;
	protected List fieldChildContentList;
	protected int fieldMaxLoop = 10000;
	protected boolean fieldDebug = false;
	
	public PageStreamer()
	{
		//log.debug("Created streamer");
	}
	
	/**
	 * This is called only once
	 * We need to exec all the page actions ahead of time
	 * And check the read permissions ahead of time
	 * Then we can do the rendering
	 * @throws OpenEditException
	 */

	public void render() throws OpenEditException
	{
		Page page = getWebPageRequest().getPage();
		boolean runlayout = page.isHtml();
		if( !runlayout)
		{
			runlayout = Boolean.parseBoolean( page.getProperty("forcelayout") );
		}
		if (runlayout)			
		{
			getChildContentList().add(getWebPageRequest().getContentPage() );

			//allow someone to programatically override the top level inner layout
			String override = (String)getWebPageRequest().getPageValue(PageRequestKeys.INNERLAYOUTOVERRIDE);
			if( override != null)
			{
				if( !override.equals(Page.BLANK_LAYOUT))
				{
					Page il = getPage(override);
					addInnerLayout( il);
				}
			}
			else
			{
				addInnerLayout( getWebPageRequest().getContentPage());
			}
			//handle layout
			String layout = getLayout();
			if ( layout != null )
			{
				Page layoutPage = getPage(layout); 
				getChildContentList().add(0,layoutPage);
			}
			User user = getWebPageRequest().getUser();
			if( user != null)
			{
				if( "debug".equals(user.get("oe_edit_mode")))
				{
					setDebug(true);
				}
			}

			if( getChildContentList().size() > 1)
			{
				//This number starts at the content page and ends in the middle of the layouts
				String maxlevels = getWebPageRequest().getRequestParameter("oemaxlevel");
				if( maxlevels == null)
				{
					maxlevels = getWebPageRequest().getContentProperty("oemaxlevel");
				}
	
				if( maxlevels != null)
				{
					int cut = Integer.parseInt(maxlevels);
					List toCut = getChildContentList();
					if( cut < toCut.size())
					{
						toCut = toCut.subList(toCut.size() - cut, toCut.size());
						fieldChildContentList = toCut;
					}
				}
			}
			
			if( getChildContentList().size() > 1)
			{
				//This number starts in the middle of the layout list and ends at the content
				String maxlevels = getWebPageRequest().getRequestParameter("oemaxlayout");
				if( maxlevels == null)
				{
					maxlevels = getWebPageRequest().getContentProperty("oemaxlayout");
				}
	
				if( maxlevels != null)
				{
					int cut = Integer.parseInt(maxlevels);
					List toCut = getChildContentList();
					if( cut < toCut.size())
					{
						toCut = toCut.subList(cut, toCut.size());
						fieldChildContentList = toCut;
					}
				}
			}
			
			
			
			String cancel = getWebPageRequest().getPage().getPageSettings().getPropertyValueFixed("cancelinnerlayout");
			if( cancel != null)
			{
				List toCut = getChildContentList();
				for (int i = 0; i < toCut.size(); i++)
				{						
					Page path = (Page) toCut.get(i);
					if( path.getPath().equals(cancel))
					{
						toCut.remove(i);
						break;
					}
				}
			}
			includeContent();
		}
		else
		{	//CSS files. Binary
			WebPageRequest request = getWebPageRequest();
			Page torender = getPageManager().getPage(page, getWebPageRequest() );			
			if ( torender != page ) //This could be a draft version
			{
				request = getWebPageRequest().copy(torender);
				getEngine().executePageActions(request);
			}
			torender.generate(request, getOutput());
		}
	}

	protected void addInnerLayout(Page inContentPage) throws OpenEditException
	{
//		log.info("Adding " + inContentPage);
		String innerlayout = inContentPage.findInnerLayout();
		if ( innerlayout != null)
		{
			UserProfile profile = getWebPageRequest().getUserProfile();
			if( profile != null)
			{
				innerlayout = profile.replaceUserVariable(innerlayout);
			}
				
			if( !inContentPage.getPath().equalsIgnoreCase(innerlayout))
			{
				//The contentpage has an inner layout set that is not itself
				Page il = getPageManager().getPage(innerlayout,getWebPageRequest().getUser() != null);
				if( !getChildContentList().contains(il))
				{
					//It is not in the list so we add it
					if(  getChildContentList().size() < getMaxLoop() )
					{
						getChildContentList().add(0,il);
						addInnerLayout(il);
					}
				}
				else
				{	
					//Special case: It is aleady in the list so we try again using the parent layout
					//We might have a directory that has two layouts inside it.
					String parentpath = PathUtilities.extractDirectoryPath(il.getParentPath());
					if( !parentpath.equals(""))
					{
						Page parent = getPageManager().getPage(parentpath,getWebPageRequest().getUser() != null);
						if( parent.hasInnerLayout())
						{
							il = getPageManager().getPage(parent.getInnerLayout(),true);
							if(  !getChildContentList().contains(il) ) //If we find two in a row we stop looking
							{
								addInnerLayout(parent);
							}
						}
					}
				}
			}
		}
		//log.info("Found " + inContentPage.getPath() + " layouts: " + getChildContentList());
	}
	protected void renderLayout() throws OpenEditException
	{
		Page inLayout = getEngine().getPageManager().getPage(getLayout(),getWebPageRequest().getUser() != null);
		if (!inLayout.exists())
		{
			getWebPageRequest().putPageValue("missingLayoutPage", inLayout.getPath());
			inLayout = getPage("/layoutnotfound.html");
			if (!inLayout.exists())
			{
				inLayout = getPage("/openedit/layoutnotfound.html");
			}
		}
		include(inLayout);
	}
	/**
	 * @deprecated use includeContent()
	 * @throws OpenEditException
	 */
	public void streamContent() throws OpenEditException
	{
		includeContent();
	}
	/**
	 * @deprecated use includeContent()
	 * @throws OpenEditException
	 */
	public void renderContent() throws OpenEditException
	{
		includeContent();
	}
	public void includeContent() throws OpenEditException
	{
		includeContent(getWebPageRequest());
	}
	public void includeContent(WebPageRequest inContext) throws OpenEditException
	{
		//if I am being called from an inner layout make sure I am at the top first
		if ( getChildContentList().size() == 0)
		{
			//throw new OpenEditException("Ran out of content on " + getWebPageRequest().getPath());
			//stream(getPage());
			return;
		}
		Page topChild = (Page)getChildContentList().remove(0);
		include( topChild ,inContext );
	}
	public void include(Page inPage) throws OpenEditException
	{
		include( inPage, getWebPageRequest() );
	}
	/**
	 * Allows a page to be streamed that uses this request as the parent request
	 */
	public void include(Page inPage, WebPageRequest inContext) throws OpenEditException
	{
		int hitcount = incrementWebPageRequestedCount(inPage);
		//This is used when viewing a layout page directly (i.e. /layout.html)
		if (hitcount >= getMaxLoop())
		{
			//ok its an infinite loop
			throw new OpenEditException(
				"Page loop detected calling "
					+ inPage.getPath()
					+ "more than " + getMaxLoop() + " times.");
		}
		//This is trickly to understand. The contents actions have already been run. Now we either
		//run the actions or if its the content page those actions have already been run

		Page torender = null;
		WebPageRequest request = inContext;
		
		if (inPage == inContext.getContentPage())
		{
			torender = getPageManager().getPage(inPage, request );			
			if ( torender != inPage ) //This could be a draft version
			{
				request = inContext.copy(torender);
				getEngine().executePageActions(request);
			}
		}
		else
		{
			//these are the included pages ie. /sidebar.html that may have their own actions
			request = inContext.copy(inPage);
            // Ensure the FILE_PATH variable has the correct value for this streamed page.
			
            request.putPageValue(PageRequestKeys.FILE_PATH, inPage.getDirectory()); //This is legacy from OE 4.0 can be deleted anytime
			torender = getPageManager().getPage(inPage, request );			
			if ( torender != inPage) //This could be a draft version
			{
				request = request.copy(torender);
			}
			if (!request.hasRedirected() )
			{
				getEngine().executePageActions(request);
			}
		}
		request.putPageValue("originalPage", inPage);
		torender.generate(request, getOutput() ); 
	}
	public void include(String inPath, WebPageRequest inReq) throws OpenEditException
	{
		String[] parts = inPath.split("[?]");
		String fullPath = PathUtilities.resolveRelativePath(parts[0], getWebPageRequest().getContentPage()
			.getPath());
		boolean canedit = false;
		User user = getWebPageRequest().getUser();
		if( user != null )
		{
			String prop = (String)user.get("showeditor");
			canedit = Boolean.parseBoolean(prop);
		}
		Page page = getEngine().getPageManager().getPage(fullPath,canedit);

		if (parts.length > 1)
		{
			Map arguments = PathUtilities.extractArguments(parts[1]);
			for (Iterator iterator = arguments.keySet().iterator(); iterator.hasNext();)
			{
				String param = (String)iterator.next();
				inReq.setRequestParameter(param, (String[])arguments.get(param));
			}
		}
		include(page, inReq);
	}
	public void include(String inPath) throws OpenEditException
	{
		include( inPath, getWebPageRequest());
	}
	/**
	 * Use include
	 * @deprecated
	 */
	public void stream(Page inPage) throws OpenEditException
	{
		include( inPage);
	}
	/**
	 * Use include
	 * @deprecated
	 */
	public void stream(Page inPage, WebPageRequest inContext) throws OpenEditException
	{
		include( inPage, inContext);
	}
	/**
	 * Use include
	 * @deprecated
	 */
	public void stream(String inPath, WebPageRequest inReq) throws OpenEditException
	{
		include( inPath, inReq);
	}
	/**
	 * Use include
	 * @deprecated
	 */
	public void stream(String inPath) throws OpenEditException
	{
		include( inPath );
	}
	public Map getWebPageRequestedCount()
	{
		if (fieldWebPageRequestedCount == null)
		{
			fieldWebPageRequestedCount = new HashMap();
		}
		return fieldWebPageRequestedCount;
	}

	protected int incrementWebPageRequestedCount(Page inPage)
	{
		Integer count = (Integer) getWebPageRequestedCount().get(inPage);
		int hitcount = 0;

		if (count != null)
		{
			hitcount = count.intValue() + 1;
		}

		getWebPageRequestedCount().put(inPage, new Integer(hitcount));
		return hitcount;
	}

	public boolean doesExist(String inPath) throws OpenEditException
	{
		if (inPath == null)
		{
			return false;
		}
		return loadRelativePath(inPath).exists();
	}

	protected Page loadRelativePath(String inPath) throws OpenEditException
	{
		String fullPath = PathUtilities.buildRelative(inPath, getWebPageRequest().getContentPage().getPath());
		return getPage(fullPath);
	}

	public Page getPage(String inPath) throws OpenEditException
	{
		return getPageManager().getPage(inPath, getWebPageRequest().getUser() != null);
	}

	public PageManager getPageManager()
	{
		return getEngine().getPageManager();
	}

	public OpenEditEngine getEngine()
	{
		return fieldEngine;
	}

	public void setEngine(OpenEditEngine engine)
	{
		fieldEngine = engine;
	}

	public WebPageRequest getWebPageRequest()
	{
		return fieldWebPageRequest;
	}

	public void setWebPageRequest(WebPageRequest WebPageRequest)
	{
		fieldWebPageRequest = WebPageRequest;
	}

	public void forward(String path, WebPageRequest inReq) throws OpenEditException
	{
		//if this is the error page then we can get into an infinite loop
		//WebPageRequest WebPageRequest = getWebPageRequest().copy();
		//WebPageRequest.setPage( getPage( path ) );	
		//getEngine().render( WebPageRequest );
		Page original = inReq.getPage();
		Page page = getPage(path);
		//remove layout overrides
		
		WebPageRequest req = getWebPageRequest();
		req.removePageValue(PageRequestKeys.INNERLAYOUTOVERRIDE);
		req.removePageValue(PageRequestKeys.LAYOUTOVERRIDE);
		if( req.getContentPage().getPath().equals(inReq.getPage().getPath())) //are we rendeing the content page?
		{
			req.putProtectedPageValue(PageRequestKeys.CONTENT,page);
		}
		req.putProtectedPageValue(PageRequestKeys.PAGE,page);
		req.putPageValue("forwardpage", original);
		getEngine().getModuleManager().executePathActions(page, req);
		getEngine().getModuleManager().executePageActions( page,req );
		inReq.setHasForwarded(true);
		//Now it continues to render as normal but with alternative content.
		//Does not support custom layouts
	}

	public PageStreamer copy()
	{
		PageStreamer streamer = new PageStreamer();
		streamer.setEngine(getEngine());
		streamer.fieldChildContentList = getChildContentList();
		streamer.setWebPageRequest(getWebPageRequest());
		streamer.setOutput(getOutput());
		return streamer;
	}

	protected String getLayout()
	{
		String override = (String) getWebPageRequest().getPageValue("layoutoverride");
		if (override != null)
		{
			if (override.equals(Page.BLANK_LAYOUT))
			{
				return null;
			}
			return override;
		}
		Page page = getWebPageRequest().getPage();

		if (page.hasLayout())
		{
			String layout = page.getLayout();
			layout = page.getPageSettings().replaceProperty(layout);
			return layout;
		}
		return null;
	}

	public List getChildContentList()
	{
		if ( fieldChildContentList == null)
		{
			fieldChildContentList = new ArrayList();
		}
		return fieldChildContentList;
	}

	public Output getOutput()
	{
		return fieldOutput;
	}

	public void setOutput(Output inOutput)
	{
		fieldOutput = inOutput;
	}
	
	public boolean canView(String inPath) throws OpenEditException
	{
		
		if(inPath.contains("?")) {
			inPath = inPath.substring(0,inPath.indexOf("?"));
		}
		Page linkpage = getPageManager().getPage(inPath,getWebPageRequest().getUser() != null); 
		if (linkpage.exists() || Boolean.parseBoolean( linkpage.getProperty("virtual")))
		{
			Permission filter = linkpage.getPermission("view");
			if( filter != null)
			{
				WebPageRequest req = getWebPageRequest().copy(linkpage);
				boolean value= filter.passes( req );
				return value;
			}
			return true;
		}
		return false;
	}

	public boolean canDo(String inDo, String inPath) throws OpenEditException
	{
		Page linkpage = getPageManager().getPage(inPath); 
		Permission filter = linkpage.getPermission(inDo);
		if( filter != null)
		{
			WebPageRequest req = getWebPageRequest().copy(linkpage);
			boolean value= filter.passes( req );
			return value;
		}
		return true;
	}
	public WebPageRequest canDoPermissions(String inPath) throws OpenEditException
	{
		Page linkpage = getPageManager().getPage(inPath,getWebPageRequest().getUser() != null); 
		WebPageRequest request = getWebPageRequest().copy(linkpage);
		request.putPageValue(PageRequestKeys.CONTENT, linkpage);
		getEngine().getModuleManager().execute("Admin.loadPermissions",request);
		return request;
	}
	
	public boolean canEdit(String inPath) throws OpenEditException
	{
		if (inPath == null)
		{
			return false;
		}
		
		boolean value  = canDo("edit",inPath);
		if(value == false)
		{
			User user = getWebPageRequest().getUser();
			if( user != null)
			{
				value = user.hasPermission("oe.filemanager.editall");
			}
		}
		return value;
	}
	

	public int getMaxLoop()
	{
		return fieldMaxLoop;
	}

	public void setMaxLoop(int inMaxLoop)
	{
		fieldMaxLoop = inMaxLoop;
	}

	public boolean isDebug()
	{
		return fieldDebug;
	}

	public void setDebug(boolean inDebug)
	{
		fieldDebug = inDebug;
	}
}
