package com.openedit.web;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * DOCUMENT ME!
 *
 * @author
 * @version 1.0
 */
public class AllTests
{

	/**
	 *  
	 *
	 * @return  
	 */
	public static Test suite()
	{
		TestSuite suite = new TestSuite("Test for com.openedit.web");

		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(BrowserTest.class));
		//$JUnit-END$
		return suite;
	}
}
