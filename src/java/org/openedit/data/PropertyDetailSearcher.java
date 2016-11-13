package org.openedit.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.openedit.Data;
import org.openedit.OpenEditException;
import org.openedit.WebPageRequest;
import org.openedit.hittracker.HitTracker;
import org.openedit.hittracker.ListHitTracker;
import org.openedit.hittracker.SearchQuery;
import org.openedit.users.User;

public class PropertyDetailSearcher extends BaseSearcher
{

	
	public void reIndexAll() throws OpenEditException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public SearchQuery createSearchQuery()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HitTracker search(SearchQuery inQuery)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIndexId()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearIndex()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAll(User inUser)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(Data inData, User inUser)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveAllData(Collection<Data> inAll, User inUser)
	{
		// TODO Auto-generated method stub
		
	}
	
	
	
	@Override
	public HitTracker getAllHits(WebPageRequest inReq)
	{
	
		
		HitTracker hits = new ListHitTracker();
		List sorted = getPropertyDetailsArchive().listSearchTypes();
		for (Iterator iterator = sorted.iterator(); iterator.hasNext();)
		{
			String searchtype = (String) iterator.next();
			Searcher target = getSearcherManager().getSearcher(getCatalogId(), searchtype);
			for (Iterator iterator2 = target.getPropertyDetails().iterator(); iterator2.hasNext();)
			{
				PropertyDetail detail = (PropertyDetail) iterator2.next();
				//detail.setId(searchtype + ":" + detail.getId());
				hits.add(detail);
			}
		}
		return hits;
	
		
	}
	
	
	
	public HitTracker getAllHits()
	{
	
		return getAllHits(null);
	}
	
	public void reindexInternal() throws OpenEditException
	{
	//NOOP
	}
}
