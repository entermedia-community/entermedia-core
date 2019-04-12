/*
 * Created on Aug 6, 2004
 */
package org.openedit.repository.filesystem;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.repository.ContentItem;
import org.openedit.repository.RepositoryException;
import org.openedit.util.PathUtilities;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public abstract class VersionedRepository extends FileRepository
{
	static final String VERSIONS = ".versions";
	static final String META_DATA = ".metadata.xml";
	private static final Log log = LogFactory.getLog(VersionedRepository.class);
	public VersionedRepository()
	{
		//We always need a default constructor
	}
	public ContentItem get( String inPath ) throws RepositoryException
	{
		File file = getFile( inPath);
		FileItem contentItem = new FileItem();
//		if ( inPath.endsWith(".html") && !file.exists() )
//		{
//			String xmlPath = PathUtilities.extractPagePath( inPath )+ ".xml";
//			File xfile = getFile(xmlPath);
//			if( xfile.exists())
//			{
//				file = xfile;
//				contentItem.setActualPath(xmlPath);
//			}
//		}
//		checkVersion(file, inPath);
		contentItem.setPath( inPath );
		contentItem.setFile( file);
		return contentItem;
	}
	public ContentItem getStub( String inPath ) throws RepositoryException
	{
		return createContentItem(inPath);
	}
	/**
	 * Be careful with this method since it can slow the system down
	 * @param inPath
	 */
	protected void checkVersion(File file, String inPath) throws RepositoryException
	{
		if (file.isDirectory())
		{
			return;
		}
		//if there is no versions directory yet then we return
		File versionsDirectory = getVersionsDirectory( file );
		if( !versionsDirectory.exists() )
		{
			return; 
		}

		int m = maxVersionNumber(file);
		if ( m == 0)
		{
			createInitialContentItem( inPath );
		}
		else
		{
			String max = String.valueOf( m );
			File lastVersion = getVersionFile( file,  max );
			if( file.lastModified() > lastVersion.lastModified() )
			{
				ContentItem newItem = createContentItem(inPath);
				newItem.setAuthor( "admin" );
				newItem.setMessage("edited version from disk");
				saveVersion( newItem );
			}
		}
	}
	
	public void put( ContentItem inContentItem ) throws RepositoryException
	{
		String path = inContentItem.getPath();
		File file = getFile( path );
		if (file.isDirectory())
		{
			if( !inContentItem.isFolder() )
			{
				throw new RepositoryException( "Error attempting to write content to a "
					+ "folder instead of a file for path " + path );
			}
			else
			{
				return; //folder already exists
			}
		}
		checkReservedPaths( path );

		if (file.exists())
		{
			if (inContentItem.getMessage() == null)
			{
				inContentItem.setMessage( inContentItem.getPath() + " modified" );
			}
			inContentItem.setType( ContentItem.TYPE_EDITED );
		}
		else
		{
			if (inContentItem.getMessage() == null)
			{
				inContentItem.setMessage( inContentItem.getPath() + " added" );
			}
			inContentItem.setType( ContentItem.TYPE_ADDED );
		}

		if (path.endsWith("/") && !file.exists())
		{
			file.mkdirs();
		}
		else
		{
			writeContent( inContentItem );
		}
			
		if (  inContentItem.isMakeVersion() )
		{
			saveVersion( inContentItem );
		}

	}
	protected String[] listFiles( final String inFileName, File versionsDirectory )
	{
		FilenameFilter filenameFilter = new FilenameFilter()
		{
			public boolean accept( File dir, String name )
			{
				return name.endsWith( inFileName ) && !name.endsWith( META_DATA );
			}
		};
		String[] filesInDirectory = versionsDirectory.list( filenameFilter );
		return filesInDirectory;
	}

	protected void checkReservedPaths( String inPath ) throws RepositoryException
	{
		if (inPath.indexOf( VERSIONS ) > -1)
		{
			throw new RepositoryException( "The path \"" + inPath + "\" is a reserved path. "
					+ "Choose a different path or use a different repository." );
		}
	}

	protected int maxVersionNumber( File inFile )
	{
		File versionsDirectory = getVersionsDirectory( inFile );
		String[] oldVersionPaths = listFiles( inFile.getName(), versionsDirectory );
		int maxVersionNum = 0;
		if ( oldVersionPaths != null)
		{
			for ( int n = 0; n < oldVersionPaths.length; n++ )
			{
				int versionNum = extractVersionNumberFromFilename( oldVersionPaths[n] );
				maxVersionNum = Math.max( maxVersionNum, versionNum );
			}
		}
		return maxVersionNum;
	}

	protected int extractVersionNumberFromFilename( String inVersionPath )
	{
		int separator = inVersionPath.indexOf( '~' );
		if (separator >= 0 )
		{
			try
			{
				//.versions/test.jpg~99

				//~._~monday
				//1~~monday
				//1~._~monday
				String name = PathUtilities.extractFileName(inVersionPath);
				separator = inVersionPath.indexOf( '~' );
				if( separator == -1)
				{
					return 0;
				}
				return Integer.parseInt( name.substring( 0, separator ) );
			}
			catch( NumberFormatException e )
			{
				log.error("Should not get errors " + e);
			}
			return 0;
		}
		else
		{
			return 0;
		}
	}
	public void move( ContentItem inSource, ContentItem inDestination ) throws RepositoryException
	{
		File sourceFile = getFile( inSource.getPath() );
		File destination = getFile( inDestination.getPath() );

		if ( inDestination.getMessage() == null)
		{
			inDestination.setMessage( inSource.getPath() + " moved to " + inDestination.getPath() );
		}
		if ( inDestination.getType() == null)
		{
			inDestination.setType( ContentItem.TYPE_MOVED);
		}
		if( inDestination.getPath().endsWith("/"))
		{
			destination.mkdirs();
		}
		moveFiles( sourceFile, destination );

		if (  inDestination.isMakeVersion() )
		{
			saveVersion( inDestination );
		}
		
	}
	
	public void copy( ContentItem inSource, ContentItem inDestination ) throws RepositoryException
	{
		File sourceFile = getFile( inSource );
		File destination = getFile( inDestination.getPath() );

		if ( inDestination.getMessage() == null)
		{
			inDestination.setMessage( inSource.getPath() + " copied to " + inDestination.getPath() );
		}
		if ( inDestination.getType() == null)
		{
			inDestination.setType( ContentItem.TYPE_COPIED);
		}
		copyFiles(sourceFile, destination);

		if (  inDestination.isMakeVersion() )
		{
			saveVersion( inDestination );
		}

	}


	public void remove( ContentItem inContentItem ) throws RepositoryException
	{
		File todelete = getFile( inContentItem.getPath() );
		if ( inContentItem.getMessage() == null)
		{
			inContentItem.setMessage( inContentItem.getPath() + " removed");
		}
		inContentItem.setType( ContentItem.TYPE_REMOVED );
/*		if ( inContentItem.exists() )
		{
			File metadata = getMetaDataFile(todelete);
			if ( metadata.exists() )
			{
				incrementVersion( inContentItem );
				saveVersion( inContentItem );
			}
		}
*/
		if( inContentItem.isMakeVersion() )
		{
			saveVersion(inContentItem);
		}
		deleteAll(todelete);
	}
	
	public abstract List getVersions( String inPath ) throws RepositoryException;
		
//	protected ContentItem getContentItem( String inPath, ContentItem inContentItem )
//	{
//		FileItem contentItem = new FileItem();
//		contentItem.setPath( inPath );
//		contentItem.setFile( getVersionFile( getFile( inPath ), inContentItem.getVersion() ) );
//		return contentItem;
//	}
	
	protected ContentItem createInitialContentItem( String inPath ) throws RepositoryException
	{
		ContentItem contentItem = createContentItem(inPath);
		contentItem.setPath(inPath);
		contentItem.setAuthor( "admin" );
		contentItem.setMessage( "automatic version" );
		contentItem.setVersion( "1" );
		contentItem.setType( ContentItem.TYPE_ADDED );
		if ( contentItem.exists() )
		{
			saveVersion( contentItem );
		}
		return contentItem;
	}

	protected File getVersionsDirectory( String inPath )
	{
		return getVersionsDirectory( new File( getExternalPath(), inPath ) );
	}

	protected File getVersionsDirectory( File inSourceFile )
	{
		File reservedDirectory = new File( inSourceFile.getParentFile(), VERSIONS );
		return reservedDirectory;
	}

	protected File getMetaDataFile( File file )
	{
		File metadata = new File( getVersionsDirectory( file ), file.getName() + META_DATA );
		return metadata;
	}

	protected abstract void saveVersion( ContentItem inContentItem ) throws RepositoryException;

	/**
	 * @param inFile
	 * @param inNumber
	 * @return
	 */
	protected File getVersionFile( File inSourceFile, String inVersionNumber )
	{
		File versionFile = new File( getVersionsDirectory( inSourceFile ), inVersionNumber + '~' + inSourceFile.getName() );
		return versionFile;
	}	
}