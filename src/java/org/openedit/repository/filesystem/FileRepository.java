/*
 * Created on Jan 12, 2005
 */
package org.openedit.repository.filesystem;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.repository.BaseRepository;
import org.openedit.repository.ContentItem;
import org.openedit.repository.OutputStreamItem;
import org.openedit.repository.Repository;
import org.openedit.repository.RepositoryException;

import com.openedit.util.FileUtils;
import com.openedit.util.PathUtilities;

/**
 * @author cburkey
 *
 */
public class FileRepository extends  BaseRepository
{
	private static final Log log = LogFactory.getLog(FileRepository.class);
	protected FileUtils fieldFileUtils;
	
	public FileRepository()
	{
	}

	public FileRepository(String inPath, File inRoot)
	{
		setPath(inPath);
		setExternalPath(inRoot.getAbsolutePath());
	}

	public ContentItem get(String inPath) throws RepositoryException
	{
		if ( log.isDebugEnabled() )
		{
			log.debug("reading:" + inPath);
		}
		
		ContentItem revision = createContentItem(inPath);
		return revision;
	}

	public ContentItem getStub(String inPath) throws RepositoryException
	{
		if ( log.isDebugEnabled() )
		{
			log.debug("reading:" + inPath);
		}
		return createContentItem(inPath);
	}

	public void put(ContentItem inContent) throws RepositoryException
	{
		if ( log.isDebugEnabled() )
		{
			log.debug("saving:" + inContent.getPath());
		}

		String path = inContent.getPath();
		File file = getFile( path );

		if (path.endsWith("/") && !file.exists())
		{
			file.mkdirs();
		}
		else
		{
			if (file.isDirectory())
			{
				throw new RepositoryException( "Error attempting to write content to a "
						+ "folder instead of a file for path " + path );
			}
			writeContent( inContent );
		}
	}

	protected File getFile( String inPath )
	{
		return new File( getAbsolutePath(inPath) );
	}
	
	protected File getFile( ContentItem inPath )
	{
		if( inPath instanceof FileItem)
		{
			FileItem f = (FileItem)inPath;
			return f.getFile();
		}
		return getFile( inPath.getPath() );
	}

	public void copy(ContentItem inSource, ContentItem inDestination) throws RepositoryException
	{
		File destination = null;

		if (inSource.isFolder())
		{
			destination = getFile( inDestination.getPath() );
			destination.mkdirs();
			try
			{
				getFileUtils().copyFiles( getFile(inSource), getFile(inDestination) );
			}
			catch ( Exception ex)
			{
				throw new RepositoryException(ex);
			}
			
		}
		else
		{
			destination = getFile( inDestination.getPath() );
			destination.getParentFile().mkdirs();
			try
			{
				getFileUtils().copyFiles( inSource.getInputStream(), new FileOutputStream(destination) );
			}
			catch ( Exception ex)
			{
				throw new RepositoryException(ex);
			}
			
		}

	}

	public void move( ContentItem inSource, ContentItem inDestination ) throws RepositoryException
	{
		File todelete = getFile( inSource.getPath() );
		File toMove = getFile( inDestination.getPath() );
		if( inDestination.getPath().endsWith("/"))
		{
			toMove.mkdirs();
		}
		moveFiles(todelete, toMove);
	}

	public void remove(ContentItem inContentItem) throws RepositoryException
	{
		File todelete = getFile( inContentItem.getPath() );
		if( todelete.getAbsolutePath().equals(getExternalPath()))
		{
			throw new RepositoryException("Delete All not allowed");
		}
		
		deleteAll(todelete);
	}

	public List getVersions(String inPath) throws RepositoryException
	{
		List list = new ArrayList(1);
		if (getFile(inPath).exists())
		{
			list.add(createContentItem(inPath));
		}
		return list;
	}
	
	protected ContentItem createContentItem( String inPath )
	{
		FileItem contentItem = new FileItem();
		contentItem.setPath( inPath );
		contentItem.setAbsolutePath( getAbsolutePath(inPath ) );
		return contentItem;
	}
	protected void deleteAll(File todelete)
	{
		getFileUtils().deleteAll( todelete );
	}

	public FileUtils getFileUtils()
	{
		if (fieldFileUtils == null)
		{
			fieldFileUtils = new FileUtils();
		}
		return fieldFileUtils;
	}

	protected void copyFiles( File source, File destination ) throws RepositoryException
	{
		try
		{
			getFileUtils().copyFiles( source, destination );
		}
		catch( Exception e )
		{
			throw new RepositoryException( e );
		}
	}
	protected void moveFiles( File source, File destination ) throws RepositoryException
	{
		try
		{
			destination.getParentFile().mkdirs();
			if ( !source.renameTo(destination))
			{
				getFileUtils().copyFiles( source, destination );
				getFileUtils().deleteAll(source);
			}
		}
		catch( Exception e )
		{
			throw new RepositoryException( e );
		}
	}

	protected void writeContent(ContentItem inContentItem) throws RepositoryException
	{	
		if ( inContentItem instanceof OutputStreamItem)
		{
			//make an output stream and return it
			OutputStreamItem osi = (OutputStreamItem)inContentItem;
			File file = getFile( inContentItem.getPath() );
			file.getParentFile().mkdirs();
			try
			{
				
				FileOutputStream fos = null;
				if( osi.getSeek() > 0)
				{
					if( file.length() != osi.getSeek())
					{
						throw new RepositoryException("Seek must match file size " + osi.getSeek() + " " + file.length());
					}
					fos = new FileOutputStream(file, true);
				}
				else
				{
					fos = new FileOutputStream(file);
				}
				inContentItem.setOutputStream(new BufferedOutputStream(fos));
			}
			catch (FileNotFoundException e)
			{
				throw new RepositoryException(e);
			}
		}
		else if ( inContentItem instanceof FileItem )
		{
			File file = getFile( inContentItem.getPath() );
			try
			{
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			catch (IOException e)
			{
				throw new RepositoryException(e);
			}
			return; //else we would end up saving it onto itself
		}
		else //Use it to write from a stream to the file
		{
			File file = getFile( inContentItem.getPath() );
			try
			{
				InputStream in = inContentItem.getInputStream();
				getFileUtils().writeFile( in, file );
			}
			catch( Exception e )
			{
				throw new RepositoryException(
						"Error writing content for path " + inContentItem.getPath() + " " +  e.getMessage(), e );
			}
		}
	}

	public boolean doesExist(String inPath) throws RepositoryException
	{
		File file = getFile( inPath );

		boolean exists = file.exists();

		if ( !exists && inPath.endsWith(".html") )
		{
			String xmlPath = PathUtilities.extractPagePath( inPath )+ ".xml";
			file = getFile( inPath );
			exists = file.exists();
		}
		return exists;
	}
	
	
	public ContentItem getLastVersion(String inPath) throws RepositoryException
	{
		return createContentItem(inPath);
	}

	public List getChildrenNames(String inParent) throws RepositoryException
	{
		if( inParent.endsWith("/"))
		{
			inParent = inParent.substring(0,inParent.length() - 1);
		}
		File file = getFile( inParent );
		String[] all = file.list();
		if( all != null)
		{
			List children = new ArrayList(all.length);
			for (int i = 0; i < all.length; i++)
			{
				boolean folder= false;
				if(PathUtilities.extractPageType(all[i]) == null)
				{
					File child = new File(file, all[i]);
					if(folder || child.isDirectory())
					{
						folder = true;
					}
				}
				
				if( showChild(all[i]))
				{
					String name = inParent + "/" + PathUtilities.extractFileName(all[i]);
					children.add(name);
				}
			}
			return children;
		}
		return Collections.EMPTY_LIST;
	}
	
	public void deleteOldVersions(String inPath) throws RepositoryException
	{
		List versions = getVersions(inPath);
		for (Iterator iterator = versions.iterator(); iterator.hasNext();) {
			ContentItem item = (ContentItem) iterator.next();
			remove(item);
		}
		
	}
	
	public void move(ContentItem inSource, Repository inSourceRepository, ContentItem inDestination) throws RepositoryException
	{
		if( inSourceRepository == this)
		{
			move( inSource, inDestination);
		}
		else if( inSourceRepository instanceof FileRepository)
		{
			File todelete = ((FileRepository)inSourceRepository).getFile( inSource.getPath() );
			File destination = getFile( inDestination.getPath() );
			if( inDestination.getPath().endsWith("/"))
			{
				destination.mkdirs();
			}
			moveFiles(todelete, destination);
		}
		else
		{
			try
			{
				File destination = getFile( inDestination.getPath() );
				if( inDestination.getPath().endsWith("/"))
				{
					destination.mkdirs();
				}
				getFileUtils().copyFiles( inSource.getInputStream(), new FileOutputStream(destination) );
				remove(inSource);
			}
			catch ( Exception ex)
			{
				throw new RepositoryException(ex);
			}

		}
		
	}
}
