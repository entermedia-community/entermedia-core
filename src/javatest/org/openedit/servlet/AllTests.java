/*
 * Created on Jun 23, 2004
 */
package org.openedit.servlet;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite( "Test for org.openedit.servlet" );
		//$JUnit-BEGIN$
		suite.addTest( new TestSuite(OpenEditEngineTest.class ));
		//$JUnit-END$
		return suite;
	}
}
