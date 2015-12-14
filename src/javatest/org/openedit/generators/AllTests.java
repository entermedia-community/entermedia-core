/*
 * Created on Jun 23, 2004
 */
package org.openedit.generators;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite( "Test for org.openedit.page" );
		//$JUnit-BEGIN$
		suite.addTest( new TestSuite( FileGeneratorTest.class ) );
		suite.addTest( new TestSuite( NestedGeneratorTest.class ) );
		suite.addTest( new TestSuite( VelocityGeneratorTest.class ) );
		//$JUnit-END$
		return suite;
	}
}
