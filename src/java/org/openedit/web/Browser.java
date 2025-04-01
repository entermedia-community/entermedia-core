/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package org.openedit.web;

import java.util.Locale;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * This interface represents a particular type of browser.
 *
 * @author cburkey
 */
public class Browser
{
	public static final int UNKNOWN_BROWSER = 0;
	public static final int MSIE_BROWSER = 1;
	public static final int GECKO_BROWSER = 2;
	public static final int NETSCAPE_BROWSER = 3;
	public static final int CHROME_BROWSER = 4;
	public static final int WEBKIT_BROWSER = 5;
	public static final int OPERA_BROWSER = 7;
	public static final int TEXT_BROWSER = 8;
	protected int fieldBrowserType = UNKNOWN_BROWSER;

	protected int fieldWidth;
	public int getWidth()
	{
		return fieldWidth;
	}

	public void setWidth(int inWidth)
	{
		fieldWidth = inWidth;
	}

	public int getHeight()
	{
		return fieldHeight;
	}

	public void setHeight(int inHeight)
	{
		fieldHeight = inHeight;
	}

	protected int fieldHeight;
	protected boolean isNarrow;
	

	public boolean isNarrow()
	{
		return isNarrow;
	}

	public void setNarrow(boolean inIsNarrow)
	{
		isNarrow = inIsNarrow;
	}

	public static final int IE_EDIT_WIDTH = 83;		//	percentage width of edit iframe in IE
	public static final int MOZILLA_EDIT_WIDTH = 100;	//	percentage width of edit iframe in Mozilla
	protected int fieldMajorVersion = 0;
	protected String fieldMinorVersion = "";
	protected String fieldUserAgent;
	protected String fieldVersion = "";
	protected Locale fieldLocale;
	protected HttpServletRequest fieldHttpServletRequest;
	
	public Browser(String inUserAgent)
	{
		fieldUserAgent = inUserAgent;
		parseUserAgent();
	}

	/**
	 * Retrieve the browser type.
	 *
	 * @return One of the <code>BROWSER</code> constants
	 */
	public int getBrowserType()
	{
		return fieldBrowserType;
	}

	/**
	 * Determine if the browser is a Microsoft browser.
	 *
	 * @return
	 */
	public boolean isMSIE()
	{
		return (getBrowserType() == MSIE_BROWSER);
	}

	/**
	 * Retrieve the major version (if one could be found).
	 *
	 * @return
	 */
	public int getMajorVersion()
	{
		return fieldMajorVersion;
	}

	/**
	 * Retrieve the minor version (if one could be found).
	 *
	 * @return
	 */
	public String getMinorVersion()
	{
		return fieldMinorVersion;
	}

	/**
	 * Determine whether the browser is a Mozilla derivative (including Netscape 6 and 7).
	 *
	 * @return
	 */
	public boolean isGecko()
	{
		return (getBrowserType() == GECKO_BROWSER);
	}

	public boolean isHtml5VideoOnly()
	{
		return (getBrowserType() == WEBKIT_BROWSER);
	}

	public boolean isHtml5Browser()
	{
		if ( isMSIE() )
		{
			int major = getMajorVersion();
			if( major != 0 && major < 9)
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Retrieve the user agent string that we used to determine the information in this browser.
	 *
	 * @return
	 */
	public String getUserAgent()
	{
		return fieldUserAgent;
	}

	/**
	 * Retrieve the browser version.
	 *
	 * @return
	 */
	public String getVersion()
	{
		return fieldVersion;
	}

	/**
	 * FIXME: This is a temporary method needed because Velocity can't handle static fields (it
	 * would seem).  This should disappear when we fix bug WSP35.
	 *
	 * @return
	 */
	public boolean isXMLEditorCompatible()
	{
		return (getBrowserType() == GECKO_BROWSER) ||
		((getBrowserType() == MSIE_BROWSER) &&
		(getVersion().compareTo("5.5") >= 0));
	}

	protected void setBrowserType(int inBrowserType)
	{
		fieldBrowserType = inBrowserType;
	}

	protected void setMajorVersion(int inMajorVersion)
	{
		fieldMajorVersion = inMajorVersion;
	}
	protected void setMajorVersion(String inMajorVersion)
	{
		if( inMajorVersion != null && inMajorVersion.length() > 0 )
		{
			try
			{
				fieldMajorVersion  = Integer.parseInt(inMajorVersion);
			}
			catch( NumberFormatException ex )
			{
				//Fail?
			}
		}
	}

	protected void setMinorVersion(String inMinorVersion)
	{
		fieldMinorVersion = inMinorVersion;
	}


	protected void setVersion(String inVersion)
	{
		fieldVersion = inVersion;
	}

	public boolean inApp()
	{
		//
		if( fieldHttpServletRequest == null)
		{
			return false;
		}
		HttpSession session = getHttpServletRequest().getSession(true);
		String inApp = getHttpServletRequest().getParameter("eminapp");

		if( inApp == null)
		{
			String version  = getHttpServletRequest().getHeader("X-emappversion");
			if( version != null)
			{
				inApp = "true";
			}
		}
		if( inApp != null)
		{
			session.setAttribute("eminapp", inApp);			
		}
		else
		{
			inApp = (String)session.getAttribute("eminapp");
		}
		return Boolean.parseBoolean(inApp);
	}
	
	protected void parseUserAgent()
	{
		//Mozilla/5.0 (Windows; U; MSIE 7.0; Windows NT 6.0; en-US)
		//Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.2.3) Gecko/20100423 Ubuntu/10.04 (lucid) Firefox/3.6.3
		//Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B334b Safari/531.21.10
		if (getUserAgent() == null)
		{
			setBrowserType(UNKNOWN_BROWSER);

			return;
		}
		
		String[] details = getUserAgent().split(" ");
		int found = UNKNOWN_BROWSER;
		//start at the end and work upwards
		for (int j = details.length - 1; j >= 0; j--)
		{
			String browser = details[j].trim();
			found = findBrowser(browser);
			if( found != UNKNOWN_BROWSER)
			{
				//get the version numbers and break out
				setBrowserType(found);
				if( !browser.contains("/") && j < details.length -1)
				{
					browser = browser + "/" + details[j+1]; 
				}
				setVersions(browser);
				continue;
			}
		}
		
		boolean isMobile = Pattern.compile("Android|iPhone|iPad|iPod", Pattern.CASE_INSENSITIVE)
				.matcher(getUserAgent())
				.find();
		
		if(isMobile)
		{
			setNarrow(true);
		}
		
	}

	protected void setVersions(String inBrowser)
	{
		// # Try to get version info out of leftover stuff
		int split = inBrowser.indexOf("/");
		if( split > -1 )
		{
			String version = inBrowser.substring(split + 1);
			version = extractNumber(version);
			setVersion(version);
			int j = getVersion().indexOf(".");

			if (j >= 0)
			{
				setMajorVersion(getVersion().substring(0, j));

				int k = getVersion().indexOf(".", j + 1);

				if (k < 0)
				{
					k = getVersion().length();
				}

				setMinorVersion(getVersion().substring(j + 1, k));
			}
			else
			{
				setMajorVersion(getVersion());
				setMinorVersion("0");
			}	
		}
		else
		{
			//look for any version numbers?
		}
	}

	protected String extractNumber(String inVersion)
	{
		StringBuffer out = new StringBuffer(inVersion.length());
		boolean founddot = false;
		for (int i = 0; i < inVersion.length(); i++)
		{
			char c = inVersion.charAt(i);
			if( Character.isDigit(c) )
			{
					out.append(c);
			}
			else if( founddot == false && c == '.' )
			{
				founddot = true; //one dot only please
				out.append(c);
			}

		}
		String result = out.toString().toLowerCase();
		return result;
	}

	private int findBrowser(String browVer)
	{
		//from more specific to more generic
		if( browVer.startsWith("("))
		{
			browVer = browVer.substring(1);
		}
		if (browVer.startsWith("Firefox"))
		{
			return GECKO_BROWSER;
		}
		if (browVer.startsWith("Safari") || browVer.contains("AppleWebKit") )
		{
			return WEBKIT_BROWSER;		
		}
		else if (browVer.startsWith("Gecko"))
		{
			return GECKO_BROWSER;
		}
		else if (browVer.startsWith("Lynx"))
		{
			return TEXT_BROWSER;
		}
		else if (browVer.startsWith("MSIE"))
		{
			return MSIE_BROWSER;
		}
		else if (browVer.startsWith("Opera"))
		{
			return OPERA_BROWSER;
		}
		else if (browVer.startsWith("Mozilla"))
		{
			return NETSCAPE_BROWSER;
		}
		// Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5355d Safari/8536.25

		return UNKNOWN_BROWSER;
	}

	public int getEditWidth()
	{
		if ( getBrowserType() == MSIE_BROWSER )
		{
			return IE_EDIT_WIDTH;
		}
		else
		{
			return MOZILLA_EDIT_WIDTH;
		}
	}

	public Locale getLocale()
	{
		return fieldLocale;
	}

	public void setLocale(Locale inLocale)
	{
		fieldLocale = inLocale;
	}

	public String getCookieListing()
	{
		if( fieldHttpServletRequest == null)
		{
			return null;
		}
		StringBuffer all = new StringBuffer();
		Cookie[] cookies = getHttpServletRequest().getCookies();
		for (int i = 0; i < cookies.length; i++)
		{
			all.append( cookies[i].getName());
			all.append('=');
			all.append(cookies[i].getValue());
			all.append(';');
			
		}
		return all.toString();
	}

	public HttpServletRequest getHttpServletRequest()
	{
		return fieldHttpServletRequest;
	}

	public void setHttpServletRequest(HttpServletRequest inHttpServletRequest)
	{
		fieldHttpServletRequest = inHttpServletRequest;
	}
}
