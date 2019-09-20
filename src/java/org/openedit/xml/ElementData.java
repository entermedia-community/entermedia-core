package org.openedit.xml;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.dom4j.Attribute;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.openedit.MultiValued;
import org.openedit.OpenEditException;
import org.openedit.data.PropertyDetail;
import org.openedit.data.PropertyDetails;
import org.openedit.data.SaveableData;
import org.openedit.data.SearchData;
import org.openedit.data.ValuesMap;
import org.openedit.modules.translations.LanguageMap;
import org.openedit.util.DateStorageUtil;

public class ElementData implements MultiValued, SaveableData, Comparable, SearchData
{
	protected Element fieldElement;
	protected String fieldSourcePath;
	protected String fieldVersion;
	protected ValuesMap fieldMap;

	protected ValuesMap getMap()
	{
		if (fieldMap == null)
		{
			fieldMap = new ValuesMap();
		}
		return fieldMap;
	}
	public Collection getObjects(String inField)
	{
		Collection values = (Collection)getValue(inField);
		return values;
	}
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
		setElement((Element) inHit);
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

	public String getVersion()
	{
		return fieldVersion;
	}

	public void setVersion(String inVersion)
	{
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
		setElement((Element) inEl);
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
		if (obj == null)
		{
			return null;
		}
		return String.valueOf(obj);
	}

	/**
	 * First we try the child node in case we have CDATA Then we try the
	 * attributes
	 */
	public Object getValue(String inId)
	{
		if( inId == null)
		{
			return null;
		}
		if (inId.equals("name"))
		{
			return getLanguageMap(inId);
		}
		else if (inId.equals(".version"))
		{
			return getVersion();//elastic search
		}
		else if (inId.equals("sourcepath"))
		{
			return getSourcePath();
		}

		String value = getElement().attributeValue(inId);

		if (value == null && getPropertyDetails() != null)
		{
			PropertyDetail detail = getPropertyDetails().getDetail(inId);
			if (detail != null)
			{
				String legacy = detail.get("legacy");
				if (legacy != null)
				{
					value = get(legacy);
				}
			}
		}

		if (value == null)
		{
			Element noderoot = getElement().element(inId);
			if (noderoot != null)
			{
				value = noderoot.getTextTrim();
				if (value == null || value.isEmpty())
				{
					value = null;
				}
			}
		}

		return value;
	}

	
	public float getFloat(String inId)
	{
		String val = get(inId);
		if (val != null)
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
		return getLanguageMap("name").getDefaultText("en");
	}

	public void setName(String inName)
	{
		LanguageMap map = getLanguageMap("name");
		map.setText("en", inName);
		setValue("name", map);
	}

	public void setId(String inNewid)
	{
		getElement().addAttribute("id", inNewid);

	}

	public void setValue(String inId, Object inValue)
	{
		if (inId.equals(".version"))
		{
			setVersion(String.valueOf(inValue));
		}
		else
		{
			//synchronized (getElement())  //TODO: Remove this now that we have proper locking?
			//{
			if (inValue == null)
			{
				removeValues(inId);
				return;
			}
			if (inId.equals("name"))
			{
				removeValues(inId);
				//save in XML format all the time
				Element child = getElement().addElement(inId);
				if (inValue instanceof LanguageMap)
				{
					//loop over languages
					LanguageMap languages = (LanguageMap) inValue;
					for (Iterator iterator = languages.keySet().iterator(); iterator.hasNext();)
					{
						String lang = (String) iterator.next();
						String val = (String) languages.get(lang);
						child.addElement("language").addAttribute("id", lang).addCDATA((String) val);
					}
				}
				else
				{
					child.addCDATA((String) inValue);
				}
				return;
			}
			//always check for a child
			Element child = getElement().element(inId);
			if (child != null)
			{
				//TODO: See if value changed?
				getElement().remove(child);
			}
			inValue = getMap().toString(inValue);

			String val = (String) inValue;
			if (val.isEmpty())
			{
				Attribute attr = getElement().attribute(inId);
				if (attr != null)
				{
					getElement().remove(attr);
				}
			}
			else
			{
				if (INVALIDSTUFF.matcher(val).find())
				{
					Attribute attr = getElement().attribute(inId);
					if (attr != null)
					{
						getElement().remove(attr);
					}

					getElement().addElement(inId).addCDATA(val);
				}
				else
				{
					getElement().addAttribute(inId, val);
				}
			}
		}
	}
	protected void removeValues(String inId)
	{
		getMap().remove(inId);
		List copy = getElement().elements(inId);
		for (Iterator iterator = copy.iterator(); iterator.hasNext();)
		{
			Element type = (Element) iterator.next();
			getElement().remove(type);
		}
		Attribute attr = getElement().attribute(inId);
		if (attr != null)
		{
			getElement().remove(attr);
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

	public ValuesMap getProperties()
	{
		ValuesMap map = new ValuesMap();
		for (Iterator iterator = keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			Object value = getValue(key);
			if( value != null)
			{
				map.put(key,value);
			}
		}
		return map;
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
		String name = getName();

		if (name == null)
		{
			name = getId();
		}

		if (name == null)
		{
			name = super.toString();
		}
		return name;
	}

	public void setProperties(Map inProperties)
	{
		for (Iterator iterator = inProperties.keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			if (key != null)
			{
				setValue(key, inProperties.get(key));
			}
		}
	}

	public Collection<String> getValues(String inPreference)
	{
		String val = get(inPreference);

		if (val == null)
			return null;

		String[] vals = null;
		if (val.contains("|"))
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
			if (iterator.hasNext())
			{
				values.append(" | ");
			}
		}
		setProperty(inKey, values.toString());
	}

	public Object clone()
	{
		try
		{
			return super.clone();
		}
		catch (Exception ex)
		{
			throw new OpenEditException(ex);
		}
	}

	@Override
	public void setProperty(String inKey, String inValue)
	{
		if( inKey.equals("name"))
		{
			setName(inValue);
		}
		else
		{
			setValue(inKey, inValue);
		}
	}

	@Override
	public void addValue(String inKey, Object inNewValue)
	{
		String values = get(inKey);
		if (values == null)
		{
			values = "";
		}
		else
		{
			values = values + "|";
		}
		values = values + String.valueOf(inNewValue);
		setProperty(inKey, values);
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

	public String getName(String inLocale)
	{
		return getLanguageMap("name").getText(inLocale);
	}

	@Override
	public Set keySet()
	{
		Set keys = new HashSet();
		keys.add("name");
		for (Iterator iterator = getAttributes().iterator(); iterator.hasNext();)
		{
			org.dom4j.Attribute attr = (org.dom4j.Attribute) iterator.next();
			keys.add(attr.getName());
		}
		for (Iterator iterator = getElement().elementIterator(); iterator.hasNext();)
		{
			Element child = (Element) iterator.next();
			keys.add(child.getName());
		}

		return keys;
	}

	public boolean getBoolean(String inString)
	{
		Object obj = getValue(inString);
		if (obj instanceof Boolean)
		{
			return (boolean) obj;
		}
		if (obj instanceof String)
		{
			return Boolean.parseBoolean((String) obj);
		}
		return false;

	}

	public LanguageMap getLanguageMap(String inKey)
	{
		LanguageMap language = (LanguageMap)getMap().getObject(inKey);
		if( language != null)
		{
			return language;
		}
		LanguageMap map = new LanguageMap();
		String textvalue = getElement().attributeValue(inKey);
		if( textvalue == null)
		{
			if( !getElement().hasMixedContent() )
			{
				textvalue = getElement().getTextTrim();
			}	
			else
			{
				textvalue = null;
			}
			if( textvalue != null && textvalue.isEmpty())
			{
				textvalue = null;
			}
			if( textvalue == null)
			{
				Element langmaptop = getElement().element(inKey);
				if( langmaptop != null)
				{
					for (Iterator iterator = langmaptop.elementIterator("language"); iterator.hasNext();)
					{
						Element childlang = (Element) iterator.next();
						map.put(childlang.attributeValue("id"),childlang.getText());
					}					
					if( map.isEmpty())
					{
						textvalue = langmaptop.getTextTrim();
						if( textvalue != null && !textvalue.isEmpty())
						map.put("en",textvalue);
					}
				}	
			}	
		}
		if( textvalue != null)
		{
			map.setText("en", textvalue);
		}
		getMap().put(inKey,map);
		return map;
	}

	public ElementData copy()
	{
		ElementData data = new ElementData();
		for (Iterator iterator = keySet().iterator(); iterator.hasNext();)
		{
			String id = (String) iterator.next();
			data.setValue(id, getValue(id));
		}
		data.setPropertyDetails(getPropertyDetails());
		return data;
	}

	@Override
	public Date getDate(String inField)
	{
		Object date = getValue(inField);
		if(date == null){
			return null;
		}

		if(date instanceof Date){
			return (Date) date;
		}
		
		return DateStorageUtil.getStorageUtil().parseFromStorage((String)date);
	}

	@Override
	public String getText(String inId, String inLocale) 
	{
		if( fieldMap == null)
		{
			return null;
		}
		Object value = getValue(inId);
		if( value instanceof LanguageMap )
		{
			LanguageMap map = (LanguageMap)value;
			return map.getText(inLocale);
		}
		return getMap().toString(value);
	}
}
