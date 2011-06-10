/*
 * Created on Nov 18, 2006
 */
package com.openedit.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.openedit.OpenEditRuntimeException;

public abstract class JarReader
{
	protected List fieldCompletedFiles;
	
	public void processInClasspath(String inName)
	{
		//Bug alert: On Windows there is a bug in JDK that locks files when calling Class.getResource()
		//So we unzip the jar files ourself so that nothing is locked
		try
		{
			String cp = System.getProperty("java.class.path");
			//log.info("Classpath: " + cp);
			String[] files = cp.split(File.pathSeparator);
			processFiles(files, inName);
		}
		catch ( Exception ex)
		{
			throw new OpenEditRuntimeException(ex);
		}
	}
	protected void processFiles(String[] files, String inName) 
	{
		if( files == null)
		{
			return;
		}
		FileUtils fileU = new FileUtils();

		try
		{
			File tmp = File.createTempFile("openedit", "cpdir");
			fileU.deleteAll(tmp );
			ZipUtil unzip = new ZipUtil();
			unzip.setFindFileName(inName);
			unzip.setExitOnFirstFind(true);
			for (int i = 0; i < files.length; i++)
			{
				File cpdir = new File( files[i]);
				if( getCompletedFiles().contains(cpdir))
				{
					continue;
				}
				getCompletedFiles().add( cpdir );
				if( cpdir.isFile())
				{
					tmp.mkdirs();
					unzip.unzip(cpdir, tmp);
					cpdir = tmp;
				}
				//log.info("Looking in:" + cpdir.getPath());
				File found = new File( cpdir,inName );
				if( found.exists() )
				{
					processFile(found);
				}
				fileU.deleteAll(tmp );
			}
		} catch ( IOException ex)
		{
			throw new OpenEditRuntimeException(ex);
		}
	}
	public void processInLibDir(File inLibDir, String inName) 
	{
		File[] jars = inLibDir.listFiles();
		if( jars != null)
		{
			String[] names = new String[jars.length];
			for (int i = 0; i < jars.length; i++)
			{
				names[i] = jars[i].getAbsolutePath();
			}
			processFiles(names, inName);
		}
		
	}	
	
	public abstract void processFile( File inFile);

	public List getCompletedFiles()
	{
		if( fieldCompletedFiles == null)
		{
			fieldCompletedFiles = new ArrayList();
		}
		return fieldCompletedFiles;
	}
	public void setCompletedFiles(List inCompletedFiles)
	{
		fieldCompletedFiles = inCompletedFiles;
	}
}
