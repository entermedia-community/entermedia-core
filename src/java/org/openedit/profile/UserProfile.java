package org.openedit.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.openedit.Data;
import org.openedit.data.Searcher;
import org.openedit.data.SearcherManager;
import org.openedit.xml.ElementData;

import com.openedit.users.User;

public class UserProfile extends ElementData
{
	protected String fieldCatalogId;
	protected SearcherManager fieldSearcherManager;
	protected Data fieldSettingsGroup;

	public String getCatalogId()
	{
		return fieldCatalogId;
	}
	public void setCatalogId(String catalogId)
	{
		fieldCatalogId = catalogId;
	}
	public SearcherManager getSearcherManager()
	{
		return fieldSearcherManager;
	}
	public void setSearcherManager(SearcherManager searcherManager)
	{
		fieldSearcherManager = searcherManager;
	}
	public String getUserId()
	{
		return get("userid");
	}

	public boolean isEnabled(String inPreference)
	{
		String val = getValue(inPreference);
		return Boolean.parseBoolean(val);
	}
	
	public String getValue(String inPreference)
	{
		if(inPreference == null){
			return null;
		}
		String val = get(inPreference);
		if (val == null && getSettingsGroup() != null)
		{
			return getSettingsGroup().get(inPreference);
		}

		return val;
	}
	public Collection getValues(String inPreference)
	{
		String val = getValue(inPreference);
		
		if (val == null)
			return null;
		
		String[] vals = val.split("\\s+");

		Collection collection = Arrays.asList(vals);
		//if null check parent
		return collection;
	}

	public String replaceUserVariable(String inValue)
	{
		if( inValue == null)
		{
			return inValue;
		}
		String value = inValue;
		int start = 0;
		while( (start = value.indexOf("$[",start)) != -1)
		{
			int end = value.indexOf("]",start);
			if( end != -1)
			{
				String key = value.substring(start+2,end);
				String variable = getValue(key); //check for property
				
				if( variable != null)
				{
					value = value.substring(0,start) + variable + value.substring(end+1);
					if(variable.length() <= end)
					{
						start = end-variable.length();
					}
					else
					{
						start =  variable.length();
					}
				}
				else
				{
					start = end;
				}
			}
		}
		if( start > 0 && inValue.equals(value))
		{
			value = value.replace('[', '{');
			value = value.replace(']', ']');
		}
		
		return value;
	}
	
	public Data getSettingsGroup()
	{
		if (fieldSettingsGroup == null)
		{
			String groupid = get("settingsgroup");
			if( groupid == null)
			{
				groupid = "guest";
			}
			Searcher settingsGroupSearcher = getSearcherManager().getSearcher(getCatalogId(), "settingsgroup");
			fieldSettingsGroup = (Data)settingsGroupSearcher.searchById(groupid);
		}
		return fieldSettingsGroup;
	}
	
	public void setSettingsGroup(String inSettingsGroupId)
	{
		setProperty("settingsgroupid", inSettingsGroupId);
		fieldSettingsGroup = null;
	}
	
	public void save(User inUser)
	{
		Searcher searcher = getSearcherManager().getSearcher(getCatalogId(), "userprofile");
		searcher.saveData(this, inUser);
	}
	public void setValues(String inKey, Collection<String> inValues)
	{
		StringBuffer values = new StringBuffer();
		for (Iterator iterator = inValues.iterator(); iterator.hasNext();)
		{
			String detail = (String) iterator.next();
			values.append(detail);
			if( iterator.hasNext())
			{
				values.append(" ");
			}
		}
		setProperty(inKey,values.toString());
	}
	
	public void addValue(String inKey, String string)
	{
		String current = get(inKey);
		if(string != null)
		{
			string = string.trim();
		}
		if (current == null || current.length() == 0)
		{
			setProperty(inKey, string);
		}
		else
		{
			setProperty(inKey, current + " " + string);
		}
	}
	
	public void removeValue(String inKey, String string)
	{
		
		Collection keys = new ArrayList(getValues(inKey));
		keys.remove(string);
		setValues(inKey, keys); 
	}
	
	public String toString()
	{
		return "User Profile";
	}
}
