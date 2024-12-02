package org.openedit.data;

import org.openedit.hittracker.HitTracker;

public class CacheEntry
{
	String fieldIndexId;
	public String getIndexId()
	{
		return fieldIndexId;
	}
	public void setIndexId(String inIndexId)
	{
		fieldIndexId = inIndexId;
	}
	public HitTracker getHits()
	{
		return fieldHits;
	}
	public void setHits(HitTracker inHits)
	{
		fieldHits = inHits;
	}
	HitTracker fieldHits;
	
}
