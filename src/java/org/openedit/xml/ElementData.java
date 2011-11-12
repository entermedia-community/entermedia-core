package org.openedit.xml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.openedit.Data;

public class ElementData implements Data, Comparable
{
	protected Element fieldElement;
	protected String fieldSourcePath;
	
	public ElementData()
	{
	}
	public ElementData(Element inEl)
	{
		setElement(inEl);
	}
	public ElementData(Object inEl)
	{
		setElement((Element)inEl);
	}
	
	public Element getElement()
	{
		if (fieldElement == null)
		{
			fieldElement = DocumentHelper.createElement("element");
		}
		return fieldElement;
	}

	public void setElement(Element inElement)
	{
		fieldElement = inElement;
	}
	
	public String get(String inId)
	{
		if( inId.equals("name"))
		{
			String name =getElement().getText();
			if(name != null && name.length() >0){
				return name;
			} else{
				return getElement().attributeValue(inId);
			}
		}
		else if( inId.equals("sourcepath"))
		{
			return getSourcePath();
		}
		return getElement().attributeValue(inId);
	}
	
	public float getFloat(String inId)
	{
		String val = get(inId);
		if( val != null)
		{
			return Float.parseFloat(val);
		}
		return 0;
	}
	
	public String getId()
	{
		return getElement().attributeValue("id");
	}
	public String getName()
	{
		return toString();
	}
	public void setName(String inName)
	{
		getElement().setText(inName);
	}
	public void setId(String inNewid)
	{
		getElement().addAttribute("id",inNewid);
		
	}
	public void setProperty(String inId, String inValue)
	{
		if( inId.equals("name"))
		{
			getElement().setText(inValue);
		}
		else
		{
			if( inValue == null || inValue.length() == 0)
			{
				Attribute attr = getElement().attribute(inId);
				if( attr != null)
				{
					getElement().remove(attr);
				}
			}
			else
			{
				getElement().addAttribute(inId,inValue);
			}
		}
	}
	public String getSourcePath()
	{
		return fieldSourcePath;
	}
	public void setSourcePath(String inSourcepath)
	{
		fieldSourcePath = inSourcepath;
	}
	public Map getProperties() 
	{
		Map all = new HashMap();
		for (Iterator iterator = getAttributes().iterator(); iterator.hasNext();)
		{
			org.dom4j.Attribute attr = (org.dom4j.Attribute) iterator.next();
			all.put(attr.getName(),attr.getValue() );
		}
		return all;
	}
	public List getAttributes()
	{
		return getElement().attributes();
	}
	public int compareTo(Object inO)
	{
		return toString().compareTo(inO.toString());
	}
	public String toString()
	{
		String name =  get("name");
		if( name == null)
		{
			name = getId();
		}
		if( name == null)
		{
			name = super.toString();
		}
		return name;
	}
	
}
