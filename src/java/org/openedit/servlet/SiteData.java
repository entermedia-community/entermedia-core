package org.openedit.servlet;

import java.util.HashMap;
import java.util.Map;

import org.openedit.data.BaseData;

public class SiteData extends BaseData
{
	protected Map<String,String> fieldParameters = new HashMap();
	
	public Map<String,String> getSiteParameters()
	{
		return fieldParameters;
	}

	public void setSiteParameter(String inKey, String inValue)
	{
		getSiteParameters().put(inKey, inValue);
	}

	public String getSiteParameter(String inName)
	{
		if( inName.equals("siteid"))
		{
			//return getId();
		}
		return getSiteParameters().get(inName);
	}
	public String getRootPath()
	{
		return get("rootpath");
	}
	public String getDomainLink()
	{
		String domainpath = get("domainpath");
		if( domainpath != null)
		{
			return domainpath;
		}

		return "/";
	}
	
}
