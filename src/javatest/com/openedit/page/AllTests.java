/*
 * Created on Jun 23, 2004
 */
package com.openedit.page;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite( "Test for com.openedit.page" );
		//$JUnit-BEGIN$
		suite.addTest( new TestSuite( PageTest.class ) );
		suite.addTest( new TestSuite( PageMetaDataTest.class ) );
		suite.addTest( new TestSuite( PageRequestTest.class ) );
		
		//$JUnit-END$
		return suite;
	}
}
