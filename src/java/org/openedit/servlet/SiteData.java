package org.openedit.servlet;

import java.util.Collection;
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
	public String getFirstDomain()
	{
		Collection values = getValues("domains");
		if(values == null || values.isEmpty())
		{
			return null;
		}
		String first = (String)values.iterator().next();
		return first;
	}
	public String fixRealPath(String inRequestedPath)
	{
		if( inRequestedPath.startsWith("/manager") || inRequestedPath.startsWith("/system") || inRequestedPath.startsWith("/openedit")  )
		{
			return inRequestedPath;
		}
		
		String apppath = get("rootpath");
		if( inRequestedPath.startsWith(apppath))
		{
			return inRequestedPath;
		}
		//This just adds back the missing /site/..
		return apppath + inRequestedPath;
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
	public String getSiteLink(String inApplicationid)
	{
		String apppath = get("rootpath");
		String id = apppath.substring(1);
		if( !inApplicationid.startsWith(id))
		{
			return "/" + inApplicationid;
		}
		
		String dir = inApplicationid.substring(apppath.length() -1, inApplicationid.length());
		return dir;
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
