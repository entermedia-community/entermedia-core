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

import org.openedit.Data;

import com.openedit.WebPageRequest;

/**
 * Looks for a variable called $data for a property name with a certain value
 * @author cburkey
 */

public class DataPropertyFilter extends BaseFilter
{
	protected String fieldPropertyName;
	
	public DataPropertyFilter()
	{
		super();
	}
	public DataPropertyFilter(String inPropertyName, String inValue)
	{
		setPropertyName(inPropertyName);
		setValue(inValue);
	}

	/**
	 * @see com.openedit.util.strainer.Filter#passes(java.lang.Object)
	 */
	public boolean passes(Object inObj) throws FilterException, ClassCastException
	{
		WebPageRequest req = (WebPageRequest) inObj;

		Data data = (Data)req.getPageValue("data");

		if (data == null)
		{
			return false;
		}
		String value = data.get(getPropertyName());
		if( value == null && getValue() == null)
		{
			return true;
		}
		if( value != null && value.equals(getValue()))
		{
			return true;
		}
		return false;
	}
	public String toString()
	{
		return "Data" + getPropertyName() + "="+ getValue();
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
