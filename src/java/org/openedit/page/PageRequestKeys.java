package org.openedit.page;

/**
 * @author Matthew Avery
 * 
 * Created on Feb 15, 2004
 *
 */
public interface PageRequestKeys
{
	/**
	 * The HttpServletRequest
	 * 
	 * Use "$request" in the Velocity page.
	 */
	public static final String REQUEST = "request";
	/**
	 * The HttpServletResponse
	 * 
	 * Use "$response" in the Velocity page.
	 */
	public static final String RESPONSE = "response";
	/**
	 * The HttpSession
	 * 
	 * Use "$session" in the Velocity page.
	 */
	public static final String SESSION = "session";
	/**
	 * The ServletContext
	 * 
	 * Use "$application" in the Velocity page.
	 */
	public static final String SERVLET_CONTEXT = "application";
	/** 
	 * The Servlet output stream
	 * 
	 * Use "$out" in the Velocity page.
	 */
	public static final String OUTPUT_STREAM = "out";
	/**
	 * A writer that wraps the servlet output stream
	 * 
	 * Use "$wout" in the Velocity page.
	 */
	public static final String OUTPUT_WRITER = "wout";
	/**
	 * The JPublish URLUtilities object which provides several static methods
	 * for manipulating paths.
	 * 
	 * Use "$url_util" in the Velocity page.
	 */
	public static final String URL_UTILITIES = "url_util";
	/**
	 * The servlet context path, e.g. if the root of an Open Edit site is at
	 * http://mydomain.org/oesite, the "home" variable will contain the String
	 * "oesite".
	 * 
	 * Use "$home" in the Velocity page.
	 */
	public static final String HOME = "home";
	
	/**
	 * This is the full address of the web server. i.e. http://www.abc.com:80
	 */
	public static final String WEB_SERVER_PATH = "webserverpath";
/**
	 * This is a relative chunk of the URL path used to help locate relative resources.
	 * If a resource is located at http://mydomain.org/oesite/foo/index.html, the
	 * urlpath variable will contain the String "oesite/foo" so that we can easily
	 * find, for instance, oesite/foo/navbar.html
	 * 
	 * Use "$urlpath" in the Velocity page.
	 */
	public static final String URL_PATH = "urlpath";
	/** 
	 * This is a relative chunk of the filepath used to help locate relative resources
	 * within a Velocity page. for a url of /content/about/about.html -> /content/about
	 * 
	 * Use "$filepath" in the Velocity page.
	 */
	public static final String FILE_PATH = "filepath";

	/**
	 * This is a Browser object, created from the request header that provides
	 * some convenience method for dealing with different browser types and OSes
	 * when different behavior is required.
	 * 
	 * Use "$browser" in the Velocity page.
	 */
	public static final String BROWSER = "browser";
	/**
	 * If a user is logged in to Open Edit, the User object will be available
	 * in Velocity.
	 * 
	 * Use "$user" in the Velocity page.
	 */
	public static final String USER = "user";
	/**
	 * A DynamicPage object representing the current page.  Page properties 
	 * can be accessed by the "get" method of this object.
	 * 
	 * Use "$page" in the Velocity page.
	 */
	public static final String PAGE = "page";
	
	/**
	 * An alias for the "page" variable
	 * 
	 * Use "$content" in the Velocity page.
	 */
	public static final String CONTENT = "content";
	/**
	 * This nifty class instantiation tool is something we found on the Velocity
	 * mailing list.  It is used for instantiating new classes within a Velocity
	 * page.
	 * 
	 * Use "$classtool" in the Velocity page.
	 */
	public static final String CLASSTOOL = "classtool"; 

	/**
	 * A PageStreamer object which allows dynamic pages to make a callback to the
	 * OpenEditEngine in order tor stream in content from other pages.
	 * 
	 * Use "$page" in the Velocity page.
	 */
	public static final String PAGES = "pages";
	
	/**
	 * Since not all path actions can be reliably secured we allow all actions to be turned off by
	 * default.
	 */
	public static final String ALLOWPATHREQUESTACTIONS = "allowpathrequestactions";
	
	
	/**
	 * Allows an action to specify an alternative layout. 
	 * NOLAYOUT is a valid value
	 */
	public static final String LAYOUTOVERRIDE = "layoutoverride";

	/**
	 * Allows an action to specify an alternative inner layout. This applies to all inner pages
	 * NOLAYOUT is a valid value
	 */
	public static final String INNERLAYOUTOVERRIDE = "innerlayoutoverride";
	public static final String PAGE_MANAGER = "pageManager";

}
