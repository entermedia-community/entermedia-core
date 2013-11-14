package org.openedit.data;

import com.openedit.WebPageRequest;
import com.openedit.hittracker.HitTracker;
import com.openedit.hittracker.SearchQuery;

public class QueryBuilder
{
	protected Searcher fieldSearcher;
	protected SearchQuery fieldQuery;
	public Searcher getSearcher()
	{
		return fieldSearcher;
	}
	public void setSearcher(Searcher inSearcher)
	{
		fieldSearcher = inSearcher;
	}
	public SearchQuery getQuery()
	{
		if( fieldQuery == null)
		{
			fieldQuery = getSearcher().createSearchQuery();
		}
		return fieldQuery;
	}
	public void setQuery(SearchQuery inQuery)
	{
		fieldQuery = inQuery;
	}
	public QueryBuilder match(String inId, String inValue)
	{
		getQuery().addMatches(inId, inValue);
		return this;
	}
	public QueryBuilder not(String inId, String inValue)
	{
		getQuery().addNot(inId, inValue);
		return this;
	}
	public QueryBuilder sort(String inId, String inValue)
	{
		getQuery().addSortBy(inId);
		return this;
	}
	public HitTracker search(WebPageRequest inContext)
	{
		HitTracker tracker = getSearcher().cachedSearch(inContext, getQuery());
		return tracker;
	}
	public HitTracker search(WebPageRequest inContext, int inHitsPerPage)
	{
		HitTracker tracker = getSearcher().cachedSearch(inContext, getQuery());
		tracker.setHitsPerPage(inHitsPerPage);
		return tracker;
	}
	public HitTracker search()
	{
		HitTracker tracker = getSearcher().search(getQuery());
		return tracker;
	}
}
