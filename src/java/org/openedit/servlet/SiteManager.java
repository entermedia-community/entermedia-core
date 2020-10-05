package org.openedit.servlet;

import java.util.Collection;
import java.util.Iterator;

import org.openedit.MultiValued;
import org.openedit.cache.CacheManager;
import org.openedit.data.Searcher;
import org.openedit.data.SearcherManager;
import org.openedit.hittracker.HitTracker;
import org.openedit.util.URLUtilities;

public class SiteManager
{
	protected CacheManager fieldCacheManager;
	protected SearcherManager fieldSearcherManager;
	protected SiteData NULLSITE = new SiteData();
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
		String basestring = base.substring(base.lastIndexOf("//") + 2,
				base.length());
		int port = basestring.indexOf(":");
		if( port > -1)
		{
			basestring = basestring.substring(0,port);
		}
		
		int nextslash = basestring.indexOf("/");
		if( nextslash > -1)
		{
			basestring = basestring.substring(0,nextslash);
		}
		basestring = basestring.toLowerCase();
		return basestring;
	}
	
	public SiteData findSiteData(String inUrl)
	{
		String domain = getDomain(inUrl);
		if( domain == null)
		{
			return null;
		}
		SiteData found = (SiteData)getCacheManager().get("systemsitedata", domain);
		if( found == null)
		{
			Searcher searcher = getSearcherManager().getSearcher("system", "site");
			HitTracker hits = searcher.query().all().search();
			if(hits.isEmpty())
			{
				found = NULLSITE;
			}
			else
			{
				for (Iterator iterator = hits.iterator(); iterator.hasNext();)
				{
					MultiValued data = (MultiValued) iterator.next();
					Collection domains = data.getValues("domains");
					if( domains != null && !domains.isEmpty() )
					{
						for (Iterator iterator2 = domains.iterator(); iterator2.hasNext();)
						{
							String  tmpdomain = (String ) iterator2.next();
							if( domain.endsWith(tmpdomain))  //*.oe.com .endswith oe.com
							{
								found = (SiteData)searcher.loadData(data);
							}
						}
					}
				}
			}
//			else  Needs to be keyed on a domain
//			{
//				//This allows a domain to be associated with extra data
//				Collection hits = getSearcherManager().query("system", "siteparameters").exact("siteid", found.getId() ).search();
//				for (Iterator iterator = hits.iterator(); iterator.hasNext();)
//				{
//					Data data = (Data) iterator.next();
//					found.setSiteParameter(data.get("parametername"),data.get("parametervalue"));
//				}
//			}
			if( found == null)
			{
				found = NULLSITE;
			}
			getCacheManager().put("systemsitedata", domain, found);
		}
		if( found == NULLSITE)
		{
			return null;
		}
		return found;
	}
	
}
