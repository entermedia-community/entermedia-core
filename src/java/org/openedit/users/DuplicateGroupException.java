/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package org.openedit.users;

/**
 * This exception is thrown when one attempts to create a group with a name that is already taken
 * by an existing group.
 *
 * @author Eric Galluzzo
 */
public class DuplicateGroupException extends UserManagerException
{
	public DuplicateGroupException()
	{
		super();
	}

	public DuplicateGroupException(String message)
	{
		super(message);
	}

	public DuplicateGroupException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public DuplicateGroupException(Throwable cause)
	{
		super(cause);
	}
}
