/*
Copyright (c) 2004 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package org.openedit.users;

/**
 * This object represents a permission that can be assigned to a group.
 *
 * @author Dennis Brown
 */
public class Permission
{
	protected String fieldName;
	protected String fieldDisplayName;
	protected String fieldDescription;
	
	public Permission()
	{
	}

	public Permission( String inName )
	{
		fieldName = inName;
	}

	public Permission( String inName, String inDisplayName )
	{
		fieldName = inName;
		fieldDisplayName = inDisplayName;
	}

	public String getName()
	{
		return fieldName;
	}

	public String getDisplayName()
	{
		return fieldDisplayName;
	}

	public void setName( String inName )
	{
		fieldName = inName;
	}

	public void setDisplayName( String inDisplayName )
	{
		fieldDisplayName = inDisplayName;
	}

	/**
	 * Two <code>Permission</code>s are equal if their names are equal.
	 */
	public boolean equals( Object o )
	{
		if ( o instanceof Permission )
		{
			Permission p = (Permission) o;
			if ( fieldName != null )
			{
				return fieldName.equals( p.fieldName );
			}
			else
			{
				return ( p.fieldName == null );
			}
		}
		else
		{
			return false;
		}
	}

	public String getDescription() {
		if (fieldDescription == null)
		{
			fieldDescription = "No description available";
		}
		return fieldDescription.replace("\n", " ");
	}

	public void setDescription(String description) {
		fieldDescription = description;
	}

	public int hashCode()
	{
		return ( fieldName != null ) ?
				fieldName.hashCode() :
				0;
	}

	public String toString()
	{
		return fieldName;
	}
}
