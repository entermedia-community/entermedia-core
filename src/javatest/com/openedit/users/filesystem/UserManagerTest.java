/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package com.openedit.users.filesystem;

import java.util.Collection;

import com.openedit.hittracker.HitTracker;
import com.openedit.users.BaseUserTest;
import com.openedit.users.Group;
import com.openedit.users.User;
import com.openedit.users.UserManager;


/**
 * This is an abstract test for {@link UserManager} implementations.  Concrete {@link UserManager}s
 * should subclass this testcase and add their own tests.
 *
 * @author Eric Galluzzo
 */
public class UserManagerTest extends BaseUserTest
{

	public UserManagerTest(String inName)
	{
		super(inName);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @throws Exception
	 */
	public void testAuthenticate() throws Exception
	{
		assertTrue(
			"Should have authenticated user1",
			fieldUserManager.authenticate(fieldUserManager.getUser("user1"), "user1pwd"));
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @throws Exception
	 */
	public void testAuthenticate_Negative() throws Exception
	{
		assertTrue(
			"Should not have authenticated user1",
			!fieldUserManager.authenticate(fieldUserManager.getUser("user1"), "thisiswrong"));
	}

	public void testLoadPermissions() throws Exception
	{
//		int size = fieldUserManager.getPermissionsManager().getAllPermissions().size();
//		assertTrue( size > 0);
		String v = "this is;: a test";
		String[] two = v.split(";");
		assertEquals( two.length , 2);

		two = v.split(":");
		assertEquals( two.length , 2);

	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @throws Exception
	 */
	public void testCreateUser() throws Exception
	{
		assertEquals("Number of users", 6, fieldUserManager.listUserNames().size());
		User newUser = fieldUserManager.createUser("newuser", "mypassword");
		assertEquals("Username", "newuser", newUser.getUserName());
		assertEquals("Number of groups", 0, newUser.getGroups().size());
		assertEquals("Number of users", 7, fieldUserManager.listUserNames().size());
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @throws Exception
	 */
	public void testDeleteGroup() throws Exception
	{
		Group group1 = fieldUserManager.getGroup("group1");
		fieldUserManager.deleteGroup(group1);
		assertEquals("Number of groups", 4, fieldUserManager.getGroups().size());
		assertEquals(
			"Number of groups of which user1 is a member", 1,
			fieldUserManager.getUser("user1").getGroups().size());
		assertEquals(
			"Number of groups of which user2 is a member", 1,
			fieldUserManager.getUser("user2").getGroups().size());
		assertTrue(
			"user1 should not be in group1",
			!fieldUserManager.getUser("user1").getGroups().contains(group1));
		assertTrue(
			"user2 should not be in group1",
			!fieldUserManager.getUser("user2").getGroups().contains(group1));
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @throws Exception
	 */
	public void testDeleteUser() throws Exception
	{
		User user1 = fieldUserManager.getUser("user1");
		fieldUserManager.deleteUser(fieldUserManager.getUser("user1"));
		assertEquals("Number of users", 5, fieldUserManager.listUserNames().size());
		assertEquals(
			"Number of users in group1", 0, fieldUserManager.getUsersInGroup("group1").size());
		assertEquals(
			"Number of users in group2", 1, fieldUserManager.getUsersInGroup("group2").size());
		assertTrue(
			"group1 should not still contain user1",
			!fieldUserManager.getUsersInGroup("group1").contains(user1));
		assertTrue(
			"group2 should not still contain user1",
			!fieldUserManager.getUsersInGroup("group2").contains(user1));
	}
	

	/**
	 * DOCUMENT ME!
	 *
	 * @throws Exception
	 */
	public void testGetGroup_Group1() throws Exception
	{
		Group group1 = fieldUserManager.getGroup("group1");
		assertEquals("Group name", "group1", group1.getName());
		assertEquals("Number of users", 1, fieldUserManager.getUsersInGroup(group1).size());
		assertTrue(
			"group1 should contain user1",
			fieldUserManager.getUsersInGroup("group1").contains(fieldUserManager.getUser("user1")));
		assertTrue(
			"group1 should contain user2",
			fieldUserManager.getUsersInGroup("group2").contains(fieldUserManager.getUser("user2")));
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @throws Exception
	 */
	public void testGetGroup_Group2() throws Exception
	{
		Group group2 = fieldUserManager.getGroup("group2");
		assertEquals("Group name", "group2", group2.getName());
		assertTrue(
			"group2 should contain user1",
			fieldUserManager.getUsersInGroup("group2").contains(fieldUserManager.getUser("user1")));
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @throws Exception
	 */
	public void testGetGroup_Group3() throws Exception
	{
		Group group3 = fieldUserManager.getGroup("group3");
		assertEquals("Group name", "group3", group3.getName());
		assertEquals("Number of users", 1, fieldUserManager.getUsersInGroup(group3).size());
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @throws Exception
	 */
	public void testGetGroups() throws Exception
	{
		assertEquals("Number of groups", 5, fieldUserManager.getGroups().size());

		for (int i = 1; i <= 3; i++)
		{
			assertTrue(
				"group" + i + " exists",
				fieldUserManager.getGroups().contains(fieldUserManager.getGroup("group" + i)));
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @throws Exception
	 */
	public void testGetUser_User1() throws Exception
	{
		User user1 = fieldUserManager.getUser("user1");
		assertNotNull(user1);
		assertEquals("Username", "user1", user1.getUserName());

		Collection groups = user1.getGroups();
		assertEquals("Number of groups", 2, groups.size());
		assertTrue(
			"user1 should be in group1", groups.contains(fieldUserManager.getGroup("group1")));
		assertTrue(
			"user1 should be in group2", groups.contains(fieldUserManager.getGroup("group2")));
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @throws Exception
	 */
	public void testGetUser_User2() throws Exception
	{
		User user2 = fieldUserManager.getUser("user2");
		assertNotNull(user2);
		assertEquals("Username", "user2", user2.getUserName());

		Collection groups = user2.getGroups();
		assertEquals("Number of groups", 1, groups.size());
		assertTrue(
			"user2 should be in group2", groups.contains(fieldUserManager.getGroup("group2")));
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @throws Exception
	 */
	public void testGetUser_User3() throws Exception
	{
		User user3 = fieldUserManager.getUser("user3");
		assertNotNull(user3);
		assertEquals("Username", "user3", user3.getUserName());
		assertEquals("Number of groups", 1, user3.getGroups().size());
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @throws Exception
	 */
	public void testGetUsers() throws Exception
	{
		HitTracker users = fieldUserManager.getUsers();
		assertEquals("Number of users", 6, users.size());

		for (int i = 1; i <= 3; i++)
		{
			User found = fieldUserManager.getUser("user" + i);
			assertTrue(
				"user" + i + " exists", users.contains(found));
		}
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
			fieldUserManager = createUserManager();
	
			// Add the following users and groups:
			//
			//     user1 => group1, group2
			//     user2 => group1
			//     user3 => (no groups)
			//
			//     group1 => user1, user2
			//     group2 => user1
			//     group3 => (no users)
			Group group1 = fieldUserManager.createGroup("group1");
			Group group2 = fieldUserManager.createGroup("group2");
			Group group3 = fieldUserManager.createGroup("group3");
			User user1 = fieldUserManager.createUser("user1", "user1pwd");
			User user2 = fieldUserManager.createUser("user2", "user2pwd");
			User user3 = fieldUserManager.createUser("user3", "user3pwd");
	
			user1.addGroup(group1);
			user1.addGroup(group2);
			
			user2.addGroup(group2);
			user3.addGroup(group3);
	}
	protected void tearDown() throws Exception
	{
		deleteUserManager(fieldUserManager);
	}
	
	public void testEncryption() throws Exception
	{
		fieldUserManager = createUserManager();

		String pw = "aniara1";
		String encpw = "DES:FSvfgMqcPvM=";
		String enc = fieldUserManager.getStringEncryption().encrypt(pw);
		assertEquals(enc,encpw);
		
		String val = fieldUserManager.getStringEncryption().decrypt(enc);
		
		User user = new FileSystemUser();
		user.setPassword(encpw);
		assertTrue( fieldUserManager.authenticate(user, encpw) );
		assertTrue( fieldUserManager.authenticate(user, pw) );
		assertEquals(val, pw);
	}
}
