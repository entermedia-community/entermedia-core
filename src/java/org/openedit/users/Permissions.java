package org.openedit.users;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.openedit.data.EntityPermissions;
import org.openedit.profile.UserProfile;

public class Permissions {

	protected UserProfile fieldUserProfile;
	protected Set fieldSettingsGroupPermissions;
	
	public Set getSettingsGroupPermissions()
	{
		return fieldSettingsGroupPermissions;
	}


	public void setSettingsGroupPermissions(Set inSettingsGroupPermissions)
	{
		fieldSettingsGroupPermissions = inSettingsGroupPermissions;
	}


	public UserProfile getUserProfile()
	{
		return fieldUserProfile;
	}


	public void setUserProfile(UserProfile inUserProfile)
	{
		fieldUserProfile = inUserProfile;
		if( fieldUserProfile.getSettingsGroup() != null)
		{
			Collection permissions = fieldUserProfile.getSettingsGroup().getValues("permissions");
			if (permissions != null)
			{
				setSettingsGroupPermissions( new HashSet(permissions) );
			}
			else
			{
				setSettingsGroupPermissions( new HashSet() );
			}
		}
		else
		{
			setSettingsGroupPermissions( new HashSet() );
		}
	}


	public Permissions(UserProfile inProfile)
	{
		setUserProfile(inProfile)	;
	}


	public EntityPermissions getEntityPermissions() {
		return getUserProfile().getEntityPermissions();
	}


	public Boolean can(String inKey)
	{
		boolean can = getSettingsGroupPermissions().contains(inKey);
		
		return can;
	}
	
	public Boolean can(String inModuleId, String inKey)
	{
		boolean can = getEntityPermissions().can(inModuleId,inKey);

		return can;
	}

	public Boolean can(String inModuleId, boolean isDataOwner, String inKey)
	{
		if( isDataOwner )
		{
			boolean can = can("owner" + inModuleId,inKey);
			if( can )
			{
				return true;
			}
		}	
		boolean can = can(inModuleId,inKey);
		return can;
	}


	
}
