package com.openedit.hittracker;

import java.util.ArrayList;
import java.util.Iterator;

import org.openedit.Data;


public class SelectedHitsTracker extends ListHitTracker
{
	public SelectedHitsTracker(HitTracker inTracker )
	{
		setTracker(inTracker);
		setList(new ArrayList( inTracker.getSelections() ) );
	}
	protected HitTracker fieldTracker;
	
	public HitTracker getTracker() {
		return fieldTracker;
	}

	public void setTracker(HitTracker inTracker) {
		fieldTracker = inTracker;
	}

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
				String assetid = (String)list.next();
				int index = getTracker().findRow("id", assetid);
				return getTracker().get(index);
			}

			public void remove() {
				// TODO Auto-generated method stub
				
			}
		};
	}
	
	@Override
	public Data get(int inCount) 
	{
		int first = getLocation(inCount);
		return getTracker().get(first);
	}

	private Integer getLocation(int inCount) 
	{
	
		String assetid = (String)getList().get(inCount);
		
		int index = getTracker().findRow("id", assetid);
		return index;
	}

	public boolean contains(Object inHit) {
		return false;
	}

}
