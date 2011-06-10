/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package com.openedit.util.strainer;

import com.openedit.WebPageRequest;
import com.openedit.page.Page;


/**
 * This filter only passes users that have a certain permission.
 *
 * @author Eric Galluzzo
 */
public class PagePropertyFilter extends BaseFilter 
{
	protected String fieldProperty;

	/**
	 * Construct a filter that passes all users.
	 *
	 * @see #setPermission(String)
	 */
	public PagePropertyFilter()
	{
		super();
	}

	/**
	 * Construct a filter that only passes users that have the given permission.
	 *
	 * @param inPermission The permission to check for
	 * @param inEQ DOCME
	 */
	public PagePropertyFilter(String inPermission, String inEQ)
	{
		setProperty(inPermission);
		setEquals(inEQ);
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
	 * Sets the permission to check for.
	 *
	 * @param permission The permission to check for
	 */
	public void setProperty(String permission)
	{
		fieldProperty = permission;
	}

	/**
	 * Returns the permission to check for.
	 *
	 * @return String
	 */
	public String getProperty()
	{
		return fieldProperty;
	}

	/**
	 * @see com.openedit.util.strainer.Filter#passes(java.lang.Object)
	 */
	public boolean passes(Object inObj) throws FilterException, ClassCastException
	{
		WebPageRequest req = (WebPageRequest) inObj;

		Page page = req.getPage();
		Object compare = page.get(getProperty());
		if( compare != null)
		{
			return getEquals().equalsIgnoreCase(String.valueOf(compare));
		}
		return false;
	}
	public String toString() {
		return getProperty() + " Property=" + getValue();
		
	}
	public void setProperty(String inKey, String inValue)
	{
		fieldProperty = inValue;
	}

	
}
