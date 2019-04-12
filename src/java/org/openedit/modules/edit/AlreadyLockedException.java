/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package org.openedit.modules.edit;

import org.openedit.OpenEditException;
import org.openedit.users.User;


/**
 * This exception indicates that a client attempted to claim a lock that was already claimed by a
 * different user.
 *
 * @author Eric Galluzzo
 */
public class AlreadyLockedException extends OpenEditException
{
	protected String fieldPath;
	protected User fieldLockOwner;

	/**
	 * Create a new exception denoting that the given path is already locked by the given user.
	 *
	 * @param inPath The path that the client attempted to lock
	 * @param inUser The user that has the path already locked
	 */
	public AlreadyLockedException(String inPath, User inUser)
	{
		super("Path \"" + inPath + " \" is already locked by " + inUser.getUserName());
		setPath(inPath);
		setLockOwner(inUser);
	}

	/**
	 * Returns the owner of the existing lock.
	 *
	 * @return User
	 */
	public User getLockOwner()
	{
		return fieldLockOwner;
	}

	/**
	 * Returns the path.
	 *
	 * @return String
	 */
	public String getPath()
	{
		return fieldPath;
	}

	/**
	 * Sets the owner of the existing lock.
	 *
	 * @param lockOwner The {@link User} to set
	 */
	protected void setLockOwner(User lockOwner)
	{
		fieldLockOwner = lockOwner;
	}

	/**
	 * Sets the path.
	 *
	 * @param path The path to set
	 */
	protected void setPath(String path)
	{
		fieldPath = path;
	}
}
