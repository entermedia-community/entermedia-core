/*
 * Created on Jul 28, 2003
 *
/*

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

import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openedit.page.Page;
import org.openedit.page.PageAction;
import org.openedit.page.PageStreamer;
import org.openedit.profile.UserProfile;
import org.openedit.users.User;

/**
 * @author Matt Avery, mavery@einnovation.com
 */
public interface WebPageRequest
{
	HttpServletRequest getRequest();
	
	HttpServletResponse getResponse();
	
	Map getJsonRequest();
	Map configureFields();

	
	void setJsonRequest(Map inReq);
	
	HttpSession getSession();
	
	/**
	 * The redirect will send the browser to a new page
	 * This looses any parameters to the original page
	 * @param inInUrl can begin with http or be a path /index.html you do not need $home in there
	 */
	void redirect( String inUrl );
	
	/**
	 * This is used to tell search engines to use the new links
	 * @param inPath
	 */
	public void redirectPermanently( String inPath);

	/**
	 * This will simply render a new page instead of the page the user 
	 * was expecting
	 * 
	 * @param inInUrl should be a path such as /index.html
	 */
	void forward( String inUrl )throws OpenEditException;
	
	/**
	 * Convenience methods for managing Velocity context objects.
	 */
	Object getPageValue( String inKey );
	
	/**
	 * Looks in the action, page and parameter for a value
	 * @param inName
	 *  
	 * @return
	 */
	String findValue(String inName);
	String findPathValue(String inName);
	public String findActionValue(String inName);
	
	String getPageProperty(String inKey );

	String getContentProperty(String inKey );

	void putPageValue( String inKey, Object inObject );
    
    /**
     * <p>
     * Add a protected page value to this page request.  Protected values are 
     * reserved for use by OpenEdit itself and should not be overriden in 
     * general practice.
     * </p>
     * @param inKey
     * @param inObject
     */
    void putProtectedPageValue(String inKey, Object inObject);
	
	void removePageValue( String inKey );
	
	Map getPageMap();
	
	/**
	 * Convenience methods for managing session objects.
	 */
	Object getSessionValue( String inKey );
	
	void putSessionValue( String inKey, Object inObject );
	
	void removeSessionValue( String inKey );
		
	String getRequestParameter( String inKey );

	/**
	 * The returned values are escaped and suitable to show in a web browser
	 * @param inKey
	 * @return
	 */
	public String getParam(String inKey);

	String[] getRequestParameters( String inKey );
		
	void setRequestParameter( String inKey, String inValue );
	
	void setRequestParameter( String inKey, String[] inValue );

	/**
	 * 
	 * @return a mutable map of the request parameters
	 */
	Map getParameterMap();
	
	// This may not be necessary
	String getRequiredParameter( String inParameterName ) throws OpenEditException;

	User getUser();

	/**
	 * @param inObject
	 */
	void setUser(User inUser);
	public void setUserProfile(UserProfile inUserProfile);
	/**
	 * This is the path to the content
	 */
	String getPath();
	
	/**
	 * This is the page that this context refers to in the getPath() method
	 * @return
	 */	
	Page getPage();
	/**
	 * Is the full path to the page you are on. This includes any arguments or parameters
	 * @return fullpath
	 */
	String getPathUrl();
    String getPathUrlWithoutContext();
	OutputStream getOutputStream();
	
	Writer getWriter();
	
	void setWriter(Writer inW);
	
	PageStreamer getPageStreamer();
	
	void putPageStreamer( PageStreamer inStreamer );

	/**
	 * @return
	 */
	boolean hasRedirected();
	boolean hasForwarded();
	void setHasRedirected(boolean inB);
	void setHasForwarded(boolean inB);

	boolean hasCancelActions();
	void setCancelActions(boolean inB);
	/**
	 * @return
	 */
	Page getContentPage();
	
	/**
	 * Determine whether this page can be edited by the given user.  The page is editable if:
	 * 
	 * <ul>
	 * <li>
	 * the page's repository is not read-only;
	 * </li>
	 * <li>
	 * the "editable" property is not present or equal to "true"; and
	 * </li>
	 * <li>
	 * the edit filter is not present or passes the given user.
	 * </li>
	 * </ul>
	 * 
	 *
	 * @param inUser The user to query
	 * @param inContext DOCME
	 *
	 * @return boolean  <code>true</code> if the page is editable by the user, <code>false</code>
	 * 		   if not
	 *
	 * @throws OpenEditException DOCME
	 */
	public boolean isEditable();

	public void setEditable(boolean inEdi);
	
	public String[] getRequestActions();

	/**
	 * @return
	 */
	WebPageRequest copy();

	/**
	 * @param inPage
	 * @return
	 */
	WebPageRequest copy(Page inPage);

	/**
	 * @param inPage
	 */
	void setPage(Page inPage);

	WebPageRequest getParent();

	/**
	 * TODO: Make this a list of actions that have been run
	 * @param inAction
	 */
	void setCurrentAction(PageAction inAction);

	PageAction getCurrentAction();

	String getLocale();

	String getLanguage();

	String getUserName();

	String getLocalRequestParameter(String inKey);
	
	public String getReferringPage();
	
	public String getRequestParamsAsList();

//	public void unpackageVariables();
	
	public String getText(String inKey);
	
	public String getSiteRoot();
	
	public String getUserProfileValue(String inKey);
	
	public UserProfile getUserProfile();

	void putAllRequestParameters(Map<String,Object> inArgs);
	
	public String getMethod();
	public void setMethod(String inMethod);
	
	
	public String getSiteUrl();
	
	public Data getData();

	boolean hasPermission(String inString);

	void closeStreams();

	void addRequestParameter(String inString, String inName);

	void putPageValues(Map inPageValues);

	public boolean getRequestParameterBoolean(String inKey, boolean inDefault);

}
