/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package com.openedit.users;

import java.util.Collection;
import java.util.Date;

import org.openedit.Data;


/**
 * This interface represents a group, which may have certain permissions.
 *
 * @author Eric Galluzzo
 */
public interface Group extends PropertyContainer, Data
{
	
	/**
	 * Not a real property, just a string that can be used to refer to the
	 * username in, e.g., search indices.
	 */
	public static final String GROUPNAME_PROPERTY = "name";
	public static final String GROUPID_PROPERTY = "id";

	/**
	 * The name of this group.
	 *
	 * @return The group name.
	 */
	String getName();

	void setName(String inName);

	String getId();

	void setId(String inId);

	/**
	 * Retrieve the date/time at which this group was created.
	 * 
	 * @return  The creation date
	 */
	Date getCreationDate();

	/**
	 * Retrieve all the names of permissions that this group grants.
	 *
	 * @return A collection of <code>String</code>s
	 */
	Collection getPermissions();
	void setPermissions(Collection inPermissions);

	/**
	 * Add the given permission to this group.  If the permission is already part of this group,
	 * this method does nothing.
	 *
	 * @param inPermission The name of the permission to add
	 *
	 * @throws UserManagerException If the permission could not be added
	 */
	void addPermission(String inPermission) throws UserManagerException;

	
	/**
	 * Determine whether this group has the given permission.
	 *
	 * @param inPermission The name of the permission
	 *
	 * @return <code>true</code> if so, <code>false</code> if not
	 */
	boolean hasPermission(String inPermission);

	/**
	 * Remove the given permission from this group.  If the permission is not part of this group,
	 * this method does nothing.
	 *
	 * @param inPermission The name of the permission to remove
	 *
	 * @throws UserManagerException If the permission could not be removed
	 */
	void removePermission(String inPermission) throws UserManagerException;

	public PropertyContainer getPropertyContainer();
	
	public long getLastModified();
	

}
