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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.users.filesystem.FileSystemObject;

/**
 * DOCUMENT ME!
 *
 * @author avery To change this generated comment edit the template variable "typecomment":
 * 		   Window>Preferences>Java>Templates.
 */
public class BaseGroup extends FileSystemObject implements Group, Serializable, Comparable
{	
	protected long fieldLastModified;
	
	private transient static Log log = null;
	private Log getLog()
	{
		if( log == null)
		{
			log = LogFactory.getLog(BaseGroup.class);
		}
		return log;
	}

	protected Collection fieldPermissions;
	protected String fieldName;
	protected String fieldId;

	public BaseGroup()
	{
	}

	/**
	 * @see org.openedit.users.Group#getName()
	 */
	public String getName()
	{
		if (fieldName == null)
			return getId();
		return fieldName;
	}

	/**
	 * @see org.openedit.users.Group#getPermissions()
	 */
	public Collection getPermissions()
	{
		if (fieldPermissions == null)
		{
			fieldPermissions = new HashSet();
		}
		return fieldPermissions;
	}
	/* (non-Javadoc)
	 * @see org.openedit.users.Group#addPermission(String)
	 */
	public void addPermission(String inPermission) throws UserManagerException
	{
		if( hasPermission(inPermission))
		{
			return;
		}
		getPermissions().add(new EnabledPermission(inPermission));
	}

	public boolean hasPermission(String inPermission)
	{
		for (Iterator iterator = getPermissions().iterator(); iterator.hasNext();)
		{
			Object existingpermission = (Object) iterator.next();
			if( existingpermission.equals(inPermission))
			{
				return true;
			}
		}
		String ok =  getPropertyContainer().getString( inPermission );

		if (Boolean.parseBoolean(ok))
		{
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.openedit.users.Group#removePermission(String)
	 */
	public void removePermission(String inPermission)
		throws UserManagerException
	{
		for (Iterator iterator = getPermissions().iterator(); iterator.hasNext();)
		{
			Object existingpermission = (Object) iterator.next();
			if( existingpermission.equals(inPermission))
			{
				getPermissions().remove(existingpermission);
				break;
			}
		}

	}

	/**
	 * Returns the string representation of this object, that can be used when sorting groups
	 * alphabetically.
	 * 
	 * @return string representing this group
	 */
	public String toString()
	{
		return getName();
	}

	public void setName(String inGroupName)
	{
		fieldName = inGroupName;
	}

	public long getLastModified()
	{
		return fieldLastModified;
	}

	public void setLastModified(long inLastModified)
	{
		fieldLastModified = inLastModified;
	}

	public String getId()
	{
		if( fieldId == null && fieldName != null && fieldName.length() != 0)
		{
			return getName();
		}
		return fieldId;
	}

	public void setId(String inId)
	{
		fieldId = inId;
	}

	public void setProperties(Map inProperties)
	{
		getProperties().putAll(inProperties);
	}

	public void setProperty(String inId, String inValue)
	{
		if( inId.equals("id"))
		{
			setId(inValue);
		}
		else if( inId.equals("name"))
		{
			setName(inValue);
		}
		else
		{
			put(inId,inValue);
		}
	}

	public void setPermissions(Collection inPermissions)
	{
		fieldPermissions = inPermissions;
		
	}

	public String get(String inPropertyName)
	{
		if( "id".equals(inPropertyName))
		{
			return getId();
		}
		else if ( "name".equals(inPropertyName))
		{
			return getName();
		}
		return super.get(inPropertyName);
	}

	@Override
	public int compareTo(Object g1) {
		Group group = (Group) g1;
		//compare by name
		return this.getName().compareToIgnoreCase(group.getName());
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
}
