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
public class OutputStreamItem extends ContentItem
{
	protected OutputStream fieldOutputStream;
	protected long fieldSeek;
	
	public long getSeek()
	{
		return fieldSeek;
	}
	public void setSeek(long inSeek)
	{
		fieldSeek = inSeek;
	}
	public OutputStreamItem()
	{
	}
	public OutputStreamItem(String inPath)
	{
		setPath(inPath);
	}
	public OutputStream getOutputStream() throws RepositoryException
	{
		return fieldOutputStream;
	}
	public void setOutputStream(OutputStream inStream)
	{
		fieldOutputStream = inStream;
	}
	public boolean exists()
	{
		return fieldOutputStream != null;
	}
	public boolean isFolder()
	{
		return false;
	}
	public boolean isWritable()
	{
		return true;
	}
	public InputStream getInputStream() throws RepositoryException
	{
		return null;
	}

}
