package org.openedit.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openedit.Data;
import org.openedit.OpenEditException;
import org.openedit.hittracker.HitTracker;
import org.openedit.hittracker.SearchQuery;
import org.openedit.users.User;

public class FilteredTracker extends HitTracker
{
	protected Searcher fieldSearcher;
	protected String fieldExternalId;
	protected String fieldExternalValue;
	protected String fieldListId;
	protected String fieldSourcePath;
	
	public String getSourcePath() {
		return fieldSourcePath;
	}

	public void setSourcePath(String inSourcePath) {
		fieldSourcePath = inSourcePath;
	}

	public void filter(String inExternalId, String inExternalValue)
	{
		setExternalId(inExternalId);
		setExternalValue(inExternalValue);
	}
	
	public HitTracker filtered()
	{
		SearchQuery query = getSearcher().createSearchQuery();
		query.addMatches(getExternalId(), getExternalValue());
		HitTracker results = getSearcher().search(query);
		return results;
	}
	
	public List filteredValues()
	{
		HitTracker tracker = filtered();
		List values = new ArrayList();
		for (Iterator iterator = tracker.iterator(); iterator.hasNext();)
		{
			Object data = iterator.next();
			String val = tracker.getValue(data, getListId());
			if( val != null)
			{
				values.add(val);
			}
		}
		return values;
	}
	
	public Iterator iterator()
	{
		return filtered().iterator();
	}
	
	public void clear()
	{
		for (Iterator iterator = iterator(); iterator.hasNext();)
		{
			Object data = iterator.next();
			if( data instanceof Data)
			{
				getSearcher().delete((Data)data, null);				
			}
			else
			{
				throw new OpenEditException("Need API to delete documents");
			}
		}
	}
	
	public String getExternalId()
	{
		return fieldExternalId;
	}
	public void setExternalId(String inExternalId)
	{
		fieldExternalId = inExternalId;
	}
	public String getExternalValue()
	{
		return fieldExternalValue;
	}
	public void setExternalValue(String inExternalValue)
	{
		fieldExternalValue = inExternalValue;
	}
	public String getListId()
	{
		return fieldListId;
	}
	public void setListId(String inListId)
	{
		fieldListId = inListId;
	}
	public Searcher getSearcher()
	{
		return fieldSearcher;
	}
	public void setSearcher(Searcher inSearcher)
	{
		fieldSearcher = inSearcher;
	}
	
	public Data findValue(String inListValue)
	{
		SearchQuery query = getSearcher().createSearchQuery();
		query.setAndTogether(true);
		query.addMatches(getExternalId(), getExternalValue());
		query.addMatches(getListId(), inListValue);
		HitTracker results = getSearcher().search(query);
		if (results.size() > 0)
		{
			Data data = (Data)results.get(0);
			return data;
		}
		return null;
	}
	
	public Data createData()
	{
		Data data = getSearcher().createNewData();
		data.setProperty(getExternalId(), getExternalValue());
		data.setSourcePath(getSourcePath());
		return data;
	}
	
	public void addRow(String inListValue, User inUser)
	{
		if (findValue(inListValue) == null)
		{
			Data data = createData();
			data.setProperty(getListId(), inListValue);
			getSearcher().saveData(data, inUser);
		}
	}
	
	public void saveRows(List inNewValues, User inUser)
	{
		removeExtraRows(inNewValues, inUser);
		List toAdd = filterToAdd(inNewValues, inUser);
		getSearcher().saveAllData(toAdd, inUser);
	}

	public List filterToAdd(List inNewValues, User inUser)
	{
		List currentValues = filteredValues();
		List toAdd = new ArrayList();
		
		for (Iterator iterator = inNewValues.iterator(); iterator.hasNext();)
		{
			String value = (String) iterator.next();
			if (!currentValues.contains(value))
			{
				Data data = createData();
				data.setProperty(getListId(), value);
				toAdd.add(data);
			}
		}
		return toAdd;
	}

	private void removeExtraRows(List inNewValues, User inUser)
	{
		for (Iterator iterator = filtered().iterator(); iterator.hasNext();)
		{
			Data data = (Data) iterator.next();
			if (!inNewValues.contains(data.get(getListId())))
			{
				getSearcher().delete(data, inUser);
			}
		}
	}
	
	public void deleteValues(List inListValues, User inUser)
	{
		for (Iterator iterator = filtered().iterator(); iterator.hasNext();)
		{
			Data data = (Data) iterator.next();
			if (inListValues.contains(data.get(getListId())))
			{
				getSearcher().delete(data, inUser);
			}
		}
	}

	public int size()
	{
		return filtered().size();
	}

	public boolean contains(Object inHit)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public Data get(int inCount)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Data toData(Object inHit)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
