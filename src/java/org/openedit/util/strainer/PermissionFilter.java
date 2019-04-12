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
import org.openedit.users.User;


/**
 * This filter only passes users that have a certain permission.
 *
 * @author Eric Galluzzo
 */
public class PermissionFilter extends BaseFilter
{
	/**
	 * Construct a filter that passes all users.
	 *
	 * @see #setPermission(String)
	 */
	public PermissionFilter()
	{
		super();
	}

	/**
	 * Construct a filter that only passes users that have the given permission.
	 *
	 * @param inPermission The permission to check for
	 */
	public PermissionFilter(String inPermission)
	{
		setPermission(inPermission);
	}

	/**
	 * Sets the permission to check for.
	 *
	 * @param permission The permission to check for
	 */
	public void setPermission(String permission)
	{
		setValue(permission);
	}

	/**
	 * Returns the permission to check for.
	 *
	 * @return String
	 */
	public String getPermission()
	{
		return fieldValue;
	}

	/**
	 * @see org.openedit.util.strainer.Filter#passes(java.lang.Object)
	 */
	public boolean passes(Object inObj) throws FilterException, ClassCastException
	{
		WebPageRequest req = (WebPageRequest) inObj;

		User user = req.getUser();

		return ((user != null) &&
		((getPermission() == null) || user.hasPermission(getPermission())));
	}
	public String toString() 
	{
		return "Permission=" + getPermission();
	}
	
}
