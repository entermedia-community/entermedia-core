package org.openedit.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.BaseWebPageRequest;
import org.openedit.ModuleManager;
import org.openedit.OpenEditException;
import org.openedit.OpenEditRuntimeException;
import org.openedit.WebPageRequest;
import org.openedit.WebServer;
import org.openedit.error.ErrorHandler;
import org.openedit.generators.Output;
import org.openedit.page.Page;
import org.openedit.page.PageRequestKeys;
import org.openedit.page.PageStreamer;
import org.openedit.page.manage.PageManager;
import org.openedit.profile.UserProfile;
import org.openedit.users.User;
import org.openedit.web.Browser;

public class RequestUtils {
	
	protected PageManager fieldPageManager;
	protected WebServer fieldWebServer;
	protected ModuleManager fieldModuleManager;
	protected ClassLoader fieldClassLoader;
	protected LocaleManager fieldLocaleManager;
	protected String fieldHome;
	protected String fieldSiteRoot;
	
	protected String getHome()
	{
		return fieldHome;
	}


	protected void setHome(String inHome)
	{
		fieldHome = inHome;
	}


	public LocaleManager getLocaleManager()
	{
		if (fieldLocaleManager == null)
		{
			fieldLocaleManager = new LocaleManager();
		}
		return fieldLocaleManager;
	}


	public void setLocaleManager(LocaleManager inLocaleManager)
	{
		fieldLocaleManager = inLocaleManager;
	}

	static final Log log = LogFactory.getLog(RequestUtils.class);
	
	public PageManager getPageManager() {
		return fieldPageManager;
	}


	public void setPageManager(PageManager fieldPageManager) {
		this.fieldPageManager = fieldPageManager;
	}


	public WebServer getWebServer() {
		return fieldWebServer;
	}


	public void setWebServer(WebServer fieldWebServer) {
		this.fieldWebServer = fieldWebServer;
	}


	public ModuleManager getModuleManager() {
		return fieldModuleManager;
	}


	public void setModuleManager(ModuleManager fieldModuleManager) {
		this.fieldModuleManager = fieldModuleManager;
	}

	public WebPageRequest createVirtualPageRequest(String path, User inUser,UserProfile inProfile)
	{
    	WebPageRequest request = createPageRequest(path, inUser);
    	request.setUser(inUser);
    	request.setUserProfile(inProfile);
    	
//    	if( inUtil != null)
//		{
//			// add the URLUtilities to the context
//			request.putProtectedPageValue(PageRequestKeys.URL_UTILITIES, inUtil);
//			request.putProtectedPageValue( PageRequestKeys.WEB_SERVER_PATH, inUtil.buildRoot() );
//			request.putProtectedPageValue(PageRequestKeys.HOME, inUtil.relativeHomePrefix());
//			
//				
//		}	
//		
		try
		{
		
			Page page = request.getPage();
			getWebServer().getOpenEditEngine().createPageStreamer( page, request);
			  Thread thread = Thread.currentThread();
			  ClassLoader oldLoader = thread.getContextClassLoader();
			  try {
			    thread.setContextClassLoader(getClassLoader());
			    log.debug("running " + path);
			    getModuleManager().executePageActions(page, request );
			    getModuleManager().executePathActions(page, request );
			  } finally {
			    thread.setContextClassLoader(oldLoader);
			  }
		}
		catch( Exception e )
		{
			handleException( path, e , request);
		}
		catch( Throwable e )
		{
			//handleException( path, e , request);
			log.error( e );
		}
		return request;
	
    }
	public ClassLoader getClassLoader()
	{
		return Thread.currentThread().getContextClassLoader();
	}
	
	private void handleException( String inActionName, Exception e, WebPageRequest inReq )
	{
		log.error( "Scheduler failed to execution action " + inActionName + " " + e.getMessage() );

		e.printStackTrace();
		
		try
		{
			ErrorHandler handler = (ErrorHandler)getModuleManager().getBean("defaultErrorHandler");
			handler.handleError(e, inReq);
		}
		catch ( Throwable ex )
		{
			log.error( "Error handle error ignored: " + ex); 
		}
	}

	public WebPageRequest createPageRequest(String inPath, User inUser) 
	{
		try
		{
			String[] parts = inPath.split("[?]"); //TODO: Move this to Util class
			Page page = getPageManager().getPage(parts[0],true);
			return createPageRequest(inUser, parts, page);
		}
		catch( Exception e )
		{
			throw new OpenEditRuntimeException(e);
		}
	}


	public WebPageRequest createPageRequest(User inUser, String[] parts, Page page)
	{
		BaseWebPageRequest request =  (BaseWebPageRequest)createPageRequest(page, null,null,inUser,null);

		request.setWriter(new StringWriter() );
		if (parts != null && parts.length > 1)
		{
			String[] args = parts[1].split("&");
			for (int i = 0; i < args.length; i++)
			{
				String[] pairs = args[i].split("=");
				request.setRequestParameter(pairs[0], pairs[1]);
			}
		}		

	//	request.putPageValue("siteRoot", getSiteRoot());

		return request;
	}
	
	public WebPageRequest createPageRequest(Page inPage, HttpServletRequest inRequest,
			HttpServletResponse inResponse, User inUser, URLUtilities util) 
	{
		BaseWebPageRequest context = (BaseWebPageRequest)getModuleManager().getBean("webPageRequest");
		context.setLocaleManager(getLocaleManager());
		context.putProtectedPageValue( PageRequestKeys.PAGE, inPage);
		context.putProtectedPageValue(PageRequestKeys.CONTENT, inPage);	
		context.putProtectedPageValue(PageRequestKeys.PAGE_MANAGER, getPageManager());
		context.putProtectedPageValue(PageRequestKeys.USER, inUser);	
		// put standard servlet stuff into the context
		if( inRequest != null)
		{
			context.putProtectedPageValue( PageRequestKeys.REQUEST, inRequest);
			context.putProtectedPageValue( PageRequestKeys.RESPONSE, inResponse);
			HttpSession session = inRequest.getSession(true);
			context.putProtectedPageValue( PageRequestKeys.SESSION, session);
			context.setRequest(inRequest);
			context.setResponse(inResponse);
			context.setSession(session);
	
			//Add item in session as page variables
			context.putPageValues(new SessionMap(session));
			try
			{
				OutputStream os = inResponse.getOutputStream();
				context.putProtectedPageValue(PageRequestKeys.OUTPUT_STREAM, os);
				// Also make a writer
				String encoding = inPage.getCharacterEncoding();
				if ( encoding != null)
				{
					//TODO: Benchmark using a new BufferedWriter(new OutputStreamWriter(os,encoding) ); I hate too many buffers
					Writer writer = new OutputStreamWriter(os,encoding);
					context.putProtectedPageValue(PageRequestKeys.OUTPUT_WRITER, writer);
				}
				else
				{
					Writer writer = new OutputStreamWriter(os ); //uses the server default LANG env variable
	 				context.putProtectedPageValue(PageRequestKeys.OUTPUT_WRITER, writer);
				}
			}
			catch (IOException ex)
			{
				log.error( ex );
				throw new OpenEditException(ex);
			}
		}
		else if( getHome() != null)
		{
			context.putProtectedPageValue(PageRequestKeys.HOME, getHome());
		}
		if( util != null)
		{
			// add the URLUtilities to the context
			context.putProtectedPageValue(PageRequestKeys.URL_UTILITIES, util);
			//context.putProtectedPageValue( PageRequestKeys.WEB_SERVER_PATH, util.buildRoot() );
			if( fieldHome == null)
			{
				setHome( util.relativeHomePrefix() );	
			}
			context.putProtectedPageValue(PageRequestKeys.HOME, getHome());
			
			if( inPage.isDynamic() ) //TODO: Move all this to an action
			{
				// urlpath is the address the link came in on
				String path = PathUtilities.extractDirectoryPath(inPage.getPath());
				context.putProtectedPageValue(PageRequestKeys.URL_PATH, util.relativeHomePrefix() + path);
	
				// filepath is the address a page within the template might live on
				context.putProtectedPageValue(PageRequestKeys.FILE_PATH, path);
	
				//TODO: Replace with Browser Action
				//add in browser info
				if(inRequest != null) {
					Browser browser = new Browser(inRequest.getHeader("User-Agent"));
					browser.setHttpServletRequest(inRequest);
					browser.setLocale( inRequest.getLocale() );
					context.putProtectedPageValue(PageRequestKeys.BROWSER, browser);
					}
				
			
		
				//Replaced with action PageValue.getLoader
	//			SessionTool sessionTool = new SessionTool( context, getModuleManager() );
	//			context.putProtectedPageValue(PageRequestKeys.CLASSTOOL, sessionTool );
			}			
		}	
		else
		{
			util = new URLUtilities(null,null);
			context.putProtectedPageValue(PageRequestKeys.URL_UTILITIES, util);
		}

		String timezonetext = inRequest.getHeader("X-TimeZone");
		if( timezonetext != null)
		{
			TimeZone timezone = TimeZone.getTimeZone(timezonetext);
			context.setTimeZone(timezone);
			context.putSessionValue("usertimezone", timezone);
		}
		
		return context;
	}
	
	public PageStreamer createPageStreamer( Page inPage, WebPageRequest inPageRequest ) throws OpenEditException
	{
		PageStreamer pageStreamer = new PageStreamer();
		pageStreamer.setEngine( getWebServer().getOpenEditEngine() ); 
		
		Output out = new Output();
		out.setWriter((Writer)inPageRequest.getPageValue(PageRequestKeys.OUTPUT_WRITER));
		out.setStream((OutputStream)inPageRequest.getPageValue(PageRequestKeys.OUTPUT_STREAM));
		
		pageStreamer.setOutput(out);
		pageStreamer.setWebPageRequest( inPageRequest);
		inPageRequest.putPageStreamer(pageStreamer );
		return pageStreamer;
	}


	
	/* 
	public String getRenderedPageContent(String inPath, WebPageRequest inReq, User inUser, URLUtilities util){
		try
		{
			Page page = getPageManager().getPage(inPath);
			WebPageRequest req;
			if(inReq == null){
				req =  createVirtualPageRequest(inPath, inUser, util);
			}
			else{
				req = createPageRequest(page, inReq.getRequest(), inReq.getResponse(), inUser, util);
				getModuleManager().executePageActions(page, req );
			    getModuleManager().executePathActions(page, req );
			}
			
			
			ByteArrayOutputStream scapture = new ByteArrayOutputStream();
			Writer capture = null;
			capture = new OutputStreamWriter(scapture, page.getCharacterEncoding());
			Output out = new Output(capture, scapture);


			page.generate(req, out);
			String output = scapture.toString();
			return output;
		}
		catch (Exception e)
		{
			throw new OpenEditRuntimeException(e);
		}
	}
	*/

	public WebPageRequest createPageRequest(Page inPage, User inUser)
	{
		return createPageRequest(inUser,(String[])null,inPage);
	}


	public Map extractValueMap(WebPageRequest inReq)
	{
		Map values = new HashMap();
		if( inReq.getSession() != null)
		{
			Enumeration enumerate = inReq.getSession().getAttributeNames();
			while( enumerate.hasMoreElements())
			{
				String key = (String)enumerate.nextElement();
				values.put(key, inReq.getSessionValue(key));
			}
		}
		values.putAll( inReq.getPageMap());
		return values;
	}


	
	
	

}