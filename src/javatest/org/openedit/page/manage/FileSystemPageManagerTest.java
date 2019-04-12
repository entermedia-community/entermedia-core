/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package org.openedit.page.manage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

import org.openedit.BaseTestCase;
import org.openedit.page.Page;
import org.openedit.page.manage.PageManager;
import org.openedit.util.OutputFiller;


/**
 * DOCUMENT ME!
 *
 * @author Eric Galluzzo
 */
public class FileSystemPageManagerTest extends BaseTestCase
{
	public static final String PATH_WITH_CONFIG = "withconfig.xml";
	public static final String PATH_WITH_DEFAULT = "/withdefault/index.html";
	public static final String PATH_WITH_MERGE = "/withdefault/merge.html";
	public static final String PATH_WITH_SITE = "/withdefault/site/merge.html";
	public static final String PATH_WITHOUT_CONFIG = "withoutconfig.html";
	public static final String NONEXISTENT_PATH = "this/does/not/exist.html";
	public static final String NEW_CONFIG = "<page><hi-mom/></page>";


	public FileSystemPageManagerTest(String inName)
	{
		super(inName);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @throws Exception
	 */
	public void testDirectoryWithDefaultConfig() throws Exception
	{
		Page page = getPage(PATH_WITH_DEFAULT);
		assertNotNull(page);
		assertEquals( PATH_WITH_DEFAULT, page.getPath() );
	}

/**  getConfigAsString() method was thoroughly tested but never used!

	public void testGetConfigAsString_Exists() throws Exception
	{
		assertTrue(fieldPageManager.getConfigAsString(PATH_WITH_CONFIG).length() > 0);
	}

	public void testGetConfigAsString_NoConfig() throws Exception
	{
		assertNull(fieldPageManager.getConfigAsString(PATH_WITHOUT_CONFIG));
	}

	public void testGetConfigAsString_NoSuchPath() throws Exception
	{
		assertNull(fieldPageManager.getConfigAsString(NONEXISTENT_PATH));
	}
*/
	/**
	 * DOCUMENT ME!
	 *
	 * @throws Exception
	 */
	public void testGetPage_Exists() throws Exception
	{
		Page page = getPage(PATH_WITH_CONFIG);
		assertTrue( page.exists() );
		assertEquals("/" + PATH_WITH_CONFIG,page.getPath() );
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @throws Exception
	 */
	public void testGetPage_NoConfig() throws Exception
	{
		assertNotNull(getPage(PATH_WITHOUT_CONFIG));
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @throws Exception
	 */
	public void testGetPage_NoSuchPath() throws Exception
	{
		assertNotNull(getPage(NONEXISTENT_PATH));
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @throws Exception
	 */
	public void testMergeConfig() throws Exception
	{
		Page page = getPage(PATH_WITH_MERGE);
		assertNotNull(page);
		assertEquals( PATH_WITH_MERGE, page.getPath() );
	}
	public void testSiteXconf() throws Exception
	{
		Page page = getPage(PATH_WITH_SITE);
		assertNotNull(page);
		assertEquals("subsales", page.get("section"));
		assertEquals("Open Edit",page.get("productName"));
		
		assertEquals( PATH_WITH_SITE, page.getPath() );		
	}
	public void testRemovePage_NoConfig() throws Exception
	{
		PageManager pageManager = getFixture().getPageManager();
		pageManager.removePage(getPage(PATH_WITHOUT_CONFIG));
		assertTrue(!getConfigFile(PATH_WITHOUT_CONFIG).exists());
	}


	public void testRemovePage_NoSuchPath() throws Exception
	{
		PageManager pageManager = getFixture().getPageManager();
		pageManager.removePage(getPage(NONEXISTENT_PATH));
		assertTrue(!getConfigFile(NONEXISTENT_PATH).exists());
	}

/** writeConfig() method was tested but never used!
 *  It seems like we could have getPageConfiguration() and putPageConfiguration()
 *  methods on the PageManager instead of getPageConfigurationReader() and
 *  getPageConfigurationWriter() methods.
 * 
	public void testRemovePage_Exists() throws Exception
	{
		fieldPageManager.writeConfig(PATH_WITHOUT_CONFIG, NEW_CONFIG);
		fieldPageManager.removePage(fieldPageManager.getPage(PATH_WITHOUT_CONFIG));
		assertTrue(!getConfigFile(PATH_WITHOUT_CONFIG).exists());
	}

	public void testWriteConfig_Exists() throws Exception
	{
		File configFile = getConfigFile(PATH_WITH_CONFIG);
		String oldContents = getFileContents(configFile);

		try
		{
			fieldPageManager.writeConfig(PATH_WITH_CONFIG, NEW_CONFIG);
			assertEquals(getFileContents(configFile), NEW_CONFIG);
		}
		finally
		{
			FileWriter writer = new FileWriter(configFile);
			writer.write(oldContents);
			writer.close();
		}
	}

	public void testWriteConfig_NoConfig() throws Exception
	{
		File configFile = getConfigFile(PATH_WITHOUT_CONFIG);

		try
		{
			fieldPageManager.writeConfig(PATH_WITHOUT_CONFIG, NEW_CONFIG);
			assertEquals(getFileContents(configFile), NEW_CONFIG);
		}
		finally
		{
			configFile.delete();
		}
	}

	public void testWriteConfig_NoSuchPath() throws Exception
	{
		File configFile = getConfigFile(NONEXISTENT_PATH);

		try
		{
			fieldPageManager.writeConfig(NONEXISTENT_PATH, NEW_CONFIG);
			assertEquals(getFileContents(configFile), NEW_CONFIG);
		}
		finally
		{
			configFile.delete();
		}
	}
*/

	public void testSiteConfig() throws Exception
	{
		Page withSite = getPage(PATH_WITH_CONFIG);
		assertNotNull(withSite);
		assertEquals("Open Edit",withSite.get("productName"));
	}

	protected File getConfigFile(String inPath) throws Exception
	{
		String baseName = inPath;
		int dotPos = inPath.lastIndexOf(".");

		if (dotPos >= 0)
		{
			baseName = inPath.substring(0, dotPos);
		}

		return new File(getRoot(), baseName + ".xconf");
	}

	protected String getFileContents(File inFile) throws IOException
	{
		FileReader reader = new FileReader(inFile);
		StringWriter writer = new StringWriter();

		try
		{
			new OutputFiller().fill(reader, writer);
		}
		finally
		{
			reader.close();
			writer.close();
		}

		return writer.toString();
	}
}
