/*
 * Created on Jan 25, 2006
 */
package com.openedit.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class DecoratedServletRequest extends HttpServletRequestWrapper
{
		protected String fieldRequestURI;
		protected String fieldPathInfo;
		public DecoratedServletRequest( HttpServletRequest inRequest)
		{
			super(inRequest);
		}
		public void setPathInfo(String inPath)
		{
			fieldPathInfo = inPath;
		}
		public void setrequestURI(String inUri)
		{
			fieldRequestURI = inUri;
		}
		public String getPathInfo() {
			if ( fieldPathInfo != null)
			{
				return fieldPathInfo;
			}
			return super.getPathInfo();
		}
		public String getRequestURI() {
			if( fieldRequestURI != null)
			{
				return fieldRequestURI;
			}
			return super.getRequestURI();
		}
}
