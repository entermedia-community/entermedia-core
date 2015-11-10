package com.openedit.util;

import java.util.ArrayList;
import java.util.List;

import com.openedit.BaseTestCase;

public class ExecTest extends BaseTestCase
{	
	
	public void testSpeed()
	{
		Exec exec = (Exec)getBean("exec");
		List comm = new ArrayList();
		comm.add("-S");
		comm.add("-d");
		comm.add("%Y-%m-%d %H:%M:%S");
		comm.add("/media/D603-EA1D/Sample EM/Content Archive/Highlights/2011/HL_12_11/HL_DEC_2011_PRESS_PDFS/HL_12_11_05_VERSE.pdf");
		comm.add("-n");
		long start = System.currentTimeMillis();
		ExecResult done = exec.runExec("exiftool", comm, true);
		assertTrue(done.isRunOk());
		log( done.getStandardOut());
		long end = System.currentTimeMillis();
		log("done in " + (end - start) + " milliseconds" );
		
	}
	private void log(String inString)
	{
		System.out.println(inString);
		
	}
	public void testRunExec()
	{
		log("testRunExec\n");
		Exec exec = (Exec)getBean("exec");
		//make sure creation went ok, file found all that jazz
		assertNotNull(exec.fieldCachedCommands);
		assertNotNull(exec.fieldXmlCommandsFilename);
		//call ffmpeg, lame, exiftool, imagemagick, ghostscript
		//for testing runExec will just call a shell script which echos to the screen
		exec.runExec("ffmpeg", null);
		assertNotNull(exec.fieldCachedCommands.get("ffmpeg"));
		exec.runExec("lame", null);
		assertNotNull(exec.fieldCachedCommands.get("lame"));
		exec.runExec("convert", null);
		assertNotNull(exec.fieldCachedCommands.get("convert"));
		exec.runExec("ghostscript", null);
		assertNotNull(exec.fieldCachedCommands.get("ghostscript"));
		exec.runExec("exiftool", null);
		assertNotNull(exec.fieldCachedCommands.get("exiftool"));
	}
}
