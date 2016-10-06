/*
 * Created on Jul 30, 2004
 */
package org.openedit.repository.filesystem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.Data;
import org.openedit.repository.ContentItem;
import org.openedit.repository.RepositoryException;
import org.openedit.util.PathUtilities;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class FileItem extends ContentItem implements Data
{
	private static final Log log = LogFactory.getLog(FileItem.class);
	protected String fieldSourcePath;
	
	protected File fieldFile;
	/**
	 * 
	 */
	public FileItem()
	{
	}
	protected String getParentPath()
	{
		return PathUtilities.extractDirectoryPath( getPath() );
	}

	public long getLastModified()
	{
		return getFile().lastModified();
	}

	public Date lastModified()
	{
		return new Date( getLastModified() );
	}

	public InputStream getInputStream() throws RepositoryException
	{
		if ( isFolder() )
		{
			return createFileListingStream();
		}
		try
		{
			if (getFile().exists() )
			{
				return new FileInputStream( getFile() );
			}
		}
		catch( FileNotFoundException e )
		{
			throw new RepositoryException( e );
		}
		return null;
	}

	public OutputStream getOutputStream() throws RepositoryException
	{
		if ( isFolder() )
		{
			log.error("Cant output to a folder");
			return null;
		}
		try
		{
			if (!getFile().getParentFile().exists() )
			{
				getFile().getParentFile().mkdirs();
			}
			return new FileOutputStream( getFile() );
		}
		catch( FileNotFoundException e )
		{
			throw new RepositoryException( e );
		}
	}

	
	protected InputStream createFileListingStream()
	{
		File[] files = getFile().listFiles();
		StringBuffer sb = new StringBuffer();
		for ( int i = 0; i < files.length; i++ )
		{
			if ( !files[i].getName().equals(".versions") )
			{
				sb.append( files[i].getName() + "\n" );
			}
		}
		return new ByteArrayInputStream( sb.toString().getBytes() );
	}
	public boolean exists()
	{
		return getFile().exists();
	}

	public boolean isFolder()
	{
		return getFile().isDirectory();
	}

	public boolean isWritable()
	{
		return true;
	}

	public File getFile()
	{
		if(fieldFile == null && fieldAbsolutePath != null)
		{
			fieldFile = new File( getAbsolutePath());
		}
		return fieldFile;
	}
	public void setFile( File file )
	{
		fieldFile = file;
	}
	/* (non-javadoc)
	 * @see com.einnovation.repository.ContentItem#getLength()
	 */
	public long getLength()
	{
		return getFile().length();
	}
	public Reader getReader() throws RepositoryException
	{
		try
		{
			if (getFile().exists() )
			{
				return new FileReader(getFile());
			}
		}
		catch( FileNotFoundException e )
		{
			throw new RepositoryException( e );
		}
		return null;
	}
	public String getAbsolutePath()
	{
		if( fieldAbsolutePath == null && fieldFile != null)
		{
			return getFile().getAbsolutePath();
		}
		return fieldAbsolutePath;
	}
	public void setAbsolutePath(String inAbsolutePath)
	{
		fieldAbsolutePath = inAbsolutePath;
	}
	
	public void setId(String inNewid)
	{
		throw new IllegalAccessError("Not implemented");
	}
	public void setProperties(Map inProperties)
	{
	}
	
	public void setName(String inName)
	{
		throw new IllegalAccessError("Not implemented");
	}
	
	public void setSourcePath(String inSourcepath)
	{
		fieldSourcePath = inSourcepath;
	}
	
	public String getSourcePath()
	{
		return fieldSourcePath;
	}
	
	public void setProperty(String inId, String inValue)
	{
		throw new IllegalAccessError("Not implemented");
	}
	
	public String get(String inId)
	{
		throw new IllegalAccessError("Not implemented");
	}
	
	public Map getProperties()
	{
		throw new IllegalAccessError("Not implemented");
	}
	
	public void setValues(String inKey, Collection<String> inValues)
	{
		StringBuffer values = new StringBuffer();
		for (Iterator iterator = inValues.iterator(); iterator.hasNext();)
		{
			String detail = (String) iterator.next();
			values.append(detail);
			if( iterator.hasNext())
			{
				values.append(" | ");
			}
		}
		setProperty(inKey,values.toString());
	}
	@Override
	public Object getValue(String inKey)
	{
		return get(inKey);
	}
	@Override
	public void setValue(String inKey, Object inValue)
	{
		setProperty(inKey, String.valueOf(inValue));
	}
	@Override
	public String toString()
	{
		String val = getPath();
		if( val == null)
		{
			val = super.toString();
		}
		return val;
	}
	public String getName(String inLocale) {
		return getName();
	}
	
	
	
	public void setLastModified(Date inDate)
	{
		// TODO Auto-generated method stub
		getFile().setLastModified(inDate.getTime());
	}
}
