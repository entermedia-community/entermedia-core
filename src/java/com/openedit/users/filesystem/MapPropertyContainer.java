/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package com.openedit.users.filesystem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import com.openedit.users.PropertyContainer;
import com.openedit.users.UserManagerException;

/**
 * Implementation of the <code>PropertyContainer</code> interface that retains
 * a map of properties in memory.
 * 
 * <p>Created on Dec. 26, 2003, to resolve issue OE-33
 * 
 * @author Dennis Brown
 */
public class MapPropertyContainer implements PropertyContainer
{
	private static transient Log log;
	private Log getLog()
	{
		if( log == null)
		{
			log =  LogFactory.getLog(FileSystemObject.class);
		}
		return log;
	}
	protected Map fieldProperties;

	/**
	 * Get all the properties on this user -- the real, live collection that is not read-only.
	 *
	 * @return Map
	 */
	protected Map getRealProperties()
	{
		if (fieldProperties == null)
		{
			fieldProperties = new HashMap();
		}

		return fieldProperties;
	}

	/**
	 * @see com.openedit.users.User#getProperties()
	 */
	public Map getProperties()
	{
		return getRealProperties();
	}

	/**
	 * @see com.openedit.users.User#get(java.lang.String)
	 */
	public String get(String inPropertyName)
	{
		return (String)getRealProperties().get(inPropertyName);
	}

	/**
	 * @see com.openedit.users.User#put(java.lang.String, java.lang.Object)
	 */
	public void put(String inPropertyName, Object inPropertyValue)
		throws UserManagerException
	{
		//validateProperty(inPropertyName, inPropertyValue);
		getRealProperties().put(inPropertyName, inPropertyValue);
	}

	/**
	 * @see com.openedit.users.User#put(java.util.Map)
	 */
	public void putAll(Map inProperties) throws UserManagerException
	{
		for (Iterator iter = inProperties.entrySet().iterator(); iter.hasNext();)
		{
			Map.Entry entry = (Map.Entry) iter.next();
			//validateProperty((String) entry.getKey(), entry.getValue());
		}

		getRealProperties().putAll(inProperties);
	}

	/**
	 * @see com.openedit.users.User#remove(java.lang.String)
	 */
	public void remove(String inPropertyName) throws UserManagerException
	{
		getRealProperties().remove(inPropertyName);
	}

	/* (non-Javadoc)
	 * @see com.openedit.users.PropertyContainer#removeAll(java.lang.String[])
	 */
	public void removeAll(String[] inProperties) throws UserManagerException
	{
		if (inProperties == null)
		{
			return;
		}
		for (int i = 0; i < inProperties.length; i++)
		{
			remove(inProperties[i]);
		}
	}

	protected Element createPropertiesElement(String inElementName)
	{
		Element propertiesElem = DocumentFactory.getInstance().createElement(inElementName);

		for (Iterator iter = getRealProperties().entrySet().iterator(); iter.hasNext();)
		{
			Map.Entry entry = (Map.Entry) iter.next();
			if ( entry.getValue() != null)
			{
				Element propertyElem = propertiesElem.addElement("property");
				propertyElem.addAttribute("name", entry.getKey().toString());
				propertyElem.addAttribute("value", entry.getValue().toString());
			}
		}

		return propertiesElem;
	}

	/**
	 * Load the properties from the given element.
	 *
	 * @param inPropertiesElement The element (hopefully created via
	 * 		  <code>createPropertiesElement</code>) from which to load the properties
	 */
	protected void loadProperties(Element inPropertiesElement)
	{
		Map properties = new HashMap();

		if (inPropertiesElement != null)
		{
			for (Iterator iter = inPropertiesElement.elementIterator(); iter.hasNext();)
			{
				Element elem = (Element) iter.next();

				if (elem.getName().equals("property"))
				{
					String name = elem.attributeValue("name");
					String value = elem.attributeValue("value");

					if ((name != null) && (value != null))
					{
						properties.put(name, value);
					}
				}
			}
		}

		fieldProperties = properties;
	}

	/**
	 * Determine whether the given name is valid.  In order to be a valid name, the first character
	 * must be a letter or underscore, and each subsequent character must be a letter, digit,
	 * underscore, or period.
	 *
	 * @param inName The name to query
	 *
	 * @return <code>true</code> if the name is valid, <code>false</code> if not
	 */
	protected boolean isValidName(String inName)
	{
		if ((inName == null) || (inName.length() == 0))
		{
			return false;
		}

		char c = inName.charAt(0);

		if ((c == '_') || Character.isLetter(c))
		{
			for (int i = 1; i < inName.length(); i++)
			{
				c = inName.charAt(i);

				if ((c != '_') && (c != '.') && !Character.isLetter(c) && !Character.isDigit(c))
				{
					return false;
				}
			}
		}

		return true;
	}


	/* (non-javadoc)
	 * @see com.openedit.users.PropertyContainer#getBoolean(java.lang.String)
	 */
	public boolean getBoolean(String inKey)
	{
		return Boolean.valueOf(getString(inKey)).booleanValue();
	}

	/* (non-javadoc)
	 * @see com.openedit.users.PropertyContainer#getString(java.lang.String)
	 */
	public String getString(String inKey)
	{
		return (String)get(inKey);
	}

	/* (non-javadoc)
	 * @see com.openedit.users.PropertyContainer#safePut(java.lang.String, java.lang.Object)
	 */
	public void safePut(String inPropertyName, Object inPropertyValue)
	{
		try
		{
			if ( inPropertyValue == null )
			{
				remove( inPropertyName );
			}
			else
			{
				put( inPropertyName, inPropertyValue );
			}
		}
		catch ( UserManagerException ex)
		{
			getLog().error( ex );
		}
	}

}
