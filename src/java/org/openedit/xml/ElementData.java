package org.openedit.xml;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Attribute;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Text;
import org.openedit.MultiValued;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;

import com.openedit.OpenEditException;

public class ElementData implements MultiValued, Comparable
{
	protected Element fieldElement;
	protected String fieldSourcePath;
	protected String fieldVersion;
	protected static final Pattern INVALIDSTUFF = Pattern.compile("[\'\"\n<>&]");  
	
	public String getVersion() {
		return fieldVersion;
	}
	public void setVersion(String inVersion) {
		fieldVersion = inVersion;
	}
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

	/**
	 * First we try the child node in case we have CDATA
	 * Then we try the attributes
	 */
	public String get(String inId)
	{
		Element child = getElement().element(inId);
		if( child != null)
		{
			return child.getText();
		}
		
		if( inId.equals("name"))
		{
			String name = getElement().attributeValue(inId);
			if( name == null)
			{
				name =getElement().getText();
			}
			return name;
		} else if(inId.equals(".version")){
			return getVersion();//elastic search
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
		setProperty("name",inName);
	}
	public void setId(String inNewid)
	{
		getElement().addAttribute("id",inNewid);
		
	}
	public void setProperty(String inId, String inValue)
	{
		if(inId.equals(".version"))
		{
			setVersion(inValue);
		}
		else
		{
			//synchronized (getElement())  //TODO: Remove this now that we have proper locking?
			//{
				if( inId.equals("name"))
				{
					for (Iterator iterator = getElement().nodeIterator(); iterator.hasNext();)
					{
						Node type = (Node) iterator.next();
						if( type instanceof Text)
						{
							getElement().remove(type);
							break;
						}
					}
				}
				//always check for a child
				Element child = getElement().element(inId);
				if( child != null)
				{
					//TODO: See if value changed?
					getElement().remove(child);
				}
				
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
					if( INVALIDSTUFF.matcher(inValue).find() )
					{
						Attribute attr = getElement().attribute(inId);
						if( attr != null)
						{
							getElement().remove(attr);
						}
						
						getElement().addElement(inId).addCDATA(inValue);
					}
					else
					{
						getElement().addAttribute(inId,inValue);						
					}
				}
			//}
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
		//all.put("name", getName()); //would this cause problems when saving?
		for (Iterator iterator = getAttributes().iterator(); iterator.hasNext();)
		{
			org.dom4j.Attribute attr = (org.dom4j.Attribute) iterator.next();
			all.put(attr.getName(),attr.getValue() );
		}
		for (Iterator iterator = getElement().elementIterator(); iterator.hasNext();)
		{
			Element child = (Element) iterator.next();
			all.put(child.getName(),child.getText());
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
