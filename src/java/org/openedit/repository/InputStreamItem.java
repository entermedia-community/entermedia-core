/*
 * Created on Jan 5, 2005
 */
package org.openedit.repository;

import java.io.InputStream;
import java.io.OutputStream;


/**
 * Lets us deal with just an input stream
 * @author cburkey
 *
 */
public class InputStreamItem extends ContentItem
{
	protected InputStream fieldInputStream;
	
	public InputStreamItem()
	{
	}
	
	/* (non-javadoc)
	 * @see com.einnovation.repository.filesystem.FileItem#getInputStream()
	 */
	public InputStream getInputStream() throws RepositoryException
	{
		return fieldInputStream;
	}
	public void setInputStream(InputStream inStream)
	{
		fieldInputStream = inStream;
	}
	public boolean exists()
	{
		return fieldInputStream != null;
	}
	public boolean isFolder()
	{
		// TODO Auto-generated method stub
		return false;
	}
	public boolean isWritable()
	{
		// TODO Auto-generated method stub
		return false;
	}
	public OutputStream getOutputStream() throws RepositoryException
	{
		return null;
	}

}
