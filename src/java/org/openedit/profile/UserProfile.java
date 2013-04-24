package org.openedit.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.Data;
import org.openedit.data.PropertyDetail;
import org.openedit.data.Searcher;
import org.openedit.data.SearcherManager;
import org.openedit.xml.ElementData;
import org.openedit.xml.XmlArchive;

import com.openedit.hittracker.HitTracker;
import com.openedit.hittracker.SearchQuery;
import com.openedit.users.User;

public class UserProfile extends ElementData
{
	protected String fieldCatalogId;
	protected SearcherManager fieldSearcherManager;
	protected Data fieldSettingsGroup;
//	protected Map<String,String> fieldSettingsGroupPermissions;
	protected Map fieldResultViews;
	protected XmlArchive fieldXmlArchive;
	protected HitTracker fieldCatalogs;
	protected HitTracker fieldUploadCatalogs;
	protected Collection fieldCombinedLibraries;
	protected Collection<Data> fieldModules;
	
	public Collection<Data> getModules()
	{
		return fieldModules;
	}

	public void setModules(Collection<Data> inModules)
	{
		fieldModules = inModules;
	}

	protected User fieldUser;

	public User getUser()
	{
	

	return fieldUser;
	}

	public void setUser(User inUser)
	{
		fieldUser = inUser;
	}

	private static final Log log = LogFactory.getLog(UserProfile.class);

	public UserProfile()
	{
		if (1 > 32)
		{

		}
	}

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
		return getId();
	}

	public boolean isEnabled(String inPreference)
	{
		String val = get(inPreference);
		return Boolean.parseBoolean(val);
	}

//	@Override
//	public String get(String inId) {
//		// TODO Auto-generated method stub
//		return getValue(inId);
//	}
//	
	public String get(String inPreference)
	{
		if (inPreference == null)
		{
			return null;
		}
		
		String val = super.get(inPreference);
		if(val == null  && inPreference.equals("firstname")){
			val = super.get("firstName");
		}
		
		if(val == null && inPreference.equals("lastname")){
			val = super.get("lastName");
		}
		
//		
		if (val == null && getSettingsGroup() != null)
		{
			val = getSettingsGroup().get(inPreference);
		}
		
//		if (val == null && getSettingsGroup() != null)
//		{
//			val = getSettingsGroupPermissions().get(inPreference);
//		}		
		if (val == null && getUser() != null)
		{
			val = getUser().get(inPreference);
		}

		return val;
	}

	
	
	public Collection getValues(String inPreference)
	{
		String val = get(inPreference);

		if (val == null)
			return null;

		String[] vals = val.split("\\s+");

		Collection collection = Arrays.asList(vals);
		// if null check parent
		return collection;
	}

	public String replaceUserVariable(String inValue)
	{
		if (inValue == null)
		{
			return inValue;
		}
		String value = inValue;
		int start = 0;
		while ((start = value.indexOf("$[", start)) != -1)
		{
			int end = value.indexOf("]", start);
			if (end != -1)
			{
				String key = value.substring(start + 2, end);
				String variable = get(key); // check for property

				if (variable != null)
				{
					value = value.substring(0, start) + variable + value.substring(end + 1);
					if (variable.length() <= end)
					{
						start = end - variable.length();
					}
					else
					{
						start = variable.length();
					}
				}
				else
				{
					start = end;
				}
			}
		}
		if (start > 0 && inValue.equals(value))
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
			String groupid = super.get("settingsgroup");
			if (groupid == null)
			{
				groupid = "guest";
			}
			Searcher settingsGroupSearcher = getSearcherManager().getSearcher(getCatalogId(), "settingsgroup");
			fieldSettingsGroup = (Data) settingsGroupSearcher.searchById(groupid);
			if (fieldSettingsGroup == null && log.isDebugEnabled())
			{
				log.debug("No settings group defined");
			}
//			else
//			{
//				Searcher searcher = getSearcherManager().getSearcher(getCatalogId(),"settingsgrouppermissions");
//				SearchQuery q = searcher.createSearchQuery();
//				q.
//				
//			}
		}
		return fieldSettingsGroup;
	}

	public void setSettingsGroup(String inSettingsGroupId)
	{
		setProperty("settingsgroup", inSettingsGroupId);
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
			if (iterator.hasNext())
			{
				values.append(" ");
			}
		}
		setProperty(inKey, values.toString());
	}

	public void setValues(String inKey, Collection<String> inValues)
	{
		StringBuffer values = new StringBuffer();
		for (Iterator iterator = inValues.iterator(); iterator.hasNext();)
		{
			String detail = (String) iterator.next();
			values.append(detail);
			if (iterator.hasNext())
			{
				values.append(" ");
			}
		}
		setProperty(inKey, values.toString());
	}

	public void addValue(String inKey, String string)
	{
		String current = get(inKey);
		if (string != null)
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
		if (collection != null)
		{
			Collection keys = new ArrayList(collection);
			keys.remove(string);
			setValues(inKey, keys);
		}
	}

	public String toString()
	{

		return getShortDescription();
		
		
		

	}
	
	
	public String getShortDescription()
	{
		StringBuffer out = new StringBuffer();
		if ( get("firstName") != null)
		{
			out.append( get("firstName") );
			out.append(" ");
		} 
		if (  get("lastName") != null)
		{
			out.append(get("lastName"));
		}
		if( out.length() == 0)
		{
			if( get("email") != null && Character.isDigit(getUserId().charAt(0) ) )
			{
				out.append( get("email") );
			}
			else
			{
				out.append( getUserId());
			}
		}
		return out.toString();
	}
	/**
	 * @deprecated Not used any more. Track permissions on an app basis
	 * @return
	 */
	public HitTracker getCatalogs()
	{
		return fieldCatalogs;
	}

	/**
	 * @deprecated Not used any more. Track permissions on an app basis
	 * @return
	 */
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

	public Map getResultViews()
	{
		return fieldResultViews;
	}

	public void setResultViews(Map inResultViews)
	{
		fieldResultViews = inResultViews;
	}

	// should not call this method
	// protected String getResultViewPreference(String inView)
	// {
	// Element id = getUserData().getElementById(inView);
	// if(id != null)
	// {
	// return(id.attributeValue("view"));
	//
	// }
	// return null;
	// }

	public void setResultViewPreference(String inView, String inPreference)
	{
		setProperty(inView, inPreference);
	}

	public int getHitsPerPageForSearchType(String inResultsView) throws Exception
	{
		String view = inResultsView + "hitsperpage";
		String value = get(view);
		if (value == null)
		{
			return 15;
		}
		return Integer.parseInt(value);
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
		String value = get(inResultsType + "sort");
		return value;
	}

	public String getViewForResultType(String inCustomView, String inResultsView)
	{
		if (inCustomView != null)
		{
			return inCustomView;
		}
		String view = getViewForResultType(inResultsView);
		if (view == null)
		{
			view = "default";
		}
		return view;
	}

	public String getViewForResultType(String inResultsView)
	{
		String view = get(inResultsView);
		return view;
	}
	/**
	 * @deprecated Not used any more. Track permissions on an app basis
	 * @return
	 */
	public Data getLastCatalog()
	{
		String catid = get("lastcatalog");
		if( getCatalogs() == null )
		{
			return null;
		}
		for (Iterator iterator = getCatalogs().iterator(); iterator.hasNext();)
		{
			Data cat = (Data) iterator.next();
			if (catid == null || cat.getId().equals(catid))
			{
				return cat;
			}
		}
		if (getCatalogs().size() > 0)
		{
			return (Data) getCatalogs().iterator().next();
		}
		return null;
	}
	/**
	 * @deprecated Not used any more. Track permissions on an app basis
	 * @return
	 */
	public HitTracker getUploadCatalogs()
	{
		return fieldUploadCatalogs;
	}

	/**
	 * @deprecated Not used any more. Track permissions on an app basis
	 * @return
	 */
	public void setUploadCatalogs(HitTracker inUploadCatalogs)
	{
		fieldUploadCatalogs = inUploadCatalogs;
	}

	public Collection getCombinedLibraries()
	{
		return fieldCombinedLibraries;
	}

	public void setCombinedLibraries(Collection inCombinedLibraries)
	{
		fieldCombinedLibraries = inCombinedLibraries;
	}

	public Data getDefaultViewForModule(String inModuleId)
	{
		Searcher viewSearcher = getSearcherManager().getSearcher(getCatalogId(), "view");
		SearchQuery q = viewSearcher.createSearchQuery();
		q.addMatches("module", inModuleId);
		q.addMatches("systemdefined", "false");
		q.addSortBy("ordering");
		HitTracker row = (HitTracker) viewSearcher.search(q);
		if (row.size() > 0)
		{
			if (row != null)
			{
				return row.get(0);
			}
		}
		return null;
	}

	@Override
	public String getSourcePath()
	{

		if (fieldSourcePath == null)
		{
			if (getUser() != null)
			{
				return getUser().getId();
			}
			if (getUserId() != null)
			{
				return getUserId();
			}

		}
		else
		{
			return fieldSourcePath;
		}
		return super.getSourcePath();
	}

	public void setProperty(String inId, String inValue)
	{
		if (getUser() != null)
		{
			if ("firstname".equalsIgnoreCase(inId))
			{
				getUser().setFirstName(inValue);
			}
			if ("lastname".equalsIgnoreCase(inId))
			{
				getUser().setLastName(inValue);

			}
			if ("email".equalsIgnoreCase(inId))
			{
				getUser().setEmail(inValue);
			}
			if ("password".equalsIgnoreCase(inId))
			{
				getUser().setPassword(inValue);
			}
		}
		if(inId.equals("settingsgroup"))
		{
			fieldSettingsGroup = null;
		}
		super.setProperty(inId, inValue);
	}
	
//	public Map<String,String> getSettingsGroupPermissions() {
//		return fieldSettingsGroupPermissions;
//	}
	
	@Override
	public String getName()
	{
		return toString();
	}	
		
	public String getText(){
		return toString();
	}
}
