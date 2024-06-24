package org.openedit.servlet;

import java.util.HashMap;
import java.util.Map;

import org.openedit.Data;
import org.openedit.page.Page;

public class RightPage
{
	
	Map fieldPageValues;
	
	public Map getPageValues()
	{
		if (fieldPageValues == null)
		{
			fieldPageValues = new HashMap();
		}

		return fieldPageValues;
	}
	public void setPageValues(Map inPageValues)
	{
		fieldPageValues = inPageValues;
	}
	protected Page fieldRightPage;
	public Page getRightPage()
	{
		return fieldRightPage;
	}
	public void setRightPage(Page inRightPage)
	{
		fieldRightPage = inRightPage;
	}
	public Map<String,Object> getParams()
	{
		if (fieldParams == null)
		{
			fieldParams = new HashMap();
		}

		return fieldParams;
	}
	public void setParams(Map inParams)
	{
		fieldParams = inParams;
	}
	protected Map fieldParams;
	public void putParam(String inString, Object inId)
	{
		getParams().put(inString,inId);
	}
	public void putPageValue(String inString, Object inFirst)
	{
		getPageValues().put(inString,inFirst);
	}
	
	
}
