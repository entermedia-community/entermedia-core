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

/**
 * This exception is thrown when an attempt is made to add a property to a user using an invalid
 * property name.
 *
 * @author Eric Galluzzo
 *
 * @see User#put(String, Object)
 */
public class InvalidPropertyNameException extends UserManagerException
{
	public InvalidPropertyNameException()
	{
		super();
	}

	public InvalidPropertyNameException(String message)
	{
		super(message);
	}

	public InvalidPropertyNameException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public InvalidPropertyNameException(Throwable cause)
	{
		super(cause);
	}
}
