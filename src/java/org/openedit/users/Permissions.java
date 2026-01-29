package org.openedit.users;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.CatalogEnabled;
import org.openedit.Data;
import org.openedit.data.EntityPermissions;
import org.openedit.data.Searcher;
import org.openedit.data.SearcherManager;
import org.openedit.hittracker.HitTracker;
import org.openedit.profile.UserProfile;

public class Permissions implements CatalogEnabled 
{
	private static final Log log = LogFactory.getLog(Permissions.class);

	protected UserProfile fieldUserProfile;
	protected Set fieldSystemRolePermissions;
	protected SearcherManager fieldSearcherManager;
	protected String fieldCatalogId;
	
	public Permissions()
	{
	}
	
	//system wide
	
	public String getCatalogId()
	{
		return fieldCatalogId;
	}

	public void setCatalogId(String inCatalogId)
	{
		fieldCatalogId = inCatalogId;
	}

	public SearcherManager getSearcherManager()
	{
		return fieldSearcherManager;
	}

	public void setSearcherManager(SearcherManager inSearcherManager)
	{
		fieldSearcherManager = inSearcherManager;
	}

	public Set getSystemRolePermissions()
	{
		return fieldSystemRolePermissions;  ///Default system permissions from the default entity?
	}

	public void setSystemRolePermissions(Set inSettingsGroupPermissions)
	{
		fieldSystemRolePermissions = inSettingsGroupPermissions;
	}
	public EntityPermissions getEntityPermissions() {
		return getUserProfile().getEntityPermissions();
	}


	public UserProfile getUserProfile()
	{
		return fieldUserProfile;
	}


	public void setUserProfile(UserProfile inUserProfile)
	{
		fieldUserProfile = inUserProfile;
	}


	public Permissions(UserProfile inProfile)
	{
		setUserProfile(inProfile)	;
	}

	//"product","createnew"
//	public Boolean can(Data module, Data inData, String inKey)
//	{
//		//First check role
//		
//		
////		String role = findEntityPermissionLevel(module);
////		if( isDataOwner )
////		{
////			boolean can = can("owner" + inModuleId,inKey);
////			if( can )
////			{
////				return true;
////			}
////		}	
//		boolean can = can(module,inKey);
//		return can;
//	}

	private Data loadModule(String inModuleId)
	{
		Data  module = getSearcherManager().getCachedData(getCatalogId(), "module", inModuleId);
		return module;
	}

	private String findModulePermissionLevel(Data inModule)
	{
		String roleid = getUserProfile().get("settingsgroup");
//		String[] standardroles = new String[]{"editor","administrator","users","owner"};
//		for (int i = 0; i < standardroles.length; i++)
//		{
//			if( standardroles[i].equals(roleid) )
//			{
//				return standardroles[i];
//			}
//		}
		return roleid;
	}

	protected boolean isEditorFor(Data inData)
	{
		if(inData == null) {
			return false;
		}
		Collection users = inData.getValues("editorusers");
		if (users != null && !users.isEmpty() )
		{
			if( users.contains(getUserProfile().getUserId() ) )
			{	
				return true;
			}
		}
		Collection groups = inData.getValues("editorgroups");
		if (groups != null && !groups.isEmpty() )
		{
			Collection<Group> usergroups = getUserProfile().getUser().getGroups();
			for (Iterator iterator = usergroups.iterator(); iterator.hasNext();)
			{
				Group group = (Group) iterator.next();
				if( groups.contains(group.getId()) )
				{
					return true;
				}
			}
		}
		Collection roles = inData.getValues("editorroles");
		if (roles != null && !roles.isEmpty() )
		{
			if( roles.contains(getUserProfile().getId() ) )
			{	
				return true;
			}
		}
		return false;
	}
	
	protected boolean isViewerOnlySet(Data inEntity)
	{
		if(inEntity == null) {
			return false;
		}
		Collection users = inEntity.getValues("viewerusers");
		if (users != null && !users.isEmpty() )
		{
			if( users.contains(getUserProfile().getUserId() ) )
			{	
				return true;
			}
		}
		Collection groups = inEntity.getValues("viewergroups");
		if (groups != null && !groups.isEmpty() )
		{
			Collection<Group> usergroups = getUserProfile().getUser().getGroups();
			for (Iterator iterator = usergroups.iterator(); iterator.hasNext();)
			{
				Group group = (Group) iterator.next();
				if( groups.contains(group.getId()) )
				{
					return true;
				}
			}
		}
		Collection roles = inEntity.getValues("viewerroles");
		if (roles != null && !roles.isEmpty() )
		{
			if( roles.contains(getUserProfile().getId() ) )
			{	
				return true;
			}
		}
		return false;
	}
	
	protected boolean isEditorFor(Data inModule, Data inEntity)
	{
		boolean iseditor = isEditorFor(inModule) ||  isEditorFor(inEntity);
		return iseditor;
	}


	//System Level
	
	public Boolean can(String inKey)  //System wide settings
	{
		if(getSystemRolePermissions() != null) {
			boolean can = getSystemRolePermissions().contains(inKey);
			return can;
		}
		return false;
	}
	//Module Level
	public Boolean can(String inModuleId, String inKey)
	{
		return canModule(inModuleId,inKey);
	}
	public Boolean canModule(String inModuleId, String inKey)
	{
		Data module = loadModule(inModuleId);
		if( module == null )
		{
			log.error("No such module" + inModuleId);
			return false;
		}
		boolean can = canModule(module,inKey);
		return can;
	}
	public Boolean canModule(Data module, String inKey)
	{
		if(module == null) {
			return false;
		}
		
		if( inKey.equals("edit") )
		{
			boolean istrue = isEditorFor(module);
			if( istrue )
			{
				return true;
			}
		}
		String role = findModulePermissionLevel(module);
//		
		boolean can = getEntityPermissions(module,role).can(inKey);
		return can;
	}
	
	//Entity level
//	public Boolean canInEntity(String inModuleId, Data inEntity, String inKey)
//	{
//		Data module = loadModule(inModuleId);
//		boolean can = can(module,inEntity,inKey);
//		return can;
//	}
	
	public Boolean canEntity(Data inModule, Data inEntity, String inKey)
	{
		if (inModule == null || inEntity == null || inKey == null)
		{
			return false;
		}
		
		if( inKey.equals("edit") )
		{
			/*
			if( isViewerOnlySet(inEntity) )
			{
				return false;
			}
			*/
			boolean istrue = isEditorFor(inModule,inEntity);
			if( istrue )
			{
				return true;
			}
		}
		
		String userpermissionlevel = findEntityPermissionLevel(inModule, inEntity);
		EntityPermissions entitypermissions = getEntityPermissions(inModule, userpermissionlevel);
		boolean can = entitypermissions.can(inKey);
		if( can )
		{
			return true;
		}	
		//can = canModule(inModule,inKey);
		return can;
	}

	private String findEntityPermissionLevel(Data inModule, Data inEntity)
	{
		String roleid = getUserProfile().get("settingsgroup");
//		if( "administrator".equals(roleid) )
//		{
//			return roleid;
//		}
		
		boolean isowner = getUserProfile().getUserId().equals(inEntity.get("owner"));
		if( isowner )
		{
			return "owner";
		}	
		//TODO: Do we support custom roles?

//		boolean iseditor = isEditorFor(inEntity);
//		if( iseditor )
//		{
//			return "editor";
//		}
//
//		iseditor = isEditorFor(inModule);
//		if( iseditor )
//		{
//			return "editor";
//		}
//				
		//Must be viewer either at entity or module level
		return roleid;
	}

	public EntityPermissions getEntityPermissions(Data inModule, String inRole)
	{
		String id = inModule + "_" + inRole;
		EntityPermissions modulepermissions = (EntityPermissions)getSearcherManager().getCacheManager().get("permissions" + getCatalogId(),id);
		if( modulepermissions == null)
		{
			Searcher searcher = getSearcherManager().getSearcher(getCatalogId(), "permissionentityassigned");
			HitTracker grouppermissions = searcher.query().exact("settingsgroup",inRole).exact("moduleid", inModule.getId()).search();
			modulepermissions = new EntityPermissions();
			for (Iterator iterator = grouppermissions.iterator(); iterator.hasNext();)
			{
				Data data = (Data) iterator.next();
				String moduleid = data.get("moduleid");
				String permissionname = data.get("permissionsentity");
				Object val = data.getValue("enabled");
				modulepermissions.putPermission(permissionname, val);
			}
			getSearcherManager().getCacheManager().put("permissions" + getCatalogId(),id, modulepermissions);
		}
		
		return modulepermissions;

	}

	
}
