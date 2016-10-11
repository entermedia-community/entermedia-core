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

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.Data;
import org.openedit.users.InvalidPropertyNameException;
import org.openedit.users.PropertyContainer;
import org.openedit.users.UnsupportedPropertyTypeException;
import org.openedit.users.UserManagerException;


/**
 * This class represents an object which is backed by an XML file.  It implements {@link
 * PropertyContainer} as a simple <code>Map</code> which only permits String-valued properties.
 *
 * @author Matt Avery
 */
public abstract class FileSystemObject implements PropertyContainer, Serializable, Data
{
	private transient static Log log = null;
	
	private Log getLog()
	{
		if( log == null)
		{
			log = LogFactory.getLog(FileSystemObject.class);
		}
		return log;
	}
	
	protected PropertyContainer fieldPropertyContainer;
	//protected Element fieldRootElement;
	//protected String fieldFile;
	protected Date fieldCreationDate;

	
	public FileSystemObject()
	{
	}
	
	public PropertyContainer getPropertyContainer()
	{
		if( fieldPropertyContainer == null)
		{
			fieldPropertyContainer = new MapPropertyContainer();
		}
		return fieldPropertyContainer;
	}
	public void setPropertyContainer(PropertyContainer inData )
	{
		fieldPropertyContainer = inData;
	}
	/**
	 * Get all the properties on this object as a read-only map.
	 *
	 * @return A read-only map of all the properties on this object
	 */
	public Map getProperties()
	{
		return getPropertyContainer().getProperties();
	}

	/**
	 * Get the value of the given property of this object.
	 *
	 * @param inPropertyName The property name
	 *
	 * @return The property value, or <code>null</code> if the property does not exist on this
	 * 		   object
	 */
	public String get( String inPropertyName )
	{
		return getPropertyContainer().get( inPropertyName );
	}
	
	public Date getCreationDate()
	{
		return fieldCreationDate;
	}
	protected void setCreationDate(Date inDate)
	{
		fieldCreationDate = inDate;
	}
	/**
	 * Set the given property of this object.  Note that depending on the implementation, only
	 * certain types may be supported for the property value.  All implementations must support at
	 * least String.  However, it is recommended that implementations support at least the
	 * following types:
	 * 
	 * <ul>
	 * <li>
	 * Boolean
	 * </li>
	 * <li>
	 * Double
	 * </li>
	 * <li>
	 * Integer
	 * </li>
	 * <li>
	 * String
	 * </li>
	 * <li>
	 * Object[]
	 * </li>
	 * </ul>
	 * 
	 * Property names must conform to the regular expression <code>[A-Za-z_][A-Za-z0-9_.]</code>.
	 * In other words, the first character must be an underscore or letter; and each subsequent
	 * character must be an underscore, period, letter, or digit.
	 *
	 * @param inPropertyName The property name
	 * @param inPropertyValue The property value
	 *
	 * @throws InvalidPropertyNameException If the property name was invalid
	 * @throws UnsupportedPropertyTypeException If the property value was of an unsupported type
	 * @throws UserManagerException If the property could not be set
	 */
	public void put( String inPropertyName, Object inPropertyValue )
		throws UserManagerException
	{
		getPropertyContainer().put( inPropertyName, inPropertyValue );
	}

	/**
	 * Add all the specified properties to this object.  Note that the keys in the given map must
	 * be strings (property names), and that the values must satisfy the implementation's
	 * requirements for property values.
	 *
	 * @param inProperties The properties to set
	 *
	 * @throws UserManagerException If any of the properties could not be set
	 *
	 * @see #put(String, Object)
	 */
	public void setProperties( Map inProperties ) throws UserManagerException
	{
		getPropertyContainer().setProperties( inProperties );
	}

	/**
	 * Remove the given property from this object.  If no such property exists, this method will do
	 * nothing.
	 *
	 * @param inPropertyName The name of the property to remove
	 *
	 * @throws UserManagerException If the property exists and could not be removed
	 */
	public void remove( String inPropertyName ) throws UserManagerException
	{
		getPropertyContainer().remove( inPropertyName );
	}
	
	/**
	 * Remove the given properties from this object.  If no such property exists, this method will do
	 * nothing.
	 *
	 * @param inProperties The names of the properties to remove
	 *
	 * @throws UserManagerException If the property exists and could not be removed
	 */
	public void removeAll( String[] inProperties ) throws UserManagerException
	{
		getPropertyContainer().removeAll( inProperties );
	}
	
	public boolean getBoolean( String inPropertyName )
	{
		return Boolean.valueOf( getString( inPropertyName ) ).booleanValue();
	}
	
	public String getString( String inPropertyName )
	{
		return (String) get( inPropertyName );
	}
	
	public void safePut( String inKey, Object inValue )
	{
		try
		{
			if ( inValue == null)
			{
				getPropertyContainer().remove( inKey );
			}
			else
			{
				Object value = inValue;
				if ( inValue instanceof String )
				{
					value = ( (String) inValue ).trim();
				}
				getPropertyContainer().put( inKey, value );
			}
		}
		catch ( UserManagerException ex)
		{
			getLog().error( ex );
		}
	}
	public String getSourcePath()
	{
		// TODO Auto-generated method stub
		return null;
	}
	public void setSourcePath(String inSourcepath)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set keySet()
	{
		return getProperties().keySet();
	}
	
}
