package org.openedit.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.CatalogEnabled;
import org.openedit.Data;
import org.openedit.MultiValued;
import org.openedit.WebPageRequest;
import org.openedit.data.BaseData;
import org.openedit.data.PropertyDetail;
import org.openedit.data.SaveableData;
import org.openedit.data.Searcher;
import org.openedit.data.SearcherManager;
import org.openedit.hittracker.HitTracker;
import org.openedit.hittracker.SearchQuery;
import org.openedit.users.User;
import org.openedit.xml.XmlArchive;

public class UserProfile extends BaseData implements SaveableData, CatalogEnabled
{
	protected User fieldUser;
	protected String fieldCatalogId;
	protected SearcherManager fieldSearcherManager;
	protected MultiValued fieldSettingsGroup;
	//	protected Map<String,String> fieldSettingsGroupPermissions;
	protected Map fieldResultViews;
	protected XmlArchive fieldXmlArchive;
	protected Collection fieldViewCategories;
	protected Collection<Data> fieldModules;
	protected Set fieldPermissions;

	private Integer getDefaultHitsPerPage() {
		String result = this.fieldSearcherManager.getData(this.fieldCatalogId, "catalogsettings", "defaulthitsperpage").get("value");
		if (result != null) return new Integer(result);
		return 15;
	}

	public Set getPermissions()
	{
		return fieldPermissions;
	}

	public void setPermissions(Set inPermissions)
	{
		fieldPermissions = inPermissions;
	}

	public Collection<Data> getModules()
	{
		return fieldModules;
	}

	public void setModules(Collection<Data> inModules)
	{
		fieldModules = inModules;
	}


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
		if (val == null && inPreference.equals("firstname"))
		{
			val = super.get("firstName");
		}

		if (val == null && inPreference.equals("lastname"))
		{
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

		if (val == null || val.trim().length() == 0)
		{
			return null;
		}
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
		if (fieldSettingsGroup == null && getCatalogId() != null)
		{
			String groupid = super.get("settingsgroup");
			if (groupid == null)
			{
				groupid = "guest";
			}
			Searcher settingsGroupSearcher = getSearcherManager().getSearcher(getCatalogId(), "settingsgroup");
			fieldSettingsGroup = (MultiValued) settingsGroupSearcher.searchById(groupid);
			if (fieldSettingsGroup == null && log.isDebugEnabled())
			{
				log.debug("No settings group defined");
			}
			if (fieldSettingsGroup != null)
			{
				Collection permissions = fieldSettingsGroup.getValues("permissions");
				if (permissions != null)
				{
					fieldPermissions = new HashSet(permissions);
				}
			}
			else
			{
				fieldPermissions = new HashSet();
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
		if (inValues == null)
		{
			return;
		}
		for (Iterator iterator = inValues.iterator(); iterator.hasNext();)
		{
			PropertyDetail detail = (PropertyDetail) iterator.next();
			values.append(detail.getId());
			if (iterator.hasNext())
			{
				values.append(" "); //TODO move to |
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
		if (get("firstName") != null)
		{
			out.append(get("firstName"));
			out.append(" ");
		}
		if (get("lastName") != null)
		{
			out.append(get("lastName"));
		}
		if (out.length() == 0)
		{
			if (get("email") != null && Character.isDigit(getUserId().charAt(0)))
			{
				out.append(get("email"));
			}
			else
			{
				out.append(getUserId());
			}
		}
		return out.toString();
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

	public int getHitsPerPageForSearchType(String inResultsView)
	{
		String view = inResultsView + "hitsperpage";
		String value = get(view);
		if (value == null)
		{
			value = get("modulehitsperpage");
			if (value == null)
			{
				return getDefaultHitsPerPage();
			}
		}
		if ("null".equals(value))
		{
			return getDefaultHitsPerPage();
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


	public Collection<String> getViewCategories()
	{
		return fieldViewCategories;
	}

	public HitTracker getSelectedLibraries(WebPageRequest inReq)
	{
		Searcher librarySearcher = getSearcherManager().getSearcher(getCatalogId(), "library");
		HitTracker tracker = librarySearcher.getAllHits();
		tracker.setSelections(getViewCategories());
		if (inReq.getUser() != null && inReq.getUser().isInGroup("administrators"))
		{
			tracker.selectAll();
		}
		//tracker.setShowOnlySelected(true);
		return tracker;

	}

	public void setViewCategories(Collection<String> inCombinedLibraries)
	{
		fieldViewCategories = inCombinedLibraries;
	}

	public Data getDefaultViewForModule(String inModuleId)
	{
		Searcher viewSearcher = getSearcherManager().getSearcher(getCatalogId(), "view");
		SearchQuery q = viewSearcher.createSearchQuery();
		q.addMatches("moduleid", inModuleId);
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
		String sourcepath = super.getSourcePath();
		if (sourcepath == null)
		{
			if (getUser() != null)
			{
				return getUser().getId();
			}
			if (getUserId() != null)
			{
				return getUserId();
			}
			return null;
		}
		else
		{
			return sourcepath;
		}
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
		if (inId.equals("settingsgroup"))
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

	public String getText()
	{
		return toString();
	}

	public HitTracker getFacetsForType(String inId)
	{
		Searcher facetsearcher = getSearcherManager().getSearcher(getCatalogId(), "userfacets");
		SearchQuery query = facetsearcher.createSearchQuery();
		query.addMatches("userid", getId());
		query.addMatches("type", inId);//needed?
		HitTracker hits = facetsearcher.search(query);
		return hits;

	}

	public boolean hasPermission(String inPropertyName)
	{
		getSettingsGroup();

		Set permissions = getPermissions();
		if( permissions == null)
		{
			return false;
		}
		return permissions.contains(inPropertyName);
	}
}
