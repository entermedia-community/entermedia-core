package com.openedit.hittracker;

import java.util.Iterator;
import java.util.Set;

import org.openedit.Data;

public class SelectedHitsTracker extends HitTracker 
{
	
	public SelectedHitsTracker(HitTracker inTracker )
	{
		setTracker(inTracker);
	}
	
	
	protected String fieldSessionId;
	public String getSessionId()
	{
		if( fieldSessionId == null)
		{
			return super.getSessionId();
		}
		return fieldSessionId;
	}

	public void setSessionId(String inSessionId)
	{
		fieldSessionId = inSessionId;
	}

	protected HitTracker fieldTracker;
	
	public HitTracker getTracker() {
		return fieldTracker;
	}

	public void setTracker(HitTracker inTracker) {
		fieldTracker = inTracker;
	}

	@Override
	public Data get(int inCount) 
	{
		return getTracker().get(inCount);
	}

	public Object first()
	{
		if( size() == 0)
		{
			return null;
		}
		return iterator().next();
	}
	@Override
	public Iterator iterator() 
	{
		return new SelectIterator(getTracker().getSelections().iterator());
	}

	public boolean contains(Object inHit) {
		return false;
	}

	@Override
	public int size() {
		return getTracker().getSelections().size();
	}

	
	class SelectIterator implements Iterator
	{
		Iterator selections;
		SelectIterator(Iterator iter)
		{
			selections = iter;
		}
		@Override
		public boolean hasNext() {
			return selections.hasNext();
		}

		public Object next() 
		{
			Integer index = (Integer)selections.next();
			return get(index);
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub
			
		}
		
	}
}
