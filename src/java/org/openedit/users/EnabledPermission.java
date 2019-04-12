package org.openedit.users;

import java.io.Serializable;

public class EnabledPermission implements Serializable
{
	int fieldId;
	String fieldPermissionId;
	String fieldGroupId;
	
	public EnabledPermission()
	{
		// TODO Auto-generated constructor stub
	}
	
	public EnabledPermission(String inPermission)
	{
		setPermissionId(inPermission);
	}

	public String getPermissionId()
	{
		return fieldPermissionId;
	}
	public void setPermissionId(String inPermissionId)
	{
		fieldPermissionId = inPermissionId;
	}
	public String getGroupId()
	{
		return fieldGroupId;
	}
	public void setGroupId(String inGroupId)
	{
		fieldGroupId = inGroupId;
	}
	
	public boolean equals(Object inObj)
	{
		String val = inObj.toString();
		return getPermissionId().equals(val);
	}
	public String toString()
	{
		return getPermissionId();
	}

	public int getId()
	{
		return fieldId;
	}

	public void setId(int inId)
	{
		fieldId = inId;
	}
}
