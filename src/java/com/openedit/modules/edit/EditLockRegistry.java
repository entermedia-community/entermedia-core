/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package com.openedit.modules.edit;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.openedit.users.User;


/**
 * This registry allows clients to claim, query, and release edit locks for paths.
 *
 * @author Eric Galluzzo
 */
public class EditLockRegistry
{
	/** Locks expire in 30 minutes. */
	protected static final long LOCK_EXPIRATION_DURATION = 30 * 60 * 1000;
	protected Map fieldPathToLockMap;

	/**
	 * Constructor for EditLockRegistry.
	 */
	public EditLockRegistry()
	{
		super();
	}

	/**
	 * Retrieve the owner of the lock for the specified path, if any.
	 *
	 * @param inPath The path for which to query the lock
	 *
	 * @return The user who has the given path locked, or <code>null</code> if none
	 */
	public User getLockOwner(String inPath)
	{
		unlockIfExpired(inPath);

		EditLock lock = getEditLock(inPath);

		return (lock == null) ? null : lock.getLockedUser();
	}

	/**
	 * Determine whether the given path is locked by any user.
	 *
	 * @param inPath The path to query
	 *
	 * @return <code>true</code> if the path is locked, <code>false</code> if not
	 */
	public boolean isLocked(String inPath)
	{
		unlockIfExpired(inPath);

		return getPathToLockMap().containsKey(inPath);
	}

	/**
	 * Determine whether the given user can lock the given path via this edit lock registry.
	 *
	 * @param inPath The path
	 * @param inUser The user
	 *
	 * @return <code>true</code> if so, <code>false</code> if not
	 */
	public boolean canLock(String inPath, User inUser)
	{
		if ((inPath == null) || (inUser == null))
		{
			return false;
		}

		unlockIfExpired(inPath);

		User owner = getLockOwner(inPath);

		boolean ok = (owner == null) || owner.equals(inUser);
		if ( !ok )
		{
			
		}
		return ok;
	}

	/**
	 * Lock the given path with the given user, regardless of who already has the path locked.
	 *
	 * @param inPath The path to lock
	 * @param inUser The user with which to lock the path
	 */
	public synchronized void forciblyLockPath(String inPath, User inUser)
	{
		forciblyLockPath(inPath, inUser, new Date());
	}

	/**
	 * Claim a lock for the given path with the given user.
	 *
	 * @param inPath The path to lock
	 * @param inUser The user with which to lock the path
	 *
	 * @throws AlreadyLockedException If the path is already locked by a different user
	 */
	public synchronized void lockPath(String inPath, User inUser)
		throws AlreadyLockedException
	{
		lockPath(inPath, inUser, new Date());
	}

	/**
	 * Release the lock for the given path, if the given user has claimed it.
	 *
	 * @param inPath The path to conditionally unlock
	 * @param inUser The user to check for
	 *
	 * @return <code>true</code> if the path was unlocked, <code>false</code> otherwise
	 */
	public synchronized boolean unlockPath(String inPath, User inUser)
	{
		unlockIfExpired(inPath);

		User oldUser = getLockOwner(inPath);

		if ((oldUser != null) && oldUser.equals(inUser))
		{
			getPathToLockMap().remove(inPath);

			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Retrieve the edit lock for the given path, if any.
	 *
	 * @param inPath The path
	 *
	 * @return The edit lock, or <code>null</code> if the given path is not locked
	 */
	protected EditLock getEditLock(String inPath)
	{
		if (inPath == null)
		{
			return null;
		}

		return (EditLock) getPathToLockMap().get(inPath);
	}

	/**
	 * Returns the map from locked paths to their {@link EditLock}s.
	 *
	 * @return Map
	 */
	protected Map getPathToLockMap()
	{
		if (fieldPathToLockMap == null)
		{
			fieldPathToLockMap = new HashMap();
		}

		return fieldPathToLockMap;
	}

	/**
	 * Lock the given path with the given user, regardless of who already has the path locked, and
	 * setting the lock creation date to the given date.
	 *
	 * @param inPath The path to lock
	 * @param inUser The user with which to lock the path
	 * @param inDate The lock creation date
	 *
	 * @throws IllegalArgumentException DOCUMENT ME!
	 */
	protected synchronized void forciblyLockPath(String inPath, User inUser, Date inDate)
	{
		if (inUser == null)
		{
			throw new IllegalArgumentException("Cannot claim an edit lock without a user");
		}

		if (inPath == null)
		{
			throw new IllegalArgumentException("Cannot claim a lock on a null path");
		}

		getPathToLockMap().put(inPath, new EditLock(inPath, inUser, inDate));
	}

	/**
	 * Claim a lock for the given path with the given user, setting the lock creation date to the
	 * given date.
	 *
	 * @param inPath The path to lock
	 * @param inUser The user with which to lock the path
	 * @param inDate The lock creation date
	 *
	 * @throws AlreadyLockedException If the path is already locked by a different user
	 * @throws IllegalArgumentException DOCUMENT ME!
	 */
	protected synchronized void lockPath(String inPath, User inUser, Date inDate)
		throws AlreadyLockedException
	{
		if (inUser == null)
		{
			throw new IllegalArgumentException("Cannot claim an edit lock without a user");
		}

		if (inPath == null)
		{
			throw new IllegalArgumentException("Cannot claim a lock on a null path");
		}

		unlockIfExpired(inPath);

		User oldUser = getLockOwner(inPath);

		if (!canLock(inPath, inUser))
		{
			throw new AlreadyLockedException(inPath, oldUser);
		}

		forciblyLockPath(inPath, inUser, inDate);
	}

	/**
	 * Remove the lock for the given path if it has expired.
	 *
	 * @param inPath The path whose lock to conditionally remove
	 */
	protected void unlockIfExpired(String inPath)
	{
		EditLock lock = getEditLock(inPath);

		if ((lock != null) && lock.isExpired())
		{
			getPathToLockMap().remove(inPath);
		}
	}

	protected class EditLock
	{
		protected Date fieldCreationDate;
		protected String fieldPath;
		protected User fieldLockedUser;

		/**
		 * Create a new edit lock.  The path and user must be filled in later.
		 */
		public EditLock()
		{
			this(null, null);
		}

		/**
		 * Create a new edit lock for the given path and user, locked at the present moment.
		 *
		 * @param inPath The path to lock
		 * @param inLockedUser The user locking the path
		 */
		public EditLock(String inPath, User inLockedUser)
		{
			this(inPath, inLockedUser, new Date());
		}

		/**
		 * Create a new edit lock for the given path and user, created at the given date.
		 *
		 * @param inPath The path to lock
		 * @param inLockedUser The user locking the path
		 * @param inCreationDate The date at which this lock was created
		 */
		public EditLock(String inPath, User inLockedUser, Date inCreationDate)
		{
			setPath(inPath);
			setCreationDate(inCreationDate);
			setLockedUser(inLockedUser);
		}

		/**
		 * Sets the date this lock was created.
		 *
		 * @param creationDate The creationDate to set
		 */
		public void setCreationDate(Date creationDate)
		{
			fieldCreationDate = creationDate;
		}

		/**
		 * Returns the date this lock was created.
		 *
		 * @return Date
		 */
		public Date getCreationDate()
		{
			return fieldCreationDate;
		}

		/**
		 * Determine whether this lock has expired.
		 *
		 * @return <code>true</code> if it has expired, <code>false</code> otherwise
		 */
		public boolean isExpired()
		{
			return (new Date().getTime() - getCreationDate().getTime()) >= LOCK_EXPIRATION_DURATION;
		}

		/**
		 * Sets the user that has the path locked.
		 *
		 * @param lockedUser The user to set
		 */
		public void setLockedUser(User lockedUser)
		{
			fieldLockedUser = lockedUser;
		}

		/**
		 * Returns the user that has the path locked.
		 *
		 * @return User
		 */
		public User getLockedUser()
		{
			return fieldLockedUser;
		}

		/**
		 * Sets the locked path.
		 *
		 * @param path The path to set
		 */
		public void setPath(String path)
		{
			fieldPath = path;
		}

		/**
		 * Returns the locked path.
		 *
		 * @return String
		 */
		public String getPath()
		{
			return fieldPath;
		}
	}
}
