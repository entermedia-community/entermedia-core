/*
 Copyright (c) 2003 eInnovation Inc. All rights reserved

 This library is free software; you can redistribute it and/or modify it under the terms
 of the GNU Lesser General Public License as published by the Free Software Foundation;
 either version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU Lesser General Public License for more details.
 */

package com.openedit.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Some file utility methods such as recursive delete.
 */
public class FileUtils
{
	
	protected Set fieldInvalidChars;
	OutputFiller fieldFiller = new OutputFiller();
	private static final Log log = LogFactory.getLog(FileUtils.class);
	/**
	 * Create a temporary directory with a unique name beginning with the given
	 * prefix.
	 * 
	 * @param inPrefix
	 *            The prefix for the created directory
	 * 
	 * @return DOCME
	 * 
	 * @throws IOException
	 *             DOCME
	 */
	public File createTempDir( String inPrefix ) throws IOException
	{
		File tempDir = File.createTempFile( inPrefix, null );
		tempDir.delete();
		tempDir.mkdir();

		return tempDir;
	}

	/**
	 * DOCME
	 * 
	 * @param fname
	 *            DOCME
	 */
	public void deleteAll( String fname )
	{
		File f = new File( fname );
		deleteAll( f );
	}
	
	

	/**
	 * Deletes any files in this directory that match the wildcard
	 * @param inMatch
	 */
	public void deleteMatch( String inMatch )
	{
		//get the parent, list the children, find the match, delete
		File search = new File( inMatch );
		File dir = search.getParentFile();

		File[] all = dir.listFiles();
		if ( all != null)
		{
			for (int i = 0; i < all.length; i++)
			{
				File f = all[i];
				if ( PathUtilities.match(f.getName(), search.getName() ) )
				{
					log.info("deleted " + f.getName());
					f.delete(); //should this do dirs?
				}
			}
		}
	}
	
	public boolean deleteOlderVersions( String inDir )
	{
		System.gc();
		boolean requiresRestart = false;
		Map map = new HashMap();
		//get the parent, list the children, find the match, delete
		File dir = new File( inDir );

		File[] all = dir.listFiles();
		if ( all != null)
		{
			for (int i = 0; i < all.length; i++)
			{
				File f = all[i];
				String fileName = f.getName();
				int dashIndex = fileName.lastIndexOf('-');
				if (dashIndex >= 0)
				{
					String base = fileName.substring(0, dashIndex);
					String version = fileName.substring(dashIndex + 1 );
					String highestVersion = (String)map.get(base);
					if (highestVersion == null || highestVersion.compareTo(version) < 0)
					{
						map.put(base, version);
					}
				}
			}

			for (int i = 0; i < all.length; i++)
			{
				File f = all[i];
				String fileName = f.getName();
				int dashIndex = fileName.lastIndexOf('-');
				if (dashIndex >= 0)
				{
					String base = fileName.substring(0, dashIndex);
					String version = fileName.substring(dashIndex + 1);
					String highestVersion = (String)map.get(base);
					if (!version.equals(highestVersion))
					{
						if (f.delete())
						{
							log.info("deleting " + f.getName());
						}
						else
						{
							log.info("deleting " + f.getName() + " on exit");
							f.deleteOnExit();
							requiresRestart = true;
						}
					}
				}
			}
		}
		return requiresRestart;
	}		
	
	public void deleteAll( File file )
	{
		if (file.isDirectory())
		{
			// If it's a dir, then delete everything in it.
			File[] fileList = file.listFiles();

			if (fileList != null)
			{
				for ( int idx = 0; idx < fileList.length; idx++ )
					deleteAll( fileList[idx] );
			}
		}
		
		// Now delete ourselves, whether a file or a dir.
		file.delete();
	}
	public void copyFileByMatch( String inMatch, String outDir) throws IOException
	{
		File out = new File( outDir );
		out.mkdirs();
		copyFileByMatch(inMatch, out );
	} 
	public void copyFileByMatch( String inMatch, File outDir) throws IOException
	{
		File file = new File( inMatch ); 
		File dir = file.getParentFile();

		File[] all = dir.listFiles();
		if ( all != null)
		{
			for (int i = 0; i < all.length; i++)
			{
				File f = all[i];
				if ( PathUtilities.match(f.getName(), file.getName() ) )
				{
					copyFiles(f, outDir);
				}
			}
		}
	}

	/**
	 * DOCME
	 * 
	 * @param inSource
	 *            DOCME
	 * @param inDest
	 *            DOCME
	 * @param inBuffer
	 *            DOCME
	 * 
	 * @throws IOException
	 *             DOCME
	 */
	public void dirCopy( File inSource, File inDest ) throws IOException
	{
		if (inSource.isDirectory())
		{
			inDest.mkdirs();

			File[] files = inSource.listFiles();
			if( files != null)
			{
				for ( int i = 0; i < files.length; i++ )
				{
					// The child directory
					File newDest = new File( inDest, files[i].getName() );
	
					if (files[i].isDirectory())
					{
						dirCopy( files[i], newDest );
					}
					else
					{
						fileCopy( files[i], newDest );
					}
				}
			}
		}
	}
	/**
	 * Does a move even if the directory already exist
	 * Also creates a directory for file to be moved into
	 * @param inSource
	 * @param inDest
	 * @throws IOException
	 */
	public void move( String inSource, String inDest ) throws IOException
	{
		move( new File( inSource), new File( inDest));
	}
	public void move( File inSource, File inDest ) throws IOException
	{
		move(inSource, inDest, false);
	}
		
	public void move(File inSource, File inDest, boolean inForce) throws IOException
	{
		if (inSource.isDirectory())
		{
			inDest.mkdirs();

			File[] files = inSource.listFiles();
			if( files != null)
			{
				for ( int i = 0; i < files.length; i++ )
				{
					// The child directory
					File newDest = new File( inDest, files[i].getName() );
	
					if (files[i].isDirectory())
					{
						move( files[i], newDest, inForce );
					}
					else
					{
						renameFile(files[i], newDest, inForce );
					}
				}
			}
			inSource.delete();
		}
		else
		{
			if( inDest.isDirectory())
			{
				inDest = new File( inDest, inSource.getName());
			}
			else
			{
				inDest.getParentFile().mkdirs();
			}
			renameFile(inSource, inDest, inForce);
		}
	}

	protected void renameFile(File inSource, File inDest, boolean inForce) throws IOException
	{
		if( !inSource.renameTo( inDest ) )
		{
			if(inForce)
			{
				copyFiles(inSource,inDest);
				boolean deleted = inSource.delete();
				if(!deleted){
					log.info("could not delete file - " + inSource);
				}
				return;
			}
			throw new IOException("Could not move " + inSource.getPath() + " to " + inDest.getPath() + " file may already exist. Use forced=true");
		}
		
	}

	/**
	 * DOCME
	 * 
	 * @param src
	 *            DOCME
	 * @param dst
	 *            DOCME
	 * @param buffer
	 *            DOCME
	 * 
	 * @throws IOException
	 *             DOCME
	 */
	public void fileCopy( File src, File dst ) throws IOException
	{
		fieldFiller.fill(src,dst);

		// Preserve the modification date of the original file.
		dst.setLastModified( src.lastModified() );
	}
	public void copyFiles( String source, String destination ) throws IOException
	{
		copyFiles( new File( source ), new File( destination ) );
	}
	public void copyFiles( File source, File destination ) throws IOException
	{
		if (source.isDirectory())
		{
			if( destination.exists() && !destination.isDirectory() )
			{
				destination.delete();
			}
			destination.mkdirs();

			//loop over all the sub files
			File[] children = source.listFiles();

			for ( int i = 0; i < children.length; i++ )
			{
				copyFiles( children[i], new File( destination, children[i].getName() ) );
			}
		}
		else
		{
			if( destination.isDirectory() )
			{
				copyFiles( source,new File( destination , source.getName() ) );
			}
			else
			{
				destination.getParentFile().mkdirs();
				fieldFiller.fill( source,destination );
				destination.setLastModified(source.lastModified());
			}
		}
	}
	/**
	 * This closes the input stream
	 * @param inInput
	 * @param inOutputFile
	 * @throws IOException
	 */
	public void writeFile( InputStream inInput, File inOutputFile ) throws IOException
	{
		//TODO: Could probably start using java.nio classes
		// Write the content
		
//		 Create any parent directories, if necessary.
		inOutputFile.getParentFile().mkdirs();
		OutputStream out = new FileOutputStream( inOutputFile );
		try
		{
			fieldFiller.fill( inInput, out );
			out.flush();
		}
		finally
		{
			if ( inInput != null)
			{
				inInput.close();
			}
			out.close();
		}
	}

	/**
	 * @param inIn
	 */
	public static void safeClose(Reader inIn)
	{
		if ( inIn != null)
		{
			try
			{
				inIn.close();
			}
			catch (IOException ex)
			{
				log.error(ex);
			}
		}
	}
	public static void safeClose(InputStream inIn)
	{
		if ( inIn != null)
		{
			try
			{
				inIn.close();
			}
			catch (IOException ex)
			{
				log.error(ex);
			}
		}
	}
	public static void safeClose(OutputStream inIn)
	{
		if ( inIn != null)
		{
			try
			{
				inIn.close();
			}
			catch (IOException ex)
			{
				log.error(ex);
			}
		}
	}
	public static void safeClose(Writer inIn)
	{
		if ( inIn != null)
		{
			try
			{
				inIn.close();
			}
			catch (IOException ex)
			{
				log.error(ex);
			}
		}
	}
	public void replace(File inFile, String inKey, String inNewKey) throws Exception
	{
		FileReader filereader = new FileReader(inFile);
		StringWriter out = new StringWriter();
	    new OutputFiller().fill( filereader, out );
	    filereader.close();
	    String readstring = out.toString();
	    readstring = readstring.replace(inKey, inNewKey  ); 
	    FileWriter filewriter = new FileWriter(inFile);
	    new OutputFiller().fill( new StringReader(readstring), filewriter);
	    filewriter.close();
	}

	public void copyFiles(InputStream inInputStream, FileOutputStream inFileOutputStream) throws IOException
	{
		try
		{	
			fieldFiller.fill(inInputStream, inFileOutputStream);
		}
		finally
		{
			safeClose(inInputStream);
			safeClose(inFileOutputStream);
		}
		
	}
	
	protected Set getInvalidChars()
	{
		if (fieldInvalidChars == null)
		{
			fieldInvalidChars = new HashSet();
			fieldInvalidChars.add("?");
			fieldInvalidChars.add("*");
			fieldInvalidChars.add("\n");
			fieldInvalidChars.add("<");
			fieldInvalidChars.add(">");
			fieldInvalidChars.add(":");
			fieldInvalidChars.add("|");
			//fieldInvalidChars.add(" \\");
			fieldInvalidChars.add(" /");
			fieldInvalidChars.add("/ ");
			fieldInvalidChars.add("#");
			fieldInvalidChars.add("%");
		}
		return fieldInvalidChars;
	}
	
	/**
	 * Tests for illegal characters in the input path.
	 * @param inPath = string to be tested for bad characters
	 * @return
	 */
	public boolean isLegalFilename(String inPath)
	{
		for (Iterator iterator = getInvalidChars().iterator(); iterator.hasNext();) 
		{
			String value=(String) iterator.next();
			if(inPath.contains(value))
			{
				return false;
			}
		}
		if( inPath.contains("/ ") )
		{
			return false;
		}
		//log.info(inPath);
		return true;
	}

	
}