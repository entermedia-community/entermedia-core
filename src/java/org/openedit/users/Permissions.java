package org.openedit.users;

import java.util.Collection;

import org.openedit.data.EntityPermissions;

public class Permissions {

	EntityPermissions fieldEntityPermissions;
	Collection fieldProfilePermissions;
	
	


	public EntityPermissions getEntityPermissions() {
		return fieldEntityPermissions;
	}

	public void setEntityPermissions(EntityPermissions inEntityPermissions) {
		fieldEntityPermissions = inEntityPermissions;
	}

	public Collection getProfilePermissions() {
		return fieldProfilePermissions;
	}

	public void setProfilePermissions(Collection inProfilePermissions) {
		fieldProfilePermissions = inProfilePermissions;
	}

	public Boolean can(String inKey)
	{
		
		boolean can = getProfilePermissions().contains(inKey);
		return can;
	}
	
	public Boolean can(String inEntity, String inKey)
	{
		boolean can = getEntityPermissions().can(inEntity,inKey);
		return can;
	}
	
}
