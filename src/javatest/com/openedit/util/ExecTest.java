package com.openedit.util;

import com.openedit.BaseTestCase;

public class ExecTest extends BaseTestCase
{	
	public void testRunExec()
	{
		System.out.println("testRunExec\n");
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
