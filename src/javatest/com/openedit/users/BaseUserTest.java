/*
 * Created on Oct 11, 2006
 */
package com.openedit.users;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.openedit.repository.ContentItem;

import com.openedit.BaseTestCase;
import com.openedit.page.Page;
import com.openedit.users.filesystem.FileSystemUserManager;
import com.openedit.util.StringEncryption;

public class BaseUserTest extends BaseTestCase
{
	protected UserManager fieldUserManager;


		public BaseUserTest(String inName)
		{
			super( inName);
		}
		
		public void testGetOrderedGroups() throws Exception{
			BaseUser user = new BaseUser(){

				@Override
				public Collection getGroups() {
					Set groups = new LinkedHashSet<Group>();
					Group g1 = new BaseGroup();
					g1.setName("cat");
					g1.setId("1");
					Group g2 = new BaseGroup();
					g2.setName("Dog");
					g2.setId("2");
					Group g3 = new BaseGroup();
					g3.setName("bear");
					g3.setId("3");
					Group g4 = new BaseGroup();
					g4.setName("deer");
					g4.setId("4");
					groups.add(g1);
					groups.add(g2);
					groups.add(g3);
					groups.add(g4);
					return groups;
				}
				
			};
			Collection<Group> result = null;
			result =  user.getOrderedGroups();
			assertNotNull("NULL result", result);
			Object[] resultArray = result.toArray();
			
			assertEquals("3", ((Group)resultArray[0]).getId());
			assertEquals("1", ((Group)resultArray[1]).getId());
			assertEquals("4", ((Group)resultArray[2]).getId());
			assertEquals("2", ((Group)resultArray[3]).getId());
		}
		
		/**
		 * DOCUMENT ME!
		 *
		 * @return
		 *
		 * @throws IOException
		 */
		public UserManager createUserManager() throws IOException
		{
			FileSystemUserManager userManager = new FileSystemUserManager();
			userManager.setUserDirectory("/test/users");

			ContentItem stub = getFixture().getPageManager().getRepository().getStub( "/test/users");
			File f = new File(stub.getAbsolutePath());
		   f.mkdirs();

			stub = getFixture().getPageManager().getRepository().getStub( "/test/groups");
			f = new File(stub.getAbsolutePath());
		   f.mkdirs();


			userManager.setGroupDirectory("/test/groups");
			Authenticator authen = (Authenticator)getFixture().getModuleManager().getBean("authenticator");
			userManager.setAuthenticator(authen);
			
			userManager.setStringEncryption((StringEncryption)getFixture().getModuleManager().getBean("stringEncryption"));
			userManager.setPageManager(getFixture().getPageManager());
			return userManager;
		}

		/**
		 * DOCUMENT ME!
		 *
		 * @param inUserManager
		 *
		 * @throws IOException
		 */
		public void deleteUserManager(UserManager inUserManager)
			throws IOException
		{
			FileSystemUserManager userManager = (FileSystemUserManager) inUserManager;
			deleteRecursively(userManager.getUserDirectory());
			deleteRecursively(userManager.getGroupDirectory());
		}

		/**
		 * Create an empty directory in a temporary location with a random name.
		 *
		 * @return The new directory
		 *
		 * @throws IOException DOCUMENT ME!
		 */
		protected File createTempDir() throws IOException
		{
			File file = File.createTempFile("FileSystemUserManagerTest", null);
			file.delete();
			file.mkdir();

			return file;
		}

		/**
		 * Delete the given file or directory and all its sub-directories.
		 *
		 * @param inFile The file or directory to delete
		 */
		protected void deleteRecursively(String inPath)
		{
			Page page = getFixture().getPageManager().getPage(inPath);
			getFixture().getPageManager().removePage(page);
		}
	}

	
