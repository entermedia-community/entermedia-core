package org.openedit.xml;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.dom4j.Attribute;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Text;
import org.dom4j.tree.DefaultText;
import org.openedit.MultiValued;
import org.openedit.OpenEditException;
import org.openedit.data.PropertyDetail;
import org.openedit.data.PropertyDetails;
import org.openedit.data.SaveableData;
import org.openedit.data.SearchData;
import org.openedit.modules.translations.LanguageMap;

public class ElementData implements MultiValued, SaveableData, Comparable ,SearchData
{
	protected Element fieldElement;
	protected String fieldSourcePath;
	protected String fieldVersion;
	protected static final Pattern INVALIDSTUFF = Pattern.compile("[\'\"\n<>&]");  
	protected PropertyDetails fieldPropertyDetails;
	
	public ElementData(Element inHit, PropertyDetails inPropertyDetails)
	{
		setElement(inHit);
		//setId(getSearchHit().getId());
		setPropertyDetails(inPropertyDetails);
	}
	
	
	public ElementData(Object inHit, PropertyDetails inPropertyDetails)
	{
		setElement((Element)inHit);
		//setId(getSearchHit().getId());
		setPropertyDetails(inPropertyDetails);
	}
	
	public PropertyDetails getPropertyDetails()
	{
		return fieldPropertyDetails;
	}

	public void setPropertyDetails(PropertyDetails inPropertyDetails)
	{
		fieldPropertyDetails = inPropertyDetails;
	}
	
	
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
	@Override
	public String get(String inId)
	{
		Object obj = getValue(inId);
		if( obj == null)
		{
			return null;
		}
		return String.valueOf(obj);
	}

	/**
	 * First we try the child node in case we have CDATA
	 * Then we try the attributes
	 */
	public Object getValue(String inId)
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
				LanguageMap map = new LanguageMap();
				for (Iterator iterator = getElement().nodeIterator(); iterator.hasNext();)
				{
					Object object = (Object) iterator.next();
					if( object instanceof DefaultText)
					{
						name = getElement().getText();
						return name;
					}
					Element childlang = (Element)object;
					map.put(childlang.attributeValue("id"),getElement().getText());
				}
				return map;
			}
			return name;
		} else if(inId.equals(".version")){
			return getVersion();//elastic search
		}
		else if( inId.equals("sourcepath"))
		{
			return getSourcePath();
		}
		
		String value =getElement().attributeValue(inId); 
	
		if( value == null && getPropertyDetails() != null)
		{
			PropertyDetail detail = getPropertyDetails().getDetail(inId);
			if( detail != null)
			{
				String legacy = detail.get("legacy");
				if( legacy != null)
				{
					value = get(legacy);
				}
			}
		}
				
				
		return value;
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
	public void setValue(String inId, Object inValue)
	{
		if(inId.equals(".version"))
		{
			setVersion(String.valueOf( inValue) );
		}
		else
		{
			//synchronized (getElement())  //TODO: Remove this now that we have proper locking?
			//{
				if( inId.equals("name"))
				{
					List copy = getElement().elements(inId);
					for (Iterator iterator = copy.iterator(); iterator.hasNext();)
					{
						Element type = (Element) iterator.next();
						getElement().remove(type);
					}
					Attribute attr = getElement().attribute(inId);
					if( attr != null)
					{
						getElement().remove(attr);
					}
					if( inValue != null)
					{
						//save in XML format all the time
						Element child = getElement().addElement(inId);
						if( inValue instanceof Map)
						{
							//loop over languages
							Map languages = (Map)inValue;
							for (Iterator iterator = languages.keySet().iterator(); iterator.hasNext();)
							{
								String lang = (String) iterator.next();
								String val = (String)languages.get(lang);
								child.addElement("language").addAttribute("id",lang).addCDATA((String)val);
							}
						}
						else
						{
							child.addCDATA((String)inValue);
						}
					}	
					return;
				}
				//always check for a child
				Element child = getElement().element(inId);
				if( child != null)
				{
					//TODO: See if value changed?
					getElement().remove(child);
				}
				if( inValue instanceof String)
				{
					String val = (String)inValue;
					if( val == null || val.length() == 0)
					{
						Attribute attr = getElement().attribute(inId);
						if( attr != null)
						{
							getElement().remove(attr);
						}
					}
					else
					{
						if( INVALIDSTUFF.matcher(val).find() )
						{
							Attribute attr = getElement().attribute(inId);
							if( attr != null)
							{
								getElement().remove(attr);
							}
							
							getElement().addElement(inId).addCDATA(val);
						}
						else
						{
							getElement().addAttribute(inId,val);						
						}
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
	public void setProperties(Map inProperties)
	{
		//getProperties().putAll(inProperties);
		for (Iterator iterator = inProperties.keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			if( key != null)
			{
				setProperty(key,(String)inProperties.get(key));
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
	
	@Override
	public void setProperty(String inKey, String inValue)
	{
		setValue(inKey,inValue);
	}


	@Override
	public void addValue(String inKey, Object inNewValue)
	{
		String values = get(inKey);
		if( values == null)
		{
			values = "";
		}
		else
		{
			values = values + "|";
		}
		values = values + String.valueOf( inNewValue );
		setProperty(inKey,values);
	}


	@Override
	public void removeValue(String inKey, Object inNewValue)
	{
		throw new OpenEditException("Not implemented");
	}


	@Override
	public Map getSearchData()
	{
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setSearchData(Map inSearchHit)
	{
		// TODO Auto-generated method stub
		
	}
	public String getName(String inLocale) {
		return getName();
	}
	

	@Override
	public Set keySet()
	{
		return getProperties().keySet();
	}
}
