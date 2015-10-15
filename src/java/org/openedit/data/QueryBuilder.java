package org.openedit.data;

import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import org.openedit.Data;

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
	public QueryBuilder since(String inId, int date)
	{
		GregorianCalendar cal = new GregorianCalendar();
		Date now = new Date();
		cal.setTime(now);
		if( date > 0)
		{
			date = 0 - date;
		}
		cal.add(GregorianCalendar.DAY_OF_MONTH, date);
		getQuery().addBetween(inId, cal.getTime(),now);
		return this;
	}
	public QueryBuilder orgroup(String inKey, Collection<String> inIds)
	{
		getQuery().addOrsGroup(inKey, inIds);
		return this;
	}
	public QueryBuilder orgroup(String inKey, String inOrs)
	{
		getQuery().addOrsGroup(inKey, inOrs);
		return this;
	}
	public QueryBuilder not(String inId, String inValue)
	{
		getQuery().addNot(inId, inValue);
		return this;
	}
	public QueryBuilder sort(String inId)
	{
		getQuery().addSortBy(inId);
		return this;
	}
	public QueryBuilder named(String inId)
	{
		getQuery().setHitsName(inId);
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
	public Data searchOne()
	{
		//getQuery().toFriendly();
		Data found = getSearcher().searchByQuery(getQuery());
		return found;
	}
	public QueryBuilder exact(String inKey, String inValue) {
		getQuery().addExact(inKey, inValue);
		return this;
	}
	
	public QueryBuilder startsWith(String inKey, String inValue) {
		getQuery().addStartsWith(inKey, inValue);
		return this;
	}
	
	public QueryBuilder or() {
		getQuery().setAndTogether(false);
		return this;
	}
	
	public QueryBuilder and() {
		getQuery().setAndTogether(true);
		return this;
	}
	
	public QueryBuilder all() 
	{
		getQuery().addMatches("id", "*");
		return this;
	}
}
