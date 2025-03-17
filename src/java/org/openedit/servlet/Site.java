package org.openedit.servlet;

import java.util.Collection;

public class Site
{
	protected SiteData fieldSiteData;

	protected String fieldSiteRootDynamic;
	
	
	public String getSiteRootDynamic()
	{
		return fieldSiteRootDynamic;
	}

	public void setSiteRootDynamic(String inSiteRootCurrent)
	{
		fieldSiteRootDynamic = inSiteRootCurrent;
	}

	public SiteData getSiteData()
	{
		return fieldSiteData;
	}

	public void setSiteData(SiteData inSiteData)
	{
		fieldSiteData = inSiteData;
	}

	
	public String getFirstDomain()
	{
		Collection values = getSiteData().getValues("domains");
		if(values == null || values.isEmpty())
		{
			return null;
		}
		String first = (String)values.iterator().next();
		return first;
	}
	public String fixRealPath(String inRequestedPath)
	{
		if(getSiteData() == null) {
			return inRequestedPath;

		}
		String apppath = getSiteData().get("rootpath");
		if( inRequestedPath.startsWith(apppath + "/"))
		{
			return inRequestedPath;
		}
		//This just adds back the missing /site/..
		return apppath + inRequestedPath;
	}

	public String getSiteLink(String inApplicationid)
	{
		if( getSiteData() == null)
		{
			return inApplicationid;
		}
		String apppath = getSiteData().get("rootpath");
		String id = apppath.substring(1);
		if( !inApplicationid.startsWith(id))
		{
			return "/" + inApplicationid;
		}
		
		String dir = inApplicationid.substring(apppath.length() -1, inApplicationid.length());
		return dir;
	}



	
}
