/*
 * Created on Jan 7, 2005
 */
package org.openedit.modules;


import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author cburkey
 *
 */
public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite("Test for org.openedit.modules");
		//$JUnit-BEGIN$
		suite.addTestSuite(ReflectorTest.class);
		//$JUnit-END$
		return suite;
	}
}
