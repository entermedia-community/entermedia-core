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

import java.util.HashMap;
import java.util.Map;

import com.openedit.users.BaseUserTest;
import com.openedit.users.Group;
import com.openedit.users.User;



/**
 * This is an abstract test for {@link User} implementations.  Concrete {@link User}s should
 * subclass this testcase and add their own tests.
 *
 * @author Eric Galluzzo
 */
public abstract class UserTest extends BaseUserTest
{
	protected User fieldUser;

	public UserTest(String inName)
	{
		super(inName);
	}

	public void testHasPermission() throws Exception
	{
		assertTrue("User should have permission1", fieldUser.hasPermission("permission1"));
		assertTrue("User should have permission3", fieldUser.hasPermission("permission3"));
	}

	public void testHasPermission_Negative() throws Exception
	{
		assertTrue(
			"User should not have nosuchpermission", !fieldUser.hasPermission("nosuchpermission"));
	}

	public void testPut() throws Exception
	{
		fieldUser.put("foo", "bar");
		assertEquals("bar", fieldUser.get("foo"));
	}

	public void testPutAll() throws Exception
	{
		Map map = new HashMap();
		map.put( "foo", "bar" );
		map.put( "baz", "wibble" );
		fieldUser.putAll(map);
		assertEquals("bar", fieldUser.get("foo"));
		assertEquals("wibble", fieldUser.get("baz"));
	}

	public void testSetPassword() throws Exception
	{
		fieldUser.setPassword("newpwd");
		assertTrue(
			"Should have authenticated with new password successfully",
			fieldUserManager.authenticate(fieldUser, "newpwd"));
		assertTrue(
			"Should not have authenticated with old password",
			!fieldUserManager.authenticate(fieldUser, "testpwd"));
	}
	
	public void testGetCreationDate() throws Exception
	{
		long currentTime = System.currentTimeMillis();
		// There shouldn't be more than about 2 seconds between the two dates,
		// even if we're using a really slow user manager like a database-backed
		// one.
		assertTrue( "Dates are not within 2 seconds of each other",
			currentTime - fieldUser.getCreationDate().getTime() <= 2000 );
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		fieldUserManager = createUserManager();
		fieldUser = fieldUserManager.createUser("testuser", "testpwd");

		Group group = fieldUserManager.createGroup("testgroup");
		group.addPermission("permission1");
		fieldUser.addGroup(group );
		fieldUserManager.saveGroup(group);
		Group group2 = fieldUserManager.createGroup("testgroup2");
		group2.addPermission("permission2");
		group2.addPermission("permission3");
		fieldUser.addGroup(group2);
		fieldUserManager.saveGroup(group2);
		fieldUserManager.saveUser(fieldUser);
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		deleteUserManager(fieldUserManager);
	}
}
