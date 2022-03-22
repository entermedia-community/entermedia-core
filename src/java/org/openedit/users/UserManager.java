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

import java.util.List;

import org.openedit.CatalogEnabled;
import org.openedit.OpenEditException;
import org.openedit.WebPageRequest;
import org.openedit.event.EventManager;
import org.openedit.hittracker.HitTracker;
import org.openedit.users.authenticate.AuthenticationRequest;
import org.openedit.util.StringEncryption;


/**
 * This interface allows the caller to retrieve users.
 *
 * @author Eric and Matt
 */
public interface UserManager extends CatalogEnabled
{
	
	/**
	 * Retrieve the group with the given name.
	 *
	 * @param inGroupName The group name
	 *
	 * @return The group, or <code>null</code> if there is no such group
	 *
	 * @throws UserManagerException If something went wrong trying to retrieve the group
	 */
	Group getGroup(String inGroupId) throws UserManagerException;

	/**
	 * Get all the groups managed by this user manager.
	 *
	 * @return A collection of {@link Group}s
	 *
	 * @throws UserManagerException If the list of groups could not be retrieved
	 */
	HitTracker getGroups();

	/**
	 * Retrieve the user with the given username.
	 *
	 * @param inUserName The username
	 *
	 * @return The user, or <code>null</code> if there is no such user
	 *
	 * @throws UserManagerException If something went wrong trying to retrieve the user
	 */
	User getUser(String inUserName) throws UserManagerException;

	User getUser(String inUserName, boolean fromCache);

	/**
	 * Get all the users managed by this user manager.
	 *
	 * @return A collection of {@link User}s
	 *
	 * @throws UserManagerException If the list of users could not be retrieved
	 */
	HitTracker getUsers();

	/**
	 * Authenticate the given user.
	 *
	 * @param inUser The user to authenticate
	 *
	 * @return <code>true</code> if the user was authenticated successfully, <code>false</code> if
	 * 		   not
	 *
	 * @throws UserManagerException If something went wrong trying to authenticate the user
	 */
	boolean authenticate(AuthenticationRequest inReq);

	boolean authenticate(User inUser, String inPassword);

	/**
	 * Create a new group with the given name.
	 *
	 * @param inGroupId The new group's ID
	 *
	 * @return The new group
	 *
	 * @throws DuplicateGroupException If there is already a group with the given Id
	 * @throws UserManagerException If the group could not be created for some reason
	 */
	Group createGroup() throws UserManagerException;
	Group createGroup(String inGroupId, String inGroupName) throws UserManagerException;

	/**
	 * Create a user with the given username and password.
	 *
	 * @param inUserName The new user's username
	 * @param inPassword The new user's password
	 *
	 * @return The new user
	 *
	 * @throws DuplicateUserException If there is already a user with the given username
	 * @throws UserManagerException If the user could not be created for some reason
	 */
	User createUser(String inUserName, String inPassword)
		throws UserManagerException;

	/**
	 * Delete the given group.
	 *
	 * @param inGroup The group to delete
	 *
	 * @throws UserManagerException If the group could not be deleted
	 */
	void deleteGroup(Group inGroup) throws UserManagerException;

	/**
	 * Delete the given user.
	 *
	 * @param inUser The user to delete
	 *
	 * @throws UserManagerException If the user could not be deleted
	 */
	void deleteUser(User inUser) throws UserManagerException;

	/**
	 * Delete the given groups.
	 *
	 * @param inGroups The groups to delete
	 *
	 * @throws UserManagerException If the groups could not be deleted
	 */
	public void deleteGroups(List inGroups) throws UserManagerException;

	/**
	 * Delete the given users.
	 *
	 * @param inUsers The users to delete
	 *
	 * @throws UserManagerException If the users could not be deleted
	 */
	public void deleteUsers(List inUsers) throws UserManagerException;
	

	/**
	 * @param emailaddress
	 * @return
	 */
	public User getUserByEmail(String emailaddress) throws UserManagerException;
	
	
	/**
	 * Saves the given user to persistent storage.
	 * 
	 * @param inUser  The user to save
	 */
	void saveUser( User inUser );

	/**
	 * Saves the given group to persistent storage.
	 * 
	 * @param inGroup  The group to save
	 */
	void saveGroup( Group inGroup );

	HitTracker getUsersInGroup(Group inGroup);

	HitTracker getUsersInGroup(String inString);
	
	Authenticator getAuthenticator();
	
	StringEncryption getStringEncryption();

	public String encryptPassword( User inUser ) throws OpenEditException;

	public String decryptPassword( User inUser ) throws OpenEditException;
	
	public void setEventManager( EventManager inHandler);
	
	public void logout(User inUser);

	User createGuestUser(String inAccount, String inPassword, String inGroupname);
	
	public String getScreenName(String userName);
	void flush();


	UserSearcher getUserSearcher();
	GroupSearcher getGroupSearcher();

	AuthenticationRequest createAuthenticationRequest(WebPageRequest inReq, String inPassword, User inUser);

	public void fireUserEvent(User inUser, String inOperation) ;

	String getEnterMediaKey(User user);

	void logIntoApp(WebPageRequest inReq,User inUser);

	String createNewTempLoginKey(String userid, String email, String first,String last);

	User checkForNewUser(String inEmail, String inTemplogincode, String groupid);
	
}
