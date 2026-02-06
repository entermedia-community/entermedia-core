/*
 * Created on May 18, 2006
 */
package org.openedit.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.openedit.Data;
import org.openedit.OpenEditException;
import org.openedit.data.ValuesMap;
import org.openedit.repository.ContentItem;
import org.openedit.util.PathUtilities;

public class XmlFile implements Data 
{
	protected long fieldLastModified = -1;
	protected String fieldPath;
	protected String fieldId;
	protected String fieldElementName;
	
	protected Element fieldRoot;
	protected List fieldAttributes;
	protected boolean fieldExist;
	protected ContentItem fieldContentItem;
	
	public ContentItem getContentItem()
	{
		return fieldContentItem;
	}
	public void setContentItem(ContentItem inContentItem)
	{
		fieldContentItem = inContentItem;
	}
	public Collection getValues(String inField)
	{
		Collection values = (Collection)getValue(inField);
		return values;
	}
	public String getPath()
	{
		return fieldPath;
	}
	public void setPath(String inFile)
	{
		fieldPath = inFile;
	}
	public String getId()
	{
		return fieldId;
	}
	public void setId(String inId)
	{
		fieldId = inId;
	}
	public long getLastModified()
	{
		return fieldLastModified;
	}
	public void setLastModified(long inLastModified)
	{
		fieldLastModified = inLastModified;
	}
	public Element getRoot()
	{
		return fieldRoot;
	}
	public void setRoot(Element inRoot)
	{
		fieldRoot = inRoot;
	}
	public List getElements()
	{
		return getRoot().elements();
	}
	public List getAttributes()
	{
		//this is a list of attributes as defined by the action that created us
		if (fieldAttributes == null)
		{
			fieldAttributes = new ArrayList();
		}
		return fieldAttributes;
	}
	public void setAttributes(List inAttributes)
	{
		fieldAttributes = inAttributes;
	}
	public Element getElement(String inName)
	{
		return getRoot().element(inName);
	}
	public Element getElementById(String inEid)
	{
		if( inEid == null)
		{
			return null;
		}
		for (Iterator iter = getElements().iterator(); iter.hasNext();)
		{
			Element element = (Element) iter.next();
			String id = element.attributeValue("id");
			if ( inEid.equals(id))
			{
				return element;
			}
		}
		return null;
	}
	public void deleteElement(Element inEid)
	{
		getRoot().remove(inEid);
	}
	
	/*
	 * TODO: Get rid of this idea.
	 * This returns the lowest natural number that could function as a unique 
	 * top level id for this file. 
	 */
	protected Integer findUniqueId()
	{
		//log.info("do not use this method");
		Set set = new HashSet();
		for (Iterator iterator = getRoot().elementIterator(); iterator.hasNext();) {
			Element element = (Element) iterator.next();
			try
			{
				int idAsNum = Integer.parseInt(element.attributeValue("id"));
				set.add(new Integer(idAsNum));
			}
			catch (NumberFormatException e)
			{
			}
		}
		
		for (int i = 1; i < set.size() + 1; i++) 
		{
			if (!set.contains(Integer.valueOf(i)))
			{
				return Integer.valueOf(i);
			}
		}
		return new Integer(set.size() + 1);
	}
	/**
	 * Should not use this since we cant decide how it should default
	 * @return
	 */
	public Element addNewElement()
	{
		if( getRoot() == null)
		{
			setRoot(DocumentHelper.createElement(getElementName()));
		}
		Element child = getRoot().addElement(getElementName());
		child.addAttribute("id", "" + findUniqueId() );				
		child.setText("New");
		return child;
	}
	public Element createElement()
	{
		Element child = DocumentHelper.createElement(getElementName());
		//child.addAttribute("id", "" + findUniqueId() );				
		return child;
	}
	
	public String getElementName()
	{
		if (fieldElementName == null)
		{
			fieldElementName = "data";
		}
		return fieldElementName;
	}
	public void setElementName(String inElementName)
	{
		fieldElementName = inElementName;
	}
	public boolean isExist()
	{
		return fieldExist;
	}
	public void setExist(boolean inExist)
	{
		fieldExist = inExist;
	}
	
	public Iterator getElements(String inName)
	{
		return getRoot().elementIterator(inName);
	}
	/**
	 * Gets the text for row based on the id attribute lower case
	 */
	public String get(String inId)
	{
		Element prop = getElementById(inId);
		if (prop != null)
		{
			return prop.getText();
		}
			
		String text =  getRoot().attributeValue(inId);
		return text;
	}
	
	
	public List keys()
	{
		List list = new ArrayList(size());
		for (Iterator iter = getElements().iterator(); iter.hasNext();)
		{
			Element prop = (Element)iter.next();
			list.add(prop.attributeValue("id"));
		}		
		return list;
	}
	public int size()
	{
		return getElements().size();
	}
	public void add(String inId, String inText)
	{
		Element child = addNewElement();
		child.addAttribute("id", inId);
		child.setText(inText);
	}
	public void add(Element inChild)
	{
		getRoot().add(inChild);
	}
	
	public void clear()
	{
		if( getRoot() != null)
		{
			setRoot(DocumentHelper.createElement(getRoot().getName()));
		}
	}
	public String getName()
	{
		return PathUtilities.extractPageName(getPath());
	}
	public ValuesMap getProperties()
	{
		// TODO Auto-generated method stub
		return null;
	}
	public String getSourcePath()
	{
		// TODO Auto-generated method stub
		return getPath();
	}
	public void setName(String inName)
	{
		
	}
	public void setProperty(String inId, String inValue)
	{
		getRoot().addAttribute(inId, inValue);
		
	}
	public void setSourcePath(String inSourcepath)
	{
		setPath(inSourcepath);
	}	
	public void setProperties(Map inProperties)
	{
		for (Iterator iterator = inProperties.entrySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			setProperty(key, (String)inProperties.get(key));
		}
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
	@Override
	public Object getValue(String inKey)
	{
		return get(inKey);
	}
	@Override
	public void setValue(String inKey, Object inValue)
	{
		setProperty(inKey, String.valueOf(inValue));
	}
	public String getName(String inLocale) {
		return getName();
	}
	
	@Override
	public Set keySet()
	{
		return getProperties().keySet();
	}

	
	public String toJsonString()
	{		
		throw new OpenEditException("NOT IMPLEMENTED");
	}
	
	
}
