/*
 * Created on Jul 28, 2003
 *
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.generators.VariablePackage;
import org.openedit.generators.VariableStore;
import org.openedit.page.Page;
import org.openedit.page.PageAction;
import org.openedit.page.PageRequestKeys;
import org.openedit.page.PageStreamer;
import org.openedit.profile.UserProfile;
import org.openedit.users.User;
import org.openedit.util.LocaleManager;
import org.openedit.util.PathUtilities;
import org.openedit.util.SessionMap;
import org.openedit.util.URLUtilities;
import org.openedit.web.Browser;

import groovy.json.JsonSlurper;

/**
 * @author Matt Avery, mavery@einnovation.com
 */
public class BaseWebPageRequest implements WebPageRequest, PageRequestKeys
{
	private static final Log log = LogFactory.getLog(BaseWebPageRequest.class);

	protected HttpServletRequest fieldHttpServletRequest;
	protected HttpServletResponse fieldHttpServletResponse;
	protected HttpSession fieldHttpSession;

	protected WebPageRequest fieldParent;
	protected Map fieldVariables;
	protected Set fieldProtectedFields;
	protected Map fieldParameters;
	protected Map fieldBackUpSession;
	protected LocaleManager fieldLocaleManager;
	protected String fieldLocale;
	protected boolean fieldHasRedirected;
	protected boolean fieldHasForwarded;
	protected boolean fieldHasCancelActions;
	protected boolean fieldEditable;
	
	protected UserProfile fieldUserProfile;
	
	public BaseWebPageRequest(WebPageRequest parent)
	{
		fieldParent = parent;
		setEditable(parent.isEditable());
		while( parent != null)
		{
			if (parent == this)
			{
				throw new OpenEditRuntimeException("can't set parent to self");
			}
			parent = parent.getParent();
		}
	}

	public BaseWebPageRequest()
	{
	}

	@Override
	public Map getJsonRequest()
	{	
		Map jsonRequest = (Map)getPageValue("_jsonRequest");
		
		if( jsonRequest == null && getRequest() != null)
		{
			JsonSlurper slurper = new JsonSlurper();
			try
			{
				Reader reader = getRequest().getReader();
				if(reader != null){
					jsonRequest = (Map)slurper.parse(reader); //this is real, the other way is just for t
					putPageValue("_jsonRequest", jsonRequest);
				}
			}
			catch ( Throwable ex)
			{
				log.error(ex);
				putPageValue("_jsonRequest", new HashMap());
				//throw new OpenEditException(ex);
			}
		}
		
		return jsonRequest;
	}
	
	@Override
	public void setJsonRequest(Map inMap)
	{
		if( getParent() != null)
		{
			getParent().putPageValue("_jsonRequest", inMap);
		}
		else
		{
			putPageValue("_jsonRequest", inMap);
		}
	}
	
	protected Set getProtectedFields()
	{
		if (fieldProtectedFields == null )
		{
			fieldProtectedFields = new HashSet();
		}
		return fieldProtectedFields;
	}

	public WebPageRequest getParent()
	{
		return fieldParent;
	}

	public HttpServletRequest getRequest()
	{
		if (fieldHttpServletRequest == null && getParent() != null)
		{
			return getParent().getRequest();
		}
		return fieldHttpServletRequest;
	}

	public HttpServletResponse getResponse()
	{
		if (fieldHttpServletResponse == null && getParent() != null)
		{
			return getParent().getResponse();
		}
		return fieldHttpServletResponse;
	}

	public HttpSession getSession()
	{
		if (fieldHttpSession == null && getParent() != null)
		{
			return getParent().getSession();
		}
		return fieldHttpSession;
	}

	public void forward(String inUrl) throws OpenEditException
	{
		//fieldHasRedirected = true;
		getPageStreamer().forward(inUrl, this);
	}
	public String getParam(String inKey)
	{
		String val = getRequestParameter(inKey);
		if( val != null)
		{
			val = URLUtilities.xmlEscape(val);
		}
		return val;
	}

	public String getReferringPage(){
		String referringPage  = getRequest().getHeader("referer");
		
		return referringPage;
	}
	
	public String getRequestParameter(String inKey)
	{
		String value = getLocalRequestParameter(inKey);
		if( value == null)
		{
			if( getRequest() != null )
			{
				value = getRequest().getParameter(inKey);
			}
			
			if( value == null && getVariables().containsKey("_jsonRequest"))
			{
				Object vals = getJsonRequest().get(inKey);
				if (vals instanceof Collection )
				{
					Collection array = (Collection)vals;
					if( array.size() > 0)
					{
						value = (String)array.iterator().next();
					}
				}
				else
				{
					value = (String)vals;
				}
			}
		}
		if ( value != null && value.length() == 0)
		{
			value = null; //null out blank strings
		}
		return value;
	}

	public String getLocalRequestParameter(String inKey)
	{
		String value = null;
		if (getLocalParameters().containsKey(inKey) )
		{
			Object  val = getLocalParameters().get(inKey);
			if( val != null)
			{
				if( val instanceof String)
				{
					value= (String)val;
				}
				else
				{
					String[] vals = (String[])val;
					if( vals != null && vals.length > 0)
					{
						value = vals[0];
					}
				}
				return value;
			}
		}
		if( value == null && getParent() != null)
		{
			return getParent().getLocalRequestParameter(inKey);
		}
		return value;
	}

	/* 
	 * @see org.openedit.WebAppContext#getParameterMap()
	 */
	public Map getParameterMap()
	{
		if (getRequest() != null)
		{
			Map combinedparams = null;
			String[] ordering = getRequest().getParameterValues("fieldorder");
			Enumeration enumeration = getRequest().getParameterNames();
			if( ordering == null)
			{
				combinedparams = new HashMap(); //unsorted				
			}
			else
			{
				combinedparams = ListOrderedMap.decorate(new HashMap());	
				//replace the enumartion with a sorted one
				for (int i = 0; i < ordering.length; i++)
				{
					String[] allv = getRequest().getParameterValues(ordering[i]);
					if( allv != null && allv.length == 1)
					{
						combinedparams.put(ordering[i], allv[0]);
					}
					else
					{
						combinedparams.put(ordering[i], allv);					
					}
					
				}
			}
			while (enumeration.hasMoreElements())
			{
				String key = (String) enumeration.nextElement();
				if( ordering != null && combinedparams.containsKey(key))
				{
					continue; //Skip if already in there
				}
				String[] allv = getRequest().getParameterValues(key);
				if( allv != null && allv.length == 1)
				{
					combinedparams.put(key, allv[0]);
				}
				else
				{
					combinedparams.put(key, allv);					
				}
			}
			if( ordering != null)
			{
				combinedparams.remove("fieldorder");
			}
			
			Map locals = getAllLocalParameters();
			combinedparams.putAll(locals);
			
			//get json stuff
			Map jsonRequest = (Map)getPageValue("_jsonRequest");
			if( jsonRequest != null)
			{
				combinedparams.putAll(jsonRequest); 
			}
			
			return combinedparams;
	
		}
		else
		{
			Map locals = getAllLocalParameters();
			return locals;
		}
		
	}
	
	/*
	 * This request is at the end.
	 */
	protected LinkedList getParentsAsList()
	{
		LinkedList parents = new LinkedList();
		BaseWebPageRequest parent = this;
		while( parent != null)
		{					
			parents.addFirst(parent);
			parent = (BaseWebPageRequest)parent.getParent();
		}
		return parents;
	}
	protected Map getLocalParameters()
	{
		if (fieldParameters == null)
		{
			fieldParameters = new HashMap();
		}
		return fieldParameters;
	}

	/*
	 * Children's params have precedence
	 */
	protected Map getAllLocalParameters()
	{
		Map params = new HashMap();
			
		for (Iterator iterator = getParentsAsList().iterator(); iterator.hasNext();) {
			BaseWebPageRequest req = (BaseWebPageRequest) iterator.next();
			if( req.fieldParameters != null)
			{
				params.putAll(req.fieldParameters);
			}	
		}
		return params;
	}
	
	/*
	 * Children have precedence
	 */
	protected Map getAllVariables()
	{
		Map vars = new HashMap();
	
		for (Iterator iterator = getParentsAsList().iterator(); iterator.hasNext();) 
		{
			BaseWebPageRequest req = (BaseWebPageRequest) iterator.next();
			if( req.fieldVariables != null)
			{
				vars.putAll(req.fieldVariables);
			}	
		}
		return vars;
	}

	/* 
	 * @see org.openedit.WebAppContext#getRequiredParameter(java.lang.String)
	 */
	public String getRequiredParameter(String inParameterName)  throws OpenEditException
	{
		String req = getRequestParameter(inParameterName);
		if (req == null)
		{
			throw new OpenEditException("Required parameter not found " + inParameterName);
		}
		return req;
	}

	/* 
	 * @see org.openedit.WebAppContext#getVariable(java.lang.String)
	 */
	public void put(String inKey, Object inValue) throws OpenEditException
	{
		putPageValue(inKey, inValue);
	}
	public String getSiteRoot()
	{
		String site = (String)getPageValue("siteRoot");
		if( site == null)
		{
			site = getContentProperty("siteRoot");
		}
		if( site == null && getRequest() != null)
		{
			StringBuffer ctx = getRequest().getRequestURL();
			site = ctx.substring( 0, ctx.indexOf("/", 8) ); //8 comes from https://
		}
		else if ( site == null)
		{
			site = getRequestParameter("siteRoot");
		}
		return site;
	}
	
	public String getSiteUrl()
	{
		URLUtilities util = (URLUtilities) get(URL_UTILITIES);
		String url = getSiteRoot() + util.getOriginalUrl();
		return url;
	}
	
	public Object get(String inKey)
	{
		return getPageValue(inKey);
	}
	public Object getPageValue(String inKey)
	{
		Object ret = getVariables().get(inKey);
		if (ret == null && getParent() != null)
		{
			return getParent().getPageValue(inKey);
		}
		return ret;
	}

	/* 
	 * @see org.openedit.WebAppContext#redirect(java.lang.String)
	 */
	public void redirect(String inUrl)
	{
		boolean alreadyRedirected = getPageValue("redirect") != null;
		String home = (String) getPageValue("home");
		if (alreadyRedirected)
		{
			log.debug("Previous redirect to " + getPageValue("redirect")
				+ " requested, cannot redirect to " + inUrl);
			return;
		}

		try
		{
			if (inUrl != null)
			{
				if (!inUrl.startsWith("http"))
				{
					if( inUrl.contains("./"))
					{
						inUrl = PathUtilities.resolveRelativePath(inUrl, getPath() );
					}
					inUrl = home + inUrl;
				}
				log.debug("Redirecting to: " + inUrl);
				putPageValue("redirect", inUrl);
				if (getResponse() != null)
				{
					getResponse().sendRedirect(inUrl);
				}
				else
				{
					log.error("No response set");
				}
				setHasRedirected(true);
				setCancelActions(true);
			}
		}
		catch (IOException e)
		{
			throw new OpenEditRuntimeException(e);
		}
	}
	/**
	 * This was added because a customer needed it for google indexes. 
	 * I wonder if we can not just use permenanet redirects all the time?
	 * @param inPath
	 */
	public void redirectPermanently( String inPath)
	{
		String home = (String) getPageValue("home");
		try
		{
			if (inPath != null)
			{
				log.debug("Perma redirect to: " + inPath);
				if (!inPath.startsWith("http"))
				{
					inPath = home + inPath;
				}
				putPageValue("redirect", inPath);
				if (getResponse() != null)
				{
					getResponse().setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY); 
					getResponse().setHeader("Location", inPath);
					getResponse().flushBuffer();
				}
				else
				{
					log.error("No response set");
				}
				setHasRedirected(true);
			}
		}
		catch (IOException e)
		{
			throw new OpenEditRuntimeException(e);
		}
		
	}
	protected Map getVariables()
	{
		if (fieldVariables == null)
		{
			fieldVariables = new HashMap();
			fieldVariables.put("context", this);
		}
		return fieldVariables;
	}

	/** 
	 * @see org.openedit.WebAppContext#setVariable(java.lang.String, java.lang.Object)
	 */
	public void putPageValue(String inKey, Object inObject)
	{
		if (getProtectedFields().contains(inKey) && getParent() != null)
		{
			throw new RuntimeException("Restricted variables can only be set at the root level");
		}
		if (inObject == null)
		{
			getVariables().remove(inKey);
		}
		else
		{
			getVariables().put(inKey, inObject);
		}
	}

	public void putProtectedPageValue(String inKey, Object inObject)
	{
		getProtectedFields().remove(inKey);
		putPageValue(inKey, inObject);
		getProtectedFields().add(inKey);
	}

	/* 
	 * @see org.openedit.WebAppContext#removeVariable(java.lang.String)
	 */
	public void removePageValue(String inKey)
	{
		getVariables().remove(inKey);
	}

	public void setRequest(HttpServletRequest inReq)
	{
		fieldHttpServletRequest = inReq;
	}

	public void setResponse(HttpServletResponse inRes)
	{
		fieldHttpServletResponse = inRes;
	}

	public void setSession(HttpSession inS)
	{
		fieldHttpSession = inS;
	}

	/* 
	 * @see org.openedit.WebPageContext#setRequestParameter(java.lang.String, java.lang.String)
	 */
	public void setRequestParameter(String inKey, String inValue)
	{
		if( inValue == null)
		{
			getLocalParameters().remove(inKey);
		}
		else
		{
			getLocalParameters().put(inKey, inValue);
		}
	}

	public void setRequestParameter(String inKey, String[] inValue)
	{
		getLocalParameters().put(inKey, inValue);
	}

	/* 
	 * @see org.openedit.WebPageContext#getVariableMap()
	 */
	public Map getPageMap()
	{
		Map combined = new HashMap();
		if (getParent() != null)
		{
			combined.putAll(getParent().getPageMap());
		}
		if( fieldVariables != null)
		{
			combined.putAll(getVariables());
		}
		return combined;
	}

	/* 
	 * @see org.openedit.WebPageContext#getRequestParameters(java.lang.String)
	 */
	public String[] getRequestParameters(String inKey)
	{
		Object parameter = null;
		if (getLocalParameters().containsKey(inKey))
		{
			parameter = getLocalParameters().get(inKey);
		}
		if( parameter == null && getParent() != null)
		{
			parameter =  getParent().getRequestParameters(inKey);
		}
		if ( parameter == null && getRequest() != null)
		{
			parameter = getRequest().getParameterValues(inKey);
			if(parameter == null)
			{
				parameter = getRequest().getParameterValues(inKey + "[]"); 			//jQuery.ajaxSettings.traditional = true;
			}
		}
		if( parameter == null && getVariables().containsKey("_jsonRequest"))
		{
			parameter = getJsonRequest().get(inKey);
			if (parameter instanceof Collection )
			{
				Collection col = (Collection)parameter;
				return (String[]) col.toArray(new String[col.size()]);
			}
		}

		if (parameter instanceof String[] || parameter == null)
		{
			return (String[]) parameter;
		}
		return new String[]{(String) parameter};
	}

	/* 
	 * @see org.openedit.WebPageContext#getStoredVariable(java.lang.String)
	 */
	public Object getSessionValue(String inKey)
	{
		if( inKey == null)
		{
			return null;
		}
		HttpSession session = getSession();
		if ( session == null)
		{
			Object found = getSessionValues().get(inKey);
			if( found == null && getParent() != null)
			{
				return getParent().getSessionValue(inKey);
			}
			return found;
		}
		return session.getAttribute(inKey);
	}

	/* 
	 * @see org.openedit.WebPageContext#setStoredVariable(java.lang.String, java.lang.Object)
	 */
	public void putSessionValue(String inKey, Object inObject)
	{
		if (getSession() == null)
		{
			if( inObject == null)
			{
				getSessionValues().remove(inKey);
			}
			else
			{
				getSessionValues().put(inKey, inObject);
			}
			return;
		}
		if( inObject == null)
		{
			getSession().removeAttribute(inKey);
		}
		else
		{
			getSession().setAttribute(inKey, inObject);
		}
		//All session values also go in page values
		putPageValue(inKey, inObject);
	}

	/* 
	 * @see org.openedit.WebPageContext#removeStoredVariable(java.lang.String)
	 */
	public void removeSessionValue(String inKey)
	{
		if (getSession() != null)
		{
			getSession().removeAttribute(inKey);
		}
		else
		{
			getSessionValues().remove(inKey);
		}
	}

	public String getPath()
	{
		if ( getPage() == null)
		{
			return null;
		}
		return getPage().getPath();
	}

	/**
	 * TODO: copy this to standard WebPageContext API
	 * @param inMap
	 */
	public void putSessionValues(SessionMap inMap)
	{
		for (Iterator iter = inMap.keySet().iterator(); iter.hasNext();)
		{
			String key = (String) iter.next();
			putSessionValue(key, inMap.get(key));
		}
	}

	/**
	 * TODO: copy this to standard WebPageContext API
	 * @param inMap
	 */
	public void putPageValues(Map inMap)
	{
		getVariables().putAll(inMap);
	}

	/* (non-javadoc)
	 * @see org.openedit.WebPageContext#getPathUrl()
	 */
	public String getPathUrl()
	{
		URLUtilities util = (URLUtilities) get(URL_UTILITIES);
		if(util == null){
			return null;
		}
		return util.requestPathWithArguments();
	}
	
	public String getPathUrlWithoutContext()
	{
		URLUtilities util = (URLUtilities) get(URL_UTILITIES);
		if(util == null){
			return null;
		}
		return util.requestPathWithArgumentsNoContext();
	}

	public OutputStream getOutputStream()
	{
		return getPageStreamer().getOutput().getStream();
	}

	public Writer getWriter()
	{
		return getPageStreamer().getOutput().getWriter();
	}

	public PageStreamer getPageStreamer()
	{
		return (PageStreamer) getPageValue(PAGES);
	}

	public void putPageStreamer(PageStreamer inStreamer)
	{
		putPageValue(PAGES, inStreamer);
	}

	public Page getPage()
	{
		Page content = (Page) getPageValue(PAGE);
		return content;
	}
	public void setPage(Page inPage)
	{
		putPageValue(PAGE,inPage);
	}

	public Page getContentPage()
	{
		Page content = (Page) getPageValue(CONTENT);
		return content;
	}

	/**
	 * This is used only if getSession() is null
	 * @return
	 */
	protected Map getSessionValues()
	{
		if (fieldBackUpSession == null)
		{
			fieldBackUpSession = new HashMap();
		}
		return fieldBackUpSession;
	}

	/* (non-javadoc)
	 * @see org.openedit.WebPageContext#hasRedirected()
	 */
	public boolean hasRedirected()
	{
		return fieldHasRedirected;
	}
	public void setHasRedirected( boolean inBol)
	{
		fieldHasRedirected = inBol;
	}
	
	/**
	 * @param inOutputStream
	 */
	public void setWriter(Writer inOutputStream)
	{
		putProtectedPageValue(OUTPUT_WRITER, inOutputStream);
	}
	
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
	public boolean isEditable() 
	{
//		Boolean canedit = (Boolean)getPageValue("canedit");
//		if( canedit != null)
//		{
//			return canedit.booleanValue();
//		}
		return fieldEditable;
	}
	public void setEditable( boolean inEditable)
	{
		fieldEditable = inEditable;
	}

	public String[] getRequestActions()
	{
		String[] actions = getRequestParameters( "oe-action" );
//		if ( actions == null)
//		{
//			actions = getRequestParameters( "wsp-action" );  //to support OE 3.0
//		}
		return actions;
	}

	/* (non-javadoc)
	 * @see org.openedit.WebPageRequest#copy()
	 */
	public WebPageRequest copy()
	{
		return new BaseWebPageRequest(this);
	}

	/* (non-javadoc)
	 * @see org.openedit.WebPageRequest#copy(org.openedit.page.Page)
	 */
	public WebPageRequest copy(Page inPage)
	{
		BaseWebPageRequest req = new BaseWebPageRequest(this);
		req.putProtectedPageValue(PageRequestKeys.PAGE,inPage);
		return req;
	}

	/* (non-javadoc)
	 * @see org.openedit.WebPageRequest#setUser(org.openedit.users.User)
	 */
	public void setUser(User inUser)
	{
		if( inUser == null)
		{
			getVariables().remove(USER); 
			if (getParent() != null)
			{
				getParent().setUser(null);
			}
		}
		else
		{
			putPageValue(USER,inUser);
		}
	}


	public User getUser()
	{
		User user = (User) getPageValue(USER);
//		if( user == null)
//		{
//			String catalogid = findValue("catalogid");
//			user = (User) getSessionValue(catalogid + "user");
//		}
		return user;
	}

	/**
	 * @param inPage
	 */
	public void setContentPage(Page inPage)
	{
		putPageValue(CONTENT,inPage);
	}

	/* (non-javadoc)
	 * @see org.openedit.WebPageRequest#setCurrentAction(org.openedit.page.PageAction)
	 */
	public void setCurrentAction(PageAction inAction)
	{
		putPageValue("exec-action",inAction);
	}

	/* (non-javadoc)
	 * @see org.openedit.WebPageRequest#getCurrentAction()
	 */
	public PageAction getCurrentAction()
	{
		return (PageAction)getPageValue("exec-action");
	}
	public String toString()
	{
		Object ret = getVariables().get("page");
		if ( ret != null)
		{
			return "page="+ ret.toString();
		}
		else if ( getParent() != null)
		{
			return "child of " + getParent().toString();
		}
		return "no parent";
	}

	public String getContentProperty(String inKey)
	{
		Page page = getContentPage();
		String locale = getLocale();
		String prop = page.getProperty(inKey, locale );
		return prop;
	}

	public String getPageProperty(String inKey)
	{
		Page page = getPage();
		String locale = getLocale();
		String prop = page.getProperty(inKey, locale );
		return prop;
	}

	public String getLocale()
	{
		String locale = (String)getPageValue("sessionlocale");
//		if( locale == null || locale.length() == 0)
//		{
//			User user = getUser();
//			if( user != null)
//			{
//				//locale = (String)user.get("locale");
//			}
//		}
		//TODO Cache this
		if( locale == null || locale.length() == 0)
		{
			if( fieldLocale != null)
			{
				return fieldLocale;
			}
			Browser browser = (Browser)getPageValue("browser");
			if( browser != null && browser.getLocale() != null)
			{
				locale = browser.getLocale().toString();
			}
			fieldLocale = locale;
		}
		if( locale == null )
		{
			locale = "en_US";
		}
		return locale;
	}

	public String getLanguage()
	{
		String language = getLocale();
		if( language != null)
		{
			int unds = language.indexOf('_');
			if( unds > -1)
			{
				language = language.substring(0,unds);
			}
		}
		return language;
	}

	public boolean hasForwarded()
	{
		return fieldHasForwarded;
	}

	public void setHasForwarded(boolean inB)
	{
		fieldHasForwarded = inB;
	}
	/**
	 * WARNING! does not return null
	 */
	public String getUserName()
	{
		User user = getUser();
		if( user != null)
		{
			return user.getUserName();
		}
		return "anonymous";
	}
	public String findValue(String inName)
	{
		String name = null;
		PageAction inAction = getCurrentAction();
		if( inAction != null && inAction.getConfig() != null)
		{
			name = inAction.getChildValue( inName );
			if( name == null)
			{
				name = inAction.getProperty(inName);				
			}
		}
		//TODO: Change the order. The content should go first?
		if( name == null)
		{
			name = getPage().get(inName);
		}
		if( name == null)
		{
			name = getContentPage().get(inName);
		}
		if( name == null)
		{
			//This should not be here. TODO: Create a new API called findReqValue() that gives this top priority
			name = getRequestParameter(inName);
		}
		name = getPage().getPageSettings().replaceProperty(name);
		return name;
	}

	public boolean hasCancelActions()
	{
		return fieldHasCancelActions;
	}

	public void setCancelActions(boolean inB)
	{
		fieldHasCancelActions = inB;
	}
	
	public String getRequestParamsAsList()
	{
		Map all = getParameterMap();
		
		StringBuffer list = new StringBuffer();
		
		for (Iterator iterator = all.keySet().iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			Object vals = all.get(name);
			if( vals instanceof String[])
			{
				String[] values = (String[])vals;
				for (int i = 0; i < values.length; i++) {
					list.append( name );
					list.append( "=" );
					list.append( values[i] );					
					if( i < values.length -1)
					{
						list.append( "&");
					}
				}
			}
			else
			{
				list.append( name );
				list.append( "=" );
				list.append( vals );
			}
			if (iterator.hasNext())
			{
				list.append( "&");
			}
		}
		return list.toString();
	}
	
	public String ajax()
	{
		String id = packageVariables();
		return "varpackageid: '" + id + "'";
	}
	public String ajaxid()
	{
		String id = packageVariables();
		return id;
	}
	public String rewriteParams()
	{
		String id = packageVariables();
		return "varpackageid=" + id + "";
	}
	
	
	public VariableStore getVariableStore()
	{
		VariableStore store = (VariableStore)getSessionValue("variablestore");
		if (store == null)
		{
			store = new VariableStore();
			putSessionValue("variablestore", store);
		}
		return store;
	}
	
	public String packageVariables()
	{
		VariablePackage varPackage = new VariablePackage();
		Map allVariables = getAllVariables();
		
		for (Iterator iterator = allVariables.keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			if (!getProtectedFields().contains(key)
					//we don't overwrite vars anyway. this might not be necessary
					&& !key.equals("pages")
					&& !key.equals("context"))
			{
				Object value = allVariables.get(key);
				varPackage.addVariable(key, value);
			}
			
		}
		
		String id = getVariableStore().addPackage(varPackage);
		return id;
	}
	
	public void unpackageVariables()
	{
		String id = getRequestParameter("varpackageid");
		if (id != null)
		{
			VariablePackage varPackage = getVariableStore().getPackage(id);
			if (varPackage != null)
			{
				Map current = getAllVariables();
				Map map = varPackage.getVariables();
				for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) 
				{
					String key = (String) iterator.next();
					
					if(!current.containsKey(key))
					{
						//log.info("putting page value" + val);
						Object val = map.get(key);
						putPageValue(key, val);
					}
					
				}
			}
		}
	}
	public String getDate(Date inDate)
	{
		String format = getUserProfileValue("shortdateformat");
		if( format != null)
		{
			String value = getLocaleManager().getDateStorageUtil().formatDateObj(inDate, format);
			return value;
		}
		return getLocaleManager().formatDateForDisplay( inDate, getLocale());
	}
	
	
	public String getHours(String inDate)
	{
		if( inDate == null || inDate.length() == 0)
		{
			return null;
		}
			
		Date date = getLocaleManager().getDateStorageUtil().parseFromStorage(inDate);
		if(date == null){
			return null;
		}
		
		return String.valueOf(date.getHours());
	}
	
	
	public String getMinutes(String inDate)
	{
		if(inDate == null){
			return null;
		}
		Date date = getLocaleManager().getDateStorageUtil().parseFromStorage(inDate);
		if(date == null){
			return null;
		}
		return String.valueOf(date.getMinutes());
	}
	
	
	
	public String getDate(String inStoredDate)
	{
		Date stored = getLocaleManager().getDateStorageUtil().parseFromStorage(inStoredDate);
		return getDate(stored);
	}
	public String getDateTime(Date inDate)
	{
		String format = getUserProfileValue("datetimeformat");
		if( format != null)
		{
			String value = getLocaleManager().getDateStorageUtil().formatDateObj(inDate, format);
			return value;
		}
		return getLocaleManager().formatDateTimeForDisplay( inDate, getLocale());
	}
	public String getDateTime(String inStoredDate)
	{
		Date stored = getLocaleManager().getDateStorageUtil().parseFromStorage(inStoredDate);
		return getDateTime(stored);
	}
	public String getText(String inKey)
	{
		String text = getPage().getText(inKey, getLocale());
		return text;
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

	public boolean isSecure()
	{
		if( getRequest() == null)
		{
			return false;
		}
		return getRequest().isSecure();
	}
	
	public UserProfile getUserProfile()
	{
		if (fieldUserProfile == null)
		{
			fieldUserProfile = (UserProfile)getPageValue("userprofile");
			if( fieldUserProfile == null && getParent() != null)
			{
				fieldUserProfile = getParent().getUserProfile();
			}
		}
		return fieldUserProfile;
	}

	public void setUserProfile(UserProfile inUserProfile)
	{
		fieldUserProfile = inUserProfile;
	}

	public String getUserProfileValue(String inKey)
	{
		if( getUserProfile() == null)
		{
			return null;
		}
		String val = getUserProfile().get(inKey);
		return val;
	}

	@Override
	public void putAllRequestParameters(Map inArgs)
	{
		getLocalParameters().putAll(inArgs);
		
	}

	
	
	public String getMethod(){
		if(getRequest() != null){
		return getRequest().getMethod();
		}
		
		return null;
	}
	
	
	public void setMethod(String inMethod){
		//NOOP
	}

}
