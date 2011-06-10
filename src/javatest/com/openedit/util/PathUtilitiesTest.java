/*
 * Created on Mar 17, 2004
 *
 */
package com.openedit.util;


import junit.framework.TestCase;

/**
 * @author dbrown
 *
 */
public class PathUtilitiesTest extends TestCase
{
	private static final String FILE_PATH = "/basedir/resources/source/test1.html";

	public PathUtilitiesTest(String arg0)
	{
		super(arg0);
	}

	public void testResolveRelative1()
	{
		String relativePath = "./layout.html";
		String combinedPath = PathUtilities.resolveRelativePath( relativePath, FILE_PATH );
		assertEquals( "/basedir/resources/source/layout.html", combinedPath );
	}

	public void testResolveRelative2()
	{
		String relativePath = "../layout.html";
		String combinedPath = PathUtilities.resolveRelativePath( relativePath, FILE_PATH );
		assertEquals( "/basedir/resources/layout.html", combinedPath );
	}

	public void testResolveRelative3()
	{
		String relativePath = "./graphics/image.gif";
		String combinedPath = PathUtilities.resolveRelativePath( relativePath, FILE_PATH );
		assertEquals( "/basedir/resources/source/graphics/image.gif", combinedPath );
	}

	public void testResolveRelative4()
	{
		String relativePath = "../../junk";
		String combinedPath = PathUtilities.resolveRelativePath( relativePath, FILE_PATH );
		assertEquals( "/basedir/junk", combinedPath );
	}
	public void testResolveRelativeTop()
	{
		String relativePath = "../../../../../../junk";
		String combinedPath = PathUtilities.resolveRelativePath( relativePath, FILE_PATH );
		assertEquals( "/junk", combinedPath );
	}

	public void testResolveRelative5()
	{
		String relativePath = "/basedir/resources/./source/layout.html";
		String combinedPath = PathUtilities.resolveRelativePath( relativePath, FILE_PATH );
		assertEquals( "/basedir/resources/source/layout.html", combinedPath );
	}

	public void testResolveRelative6()
	{
		String relativePath = "layout.html";
		String combinedPath = PathUtilities.resolveRelativePath( relativePath, FILE_PATH );
		assertEquals( "/basedir/resources/source/layout.html", combinedPath );
	}

	public void testResolveRelative7()
	{
		String filePath = "/basedir/resources/source/junk/";
		String relativePath = "layout.html";
		String combinedPath = PathUtilities.resolveRelativePath( relativePath, filePath );
		assertEquals( "/basedir/resources/source/junk/layout.html", combinedPath );
	}

	public void testResolveRelative8()
	{
		String filePath = "/basedir/resources/";
		String relativePath = "../../junk";
		String combinedPath = PathUtilities.resolveRelativePath( relativePath, filePath );
		assertEquals( "/junk", combinedPath );
	}
}
