package org.openedit.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Set;

import org.openedit.Data;
import org.openedit.OpenEditException;
import org.openedit.WebPageRequest;
import org.openedit.hittracker.HitTracker;
import org.openedit.hittracker.SearchQuery;
import org.openedit.hittracker.Term;
import org.openedit.profile.UserProfile;
import org.openedit.users.Group;

public class QueryBuilder
{
	protected Searcher fieldSearcher;
	protected SearchQuery fieldQuery;
	protected boolean fieldIgnoreBlank = false;
	
	public boolean isIgnoreBlank()
	{
		return fieldIgnoreBlank;
	}
	public void setIgnoreBlank(boolean inIgnoreBlank)
	{
		fieldIgnoreBlank = inIgnoreBlank;
	}
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
	
	public QueryBuilder andExact(String inId, Collection inValues)
	{
		String[] vals = (String[])inValues.toArray(new String[inValues.size()]);
		getQuery().addAndGroup(inId,vals);
		return this;
	}
	
	
	public QueryBuilder contains(String inId, String inValue)
	{
		getQuery().addContains(inId, inValue);
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
	
	public QueryBuilder before(String inId, int date)
	{
		GregorianCalendar cal = new GregorianCalendar();
		Date now = new Date();
		cal.setTime(now);
		if( date > 0)
		{
			date = 0 - date;
		}
		cal.add(GregorianCalendar.DAY_OF_MONTH, date);
		getQuery().addBefore(inId, cal.getTime());
		
		return this;
	}
	
	public QueryBuilder after(String inKey, Date inValue){
		getQuery().addAfter(inKey, inValue);
		return this;
	}
	public QueryBuilder between(String inKey, Date start, Date end){
		getQuery().addBetween(inKey, start, end);
		return this;
	}
	
	public QueryBuilder hitsPerPage(int inHitsPerPage)
	{
		getQuery().setHitsPerPage(inHitsPerPage);
		return this;
	}
	
	/**
	 * Pass in strings ids or Data objects
	 * @param inKey
	 * @param objects
	 * @return
	 */
	public QueryBuilder orgroup(String inKey, Collection inDataCollection)
	{
		String[] ids = extractIds(inDataCollection);
		if( ids == null)
		{
			return this;
		}
		getQuery().addOrsGroup(inKey, Arrays.asList( ids ) );
		return this;
	}
	
	private String[] extractIds(Collection inDataCollection)
	{
		return getQuery().extractIds(inDataCollection);
	}
	public QueryBuilder andgroup(String inKey, Collection inDataCollection)
	{
		String[] ids = extractIds(inDataCollection);
		if( ids == null)
		{
			return this;
		}
		getQuery().addAndGroup(inKey, ids);
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
		getQuery().setHitsPerPage(inHitsPerPage);
		HitTracker tracker = getSearcher().cachedSearch(inContext, getQuery());
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
	public Data searchOne(WebPageRequest inContext)
	{
		//getQuery().toFriendly();
		getQuery().setHitsPerPage(1);
		HitTracker tracker = getSearcher().cachedSearch(inContext, getQuery());
		if(tracker == null) {
			return null;
		}
		Data found = (Data) tracker.first();
		return found;
	}
	public QueryBuilder exact(String inKey, boolean inValue) 
	{
		return exact(inKey,String.valueOf(inValue));
	}
	public QueryBuilder exact(String inKey, String inValue) {
		if( inValue == null)
		{
			if(!isIgnoreBlank()) {
				throw new OpenEditException("Value is empty for " + inKey);
			} 
			else {return this;}
		}
		getQuery().addExact(inKey, inValue);
		return this;
	}
	public QueryBuilder exact(String inKey, Data inData) 
	{
		if( inData != null)
		{
			return exact(inKey,inData.getId());
		}
		else if(!isIgnoreBlank()) 
		{
				throw new OpenEditException("Value is empty for " + inKey);
		}
		return this;
	}
	
	
	public QueryBuilder freeform(String inKey, String inValue) {
		if( inValue == null)
		{
			throw new OpenEditException("Value is empty for " + inKey);
		}
		getQuery().addFreeFormQuery(inKey, inValue);
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
	
	
	
	public QueryBuilder missing(String inKey) {
		getQuery().addMissing(inKey);
		return this;
	}
	
	public QueryBuilder exists(String inKey) {
		getQuery().addExists(inKey);
		return this;
	}
	
	
	public QueryBuilder all() 
	{
		getQuery().addMatches("id", "*");
		return this;
	}
	
	public QueryBuilder on(String inKey, Date inDate)
	{
		getQuery().addOn(inKey, inDate);
		return this;
	}
	
	public QueryBuilder before(String inKey, Date inValue){
		getQuery().addBefore(inKey, inValue);
		return this;
	}
	

	public QueryBuilder enduser(boolean inB)
	{
		getQuery().setEndUserSearch(inB);
		return this;
	}
	public QueryBuilder id(String inId)
	{
		getQuery().addExact("id",inId);
		return this;
	}
	public QueryBuilder addFacet(String inString)
	{
		getQuery().addAggregation(inString);
		return this;
	}
	
	public QueryBuilder facet(String inString)
	{
		return addFacet(inString);
	}
	
	
	
	public QueryBuilder facets(Collection<PropertyDetail> inFacets)
	{
		getQuery().setFacets(inFacets);
		return this;
	}

	
	public QueryBuilder notgroup(String inField, Collection inIds)
	{
		if( inIds ==  null || inIds.isEmpty())
		{
			return this;
		}

		getQuery().addNots(inField, inIds);
		return this;
	}
	
	public QueryBuilder permissions(UserProfile inProfile)
	{
		Collection groupids = new ArrayList();
		if( inProfile == null || inProfile.getUser() == null)
		{
			groupids.add("anonymous");
		}
		else
		{
			for (Iterator iterator = inProfile.getUser().getGroups().iterator(); iterator.hasNext();)
			{
				Group group = (Group) iterator.next();
				groupids.add(group.getId());
			}
		}
		String roleid = null;
		if( inProfile.getSettingsGroup() != null)
		{
			roleid = inProfile.getSettingsGroup().getId();
		}
		else
		{
			roleid = "anonymous";
		}
			orgroup("viewgroups", groupids).
			match("viewroles", roleid).
			match("viewusers", inProfile.getUserId());
		return this;
		
	}
	public QueryBuilder ids(Collection<String> inAssetIds)
	{
		return orgroup("id",inAssetIds);
	}
	public QueryBuilder ignoreEmpty()
	{
		setIgnoreBlank(true);
		return  this;
	}
	
	
	public QueryBuilder attachSecurity(WebPageRequest inReq) {
		if(inReq != null) {
			getQuery().setEndUserSearch(true);

			getSearcher().getSearchSecurity().attachSecurity(inReq, getSearcher(), getQuery());
		}
		return this;

	}

	@Override
	public String toString()
	{
		return getQuery().toQuery();
	}
	public QueryBuilder includeDescription()
	{
		getQuery().setIncludeDescription(true);
		return this;
		
	}
	public QueryBuilder terms(WebPageRequest inReq)
	{
		getSearcher().addStandardSearchTerms(inReq);
		return this;
	}
}
