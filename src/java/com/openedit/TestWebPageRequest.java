/*
 * Created on Jul 29, 2003
 */
package com.openedit;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cburkey
 */
public class TestWebPageRequest extends BaseWebPageRequest
{
	Map fieldFakeSession = new HashMap();
	Map fieldFakeProperties = new HashMap();
	protected String fieldMethod = "GET";
	
	public String getMethod() {
		return fieldMethod;
	}

	public void setMethod(String inMethod) {
		fieldMethod = inMethod;
	}

	/**
	 * @param inContext
	 */
	public TestWebPageRequest(WebPageRequest inContext)
	{
		super(inContext);
	}

	/**
	 * 
	 */
	public TestWebPageRequest()
	{
	}

	/* (non-javadoc)
	 * @see com.openedit.WebPageContext#getSessionValue(java.lang.String)
	 */
	public Object getSessionValue(String inInKey)
	{
		return fieldFakeSession.get(inInKey);
	}

	/* (non-javadoc)
	 * @see com.openedit.WebPageContext#putSessionValue(java.lang.String, java.lang.Object)
	 */
	public void putSessionValue(String inInKey, Object inInObject)
	{
		fieldFakeSession.put(inInKey, inInObject);
		putPageValue(inInKey, inInObject);
	}

	public String getRequestParameter(String inKey)
	{
		if (getLocalParameters().containsKey(inKey))
		{
			Object val = getLocalParameters().get(inKey);
			if( val instanceof String)
			{
				return (String)val;
			}
			String[] vals = (String[])val;
			if( vals.length > 0)
			{
				return vals[0];
			}
			return null;
		}
		else
		{
			return null;
		}

	}
	public String[] getRequestParameters( String inKey )
	{
		Object parameter = null;
		if (getLocalParameters().containsKey(inKey))
		{
			parameter = getLocalParameters().get(inKey);
		}
		
		if (parameter instanceof String[] || parameter == null)
		{
			return (String[]) parameter;
		}
		return new String[] {(String) parameter };
	}
	/* (non-javadoc)
	 * @see com.openedit.DefaultPageContext#removeSessionValue(java.lang.String)
	 */
	public void removeSessionValue(String inKey)
	{
		fieldFakeSession.remove(inKey);
	}
}
