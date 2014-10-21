/*
 * Created on Oct 19, 2004
 */
package com.openedit.servlet;

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
import org.openedit.event.WebEventHandler;

import com.openedit.ModuleManager;
import com.openedit.OpenEditException;
import com.openedit.Shutdownable;
import com.openedit.WebPageRequest;
import com.openedit.error.ErrorHandler;
import com.openedit.generators.Output;
import com.openedit.page.Page;
import com.openedit.page.PageRequestKeys;
import com.openedit.page.PageStreamer;
import com.openedit.page.manage.PageManager;
import com.openedit.util.RequestUtils;
import com.openedit.util.URLUtilities;

public class BaseOpenEditEngine implements OpenEditEngine 
{
	private static final Log log = LogFactory.getLog(BaseOpenEditEngine.class);
	protected PageManager fieldPageManager;
	protected ModuleManager fieldModuleManager;
	protected List fieldWelcomeFiles;
	protected ErrorHandler fieldErrorHandler;
	protected String fieldVersion;
	protected boolean fieldHideFolders = true;
	protected WebEventHandler fieldPageEventHandler;
	protected RequestUtils fieldRequestUtils;
	
	public void render( HttpServletRequest inRequest, HttpServletResponse inResponse ) throws IOException, OpenEditException 
	{
		checkEngineInit( inResponse );
	    URLUtilities util = new URLUtilities(inRequest, inResponse);

	    String requestedPath = util.getOriginalPath();
	    HttpSession session = inRequest.getSession(false);
	    boolean checkdates = false;
	    if( session != null)
	    {
	    	checkdates = session.getAttribute("user") != null;
	    }
	    Page page = getPageManager().getPage( requestedPath,checkdates);
	    
		//If link does not exists. Then put a real welcome page on there so that fallback will work
		if ( page.isFolder() )
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
			}
    	}
		else
		{
			if( page.getPageType() == null)
			{
				boolean found = false;
				String alternative_page = page.get("alternative_page");
				if(alternative_page != null){
					page = getPageManager().getPage( alternative_page,false);
					found=true;
				}
				
				String alternative = page.get("alternative_extension");
				if(alternative != null && !found)
				{
					page = getPageManager().getPage( requestedPath + "." + alternative,false);
				}
			}
		}
	    if ( page.isHtml() )
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
		Page transpage = getPageManager().getPage(page,context);
		if(! transpage.getPath().equals(page.getPath())){
			
			context.setPage(transpage);
			
		}
		beginRender(context);
	}
	
	/**
     * @see com.openedit.servlet.OpenEditEngine#hideFolders()
     */
	public boolean hideFolders()
	{
		return fieldHideFolders;
	}
	
	/**
     * @see com.openedit.servlet.OpenEditEngine#setHideFolders(boolean)
     */
	public void setHideFolders( boolean inFlag )
	{
		fieldHideFolders = inFlag;
	}

	/**
     * @see com.openedit.servlet.OpenEditEngine#beginRender(com.openedit.WebPageRequest)
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
			}
		}
		catch( Exception e )
		{
			log.error(e);
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
     * @see com.openedit.servlet.OpenEditEngine#executePageActions(com.openedit.WebPageRequest)
     */
	public void executePageActions( WebPageRequest inPageRequest ) throws OpenEditException
	{	
		getModuleManager().executePageActions( inPageRequest.getPage(),inPageRequest );
	}
	/**
     * @see com.openedit.servlet.OpenEditEngine#executePathActions(com.openedit.WebPageRequest)
     */
	public void executePathActions( WebPageRequest inPageRequest ) throws OpenEditException
	{	
		getModuleManager().executePathActions( inPageRequest.getPage(), inPageRequest );
	}
	
	/**
     * @see com.openedit.servlet.OpenEditEngine#getModuleManager()
     */
	public ModuleManager getModuleManager()
	{
		return fieldModuleManager;
	}
	/**
     * @see com.openedit.servlet.OpenEditEngine#setModuleManager(com.openedit.ModuleManager)
     */
	public void setModuleManager( ModuleManager moduleManager )
	{
		fieldModuleManager = moduleManager;
	}
	/**
     * @see com.openedit.servlet.OpenEditEngine#getPageManager()
     */
	public PageManager getPageManager()
	{
		return fieldPageManager;
	}
	/**
     * @see com.openedit.servlet.OpenEditEngine#setPageManager(com.openedit.page.manage.PageManager)
     */
	public void setPageManager( PageManager pageManager )
	{
		fieldPageManager = pageManager;
	}

	/**
     * @see com.openedit.servlet.OpenEditEngine#getWelcomePath()
     */
	public List getWelcomeFiles()
	{
		return fieldWelcomeFiles;
	}
	/**
     * @see com.openedit.servlet.OpenEditEngine#setWelcomePath(java.lang.String)
     */
	public void setWelcomeFiles( List welcomePath )
	{
		fieldWelcomeFiles = welcomePath;
	}
	/**
     * @see com.openedit.servlet.OpenEditEngine#getErrorHandler()
     */
	public ErrorHandler getErrorHandler()
	{
		return fieldErrorHandler;
	}
	/**
     * @see com.openedit.servlet.OpenEditEngine#setErrorHandler(com.openedit.error.ErrorHandler)
     */
	public void setErrorHandler( ErrorHandler errorHandler )
	{
		fieldErrorHandler = errorHandler;
	}
	/**
     * @see com.openedit.servlet.OpenEditEngine#getVersion()
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
     * @see com.openedit.servlet.OpenEditEngine#shutdown()
     */
	public void shutdown()
	{
		Object[] beans = getModuleManager().getLoadedBeans().toArray();

		for (int i = 0; i < beans.length; i++)
		{
			Object module = (Object) beans[i];
			if( module instanceof Shutdownable)
			{
				((Shutdownable)module).shutdown();
			}
			
		}
	}


	public void setPageEventHandler(WebEventHandler inWebEventHandler)
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