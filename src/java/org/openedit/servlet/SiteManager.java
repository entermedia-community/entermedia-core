package org.openedit.servlet;

import org.openedit.cache.CacheManager;
import org.openedit.data.SearcherManager;

public class SiteManager
{
	protected CacheManager fieldCacheManager;
	protected SearcherManager fieldSearcherManager;
	
	public SearcherManager getSearcherManager()
	{
		return fieldSearcherManager;
	}

	public void setSearcherManager(SearcherManager inSearcherManager)
	{
		fieldSearcherManager = inSearcherManager;
	}

	public CacheManager getCacheManager()
	{
		return fieldCacheManager;
	}

	public void setCacheManager(CacheManager inCacheManager)
	{
		fieldCacheManager = inCacheManager;
	}
	
	public String getDomain(String base) {
		// string off start
		String basestring = base.substring(base.lastIndexOf("//") + 1,
				base.length());
		int port = basestring.indexOf(":");
		if( port > -1)
		{
			basestring = basestring.substring(0,port);
		}
		
		int nextslash = basestring.indexOf("/");
		if( nextslash == -1)
		{
			basestring = basestring.substring(0,nextslash);
		}
		basestring = basestring.toLowerCase();
		return basestring;
	}
	
	public SiteData findSiteData(String inUrl)
	{
		String domain = getDomain(inUrl);
		SiteData found = (SiteData)getCacheManager().get("systemsitedata", domain);
		if( found == null)
		{
			found = (SiteData)getSearcherManager().query("system", "site").exact("domain", domain ).searchOne();
			getCacheManager().put("systemsitedata", domain, found);
		}
		return found;
	}
	
}
