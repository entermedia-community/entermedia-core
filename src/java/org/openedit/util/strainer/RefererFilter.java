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

import javax.servlet.http.HttpServletRequest;

import org.openedit.WebPageRequest;
import org.openedit.util.PathUtilities;


/**
 * This filter only passes users that have a certain permission.
 *
 */
public class RefererFilter extends BaseFilter
{
	public RefererFilter()
	{
		super();
	}
	public RefererFilter(String inURl)
	{
		setValue(inURl);
	}

	public void setUrlMatches(String inUrlMatches)
	{
		setValue(inUrlMatches);
	}

	/**
	 * @see org.openedit.util.strainer.Filter#passes(java.lang.Object)
	 */
	public boolean passes(Object inObj) throws FilterException, ClassCastException
	{
		WebPageRequest req = (WebPageRequest) inObj;
		HttpServletRequest request = req.getRequest();

		if (request == null)
		{
			return false;
		}
		String att = (String) request.getHeader("Referer");
		if( att != null )
		{
			if( PathUtilities.match(att, getValue() ) )
			{
				return true;
			}
		}
		return false;
	}

	public String toString() 
	{
		return " Referer=" + getValue();
	}
}
