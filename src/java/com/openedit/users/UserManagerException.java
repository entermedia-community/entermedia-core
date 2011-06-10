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

import com.openedit.OpenEditRuntimeException;

/**
 * This exception is thrown from {@link UserManager}.
 *
 * @author Eric and Matt
 */
public class UserManagerException extends OpenEditRuntimeException
{
	public UserManagerException()
	{
		super();
	}

	public UserManagerException(String message)
	{
		super(message);
	}

	public UserManagerException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public UserManagerException(Throwable cause)
	{
		super(cause);
	}
}
