/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

/*
 * Created on Jun 18, 2003
 *
 */
package org.openedit;

import java.io.File;

import org.openedit.modules.BaseModule;
import org.openedit.page.Page;

import junit.framework.TestCase;


/**
 * DOCUMENT ME!
 *
 * @author cburkey
 */
public class BaseTestCase extends TestCase
{
	protected  TestFixture fieldFixture;
	protected  static TestFixture fieldStaticFixture;

	/**
	 * 
	 */
	public BaseTestCase()
	{
		super(""); //this is not needed in newer versions of JUnit
	}
	/**
	 * Constructor for BaseTestCase.
	 *
	 * @param arg0
	 */
	public BaseTestCase(String arg0)
	{
		super(arg0);
	}

	public Page getPage( String inPath ) throws OpenEditException
	{
		return getFixture().getPageManager().getPage( inPath, true );
	}
	/**
	 * DOCUMENT ME!
	 *
	 * @param inFixture
	 */
	public void setFixture(TestFixture inFixture)
	{
		fieldFixture = inFixture;
	}

	public TestFixture getFixture()
	{
//		if (fieldFixture == null)
//		{
//			fieldFixture = new TestFixture();
//		}
//		return fieldFixture;
		return getStaticFixture();
	}
	public TestFixture getStaticFixture()
	{
		if (fieldStaticFixture == null)
		{
			fieldStaticFixture = new TestFixture();
			oneTimeSetup();
		}
		return fieldStaticFixture;
	}
	
	protected void oneTimeSetup() {
		// TODO Auto-generated method stub
		
	}
	protected void tearDown() throws Exception
	{
		super.tearDown();
		if ( fieldFixture != null)
		{
			getFixture().getWebServer().getOpenEditEngine().shutdown();
		}
	}
	/**	Delete the specified directory and all files within it */
	protected void deleteDirectory(File directory)
	{
		File[] containedFiles = directory.listFiles();
		if (containedFiles != null)
		{
			for (int n = 0; n < containedFiles.length; n++)
			{
				File file = containedFiles[ n ];
				if (file.isDirectory())
				{
					deleteDirectory( file );
				}
				else
				{
					file.delete();
				}
			}
		}
		directory.delete();
	}
	
	protected File getRoot()
	{
		return getFixture().getWebServer().getRootDirectory();
	}
	
	protected BaseModule getModule( String inKey )
	{
		return getFixture().getModuleManager().getModule( inKey );
	}
	
	protected Object getBean( String inKey )
	{
		return getFixture().getModuleManager().getBean( inKey );
	}
}
