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
import com.openedit.util.PathUtilities;


/**
 * This filter only passes users that have a certain permission.
 *
 * @author Eric Galluzzo
 */
public class PathFilter extends BaseFilter
{

	/**
	 * Construct a filter that passes all users.
	 *
	 * @see #setPermission(String)
	 */
	public PathFilter()
	{
		super();
	}

	/**
	 * Construct a filter that only passes users that have the given permission.
	 *
	 * @param inPermission The permission to check for
	 */
	public PathFilter(String inPermission)
	{
		setPath(inPermission);
	}

	/**
	 * Sets the permission to check for.
	 *
	 * @param inValue The permission to check for
	 */
	public void setPath(String inPath)
	{
		setValue(inPath);
	}

	/**
	 * Returns the permission to check for.
	 *
	 * @return String
	 */
	public String getPath()
	{
		return fieldValue;
	}

	/**
	 * @see com.openedit.util.strainer.Filter#passes(java.lang.Object)
	 */
	public boolean passes(Object inObj) throws FilterException, ClassCastException
	{
		WebPageRequest req = (WebPageRequest) inObj;
		String path = req.getContentPage().replaceProperty(getPath());
		boolean ok = PathUtilities.match(req.getPage().getPath(), path);
		return ok;
		//return (getPath() == null) || .startsWith(getPath());
	}

	public String toString() 
	{
		return "Path=" + getPath();
	}
}
