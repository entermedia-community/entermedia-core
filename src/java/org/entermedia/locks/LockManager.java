package org.entermedia.locks;

import java.util.Date;

import org.openedit.Data;
import org.openedit.data.Searcher;
import org.openedit.data.SearcherManager;
import org.openedit.util.DateStorageUtil;

import com.openedit.hittracker.HitTracker;
import com.openedit.hittracker.SearchQuery;

public class LockManager
{
	protected SearcherManager fieldSearcherManager;
	
	public SearcherManager getSearcherManager()
	{
		return fieldSearcherManager;
	}

	public void setSearcherManager(SearcherManager inSearcherManager)
	{
		fieldSearcherManager = inSearcherManager;
	}

	public Lock lock(String inCatId, String inPath, String inOwnerId)
	{
		Searcher searcher = getLockSearcher(inCatId);
		Data lockrequest = searcher.createNewData();
		lockrequest.setProperty("path",inPath);
		lockrequest.setProperty("owner",inOwnerId);
		lockrequest.setProperty("date", DateStorageUtil.getStorageUtil().formatForStorage(new Date()));
		searcher.saveData(lockrequest, null);

		SearchQuery q = searcher.createSearchQuery();
		q.addExact("path",inPath);
		q.addSortBy("date");
		
		HitTracker tracker = searcher.search(q);
		Data first = (Data)tracker.first();
		String ownerid = first.get("ownerid");
		if( ownerid.equals(inOwnerId))
		{
			Lock lock = new Lock();
			lock.setDate(new Date());
			lock.setOwnerId(inOwnerId);
			lock.setPath(inPath);
			return lock;
		}
		return null;
	}
	
	protected Searcher getLockSearcher(String inCatalogId)
	{
		getSearcherManager().getSearcher(inCatalogId, "lockSearcher");
		
		return null;
	}

	public Lock lockIfPossible(String inCatId, String inPath, String inOwnerId)
	{
		Lock lock = lock(inCatId, inPath, inOwnerId);
		if( lock != null && lock.isOwner(inOwnerId))
		{
			return lock;
		}
		return null;
	}
	
	public Lock release(String inCatId, String inPath, String inOwnerId)
	{
		Searcher searcher = getLockSearcher(inCatId);
		Data lockrequest = searcher.createNewData();
		lockrequest.setProperty("path",inPath);
		lockrequest.setProperty("owner",inOwnerId);
		lockrequest.setProperty("date", DateStorageUtil.getStorageUtil().formatForStorage(new Date()));
		searcher.saveData(lockrequest, null);

		return null;
		
	}

	
}
