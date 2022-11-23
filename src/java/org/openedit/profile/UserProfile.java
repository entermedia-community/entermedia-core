package org.openedit.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.entermediadb.asset.Category;
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
import org.openedit.users.Group;
import org.openedit.users.User;
import org.openedit.users.UserManager;
import org.openedit.users.UserManagerException;
import org.openedit.xml.XmlArchive;

public class UserProfile extends BaseData implements SaveableData, CatalogEnabled, User
{
	protected String fieldCatalogId;
	protected SearcherManager fieldSearcherManager;
	protected MultiValued fieldSettingsGroup;
	//	protected Map<String,String> fieldSettingsGroupPermissions;
	protected Map fieldResultViews;
	protected XmlArchive fieldXmlArchive;
	protected Collection fieldViewCategories;
	protected Collection<String> fieldCollectionIds;
	protected Collection<Data> fieldModules;
	protected Set fieldPermissions;
	protected String fieldSettingsGroupIndexId;
	protected UserManager fieldUserManager;

	public UserManager getUserManager()
	{
		return fieldUserManager;
	}

	public void setUserManager(UserManager inUserManager)
	{
		fieldUserManager = inUserManager;
	}

	public String getSettingsGroupIndexId()
	{
		return fieldSettingsGroupIndexId;
	}

	public void setSettingsGroupIndexId(String inIndexId)
	{
		fieldSettingsGroupIndexId = inIndexId;
	}

	public Set getPermissions()
	{
		return fieldPermissions;
	}

	public void setPermissions(Set inPermissions)
	{
		fieldPermissions = inPermissions;
	}

	public boolean hasModule(String inId)
	{
		for (Iterator iterator = getModules().iterator(); iterator.hasNext();)
		{
			Data data = (Data) iterator.next();
			if( data.getId().equals(inId))
			{
				return true;
			}
		}
		return false;
	}
	
	public Collection<Data> getModules()
	{
		return fieldModules;
	}

	public void setModules(Collection<Data> inModules)
	{
		fieldModules = inModules;
	}

	public Collection<Data> getEntities()
	{
		Collection<Data> items = new ArrayList();
		for (Iterator iterator = getModules().iterator(); iterator.hasNext();)
		{
			Data module = (Data) iterator.next();
			if( Boolean.parseBoolean( module.get("isentity") ) )
			{
				items.add(module);
			}
		}
		return items;
	}


	public User getUser()
	{
		User user = (User)getSearcherManager().getCachedData(getCatalogId(), "user",
					super.get("userid"));
		return user;
	}


	private static final Log log = LogFactory.getLog(UserProfile.class);

	public UserProfile()
	{
//		if (1 > 32)
//		{
//
//		}
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

//	public Collection<String> getCollectionIds()
//	{
//		return fieldCollectionIds;
//	}
//
//	public void setCollectionIds(Collection<String> inCollectionIds)
//	{
//		fieldCollectionIds = inCollectionIds;
//	}
	public boolean isInRole(String inRole)
	{
		if( inRole.equals( get("settingsgroup")))
		{
			return true;					
		}
		return false;					
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

		//		
		if (val == null && getSettingsGroup() != null)
		{
			val = getSettingsGroup().get(inPreference);
		}

		//		if (val == null && getSettingsGroup() != null)
		//		{
		//			val = getSettingsGroupPermissions().get(inPreference);
		//		}		
		if (val == null && !inPreference.equals("userid"))
		{
			User user = getUser();
			if( user != null)
			{
				val = getUser().get(inPreference);
			}
		}

		return val;
	}
	
	public Object getValue(String inKey)
	{
		Object val = super.getValue(inKey);
		if( val != null)
		{
			return val;
		}
		if(inKey.equals("name")) {
			return getName();
		}
		if( inKey.equals("sendcollectionnotifications") || inKey.equals("sendapprovalnotifications") ||  inKey.equals("assethitsperpage") ||  inKey.equals("modulehitsperpage") )
		{
			if(getSettingsGroup() != null){
			//if we have a local value then user it. Otherwise use parent.
			return getSettingsGroup().getValue(inKey);
			}
		}
		
		if (val == null && !inKey.equals("userid"))
		{
			User user = getUser();
			if( user != null)
			{
				val = getUser().getValue(inKey);
			}
		}
		
		return null;
	}
	
	public Collection getValues(String inPreference)
	{
		Object value = super.getValue(inPreference);
		if( value instanceof Collection)
		{
			Collection col = (Collection) value;
			if( !col.isEmpty() )
			{
				return col;
			}
		}
		if (value == null && getSettingsGroup() != null)
		{
			value = getSettingsGroup().getValue(inPreference);
		}
		String val = null;
		if( value instanceof Collection)
		{
			Collection col = (Collection) value;
			if( !col.isEmpty() )
			{
				return col;
			}
		}
		else if( value != null)
		{
			val = String.valueOf(value);
		}
		if( val == null)
		{
			val = get(inPreference);
		}
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

	public void save()
	{
		save(null);
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

	public boolean containsValue(String inKey, String inValue)
	{
		Collection current = getValues(inKey);
		
		if( current != null && current.contains(inValue))
		{
			return true;
		}
		return false;
	}
	public void addValue(String inKey, String string)
	{
		Collection current = getValues(inKey);
		if( current == null)
		{
			current = new ArrayList();
		}
		else
		{
			current = new ArrayList(current);
		}
		if (string != null)
		{
			string = string.trim();
		}
		if( !current.contains(string) )
		{
			current.add(string);
		}
		setValue(inKey, current);
				
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
				return 36;
			}
		}
		if ("null".equals(value))
		{
			return 36;
		}
		try
		{
			return Integer.parseInt(value);			
		}
		catch (Exception ex)
		{
			return 36;
		}
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


	public Collection<Category> getViewCategories()
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

	public void setViewCategories(Collection<Category> inCombinedLibraries)
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
		saveUserIfChanged(inId, inValue);
		if (inId.equals("settingsgroup"))
		{
			fieldSettingsGroup = null;
		}
		super.setProperty(inId, inValue);
	}

	
	@Override
	public void setValue(String inId, Object inValue)
	{ 
		if (get("userid") != null)
		{
			String val = (String) String.valueOf( inValue );
			saveUserIfChanged(inId, val);
		}
		super.setValue(inId, inValue);
	}

	protected void saveUserIfChanged(String inId, String val)
	{
		if ("lastName".equalsIgnoreCase(inId)
				|| "firstName".equalsIgnoreCase(inId)
				|| "email".equalsIgnoreCase(inId))
		{
			String oldvalue = getUser().get(inId);
			if( oldvalue == null || !oldvalue.equals(val)) 
			{
				User user = getUser();
				user.setValue(inId,val);
				//getSearcherManager().getSearcher(getCatalogId(), "user").saveData(user);
				//getUserManager().saveUser(user);
			}
		}
	}	

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

	public boolean isInGroup(String inGroupId)
	{
		if( getUser() == null)
		{
			return false;
		}
		return getUser().isInGroup(inGroupId);
	}
	
	public void removeAllStartWith(String inName)
	{
		Collection collection = getMap().keySet();
		Collection toremove = new HashSet();
		for (Iterator iterator = collection.iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			if( key.startsWith(inName))
			{
				toremove.add(key);
			}
		}
		for (Iterator iterator = toremove.iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			removeValue(key);
		}
		
	}

	@Override
	public String getFirstName()
	{
		return getUser().getFirstName();
	}

	@Override
	public String getLastName()
	{
		return getUser().getLastName();
	}

	@Override
	public String getEmail()
	{
		return getUser().getEmail();
	}

	@Override
	public void setFirstName(String inFirstName)
	{
		getUser().setFirstName(inFirstName);
	}

	@Override
	public void setLastName(String inLastName)
	{
		getUser().setLastName(inLastName);
		
	}

	@Override
	public void setEmail(String inEmail)
	{
		getUser().setEmail(inEmail);
		
	}

	@Override
	public Collection getGroups()
	{
		// TODO Auto-generated method stub
		return getUser().getGroups();
	}

	@Override
	public String getPassword()
	{
		return getUser().getPassword();
	}

	@Override
	public void setPassword(String inPassword) throws UserManagerException
	{
		getUser().setPassword(inPassword);		
	}

	@Override
	public String getUserName()
	{
		// TODO Auto-generated method stub
		return getUser().getUserName();
	}

	@Override
	public boolean isEnabled()
	{
		return getUser().isEnabled();
	}

	@Override
	public void setEnabled(boolean inEnabled)
	{
		getUser().setEnabled(inEnabled);
		
	}

	@Override
	public boolean hasProperty(String inProperty)
	{
		return getUser().hasProperty(inProperty);
	}
	
	@Override
	public String getEnterMediaKey()
	{
		return getUser().getEnterMediaKey();
	}

	@Override
	public void setUserName(String inUserName)
	{
		getUser().setUserName(inUserName);
	}

	@Override
	public List listGroupPermissions()
	{
		return getUser().listGroupPermissions();
	}

	@Override
	public void addGroup(Group inGroup)
	{
		getUser().addGroup(inGroup);		
	}

	@Override
	public void removeGroup(Group inGroup)
	{
		getUser().removeGroup(inGroup);
	}

	@Override
	public boolean isInGroup(Group inGroup)
	{
		return getUser().isInGroup(inGroup);
	}

	@Override
	public boolean isVirtual()
	{
		return getUser().isVirtual();
	}

	@Override
	public void setVirtual(boolean inVirtual)
	{
		getUser().setVirtual(inVirtual);
	}

	@Override
	public String getScreenName()
	{
		return getUser().getScreenName();
	}

	@Override
	public Map listAllProperties()
	{
		return getUser().listAllProperties();
	}

	@Override
	public void setGroups(Collection inGroupslist)
	{
		getUser().setGroups(inGroupslist);
	}
	
}
