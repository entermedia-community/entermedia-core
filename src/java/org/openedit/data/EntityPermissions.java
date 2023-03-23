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

	Map fieldEntityPermissions;
	
	public Map<String,Map> getEntityPermissions()
	{
		if (fieldEntityPermissions == null)
		{
			fieldEntityPermissions = new HashMap();
			
		}

		return fieldEntityPermissions;
	}

	public void setEntityPermissions(Map inEntityPermissions)
	{
		fieldEntityPermissions = inEntityPermissions;
	}

	public Map getEntityPermissions(String inEntityId)
	{
		Map permissions = getEntityPermissions().get(inEntityId);
		return permissions;
	}
	
	public void putPermission(String inEntityId, String inId, Object value)
	{
		if(value == null) {
			//Don't include anything if the value isn't set at all in the database
			return;
		}
		Map permissions = getEntityPermissions(inEntityId);
		if( permissions == null)
		{
			permissions = new HashMap();
			getEntityPermissions().put(inEntityId,permissions);
		}
		permissions.put(inId,Boolean.valueOf(value.toString()));
	}

	public Boolean can(String inEntityId, String inKey) 
	{
		Map<String,Boolean> permissions = getEntityPermissions(inEntityId);
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
