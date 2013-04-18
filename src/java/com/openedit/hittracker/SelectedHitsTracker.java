package com.openedit.hittracker;

import java.util.Iterator;

import org.openedit.Data;


public class SelectedHitsTracker extends ListHitTracker
{
	public SelectedHitsTracker(HitTracker inTracker )
	{
		setTracker(inTracker);
		setList(inTracker.getSelectedHits());
	}
	protected HitTracker fieldTracker;
	
	public HitTracker getTracker() {
		return fieldTracker;
	}

	public void setTracker(HitTracker inTracker) {
		fieldTracker = inTracker;
	}
//	
//	@Override
//	public boolean hasMultipleSelections() {
//		// TODO Auto-generated method stub
//		return getTracker().hasMultipleSelections();
//	}
//	@Override
//	public boolean hasSelections() {
//		// TODO Auto-generated method stub
//		return getTracker().hasSelections();
//	}
	
	public Iterator iterator()
	{
		final Iterator list = getList().iterator();
		return new Iterator() 
		{
			public boolean hasNext() {
				// TODO Auto-generated method stub
				return list.hasNext();
			}

			public Object next() {
				Integer i = (Integer)list.next();
				return getTracker().get(i);
			}

			public void remove() {
				// TODO Auto-generated method stub
				
			}
		};
	}
	
	@Override
	public Data get(int inCount) 
	{
		Integer first = getLocation(inCount);
		return getTracker().get(first);
	}

	private Integer getLocation(int inCount) {
		Integer first = (Integer)getList().get(inCount);
		return first;
	}

	public boolean contains(Object inHit) {
		return false;
	}

}
