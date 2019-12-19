/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package org.openedit.users.filesystem;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openedit.data.ValuesMap;

/**
 * Implementation of the <code>PropertyContainer</code> interface that retains
 * a map of properties in memory.
 * 
 * <p>Created on Dec. 26, 2003, to resolve issue OE-33
 * 
 * @author Dennis Brown
 */
public class MapPropertyContainer  extends ValuesMap
{
	private static transient Log log;
	private Log getLog()
	{
		if( log == null)
		{
			log =  LogFactory.getLog(MapPropertyContainer.class);
		}
		return log;
	}


	protected Element createPropertiesElement(String inElementName)
	{
		Element propertiesElem = DocumentFactory.getInstance().createElement(inElementName);

		for (Iterator iter = keySet().iterator(); iter.hasNext();)
		{
			String key = (String) iter.next();
			if ( !"id".equals(key) && !"name".equals(key))
			{
				String value = (String)get(key);
				Element propertyElem = propertiesElem.addElement("property");
				propertyElem.addAttribute("name", key);
				propertyElem.addAttribute("value", value);
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
						if(name.contains(".")){
							name.replace(".", "_");
						}
						if( name.equals("emrecordstatus") )
						{
							JSONParser parser = new JSONParser();
							Map values = null;
							try
							{
								values = (Map)parser.parse(value);
								put(name,values);
							}
							catch (ParseException e)
							{
								e.printStackTrace();
							}
						}
						else
						{
							put(name, value);
						}
					}
				}
			}
		}
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

}
