/*
 * Created on Mar 17, 2004
 *
 */

package com.openedit;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * @author dbrown
 *
 */
public class AllTests
{
	public static void main(String[] args) throws Exception
	{
		TestRunner.run(suite());
	}
	public static Test suite() throws Exception
	{
		String rootPath = System.getProperty( "oe.root.path" );
		if ( rootPath == null )
		{
			System.setProperty("oe.root.path", "resources/test");
		}
		
		TestSuite suite = new TestSuite("All Open Edit Tests");
		suite.addTest( com.openedit.generators.AllTests.suite() );
		suite.addTest( com.openedit.page.AllTests.suite() );
		suite.addTest( com.openedit.page.finder.AllTests.suite() );
		suite.addTest( com.openedit.page.manage.AllTests.suite() );
		suite.addTest( com.openedit.servlet.AllTests.suite() );
		suite.addTest( com.openedit.util.AllTests.suite() );
		suite.addTest( com.openedit.web.AllTests.suite() );
		//suite.addTest( com.openedit.users.filesystem.AllTests.suite() );
		suite.addTest( org.openedit.repository.AllTests.suite() );
		suite.addTest( com.openedit.modules.AllTests.suite(  ) );

		return suite;
	}
}
