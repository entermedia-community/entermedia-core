package org.openedit.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.openedit.Data;
import org.openedit.data.PropertyDetail;
import org.openedit.data.Searcher;
import org.openedit.data.SearcherManager;
import org.openedit.xml.ElementData;
import org.openedit.xml.XmlArchive;

import com.openedit.hittracker.HitTracker;
import com.openedit.users.User;

public class UserProfile extends ElementData
{
	protected String fieldCatalogId;
	protected SearcherManager fieldSearcherManager;
	protected Data fieldSettingsGroup;
	protected Map fieldResultViews;
	protected XmlArchive fieldXmlArchive;
	protected User fieldUser;
	protected HitTracker fieldCatalogs;
	protected HitTracker fieldUploadCatalogs;
	

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
	public void setValuesFromDetails(String inKey, Collection<PropertyDetail> inValues)
	{
		StringBuffer values = new StringBuffer();
		for (Iterator iterator = inValues.iterator(); iterator.hasNext();)
		{
			PropertyDetail detail = (PropertyDetail) iterator.next();
			values.append(detail.getId());
			if( iterator.hasNext())
			{
				values.append(" ");
			}
		}
		setProperty(inKey,values.toString());
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
		Collection collection = getValues(inKey);
		if( collection != null)
		{
			Collection keys = new ArrayList(collection);
			keys.remove(string);
			setValues(inKey, keys);
		}
	}
	
	public String toString()
	{
		return "User Profile";
	}
	
	
	public HitTracker getCatalogs()
	{
		return fieldCatalogs;
	}

	public void setCatalogs(HitTracker inCatalogs)
	{
		fieldCatalogs = inCatalogs;
	}

	public XmlArchive getXmlArchive()
	{
		return fieldXmlArchive;
	}

	public void setXmlArchive(XmlArchive inXmlArchive)
	{
		fieldXmlArchive = inXmlArchive;
	}

	public User getUser()
	{
		return fieldUser;
	}

	public void setUser(User inUser)
	{
		fieldUser = inUser;
	}

	public Map getResultViews()
	{
		return fieldResultViews;
	}

	public void setResultViews(Map inResultViews)
	{
		fieldResultViews = inResultViews;
	}
	//should not call this method
//	protected String getResultViewPreference(String inView)
//	{
//		Element id = getUserData().getElementById(inView);
//		if(id != null)
//		{
//			return(id.attributeValue("view"));
//			
//		}
//		return null;
//	}

	public void setResultViewPreference(String inView, String inPreference)
	{
		setProperty(inView, inPreference);
	}
	
	public int getHitsPerPageForSearchType(String inResultsView) throws Exception
	{
		String view = inResultsView + "hitsperpage";
		String value = getValue(view);
		if( value == null)
		{
			return 20;
		}
		return Integer.parseInt( value );
	}
	public void setHitsPerPageForSearchType(String inResultsView, int inHits)
	{
		setProperty(inResultsView + "hitsperpage", String.valueOf(inHits));
	}
	public void setSortForSearchType(String inResultsView, String inSort) 
	{
		setProperty(inResultsView + "sort", inSort);
	}
	public String getSortForSearchType(String inResultsType)
	{
		String value = getValue(inResultsType + "sort");
		return value;
	}
	public String getViewForResultType(String inCustomView, String inResultsView)
	{
		if( inCustomView != null)
		{
			return inCustomView;
		}
		String view = getViewForResultType(inResultsView);
		if( view == null)
		{
			view = "default";
		}
		return view;
	}	
	public String getViewForResultType(String inResultsView)
	{
		String view = getValue(inResultsView);
		return view;
	}

	
	public Data getLastCatalog()
	{
		String catid = get("lastcatalog");
		for (Iterator iterator = getCatalogs().iterator(); iterator.hasNext();)
		{
			Data cat = (Data) iterator.next();
			if( catid == null || cat.getId().equals(catid))
			{
				return cat;
			}
		}
		if( getCatalogs().size() > 0)
		{
			return (Data)getCatalogs().iterator().next();
		}
		return null;
	}

	public HitTracker getUploadCatalogs()
	{
		return fieldUploadCatalogs;
	}

	public void setUploadCatalogs(HitTracker inUploadCatalogs)
	{
		fieldUploadCatalogs = inUploadCatalogs;
	}


	
}
