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

import javax.servlet.ServletRequest;

import org.openedit.WebPageRequest;


/**
 * This filter only passes users that have a certain permission.
 *
 * @author Eric Galluzzo
 */
public class RequestAttributeFilter extends BaseFilter
{
	protected String fieldAttribute;

	/**
	 * Construct a filter that passes all users.
	 *
	 * @see #setPermission(String)
	 */
	public RequestAttributeFilter()
	{
		super();
	}

	/**
	 * Construct a filter that only passes users that have the given permission.
	 *
	 * @param inPermission The permission to check for
	 * @param inEq DOCME
	 */
	public RequestAttributeFilter(String inPermission, String inEq)
	{
		setAttribute(inPermission);
		setEquals(inEq);
	}

	/**
	 * Sets the permission to check for.
	 *
	 * @param permission The permission to check for
	 */
	public void setAttribute(String permission)
	{
		fieldAttribute = permission;
	}

	/**
	 * Returns the permission to check for.
	 *
	 * @return String
	 */
	public String getAttribute()
	{
		return fieldAttribute;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param inString
	 */
	public void setEquals(String inString)
	{
		setValue(inString);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	public String getEquals()
	{
		return fieldValue;
	}

	/**
	 * @see org.openedit.util.strainer.Filter#passes(java.lang.Object)
	 */
	public boolean passes(Object inObj) throws FilterException, ClassCastException
	{
		WebPageRequest req = (WebPageRequest) inObj;
		ServletRequest request = req.getRequest();

		if (request == null)
		{
			return false;
		}

		String att = (String) request.getAttribute(getAttribute());

		return (getAttribute() == null) || getEquals().equalsIgnoreCase(att);
	}

	public String toString() 
	{
		return getAttribute() + " Parameter=" + getEquals();
	}
}
