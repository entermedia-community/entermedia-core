/*
 * Created on Aug 1, 2003
 *
/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/
package org.openedit.repository;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.openedit.util.PathUtilities;

/**
 * The ContentItem is a lot like a versioned network "file".
 *  
 * @author Matt Avery, mavery@einnovation.com
 */
public abstract class ContentItem
{

	public final static String TYPE_EDITED = "edited";
	public final static String TYPE_ADDED = "added";
	public final static String TYPE_MOVED = "moved";
	public final static String TYPE_COPIED = "copied";
	public final static String TYPE_REMOVED = "removed";
	public final static String TYPE_APPROVED = "approved";

	protected String fieldAuthor;
	protected String fieldMessage;
	protected String fieldVersion;
	protected String fieldType;
	protected String fieldPreviewImage;
	protected String fieldPath;
	protected String fieldActualPath; //only used when content actually comes from another path
	protected String fieldAbsolutePath; //Used as an URL or full system location
	protected boolean fieldStub;
	protected Date fieldLastModified;
	protected long fieldLength = -1;
	protected boolean fieldMakeVersion = true; //used when saving content
	protected OutputStream fieldOutputStream;
	
	
	public boolean isStub()
	{
		return fieldStub;
	}
	public void setStub(boolean inStub)
	{
		fieldStub = inStub;
	}
	public void setAbsolutePath(String inAbsolutePath)
	{
		fieldAbsolutePath = inAbsolutePath;
	}

	public String getPreviewImage()
	{
		return fieldPreviewImage;
	}

	public void setPreviewImage(String inPreviewImage)
	{
		fieldPreviewImage = inPreviewImage;
	}

	/**
	 * This path is relative.
	 * 
	 * @return
	 */
	public String getPath()
	{
		return fieldPath;
	}
	public void setPath( String path )
	{
		fieldPath = path;
	}
	public String getName()
	{
		if( getPath().endsWith("/"))
		{
			String path  = getPath().substring(0,getPath().length() - 1);
			return PathUtilities.extractFileName(path);
		}
		return PathUtilities.extractFileName(getPath());
	}
	/**
	 * If exists() == false, this method should return 1/1/1970 instead
	 * of null.
	 * 
	 * @return
	 */
	public Date lastModified()
	{
		//lazy load this?
//		if( fieldLastModified == null)
//		{
//			fieldLastModified = new Date();
//		}
		return fieldLastModified;
	}
	public long getLastModified()
	{
		if( lastModified() != null)
		{
			return lastModified().getTime();
		}
		return 0;
	}
	public void setLastModified(Date inDate)
	{
		fieldLastModified = inDate;
	}
	/**
	 * This will return the contents of the file this item points to *or*
	 * a listing of the path in XHTML if we are pointing to a "directory".
	 * 
	 * @return An input stream of the content
	 * @throws RepositoryException
	 */
	public abstract InputStream getInputStream() throws RepositoryException;

	public abstract boolean exists();
	
	public abstract boolean isFolder();


	public abstract boolean isWritable();
	
	//valid types are edited, added, removed
	
	/**
	 * I would almost like to see this return something more structured than
	 * a String.  For instance, I could see the Revision being used in conjunction
	 * to an issue tracking system which might have quite a lot of information in
	 * the "message" string.  In that case, I would store this guy as an XML file.
	 * 
	 * @return A meaningful message for this revision.
	 */
	public String getAuthor()
	{
		return fieldAuthor;
	}
	public void setAuthor( String author )
	{
		fieldAuthor = author;
	}

	public String getMessage()
	{
		return fieldMessage;
	}
	public void setMessage( String message )
	{
		fieldMessage = message;
	}
	/**
	 * This could be changed to return an integer.  Also, the field names could
	 * be shortened.
	 * 
	 * @return
	 */
	public String getType()
	{
		return fieldType;
	}
	public void setType( String type )
	{
		fieldType = type;
	}
	/**
	 * This is the one and only method of the "Versioned" interface.  There
	 * also exists "VersionComparator" and "VersionFormat" classes that could
	 * be used in conjunction with this interface.
	 * 
	 * @return The version String.
	 */
	public String getVersion()
	{
		return fieldVersion;
	}
	public void setVersion( String version )
	{
		fieldVersion = version;
	}
	
	/**
	 * Should this class make a version of the file when saving
	 * @return
	 */	
	public boolean isMakeVersion()
	{
		return fieldMakeVersion;
	}
	public void setMakeVersion(boolean inMakeVersion)
	{
		fieldMakeVersion = inMakeVersion;
	}
	/**
	 * @return
	 */
	public long getLength()
	{
		return fieldLength;
	}
	
	/**
	 * Used to point at the OpenEdit path that this actually goes to
	 * @deprecated The usage is not clear. Use getPath() for OpenEdit paths and getAbsolutePath() for system paths
	 */
	public String getActualPath()
	{
		if( fieldActualPath == null)
		{
			return getPath();
		}
		return fieldActualPath;
	}
	public void setActualPath(String inActualPath)
	{
		fieldActualPath = inActualPath;
	}
	public String getId()
	{
		String  id = PathUtilities.makeId(getPath());
		id = id.replace('/', '_');
		return id;
	}
	public OutputStream getOutputStream() throws RepositoryException
	{
		return fieldOutputStream;
	}

	public void setOutputStream(OutputStream inOutputStream)
	{
		fieldOutputStream = inOutputStream;
	}
	public String getAbsolutePath()
	{
		if( fieldAbsolutePath == null)
		{
			return getPath();
		}
		return fieldAbsolutePath;
	}
}
