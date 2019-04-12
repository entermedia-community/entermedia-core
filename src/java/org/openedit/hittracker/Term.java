package org.openedit.hittracker;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
	protected Object fieldData;
	protected boolean fieldUserFilter;
	
	public boolean isUserFilter()
	{
		return fieldUserFilter;
	}

	public void setUserFilter(boolean inUserFilter)
	{
		fieldUserFilter = inUserFilter;
	}

	public Object getData()
	{
		return fieldData;
	}

	public void setData(Object inData)
	{
		fieldData = inData;
	}

	public Object[] getValues()
	{
//		if( fieldValues == null && fieldValue != null)
//		{
//			return new Object[] { getValue() };
//		}
		return fieldValues;
	}
	public Collection getValueCollection()
	{
		if( fieldValues != null)
		{
			return Arrays.asList(fieldValues);
		}
		if( fieldValue != null)
		{
			return Arrays.asList(fieldValue);
		}
		return Collections.EMPTY_LIST;
	}
	public void setValues(Object[] inValues)
	{
		fieldValues = inValues;
	}

	public boolean containsValue(String inValue)
	{
		if( inValue.equals( fieldValue) )
		{
			return true;
		}
		if( getValues() != null)
		{
			for (int i = 0; i < getValues().length; i++)
			{
				if( inValue.equals( getValues()[i]) )
				{
					return true;
				}
			}
		}
		return false;
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
		if( fieldOperation == null)
		{
			return (String)getParameters().get("op");
		}
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
			term.addAttribute(key, (String) getValue(key));
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

	public Object getValue(String inKey)
	{
		Object val =  getParameters().get(inKey);
		if( val == null && "op".equals(inKey))
		{
			val = getOperation();
		}
		return val;
	}

	public void addValue(String inKey, Object value)
	{
		getParameters().put(inKey, value);
	}
	
	public String toString()
	{
		return getId() + " = '" + getValue() + "'";
	}
}
