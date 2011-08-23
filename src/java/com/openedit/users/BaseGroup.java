/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package com.openedit.users;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openedit.users.filesystem.FileSystemObject;

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
	 * @see com.openedit.users.Group#getName()
	 */
	public String getName()
	{
		if (fieldName == null)
			return getId();
		return fieldName;
	}

	/**
	 * @see com.openedit.users.Group#getPermissions()
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
	 * @see com.openedit.users.Group#addPermission(String)
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
	 * @see com.openedit.users.Group#removePermission(String)
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
		if( fieldId == null && fieldName != null )
		{
			return getName();
		}
		return fieldId;
	}

	public void setId(String inId)
	{
		fieldId = inId;
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
}
