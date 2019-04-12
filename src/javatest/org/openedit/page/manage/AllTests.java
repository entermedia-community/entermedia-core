/*
 * Created on Jun 23, 2004
 */
package org.openedit.page.manage;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite( "Test for org.openedit.pagemanager" );
		//$JUnit-BEGIN$
		suite.addTest( new TestSuite( FileSystemPageManagerTest.class ) );
		suite.addTest( new TestSuite( PageManagerTest.class ) );
		//$JUnit-END$
		return suite;
	}
}
