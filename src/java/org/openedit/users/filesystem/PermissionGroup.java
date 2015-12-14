package org.openedit.users.filesystem;

import java.util.Collections;
import java.util.List;

public class PermissionGroup
{
	protected String fieldName;
	protected List fieldPermissions;
	public PermissionGroup()
	{
		
	}
	
	public PermissionGroup(List inPermissions)
	{
		setPermissions(inPermissions);
	}
	
	public String getName()
	{
		return fieldName;
	}
	public void setName(String inName)
	{
		fieldName = inName;
	}
	public List getPermissions()
	{
		if ( fieldPermissions == null)
		{
			fieldPermissions = Collections.EMPTY_LIST;
		}
		return fieldPermissions;
	}
	public void setPermissions(List inPermissions)
	{
		fieldPermissions = inPermissions;
	}
}
