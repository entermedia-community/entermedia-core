/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package org.openedit;



/**
 * DOCUMENT ME!
 *
 * @author cburkey To change this generated comment edit the template variable "typecomment":
 * 		   Window>Preferences>Java>Templates. To enable and disable the creation of type comments
 * 		   go to Window>Preferences>Java>Code Generation.
 */
public class OpenEditRuntimeException extends RuntimeException
{

	private static final long serialVersionUID = -2930548912472035638L;

	/**
	 * Constructor for OpenEditRuntimeException.
	 */
	public OpenEditRuntimeException()
	{
		super();
	}

	/**
	 * Constructor for OpenEditRuntimeException.
	 *
	 * @param message
	 */
	public OpenEditRuntimeException(String message)
	{
		super(message);
	}

	/**
	 * Constructor for OpenEditRuntimeException.
	 *
	 * @param message
	 * @param t
	 */
	public OpenEditRuntimeException(String message, Throwable t)
	{
		super(message, t);
	}

	/**
	 * Constructor for OpenEditRuntimeException.
	 *
	 * @param t
	 */
	public OpenEditRuntimeException(Throwable t)
	{
		super(t);
	}
}
