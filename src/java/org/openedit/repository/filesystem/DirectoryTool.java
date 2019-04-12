/*
 * Created on Dec 23, 2004
 */
package org.openedit.repository.filesystem;

import java.io.File;

import org.openedit.util.FileUtils;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class DirectoryTool
{
	protected File fieldRootDirectory;
	protected FileUtils fieldFileUtils = new FileUtils();

	public File getRootDirectory()
	{
		if ( fieldRootDirectory == null )
		{
			String tempDir = System.getProperty( "java.io.tmpdir" );
			fieldRootDirectory = new File( tempDir, "oe_version_test"  ); //use the same one each time
			getFileUtils().deleteAll( fieldRootDirectory );
			fieldRootDirectory.mkdir();
		}
		return fieldRootDirectory;
	}
	
	public FileUtils getFileUtils()
	{
		return fieldFileUtils;
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	public void tearDown() throws Exception
	{
		getFileUtils().deleteAll( getRootDirectory() );
	}
}
