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

import org.openedit.WebPageRequest;
import org.openedit.users.User;

/**
 * Uses properties on the logged in user 
 * @author imiller
 */

public class UserPropertyFilter extends BaseFilter
{
	protected String fieldPropertyName;
	
	public UserPropertyFilter()
	{
		super();
	}
	public UserPropertyFilter(String inPropertyName, String inValue)
	{
		setPropertyName(inPropertyName);
		setValue(inValue);
	}

	/**
	 * @see org.openedit.util.strainer.Filter#passes(java.lang.Object)
	 */
	public boolean passes(Object inObj) throws FilterException, ClassCastException
	{
		WebPageRequest req = (WebPageRequest) inObj;

		User data = (User)req.getPageValue("user");

		if (data == null)
		{
			return false;
		}
		String value = data.get(getPropertyName());
		if( value == null && getValue() == null)
		{
			return true;
		}
		if(value == null){
			return true;
		}
		String[] values = value.split(" ");
		for (String string : values)
		{		
			if( value != null && string.equals(getValue()))
			{
				return true;
			}
		}
		return false;
	}
	public String toString()
	{
		return "UserProperty" + getPropertyName() + "="+ getValue();
	}
	public String getPropertyName()
	{
		return fieldPropertyName;
	}
	public void setPropertyName(String inPropertyName)
	{
		fieldPropertyName = inPropertyName;
	}
}
