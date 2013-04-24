package org.openedit.xml;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.dom4j.Attribute;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.openedit.MultiValued;

import com.openedit.OpenEditException;

public class ElementData implements MultiValued, Comparable
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
		//TODO: Deal with XML in the value if XML addCData
		if( inId.equals("name"))
		{
			
			getElement().setText(inValue);
		}
		else
		{
			synchronized (getElement())
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
		//all.put("name", getName()); 
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
		if(name == null){
			name = getElement().getText();
		}
		
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
	public void setProperties(Map<String,String> inProperties)
	{
		//getProperties().putAll(inProperties);
		for (Iterator iterator = inProperties.keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			if( key != null)
			{
				setProperty(key,inProperties.get(key));
			}
		}
	}
	
	
	public Collection<String> getValues(String inPreference)
	{
		String val = get(inPreference);
		
		if (val == null)
			return null;
		
		String[] vals = null;
		if( val.contains("|") )
		{
			vals = VALUEDELMITER.split(val);
		}
		else
		{
			vals = val.split("\\s+"); //legacy
		}

		Collection collection = Arrays.asList(vals);
		//if null check parent
		return collection;
	}
	
	public void setValues(String inKey, Collection<String> inValues)
	{
		StringBuffer values = new StringBuffer();
		for (Iterator iterator = inValues.iterator(); iterator.hasNext();)
		{
			String detail = (String) iterator.next();
			values.append(detail);
			if( iterator.hasNext())
			{
				values.append(" | ");
			}
		}
		setProperty(inKey,values.toString());
	}
	public Object clone()
	{
		try
		{
			return super.clone();
		}
		catch ( Exception ex )
		{
			throw new OpenEditException(ex);
		}
	}
}
