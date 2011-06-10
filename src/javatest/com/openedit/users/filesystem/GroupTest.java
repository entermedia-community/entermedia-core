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

import com.openedit.users.BaseUserTest;
import com.openedit.users.Group;
import com.openedit.users.User;


/**
 * This is an abstract test for {@link Group} implementations.  Concrete {@link Group}s should
 * subclass this testcase and add their own tests.
 *
 * @author Eric Galluzzo
 */
public class GroupTest extends BaseUserTest
{
	protected Group fieldGroup;
	protected User fieldUser;

	public GroupTest(String inName)
	{
		super(inName);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @throws Exception
	 */
	public void hasPermission() throws Exception
	{
		assertTrue("Should have testpermission", fieldGroup.hasPermission("testpermission"));
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @throws Exception
	 */
	public void testAddPermission() throws Exception
	{
		fieldGroup.addPermission("newpermission");
		assertEquals("Number of permissions", 2, fieldGroup.getPermissions().size());
		assertTrue("Should now have newpermission", fieldGroup.hasPermission("newpermission"));
	}


	/**
	 * DOCUMENT ME!
	 *
	 * @throws Exception
	 */
	public void testRemovePermission() throws Exception
	{
		fieldGroup.removePermission("testpermission");
		assertEquals("Number of permissions", 0, fieldGroup.getPermissions().size());
		assertTrue(
			"Should not still have testpermission", !fieldGroup.hasPermission("testpermission"));
	}



	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		fieldUserManager = createUserManager();
		fieldUser = fieldUserManager.createUser("testuser", "testpwd");

		fieldGroup = fieldUserManager.createGroup("testgroup");
		fieldGroup.addPermission("testpermission");
		fieldUser.addGroup(fieldGroup);
		fieldUserManager.saveGroup(fieldGroup);
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		deleteUserManager(fieldUserManager);
	}
}
