/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package org.openedit.users;

import java.io.Serializable;
import java.util.Map;


/**
 * This interface represents an object that can contain properties.
 *
 * @author Eric Galluzzo
 */
public interface PropertyContainer extends Serializable
{
	/**
	 * Get all the properties on this object as a read-only map.
	 *
	 * @return A read-only map of all the properties on this object
	 */
	Map getProperties();

	/**
	 * Get the value of the given property of this object.
	 *
	 * @param inPropertyName The property name
	 *
	 * @return The property value, or <code>null</code> if the property does not exist on this
	 * 		   object
	 */
	String get(String inPropertyName);

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
	void put(String inPropertyName, Object inPropertyValue)
		throws UserManagerException;

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
	void setProperties(Map inProperties) throws UserManagerException;

	/**
	 * Remove the given property from this object.  If no such property exists, this method will do
	 * nothing.
	 *
	 * @param inPropertyName The name of the property to remove
	 *
	 * @throws UserManagerException If the property exists and could not be removed
	 */
	void remove(String inPropertyName) throws UserManagerException;
	
	/**
	 * Remove the given properties from this object.  Any properties that do not
	 * exist will be silently ignored.
	 *
	 * @param inProperties The names of the properties to remove
	 *
	 * @throws UserManagerException If a property exists and could not be removed
	 */
	void removeAll( String[] inProperties ) throws UserManagerException;

	/**
	 * Returns the value for the given property, converted from a String to a
	 * boolean.
	 * 
	 * @param inPropertyName  The property name
	 * 
	 * @return  The boolean value
	 */
	boolean getBoolean( String inPropertyName );

	/**
	 * Returns the string value for the given property.
	 * 
	 * @param inPropertyName  The property name
	 * 
	 * @return  The string value
	 */
	String getString( String inPropertyName );

	/**
	 * Puts the given property in this map, except that null values are removed
	 * and no saving is performed.
	 * 
	 * @param inPropertyName   The property name
	 * @param inPropertyValue  The property value
	 */
	void safePut( String inPropertyName, Object inPropertyValue );
}
