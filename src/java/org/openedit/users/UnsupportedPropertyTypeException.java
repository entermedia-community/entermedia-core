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
 * This exception is thrown whenever one attempts to set a property of an unsupported type on a
 * {@link User}.
 *
 * @author Eric Galluzzo
 */
public class UnsupportedPropertyTypeException extends UserManagerException
{
	/**
	 * Constructor for UnsupportedPropertyTypeException.
	 */
	public UnsupportedPropertyTypeException()
	{
		super();
	}

	/**
	 * Constructor for UnsupportedPropertyTypeException.
	 *
	 * @param message
	 */
	public UnsupportedPropertyTypeException(String message)
	{
		super(message);
	}

	/**
	 * Constructor for UnsupportedPropertyTypeException.
	 *
	 * @param message
	 * @param cause
	 */
	public UnsupportedPropertyTypeException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Constructor for UnsupportedPropertyTypeException.
	 *
	 * @param cause
	 */
	public UnsupportedPropertyTypeException(Throwable cause)
	{
		super(cause);
	}
}
