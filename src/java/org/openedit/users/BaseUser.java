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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.openedit.MultiValued;
import org.openedit.OpenEditRuntimeException;
import org.openedit.data.BaseData;


/**
 * This class represents a user as an XML file.
 *
 * @author Eric and Matt
 */
public class BaseUser extends BaseData implements User, Comparable
{
	protected GroupSearcher fieldGroupSearcher;
	
	public GroupSearcher getGroupSearcher()
	{
		return fieldGroupSearcher;
	}

	public void setGroupSearcher(GroupSearcher inGroupSearcher)
	{
		fieldGroupSearcher = inGroupSearcher;
	}

	public BaseUser()
	{
		super();
	}
	
	
	public String getEmail()
	{
		return get(EMAIL_PROPERTY);
	}

	public String getFirstName()
	{
		return get(FIRST_NAME_PROPERTY);
	}
	public void setFirstName( String inName)
	{
		setProperty( FIRST_NAME_PROPERTY, inName);
	}
	/**
	 * @see org.openedit.users.User#getGroups()
	 */
	public Collection getGroups()
	{
		Collection groups = (Collection)getValue("groups");
		if( groups == null)
		{
			groups = Collections.emptyList();
		}
		return groups;
	}

	public void setGroups(Collection inGroups)
	{
		setValue("groups",inGroups);
	}
	public String getLastName()
	{
		return get(LAST_NAME_PROPERTY);
	}

	public void setLastName( String inName)
	{
		setProperty( LAST_NAME_PROPERTY, inName);
	}
	public void setEmail( String inEmail )
	{
		setProperty(EMAIL_PROPERTY, inEmail);
	}
	/**
	 * @see org.openedit.users.User#setPassword(String)
	 */
	public void setPassword(String inPassword) throws UserManagerException
	{
		if(inPassword == null){
			return;
		}
		setProperty("password",inPassword);
	}

	/**
	 * @see org.openedit.users.User#getUserName()
	 */
	public String getUserName()
	{
		return getId();
	}

	public void setUserName( String inName)
	{
		setId(inName);
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
			if( getUserName().length() < 8 && !Character.isDigit(getUserName().charAt(0) ) )
			{
				out.append( " - " );
				out.append( getUserName() );
			}
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

	public String get(String inPropertyName)
	{
		String value = super.get( inPropertyName );
//		if( value == null ) //this might be a new user
//		{
//			for (Iterator iterator = getGroups().iterator(); iterator.hasNext();)
//			{
//				Group group = (Group) iterator.next();
//				value = group.get(inPropertyName);
//				if ( value != null)
//				{
//					return value;
//				}
//			}
//		}
		if ("".equals(value))
			return null;
		return value;
	}
	
	@Override
	public Object getValue(String inKey)
	{
		if( "name".equals( inKey ) )
		{
			String val = getShortDescription();
			if( val != null)
			{
				return val;
			}
		}
		return super.getValue(inKey);
	}
	
	public String getScreenName()
	{
		String sn = (String)get("screenname");
		
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
	 * @see org.openedit.users.User#hasPermission(String)
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
		String ok =  get( inPermission );

		if (Boolean.parseBoolean(ok))
		{
			return true;
		}

		return false;
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
		return get("password");
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
		addValue("groups",inGroup);
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
		removeValue("groups",inGroup);
	}

	public String toString()
	{
		return getScreenName();
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
		return getBoolean("virtual");
	}

	public void setVirtual(boolean inVirtual)
	{
		setValue("virtual",inVirtual);
	}

	public int compareTo(Object arg0)
	{
		User user = (User)arg0;
		return user.getShortDescription().compareTo(getShortDescription());
	}

	public void setProperties(Map inProperties)
	{
		getProperties().putAll(inProperties);
		if( inProperties.containsKey("groups"))
		{
			setValue("groups", inProperties.get("groups"));
		}
	}
	@Override
	public void setValue(String inKey, Object inValue)
	{
		if( inKey.equals("groups"))
		{
			Collection groups = new ArrayList();
			Collection<String> vals = null;
			if( inValue instanceof String)
			{
				inValue = parseList((String)inValue);				
			}
			Collection objects = (Collection)inValue;
			for (Iterator iterator = objects.iterator(); iterator.hasNext();)
			{
				Object object = (Object) iterator.next();
				if(object instanceof Group)
				{
					groups.add(object);
				}
				else
				{
					Group group = getGroupSearcher().getGroup((String)object);
					if( group != null)
					{
						groups.add( group );
					}
				}
			}
			inValue = groups;
		}
		super.setValue(inKey, inValue);
	}
	protected Collection parseList(String inValue)
	{
		String[] vals = null; 
		if( inValue.contains("|") )
		{
			vals = MultiValued.VALUEDELMITER.split(inValue);
		}
		else
		{
			vals = new String[]{inValue};
		}
		return Arrays.asList(vals);
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

	public void put(String inString, String inString2)
	{
		setValue(inString,inString2);
		
	}

	@Override
	public String getEnterMediaKey()
	{
		// TODO Auto-generated method stub
		return null;
	}



}
