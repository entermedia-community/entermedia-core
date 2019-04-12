/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package org.openedit.util.strainer;

import org.openedit.WebPageRequest;
import org.openedit.profile.UserProfile;


/**
 * This filter only passes users who are members of a certain group.
 *
 */
public class SettingsGroupFilter extends BaseFilter
{

	/**
	 * Construct a filter that passes all users.
	 *
	 * @see #setGroupName(String)
	 */
	public SettingsGroupFilter()
	{
		super();
	}

	/**
	 * Construct a filter that only passes users that are part of the group with the given name.
	 *
	 * @param inGroupName The group name to check for
	 */
	public SettingsGroupFilter(String inGroupId)
	{
		setGroupId(inGroupId);
	}

	/**
	 * Sets the group name.
	 *
	 * @param groupName The group name to set
	 */
	public void setGroupId(String groupId)
	{
		setValue(groupId);
	}

	/**
	 * Returns the group name.
	 *
	 * @return String
	 */
	public String getGroupId()
	{
		return fieldValue;
	}

	/**
	 * @see org.openedit.util.strainer.Filter#passes(java.lang.Object)
	 */
	public boolean passes(Object inObj) throws FilterException, ClassCastException
	{
		WebPageRequest req = (WebPageRequest) inObj;

		UserProfile profile = req.getUserProfile();

		if (profile == null || profile.getSettingsGroup() == null)
		{
			return false;
		}

		if (getGroupId() == null)
		{
			return true;
		}
		String id = profile.getSettingsGroup().getId();
		if (id.equalsIgnoreCase(getGroupId()))
		{
				return true;
		}

		return false;
	}
	public String toString()
	{
		return "SettingsGroup= " + getGroupId();
	}
	public boolean equals(Object inObj)
	{
		if (inObj instanceof SettingsGroupFilter)
		{
			SettingsGroupFilter toCompare = (SettingsGroupFilter)inObj;
			return getGroupId().equals(toCompare.getGroupId());
		}
		return false;
	}
}
