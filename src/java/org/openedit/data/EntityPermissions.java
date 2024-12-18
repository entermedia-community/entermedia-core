package org.openedit.data;

import java.util.HashMap;
import java.util.Map;

public class EntityPermissions
{
	String fieldSettingsGroup;
	public String getSettingsGroup() {
		return fieldSettingsGroup;
	}

	public void setSettingsGroup(String inSettingsGroup) {
		fieldSettingsGroup = inSettingsGroup;
	}

	Map fieldPermissions;
	
	public Map<String,Map> getPermissions()
	{
		if (fieldPermissions == null)
		{
			fieldPermissions = new HashMap();
			
		}

		return fieldPermissions;
	}

	public void setPermissions(Map inEntityPermissions)
	{
		fieldPermissions = inEntityPermissions;
	}

	public Map getPermissions(String inPermissionBundle)
	{
		Map permissions = getPermissions().get(inPermissionBundle);
		return permissions;
	}
	
	public void putPermission(String inModuleId, String inId, Object value)
	{
		if(value == null) {
			//Don't include anything if the value isn't set at all in the database
			return;
		}
		Map permissions = getPermissions(inModuleId);
		if( permissions == null)
		{
			permissions = new HashMap();
			getPermissions().put(inModuleId,permissions);
		}
		permissions.put(inId,Boolean.valueOf(value.toString()));
	}

	public Boolean can(String inModuleId, String inKey) 
	{
		Map<String,Boolean> permissions = getPermissions(inModuleId);
		if( permissions == null)
		{
			return true;
		}
		Boolean can = permissions.get(inKey);
		if( can == null)
		{
			return false;
		}
		return can;
	}
	
}
