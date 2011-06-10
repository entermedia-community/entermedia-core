package com.openedit.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class AltExecTest extends TestCase
{
	String pathToShellScript;
	protected void setUp() throws Exception
	{
		super.setUp();
		File file = new File( "resources/test/timedexec/sleepecho.sh");
		assertTrue( file.exists() );
		pathToShellScript = file.getAbsolutePath();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	public void testRunExec()
	{
		System.out.println("testRunExec\n");
		AltExec exec = new AltExec();
		List commands = new ArrayList();
		commands.add( pathToShellScript);
		commands.add( "5");
		long start = System.currentTimeMillis();
		exec.runExec( commands );
		long duration = System.currentTimeMillis() - start;
		assertTrue( duration >= 5000 );
		assertTrue( duration < 6000 );
		assertNotNull( exec.getStandardOutput() );

	}
	
	public void testRunExec_time_limited()
	{
		System.out.println("testRunExec_time_limited\n");
		AltExec exec = new AltExec();
		assertTrue( !exec.isTimeLimited() );
		exec.setTimelimit( 3 );
		assertTrue( exec.isTimeLimited() );
		exec.setTimelimit( 0 );
		assertTrue( !exec.isTimeLimited() );
		exec.setTimelimit( 3000 );
		assertTrue( exec.isTimeLimited() );
		
		List commands = new ArrayList();
		commands.add( pathToShellScript);
		commands.add( "5");
		long start = System.currentTimeMillis();
		exec.runExec( commands );
		long duration = System.currentTimeMillis() - start;
		assertTrue( duration >= 2500 );
		assertTrue( duration < 3500 );
		assertTrue( exec.getErrorOutput() == null );
		assertTrue( exec.getStandardOutput() == null );
	}
	
	public void testRunExec_long_timer()
	{
		System.out.println("testRunExec_long_timer\n");
		AltExec exec = new AltExec();
		assertTrue( !exec.isTimeLimited() );
		exec.setTimelimit( 3 );
		assertTrue( exec.isTimeLimited() );
		exec.setTimelimit( 0 );
		assertTrue( !exec.isTimeLimited() );
		exec.setTimelimit( 5000 );
		assertTrue( exec.isTimeLimited() );
		
		List commands = new ArrayList();
		commands.add( pathToShellScript);
		commands.add( "3");
		long start = System.currentTimeMillis();
		exec.runExec( commands );
		long duration = System.currentTimeMillis() - start;
		assertTrue( duration >= 2500 );
		assertTrue( duration < 3500 );
		assertNotNull( exec.getStandardOutput() );
	}

}
