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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openedit.util.DateStorageUtil;

import com.openedit.OpenEditRuntimeException;
import com.openedit.users.filesystem.FileSystemObject;


/**
 * This class represents a user as an XML file.
 *
 * @author Eric and Matt
 */
public class BaseUser extends FileSystemObject implements User, Comparable
{
	protected Collection fieldGroups;
	protected String fieldPassword;
	protected String fieldId;
	protected boolean fieldVirtual;
	protected String fieldLastLoginTime;
	
	public BaseUser()
	{
		super();
	}
	
	public void setLastLoginTime(String lastLoginTime) {
		fieldLastLoginTime = lastLoginTime;
		
	}

	public String getLastLoginTime() {
		return fieldLastLoginTime;
	}
	
	public String getEmail()
	{
		return getString(EMAIL_PROPERTY);
	}

	public String getFirstName()
	{
		return getString(FIRST_NAME_PROPERTY);
	}
	public void setFirstName( String inName)
	{
		safePut( FIRST_NAME_PROPERTY, inName);
	}
	/**
	 * @see com.openedit.users.User#getGroups()
	 */
	public Collection getGroups()
	{
		if (fieldGroups == null)
		{
			fieldGroups = new HashSet(3);
		}
		return fieldGroups;
	}
	public void setGroups(Collection inGroups)
	{
		fieldGroups = inGroups;
	}
	public String getLastName()
	{
		return getString(LAST_NAME_PROPERTY);
	}

	public void setLastName( String inName)
	{
		safePut( LAST_NAME_PROPERTY, inName);
	}
	public void setEmail( String inEmail )
	{
		safePut(EMAIL_PROPERTY, inEmail);
	}
	/**
	 * @see com.openedit.users.User#setPassword(String)
	 */
	public void setPassword(String inPassword) throws UserManagerException
	{
		if(inPassword == null){
			return;
		}
		fieldPassword = inPassword;
	}

	/**
	 * @see com.openedit.users.User#getUserName()
	 */
	public String getUserName()
	{
		return getId();
	}

	public void setUserName( String inName)
	{
		setId( inName);
	}
	
	public String getShortDescription()
	{
		StringBuffer out = new StringBuffer();
		if ( getFirstName() != null)
		{
			out.append( getFirstName() );
			out.append(" ");
		}
		if ( getLastName() != null)
		{
			out.append(getLastName());
		}
		if( out.length() == 0)
		{
			if( getEmail() != null && Character.isDigit(getUserName().charAt(0) ) )
			{
				out.append(getEmail());
			}
			else
			{
				out.append( getUserName());
			}
		}
		return out.toString();
	}

	
	public String getScreenName()
	{
		String sn = (String)getProperty("screenname");
		
		if (sn == null)
		{
			return getShortDescription();
		}
		return sn;
	}

//	public String getClearPassword() throws UserManagerException
//	{
//		String password = getPassword();
//		if( !password.startsWith("DES:") )
//		{
//			return password;
//		}
//		else
//		{
//			return decrypt(password);
//		}
//	}
	
//	protected String decrypt(String inPassword) throws UserManagerException
//	{
//		long encryptionKey = 7939805759879765L; //TODO: Move this to authenticator
//		encryptionKey++;
//		try
//		{
//			StringEncryption encrypter = new StringEncryption( StringEncryption.DES_ENCRYPTION_SCHEME, encryptionKey + "42" + encryptionKey );
//			String code = inPassword.substring(4,inPassword.length()); //take off the DES:
//			String decryptedString = encrypter.decrypt( code );
//			return decryptedString;
//		} catch ( Exception ex)
//		{
//			throw new UserManagerException(ex);
//		}
//	}

	
	/**
	 * @see com.openedit.users.User#hasPermission(String)
	 */
	public boolean hasPermission(String inPermission)
	{
		for (Iterator iter = getGroups().iterator(); iter.hasNext();)
		{
			Group group = (Group) iter.next();

			if (group.hasPermission(inPermission))
			{
				return true;
			}
		}

		//cburkey, seems like users may need custom permissions so I added this
		String ok =  getPropertyContainer().getString( inPermission );

		if (Boolean.parseBoolean(ok))
		{
			return true;
		}

		return false;
	}
	
	public Object getProperty( String inPropertyName )
	{
		
		Object value =  getPropertyContainer().get( inPropertyName );
		if( value == null && fieldGroups != null) //this might be a new user
		{
			for (Iterator iterator = getGroups().iterator(); iterator.hasNext();)
			{
				Group group = (Group) iterator.next();
				value = group.get(inPropertyName);
				if ( value != null)
				{
					return value;
				}
			}
		}
		if ("".equals(value))
			return null;
		return value;
	}

	public String get(String inPropertyName)
	{
		if( "id".equals(inPropertyName))
		{
			return getId();
		}
		else if( "userName".equals(inPropertyName))
		{
			return getUserName();
		}
		else if( "password".equals(inPropertyName))
		{
			return getPassword();
		}
		else if( "name".equals(inPropertyName))
		{
			return getName();
		}
		else if( "screenname".equals(inPropertyName))
		{
			return getScreenName();
		}
		else if ("creationdate".equals(inPropertyName)){
			return DateStorageUtil.getStorageUtil().formatForStorage(getCreationDate());
		}
		else if( "groups".equals(inPropertyName)){
			StringBuffer groups = new StringBuffer();
			for (Iterator iterator = getGroups().iterator(); iterator.hasNext();)
			{
				Group group = (Group) iterator.next();
				groups.append(group.getId());
				if( iterator.hasNext() )
				{
					groups.append(" | ");
				}
			}
			if( groups.length() == 0)
			{
				return null;
			}
			return groups.toString();
		}
		return (String)getProperty(inPropertyName);
	}
	
	public boolean hasProperty(String inName )
	{
		boolean has = getProperties().containsKey(inName);
		return has;
	}
	
	public boolean isPropertyTrue(String inName)
	{
		String prop = (String)getProperties().get(inName);
		return Boolean.parseBoolean(prop);
	}
	public boolean isPropertyEqualTo(String inName,String inValue)
	{
		String prop = (String)getProperties().get(inName);
		return prop == inValue || (prop != null && prop.equals(inValue));
	}
	
	public List listGroupPermissions()
	{
		List all = new ArrayList();
		for (Iterator iter = getGroups().iterator(); iter.hasNext();)
		{
			Group group = (Group) iter.next();
			for (Iterator iterator = group.getPermissions().iterator(); iterator.hasNext();)
			{
				Object per = iterator.next();
				all.add(per.toString());
			}
		}
		return all;
	}
	
	/**
	 * Returns the password.
	 *
	 * @return String
	 */
	public String getPassword()
	{
		return fieldPassword;
	}

	/**
	 * Add the given group to the list of groups to which this user belongs. If the given group is
	 * already in the list of groups, this method does nothing.
	 *
	 * @param inGroup The group to which to add this user
	 *
	 * @throws UserManagerException DOCUMENT ME!
	 */
	public void addGroup(Group inGroup)
	{
		if( inGroup == null)
		{
			throw new OpenEditRuntimeException("Dont add null groups");
		}
		removeGroup(inGroup);
		getGroups().add(inGroup);
	}

	/**
	 * Remove the given group from the list of groups to which this user belongs.  If the given
	 * group is not in the list of groups, this method does nothing.
	 *
	 * @param inGroup The group from which to remove this user
	 *
	 * @throws UserManagerException DOCUMENT ME!
	 */
	public void removeGroup(Group inGroup)
	{
		for (Iterator iterator = getGroups().iterator(); iterator.hasNext();)
		{
			Group group = (Group) iterator.next();
			if( group.getId().equals(inGroup.getId()))
			{
				getGroups().remove(group);
				return;
			}
		}
	}

	public String toString()
	{
		return getScreenName();
	}

	public void clearGroups()
	{
		if ( fieldGroups != null)
		{
			getGroups().clear();			
		}
	}

	public boolean isInGroup(Group inGroup)
	{
		if( inGroup == null || inGroup.getId() == null)
		{
			return false;
		}
		for (Iterator iterator = getGroups().iterator(); iterator.hasNext();)
		{
			Group existing = (Group) iterator.next();
			if(existing.getId() == null){
				return false;
			}
			if( existing.getId().equals(inGroup.getId()))
			{
				return true;
			}
		}
		return false;
	}

	
	
	public boolean isInGroup(String inGroup)
	{	for (Iterator iterator = getGroups().iterator(); iterator.hasNext();)
		{
			Group existing = (Group) iterator.next();
			if(existing.getId() == null){
				return false;
			}
			if( existing.getId().equals(inGroup))
			{
				return true;
			}
		}
		return false;
	}

	
	
	public boolean isVirtual()
	{
		return fieldVirtual;
	}

	public void setVirtual(boolean inVirtual)
	{
		fieldVirtual = inVirtual;
	}

	public int compareTo(Object arg0)
	{
		User user = (User)arg0;
		return user.getShortDescription().compareTo(getShortDescription());
	}

	public String getId()
	{
		return fieldId;
	}

	public String getName()
	{
		return toString();
	}

	public void setName(String inName)
	{
		//not editable
	}
	
	public void setId(String inNewid)
	{
		fieldId = inNewid;
	}

	public void setProperties(Map<String,String> inProperties)
	{
		getProperties().putAll(inProperties);
	}

	public void setProperty(String inId, String inValue)
	{
		if("password".equals(inId)){
			setPassword(inValue);
			return;
		}
		
		if(inValue == null){
			getPropertyContainer().remove(inId);
		}
		if(inId == null){
			return;
		}
		getPropertyContainer().put(inId, inValue);
		if("lastname".equals(inId.toLowerCase())){
			setLastName(inValue);
		}
		if("firstname".equals(inId.toLowerCase())){
			setFirstName(inValue);
		}
	}

	public boolean isEnabled() 
	{
		String enabled = get("enabled");
		if( enabled == null )
		{
			return true;
		}
		return Boolean.parseBoolean(enabled);
	}

	public void setEnabled(boolean inEnabled)
	{
		setProperty("enabled", String.valueOf(inEnabled));
	}
	
	public Map listAllProperties()
	{
		Map all = new HashMap();
		
		for (Iterator iterator = getGroups().iterator(); iterator.hasNext();)
		{
			Group group = (Group) iterator.next();
			if( group.getProperties() != null)
			{
				all.putAll(group.getProperties() );
			}
		}
		if( getProperties() != null)
		{
			all.putAll(getProperties() );
		}
		return all;
	}

	public Collection<Group> getOrderedGroups()
	{
		List groups = new ArrayList(getGroups());
		Collections.sort(groups);
		return groups;
	}
	
	public Collection<Group> getEnabledOrderedGroups()
	{
		Collection<Group> groups = getOrderedGroups();
		ArrayList<Group> enabledgroups = new ArrayList<Group>();
		for (Iterator iterator = groups.iterator(); iterator.hasNext();) {
			Group group = (Group) iterator.next();
			if(Boolean.parseBoolean(group.get("enabled")))
			{
				enabledgroups.add(group);
			}
		}
		return enabledgroups;
	}
	public Collection getValues(String inPreference)
	{
		String val = get(inPreference);
		
		if (val == null)
			return null;
		
		String[] vals = val.split("\\s+");

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
				values.append(" ");
			}
		}
		setProperty(inKey,values.toString());
	}
}
