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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openedit.Data;


/**
 * This interface represents a user.
 *
 * @author Eric and Matt
 */
public interface User extends Data
{
	public static final String FIRST_NAME_PROPERTY = "firstName";
	public static final String LAST_NAME_PROPERTY = "lastName";
	public static final String EMAIL_PROPERTY = "email";
	
	/**
	 * Not a real property, just a string that can be used to refer to the
	 * username in, e.g., search indices.
	 */
	public static final String USERNAME_PROPERTY = "user-name";

	/**
	 * Retrieve this user's first name.  This is a convenience method that is the same as calling
	 * <code>get( FIRST_NAME_PROPERTY )</code>.
	 *
	 * @return The user's first name, or <code>null</code> if the user has no first name
	 */
	String getFirstName();

	/**
	 * Retrieve this user's last name.  This is a convenience method that is the same as calling
	 * <code>get( LAST_NAME_PROPERTY )</code>.
	 *
	 * @return The user's last name, or <code>null</code> if the user has no last name
	 */
	String getLastName();

	/**
	 * Retrieve this user's email address.  This is a convenience method that is the same as
	 * calling <code>get( EMAIL_PROPERTY )</code>.
	 *
	 * @return The user's email address, or <code>null</code> if the user has no last name
	 */
	String getEmail();
	
	/**
	 * Set this user's first name.  This is a convenience method that is the
	 * same as calling <code>put( FIRST_NAME_PROPERTY, inFirstName )</code>.
	 * 
	 * @param inFirstName  The user's first name, or <code>null</code> to clear
	 *                     the first name
	 */
	void setFirstName( String inFirstName );
	
	/**
	 * Set this user's last name.  This is a convenience method that is the
	 * same as calling <code>put( LAST_NAME_PROPERTY, inLastName )</code>.
	 * 
	 * @param inFirstName  The user's last name, or <code>null</code> to clear
	 *                     the last name
	 */
	void setLastName( String inLastName );
	
	/**
	 * Set this user's email address.  This is a convenience method that is the
	 * same as calling <code>put( EMAIL_PROPERTY, inEmail )</code>.
	 * 
	 * @param inFirstName  The user's email address, or <code>null</code> to
	 *                     clear the email address
	 */
	void setEmail( String inEmail );

	/**
	 * Retrieve all the groups of which this user is a member.
	 *
	 * @return A collection of {@link Group}s
	 */
	Collection getGroups();
	
	public boolean isInGroup(String inGroupId); 
	/**
	 * DOCUMENT ME!
	 * @return
	 */
	public String getPassword();
	
	//public String getClearPassword();
	
	/**
	 * Set this user's password to the given password.
	 * 
	 * <p>
	 * FIXME: Do we need to pass in the old password too?
	 * </p>
	 *
	 * @param inPassword The new password
	 *
	 * @throws UserManagerException If the password could not be set
	 */
	void setPassword(String inPassword) throws UserManagerException;

	/**
	 * Retrieve the user's login name (e.g. "mavery").
	 *
	 * @return The login name
	 */
	String getUserName();
	
	
	/**
	 * Determines whethwer or not this user is allowed to login.
	 * @return
	 */
	public boolean isEnabled();
	
	/**
	 * Enables or disables this user. A disabled user will not be allowed to login.
	 * @param inEnabled <code>true</code> to enable the user to login. <code>false</code> otherwise. 
	 */
	public void setEnabled(boolean inEnabled);
	
	
	/**
	 * Determine whether or not this user has the given permission, by looking through the user's
	 * groups.
	 *
	 * @param inPermission The permission
	 *
	 * @return <code>true</code> if this user has the given permission, <code>false</code> if not
	 */
	boolean hasPermission(String inPermission);

	boolean hasProperty(String inProperty);

	public String getShortDescription();

	void setUserName(String inUserName);
	
	public List listGroupPermissions();

	public void addGroup(Group inGroup);
	
	void removeGroup(Group inGroup);

	boolean isInGroup(Group inGroup);
	
	public boolean isVirtual();
	public void setVirtual(boolean inVirtual);

	public String getScreenName();
	Map listAllProperties();

	Collection getValues(String inString);

	boolean getBoolean(String inString);

	void setGroups(Collection inGroupslist);
	
	
	public String getEnterMediaKey(); //Hash

	boolean isInGroup(Collection<String> inEditorgroups);
	


	
	
	
}
