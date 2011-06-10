package com.openedit.hittracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.openedit.Data;

import com.openedit.WebPageRequest;

public class CompositeHitTracker extends HitTracker
{
	protected Map fieldHitTrackers;
	protected HitTracker fieldValues;
	
	public HitTracker getSubTracker(String inKey)
	{
		return (HitTracker) getHitTrackers().get(inKey);
	}
	
	public void addSubTracker(String inKey, HitTracker inTracker)
	{
		getHitTrackers().put(inKey, inTracker);
		fieldValues = null;
	}

	public List getPageOfHits()
	{
		List result = new ArrayList();
		for (Iterator iterator = getHitTrackers().values().iterator(); iterator.hasNext();)
		{
			HitTracker tracker = (HitTracker) iterator.next();
			result.addAll(tracker.getPageOfHits());
		}
		return result;
	}

	protected HitTracker getValues()
	{
		if( fieldValues == null)
		{	
			fieldValues = new ListHitTracker();
			for (Iterator iterator = getHitTrackers().values().iterator(); iterator.hasNext();)
			{
				HitTracker tracker = (HitTracker) iterator.next();
				fieldValues.addAll(tracker);
			}
		}
		return fieldValues;
	}
	
	public Map getHitTrackers()
	{
		if (fieldHitTrackers == null)
		{
			fieldHitTrackers = ListOrderedMap.decorate(new HashMap());
		}

		return fieldHitTrackers;
	}

	public boolean contains(Object inHit)
	{
		return getValues().contains(inHit);
	}

	private HitTracker getFirstTracker()
	{
		if(getHitTrackers().size() > 0)
		{
			HitTracker tracker = (HitTracker)getHitTrackers().values().iterator().next();
			return tracker;
		}
		return null;
	}
	public Data get(int inCount)
	{
		if(inCount == 0)
		{
			//Avoid calling getValues for performance reasons
			HitTracker tracker = getFirstTracker();
			if(tracker!= null && tracker.size()>0)
			{
				return tracker.get(0);
			}
		}
		return (Data)getValues().get(inCount);
	}

	public Iterator iterator()
	{
		return getValues().iterator();
	}

	public int size()
	{
		int count = 0;
		for (Iterator iterator = getHitTrackers().values().iterator(); iterator.hasNext();)
		{
			HitTracker tracker = (HitTracker) iterator.next();
			count += tracker.size(); 
		}
		return count;
	}

	public String getValue(Object inHit, String inKey)
	{
		
		for (Iterator iterator = getHitTrackers().values().iterator(); iterator.hasNext();)
		{
			HitTracker tracker = (HitTracker) iterator.next();
			String value = tracker.getValue(inHit, inKey);
			if(value!=null)
				return value;
			
		}
		return null;
	}
	
	public void addToSubTracker(String inKey, Object inHit)
	{
		HitTracker sub = getSubTracker(inKey);
		if (sub != null)
		{
			sub.add(inHit);
		}
	}
	
	public void ensureHasSubTracker(WebPageRequest inReq, String inKey)
	{
		if (getSubTracker(inKey) == null)
		{
			HitTracker sub = new ListHitTracker();
			SearchQuery subQuery = getSearchQuery().copy();
			subQuery.setHitsName("subhits");
			subQuery.setCatalogId(inKey);
			sub.setSearchQuery(subQuery);
			
			inReq.putSessionValue(sub.getSessionId(), sub);
			
			addSubTracker(inKey, sub);
		}
	}

	public void setDataSource(String inDataSource) {
		super.setDataSource(inDataSource);
		for (Iterator iterator = getHitTrackers().values().iterator(); iterator.hasNext();) {
			HitTracker tracker = (HitTracker) iterator.next();
			tracker.setDataSource(inDataSource);
		}
	}
	
	public void clear()
	{
		getHitTrackers().clear();
		fieldValues = null;
	}
	
	public List getCatalogIds()
	{
		ArrayList<String> catalogids = new ArrayList<String>();
		for (Iterator iterator = getHitTrackers().values().iterator(); iterator.hasNext();)
		{
			HitTracker sub = (HitTracker) iterator.next();
			catalogids.add(sub.getCatalogId());
		}
		return catalogids;
	}
}
