/*
 * Created on Jun 23, 2004
 */
package org.openedit.util;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite( "Test for org.openedit.util" );
		//$JUnit-BEGIN$
		suite.addTest( new TestSuite( PathUtilitiesTest.class ));
		suite.addTest( new TestSuite( URLUtilitiesTest.class ));
		//This is prone to errors due to down server
		//$JUnit-END$
		return suite;
	}
}
