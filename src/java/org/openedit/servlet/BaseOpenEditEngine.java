/*
 * Created on Oct 19, 2004
 */
package org.openedit.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.ModuleManager;
import org.openedit.OpenEditException;
import org.openedit.Shutdownable;
import org.openedit.WebPageRequest;
import org.openedit.error.ErrorHandler;
import org.openedit.event.EventManager;
import org.openedit.generators.Output;
import org.openedit.page.Page;
import org.openedit.page.PageRequestKeys;
import org.openedit.page.PageStreamer;
import org.openedit.page.manage.PageManager;
import org.openedit.util.RequestUtils;
import org.openedit.util.URLUtilities;

public class BaseOpenEditEngine implements OpenEditEngine 
{
	private static final Log log = LogFactory.getLog(BaseOpenEditEngine.class);
	protected PageManager fieldPageManager;
	protected ModuleManager fieldModuleManager;
	protected List fieldWelcomeFiles;
	protected ErrorHandler fieldErrorHandler;
	protected String fieldVersion;
	protected boolean fieldHideFolders = true;
	protected EventManager fieldPageEventHandler;
	protected RequestUtils fieldRequestUtils;
	
	public void render( HttpServletRequest inRequest, HttpServletResponse inResponse ) throws IOException, OpenEditException 
	{
		checkEngineInit( inResponse );
		//inRequest.setCharacterEncoding( "UTF-8" ); //This needs to be the first thing we do. Dont call getParameter
	    URLUtilities util = new URLUtilities(inRequest, inResponse);

	    String requestedPath = util.getOriginalPath();
	    SiteData sitedata = getSiteManager().findSiteData(util.siteRoot());
	    if(sitedata != null)
	    {
	    	requestedPath = sitedata.findAppPath(requestedPath);
	    }
	    boolean checkdates = false;
		HttpSession session = inRequest.getSession(false);
		if ( session != null)
		{
			String mode = (String)session.getAttribute("oe_edit_mode");
			if( "debug".equals(mode) || "editing".equals(mode))
			{
				checkdates = true;
			}
		}
//		if( !checkdates )
//		{
//			checkdates = Boolean.parseBoolean( inRequest.getParameter("reload") ); //Dont call before we call setCharacterEncoding
//		}
	    Page page = getPageManager().getPage( requestedPath,checkdates);
	    
		//If link does not exists. Then put a real welcome page on there so that fallback will work
	    boolean wasfolder = page.isFolder(); 
		if ( wasfolder )
	    {
	    	page = findWelcomePage(page, checkdates); 
			if( !util.requestPath().endsWith("/"))
			{
	    		String contextPath = inRequest.getContextPath();
				if( contextPath == null )
				{
					contextPath  = ""; //No Webapp
				}
				inResponse.sendRedirect(contextPath + page.getPath() );
				return;
			}
    	}
		if( page.getPageType() == null || wasfolder)
		{
			boolean found = false;
			String alternative_page = page.get("alternative_page");
			if(alternative_page != null)
			{
				page = getPageManager().getPage( alternative_page,false);
			}
			else
			{
				String alternative = page.get("alternative_extension");
				if(alternative != null)
				{
					page = getPageManager().getPage( requestedPath + "." + alternative,false);
				}
			}	
		}
		//inResponse.addHeader("Connection", "Keep-Alive");
		//inResponse.addHeader("Keep-Alive", "timeout=60000");

	    if ( page.isDynamic() )
		{
			inRequest.setCharacterEncoding( page.getCharacterEncoding() );
			inResponse.setContentType( page.getMimeType() + "; charset=" + page.getCharacterEncoding() );
		    inResponse.setHeader("Cache-Control","proxy-revalidate"); //must revalidate does not work well with IE7
		    //Since they cannot hit the back button on a form submit
		}
		else
		{
			String mime = page.getMimeType();
			inResponse.setContentType( mime );
		}

		WebPageRequest context = createWebPageRequest( page, inRequest, inResponse, util );
		String applicationid = page.getProperty("applicationid");
		if( sitedata != null)
		{
			context.putPageValue("sitedata", sitedata);
			page.setProperty("apphome", sitedata.getAppHome(applicationid));
		}
		else
		{
			page.setProperty("apphome", "/" + applicationid);			
		}
		context.putPageValue("reloadpages", checkdates);
		Page transpage = getPageManager().getPage(page,context);
		if(! transpage.getPath().equals(page.getPath())){
			
			context.setPage(transpage);
			
		}
		beginRender(context);
		//if ( page.isDynamic() )
		//{
			context.closeStreams(); 
		//}
	}
	
	/**
     * @see org.openedit.servlet.OpenEditEngine#hideFolders()
     */
	public boolean hideFolders()
	{
		return fieldHideFolders;
	}
	
	/**
     * @see org.openedit.servlet.OpenEditEngine#setHideFolders(boolean)
     */
	public void setHideFolders( boolean inFlag )
	{
		fieldHideFolders = inFlag;
	}

	/**
     * @see org.openedit.servlet.OpenEditEngine#beginRender(org.openedit.WebPageRequest)
     */
	public void beginRender(WebPageRequest pageRequest) throws OpenEditException
	{
		try
		{
			//log.debug("Running: " + pageRequest.getPathUrl());
			Page page = pageRequest.getPage();
			PageStreamer pageStreamer = null;
			pageStreamer = createPageStreamer( page,pageRequest );
			
			executePathActions(pageRequest);
			if( !pageRequest.hasRedirected())
			{
				getModuleManager().executePageActions( page,pageRequest );
			}
			if( !pageRequest.hasRedirected())
			{
				pageStreamer.render();
				//The GzipFilter does a close() on it's content
				//The FileGenerater sets a content length (Mostly)
				//TODO: Put only one close() in here?
			}
		}
		catch( Throwable e )
		{
			log.error("Problem redering page",e);
			//e.printStackTrace();
			boolean ok = getErrorHandler().handleError( e, pageRequest );
			
			if(!ok )
			{
				if( e instanceof OpenEditException )
				{
					throw (OpenEditException )e;
				}	
				else
				{
					throw new OpenEditException(e);
				}
			}
		}

	}
/**
 * @deprecated call RequestUtils.createPageStreamer
 */
	public PageStreamer createPageStreamer( Page inPage, WebPageRequest inPageRequest ) throws OpenEditException
	{
		PageStreamer pageStreamer = new PageStreamer();
		pageStreamer.setEngine( this ); 
		
		Output out = new Output();
		out.setWriter((Writer)inPageRequest.getPageValue(PageRequestKeys.OUTPUT_WRITER));
		out.setStream((OutputStream)inPageRequest.getPageValue(PageRequestKeys.OUTPUT_STREAM));
		
		pageStreamer.setOutput(out);
		pageStreamer.setWebPageRequest( inPageRequest);
		inPageRequest.putPageStreamer(pageStreamer );
		return pageStreamer;
	}


	protected void checkEngineInit( HttpServletResponse inResponse ) throws IOException
	{
		if ( getPageManager() == null )
		{
			inResponse.getWriter().print(
				"<html>Server is not initialized, please check the logs for errors</html>");
			return;
		}
	}
	
	protected WebPageRequest createWebPageRequest(
			Page inPage,
			HttpServletRequest inRequest,
			HttpServletResponse inResponse, URLUtilities util) throws OpenEditException
		{
			WebPageRequest context = getRequestUtils().createPageRequest(inPage, inRequest, inResponse,null, util);
			//context.putProtectedPageValue( "version", getVersion());
			return context;
		}

	/**
     * @see org.openedit.servlet.OpenEditEngine#executePageActions(org.openedit.WebPageRequest)
     */
	public void executePageActions( WebPageRequest inPageRequest ) throws OpenEditException
	{	
		getModuleManager().executePageActions( inPageRequest.getPage(),inPageRequest );
	}
	/**
     * @see org.openedit.servlet.OpenEditEngine#executePathActions(org.openedit.WebPageRequest)
     */
	public void executePathActions( WebPageRequest inPageRequest ) throws OpenEditException
	{	
		getModuleManager().executePathActions( inPageRequest.getPage(), inPageRequest );
	}
	
	/**
     * @see org.openedit.servlet.OpenEditEngine#getModuleManager()
     */
	public ModuleManager getModuleManager()
	{
		return fieldModuleManager;
	}
	/**
     * @see org.openedit.servlet.OpenEditEngine#setModuleManager(org.openedit.ModuleManager)
     */
	public void setModuleManager( ModuleManager moduleManager )
	{
		fieldModuleManager = moduleManager;
	}
	/**
     * @see org.openedit.servlet.OpenEditEngine#getPageManager()
     */
	public PageManager getPageManager()
	{
		return fieldPageManager;
	}
	/**
     * @see org.openedit.servlet.OpenEditEngine#setPageManager(org.openedit.page.manage.PageManager)
     */
	public void setPageManager( PageManager pageManager )
	{
		fieldPageManager = pageManager;
	}

	public SiteManager getSiteManager()
	{
		return (SiteManager)getModuleManager().getBean("system","siteManager");
	}
	/**
     * @see org.openedit.servlet.OpenEditEngine#getWelcomePath()
     */
	public List getWelcomeFiles()
	{
		return fieldWelcomeFiles;
	}
	/**
     * @see org.openedit.servlet.OpenEditEngine#setWelcomePath(java.lang.String)
     */
	public void setWelcomeFiles( List welcomePath )
	{
		fieldWelcomeFiles = welcomePath;
	}
	/**
     * @see org.openedit.servlet.OpenEditEngine#getErrorHandler()
     */
	public ErrorHandler getErrorHandler()
	{
		return fieldErrorHandler;
	}
	/**
     * @see org.openedit.servlet.OpenEditEngine#setErrorHandler(org.openedit.error.ErrorHandler)
     */
	public void setErrorHandler( ErrorHandler errorHandler )
	{
		fieldErrorHandler = errorHandler;
	}
	/**
     * @see org.openedit.servlet.OpenEditEngine#getVersion()
     */
	/*
	protected boolean requireVersion(int majorVersion, int minorVersion)
	{
		ServletContext servletContext = getServletContext();
		return (
			(servletContext.getMajorVersion() > majorVersion)
				|| (servletContext.getMajorVersion() == majorVersion
					&& servletContext.getMinorVersion() >= minorVersion));
	}
	*/
	public String getVersion()
	{
		if (fieldVersion == null)
		{
			Package thisPackage = getClass().getPackage();
			if (thisPackage != null)
			{
				fieldVersion = thisPackage.getImplementationVersion();
			}
			if (fieldVersion == null)
			{
				fieldVersion = "dev";
			}
		}
		return fieldVersion;
	}

	/**
     * @see org.openedit.servlet.OpenEditEngine#shutdown()
     */
	public void shutdown()
	{
		System.out.println("OpenEditEngine shutdown start");
		Object[] beans = getModuleManager().getLoadedBeans().toArray();

		for (int i = 0; i < beans.length; i++)
		{
			Object module = (Object) beans[i];
			if( module instanceof Shutdownable)
			{
				try
				{
					((Shutdownable)module).shutdown();
				}
				catch (Throwable ex)
				{
					log.error(ex);
				}
			}
		}
		System.out.println("OpenEditEngine shutdown complete");
	}


	public void setPageEventHandler(EventManager inWebEventHandler)
	{
		fieldPageEventHandler = inWebEventHandler;
	}

	public RequestUtils getRequestUtils()
	{
		return fieldRequestUtils;
	}

	public void setRequestUtils(RequestUtils inRequestUtils)
	{
		fieldRequestUtils = inRequestUtils;
	}
	protected Page findWelcomePage(Page inDirectory, boolean indates) throws OpenEditException
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
	    		return getPageManager().getPage(dir + index,indates);
		    }
		}
		return getPageManager().getPage( dir + "index.html",indates);
	}
}