/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package com.openedit.util.strainer;

import java.util.Iterator;

import com.openedit.WebPageRequest;
import com.openedit.users.Group;
import com.openedit.users.User;


/**
 * This filter only passes users who are members of a certain group.
 *
 * @author Eric Galluzzo
 */
public class GroupFilter extends BaseFilter
{

	/**
	 * Construct a filter that passes all users.
	 *
	 * @see #setGroupName(String)
	 */
	public GroupFilter()
	{
		super();
	}

	/**
	 * Construct a filter that only passes users that are part of the group with the given name.
	 *
	 * @param inGroupName The group name to check for
	 */
	public GroupFilter(String inGroupId)
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
	 * @see com.openedit.util.strainer.Filter#passes(java.lang.Object)
	 */
	public boolean passes(Object inObj) throws FilterException, ClassCastException
	{
		WebPageRequest req = (WebPageRequest) inObj;

		User user = req.getUser();

		if (user == null)
		{
			return false;
		}

		if (getGroupId() == null)
		{
			return true;
		}

		for (Iterator iter = user.getGroups().iterator(); iter.hasNext();)
		{
			Group group = (Group) iter.next();

			if (group.getId().equalsIgnoreCase(getGroupId()))
			{
				return true;
			}
		}

		return false;
	}
	public String toString()
	{
		return "Group= " + getGroupId();
	}
	public boolean equals(Object inObj)
	{
		if (inObj instanceof GroupFilter)
		{
			GroupFilter toCompare = (GroupFilter)inObj;
			return getGroupId().equals(toCompare.getGroupId());
		}
		return false;
	}
}
