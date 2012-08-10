package com.openedit.hittracker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.openedit.data.PropertyDetail;

abstract public class Term {
	protected String fieldId;
	protected PropertyDetail fieldDetail;
	protected String fieldOperation;
	protected String fieldValue;
	protected Object[] fieldValues;
	
	public Object[] getValues()
	{
		return fieldValues;
	}

	public void setValues(Object[] inValues)
	{
		fieldValues = inValues;
	}

	protected Map fieldParameters;

	public Map getParameters()
	{
		if (fieldParameters == null)
		{
			fieldParameters = new HashMap();
		}
		return fieldParameters;
	}

	public void setParameters(Map inParameters)
	{
		fieldParameters = inParameters;
	}

	public String getId()
	{
		if( fieldId == null && getDetail() != null)
		{
			return getDetail().getId();
		}
		return fieldId;
	}

	public void setId(String inId)
	{
		fieldId = inId;
	}

	public String getOperation()
	{
		return fieldOperation;
	}

	public void setOperation(String inOperation)
	{
		fieldOperation = inOperation;
	}

	public String getValue()
	{
		return fieldValue;
	}

	public void setValue(String inValue)
	{
		fieldValue = inValue;
	}

	abstract public String toQuery();
	public Element toXml()
	{
		Element term = DocumentHelper.createElement("term");
		term.addAttribute("id", getDetail().getId());
		term.addAttribute("val", getValue());
		term.addAttribute("op", getOperation());
		for (Iterator iterator = getParameters().keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			term.addAttribute(key, getParameter(key));
		}
//		if (getParameter("op") != null)
//			term.addAttribute("realop", getParameter("op"));
		
		return term;
	}	
	
	public PropertyDetail getDetail()
	{
		return fieldDetail;
	}

	public void setDetail(PropertyDetail inDetail)
	{
		fieldDetail = inDetail;
		//fieldId = inDetail.getId() + "_" + System.currentTimeMillis();
	}

	public void setFieldParameters(Map fieldParameters)
	{
		this.fieldParameters = fieldParameters;
	}

	public String getParameter(String inKey)
	{
		String val = (String) getParameters().get(inKey);
		if( val == null && "op".equals(inKey))
		{
			val = getOperation();
		}
		return val;
	}

	public void addParameter(String inKey, String value)
	{
		getParameters().put(inKey, value);
	}
	
	public String toString()
	{
		return getId() + " = '" + getValue() + "'";
	}
}
