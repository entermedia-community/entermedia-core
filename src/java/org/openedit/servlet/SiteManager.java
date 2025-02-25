package org.openedit.servlet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
	protected Site NULLSITE = new Site();
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
	
	public Site findSiteData(URLUtilities inUrlUtil)
	{
		String domain = inUrlUtil.domain();
		if( domain == null)
		{
			return null;
		}
		Site found = (Site)getCacheManager().get("systemsitedata", domain);
		if( found == null)
		{
			found = new Site();
			
			found.setSiteRootDynamic( inUrlUtil.siteRoot() );
			
			Searcher searcher = getSearcherManager().getSearcher("system", "site");
			HitTracker hits = searcher.query().all().search();
			if(!hits.isEmpty())
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
								SiteData sitedata = (SiteData)searcher.loadData(data);
								found.setSiteData(sitedata);
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
			getCacheManager().put("systemsitedata", domain, found);
		}
		return found;
	}
	
}
