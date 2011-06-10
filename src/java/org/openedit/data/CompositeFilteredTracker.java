package org.openedit.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.openedit.users.User;

public class CompositeFilteredTracker extends FilteredTracker
{
	protected List fieldFilteredTrackers;
	
	public List getFilteredTrackers()
	{
		if (fieldFilteredTrackers == null)
		{
			fieldFilteredTrackers = new ArrayList();
		}
		return fieldFilteredTrackers;
	}
	
	public void filter(String inExternalId, String inExternalValue)
	{
		fieldFilteredTrackers = new ArrayList();
		if (inExternalValue != null)
		{
			String[] externalValues = inExternalValue.split(":");
			for (int i = 0; i < externalValues.length; i++)
			{
				FilteredTracker tracker = new FilteredTracker();
				tracker.setSearcher(getSearcher());
				tracker.setListId(getListId());
				tracker.filter(inExternalId, externalValues[i]);
				fieldFilteredTrackers.add(tracker);
			}	
		}
	}
	
	public List filteredValues()
	{
		List intersection = null;
		for (Iterator iterator = getFilteredTrackers().iterator(); iterator.hasNext();)
		{
			FilteredTracker tracker = (FilteredTracker) iterator.next();
			List values = tracker.filteredValues();
			if (intersection == null)
			{
				intersection = values;
			}
			intersection.retainAll(values);
		}
		return intersection;
	}
	
	public void deleteValues(List inListValues, User inUser)
	{
		for (Iterator iterator = getFilteredTrackers().iterator(); iterator.hasNext();)
		{
			FilteredTracker tracker = (FilteredTracker) iterator.next();
			tracker.deleteValues(inListValues, inUser);
		}
	}
	
	public void saveRows(List inListValues, User inUser)
	{
		for (Iterator iterator = getFilteredTrackers().iterator(); iterator.hasNext();)
		{
			FilteredTracker tracker = (FilteredTracker) iterator.next();
			tracker.saveRows(inListValues, inUser);
		}
	}

}
