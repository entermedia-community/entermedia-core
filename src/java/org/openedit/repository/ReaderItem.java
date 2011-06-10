package org.openedit.repository;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Date;

import org.openedit.util.ReaderInputStream;


/**
 * Used to write items where all we have is a Reader
 * @author cburkey
 */

public class ReaderItem extends ContentItem
{
	protected Reader fieldReader;
	protected String fieldEncoding;
	
	public ReaderItem(String inPath, Reader inReader, String inEncoding)
	{
		setReader(inReader);
		setEncoding(inEncoding);
		fieldPath = inPath;
	}
	public void setLastModified(Date inDate)
	{
		// TODO Auto-generated method stub
		super.setLastModified(inDate);
	}
	public boolean exists()
	{
		return fieldReader != null;
	}
	public boolean isFolder()
	{
		return false;
	}
	public boolean isWritable()
	{
		return true;
	}

	public Reader getReader()
	{
		return fieldReader;
	}

	public void setReader(Reader inReader)
	{
		fieldReader = inReader;
	}
	public String getEncoding()
	{
		return fieldEncoding;
	}
	public void setEncoding(String inEncoding)
	{
		fieldEncoding = inEncoding;
	}
	public InputStream getInputStream() throws RepositoryException
	{
		return new ReaderInputStream(getReader(),getEncoding());
	}
	public OutputStream getOutputStream() throws RepositoryException
	{
		return null;
	}
}
