/*
 * Created on Aug 30, 2004
 */
package org.openedit.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

import org.openedit.BaseTestCase;
import org.openedit.page.Page;
import org.openedit.page.manage.PageManager;
import org.openedit.util.PageZipUtil;
import org.openedit.util.ZipUtil;

/**
 * @author Eric Broyles <eric.broyles@ugs.com>
 */
public class ZipUtilitiesTest extends BaseTestCase
{

    public ZipUtilitiesTest( String arg0 )
	{
		super( arg0 );
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ZipUtilitiesTest.class);
    }
	
	protected PageManager getPageManager() throws Exception
	{
		return getFixture().getPageManager();
	}
    
    public void testPageZipUtil() throws Exception
    {
    	
    	Page page = getPageManager().getPage("/zip_test/test_sub/");
    	assertEquals("true", page.get("excludefromzips"));
    	
    	
    	PageZipUtil pageZipUtil = new PageZipUtil(getPageManager());
    	ByteArrayOutputStream os = new ByteArrayOutputStream();
    	pageZipUtil.setRoot(getRoot());
    	pageZipUtil.zipFile("/zip_test", os);
    	int len = os.toByteArray().length;
    	assertTrue(len > 0);
    	assertTrue(len < 1000);
    }

    public void testComposeEntryPath()
    {
    	String name = "home/data/subdir/file.txt";
    	String pathSegment = "/home/data";
    	ZipUtil zipUtil = new ZipUtil();
    	
//    	assertEquals( "/backup/path/subdir/file.txt", zipUtil.composeEntryPath( name, pathSegment ));
//    	
//
//    	pathSegment = "home/data";
//
//    	assertEquals( "/backup/path/subdir/file.txt", zipUtil.composeEntryPath( name, pathSegment));
 
    }
}
