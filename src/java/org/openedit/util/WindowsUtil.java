package org.openedit.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openedit.OpenEditException;
import com.openedit.util.Exec;
import com.openedit.util.ExecResult;
import com.openedit.util.FileUtils;
import com.openedit.util.PathUtilities;

public class WindowsUtil
{

	private static final Log log = LogFactory.getLog( WindowsUtil.class );

	protected File fieldRoot;

	protected String fieldCommandName;

	protected FileUtils fieldFileUtils;

	public FileUtils getFileUtils()
	{
		if ( fieldFileUtils == null )
		{
			fieldFileUtils = new FileUtils();

		}

		return fieldFileUtils;
	}

	public File getRoot()
	{
		return fieldRoot;
	}

	public void setRoot( File inRoot )
	{
		fieldRoot = inRoot;
	}

	public String getCommandName()
	{
		if ( fieldCommandName == null )
		{
			fieldCommandName = "unlocker";
		}

		return fieldCommandName;
	}

	public boolean unlockFile( File inFile ) throws OpenEditException
	{
		if ( !System.getProperty( "os.name" ).toUpperCase().contains( "WINDOWS" ) )
		{
			//this is only necessary on windows 
			return true;
		}
		long start = System.currentTimeMillis();
		List com = new ArrayList();

		com.add( inFile.toString() );
		com.add( "/S" );
		if ( runExec( com ) )
		{
			log.info( "Unlock completed in :" + ( System.currentTimeMillis() - start ) + " "
					+ inFile.getName() );
			return true;
		}
		return false;
	}

	public void deleteMatch( String inMatch ) throws OpenEditException
	{
		//get the parent, list the children, find the match, delete
		File search = new File( inMatch );
		File dir = search.getParentFile();

		File[] all = dir.listFiles();
		if ( all != null )
		{
			for ( int i = 0; i < all.length; i++ )
			{
				File f = all[ i ];
				if ( PathUtilities.match( f.getName(), search.getName() ) )
				{
					log.info( "deleted " + f.getName() );
					delete( f );
				}
			}
		}
	}

	public boolean delete( File inFile ) throws OpenEditException
	{
		if ( !System.getProperty( "os.name" ).toUpperCase().contains( "WINDOWS" ) )
		{
			inFile.delete();
			return true;
		}

		long start = System.currentTimeMillis();
		List com = new ArrayList();
		com.add( inFile.toString() );
		com.add( "/D" );
		com.add( "/S" );

		if ( runExec( com ) )
		{
			log.info( "Delete completed in :" + ( System.currentTimeMillis() - start ) + " "
					+ inFile.getName() );
			return true;
		}
		return false;
	}

	protected boolean runExec( List inCom ) throws OpenEditException
	{
		Exec exec = new Exec();
		ExecResult result = exec.runExec( getCommandName(), inCom );
		if ( !result.isRunOk() )
		{
			log.info( "Resize failed running again with output tracking" );
			result = exec.runExec( inCom, true);
			log.info( result.getStandardOut() + " error output:" + result.getStandardError() );
		}
		return result.isRunOk();
	}

	public boolean deleteOlderVersions( String inDir ) throws Exception
	{
		System.gc();
		boolean requiresRestart = false;
		Map map = new HashMap();
		//get the parent, list the children, find the match, delete
		File dir = new File( inDir );

		File[] all = dir.listFiles();
		if ( all != null )
		{
			for ( int i = 0; i < all.length; i++ )
			{
				File f = all[ i ];
				String fileName = f.getName();
				int dashIndex = fileName.lastIndexOf( '-' );
				if ( dashIndex >= 0 )
				{
					String base = fileName.substring( 0, dashIndex );
					String version = fileName.substring( dashIndex + 1 );
					String highestVersion = (String) map.get( base );
					if ( highestVersion == null || highestVersion.compareTo( version ) < 0 )
					{
						map.put( base, version );
					}
				}
			}

			for ( int i = 0; i < all.length; i++ )
			{
				File f = all[ i ];
				String fileName = f.getName();
				int dashIndex = fileName.lastIndexOf( '-' );
				if ( dashIndex >= 0 )
				{
					String base = fileName.substring( 0, dashIndex );
					String version = fileName.substring( dashIndex + 1 );
					String highestVersion = (String) map.get( base );
					if ( !version.equals( highestVersion ) )
					{
						if ( f.delete() )
						{
							log.info( "deleting " + f.getName() );

						}
						else
						{
							delete( f );
							log.info( "deleting " + f.getName() + " on exit" );
							f.deleteOnExit();
							requiresRestart = true;
						}
					}
				}
			}
		}
		return requiresRestart;
	}
}
