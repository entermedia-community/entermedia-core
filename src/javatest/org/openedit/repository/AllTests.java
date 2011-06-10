/*
 * Created on Jan 4, 2005
 */
package org.openedit.repository;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.openedit.repository.filesystem.BaseRepositoryTest;


/**
 * @author cburkey
 *
 */
public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite("Test for com.einnovation.repository");
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(CompoundRepositoryTest.class));
		suite.addTest(new TestSuite(BaseRepositoryTest.class));
		//$JUnit-END$
		return suite;
	}
}
