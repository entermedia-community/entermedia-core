package org.openedit.servlet;

import java.util.Map;

import org.openedit.data.BaseData;

public class SiteData extends BaseData
{
	protected Map fieldParameters;
	
	public Map getSiteParameters()
	{
		return fieldParameters;
	}

	public String findAppPath(String inRequestedPath)
	{
		if( inRequestedPath.startsWith("/manager"))
		{
			return inRequestedPath;
		}
		String apppath = get("rootpath");
		return apppath + inRequestedPath;
	}
}
