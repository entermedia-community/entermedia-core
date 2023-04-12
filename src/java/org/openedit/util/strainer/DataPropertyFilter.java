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

import java.util.HashMap;
import java.util.Map;

import org.openedit.Data;
import org.openedit.WebPageRequest;
import org.openedit.data.SearcherManager;
import org.openedit.util.Replacer;

/**
 * Looks for a variable called $data for a property name with a certain value
 * @author cburkey
 */

public class DataPropertyFilter extends BaseFilter
{
	protected String fieldBeanName;
	public String getBeanName()
	{
		if( fieldBeanName == null)
		{
			return "data";
		}
		return fieldBeanName;
	}
	public void setBeanName(String inBeanName)
	{
		fieldBeanName = inBeanName;
	}
	protected SearcherManager fieldSearcherManager;
	
	public SearcherManager getSearcherManager()
	{
		return fieldSearcherManager;
	}
	public void setSearcherManager(SearcherManager inSearcherManager)
	{
		fieldSearcherManager = inSearcherManager;
	}
	public DataPropertyFilter()
	{
		super();
	}
	public DataPropertyFilter(String inPropertyName, String inValue)
	{
		setProperty("property", inPropertyName);
		setValue(inValue);
	}

	/**
	 * @see org.openedit.util.strainer.Filter#passes(java.lang.Object)
	 */
	public boolean passes(Object inObj) throws FilterException, ClassCastException
	{
		WebPageRequest req = (WebPageRequest) inObj;

		Data data = (Data)req.getPageValue(getBeanName());

		if (data == null)
		{
			return false;
		}
		String value = data.get(getPropertyName());
		if( value == null && getValue() == null)
		{
			return true;
		}
		
		String resolvedvalue = getValue();
		//${context.getUserName()}
		String catalogid = req.findPathValue("catalogid");
		Replacer replacer = getSearcherManager().getReplacer(catalogid);
		Map params = new HashMap();
		params.put("context", req);
		params.putAll(req.getPageMap());
		resolvedvalue = replacer.replace(resolvedvalue, params);
		if( value != null && value.equals(resolvedvalue))
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
		return get("property");
	}
	public void setPropertyName(String inPropertyName)
	{
		setProperty("property", inPropertyName);
	}
}
