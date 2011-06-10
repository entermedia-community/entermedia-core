/*
 * Created on Aug 8, 2004
 */
package org.openedit.repository.filesystem;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.repository.ContentItem;
import org.openedit.repository.RepositoryException;
import org.openedit.util.ReaderInputStream;



/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class StringItem	 extends ContentItem
{
	protected String fieldContent;
	protected String fieldOutputEncoding;
	protected boolean fieldWritable = true;
	private static final Log log = LogFactory.getLog(StringItem.class);
	
	public StringItem( String inPath, String inContent, String inEncoding )
	{
		//fieldLastModified = new Date();
		if ( inContent == null)
		{
			inContent = "";
		}
		fieldContent = inContent;
		fieldPath = inPath;
		fieldOutputEncoding  = inEncoding;
		//setLastModified( new Date() );
	}
	/**
	 * 
	 */
	public StringItem()
	{
	}
	
	public InputStream getInputStream() throws RepositoryException
	{
		if ( getOutputEncoding() == null)
		{
			log.error("Encoding not defined");
			return new ByteArrayInputStream(getContent().getBytes());
		}
		try
		{
			//BufferedReader reader = new BufferedReader ( new InputStreamReader ( in ) );
			return new ReaderInputStream(new StringReader(getContent()),getOutputEncoding());
		} catch ( Exception ex)
		{
			log.error(ex);
			throw new RepositoryException(ex);
		}
	}

	public boolean exists()
	{
		return getContent() != null;
	}

	public boolean isFolder()
	{
		return false;
	}
	/**
	 * We use the StringItem to make changes and save them back again
	 */	
	public boolean isWritable()
	{
		return fieldWritable;
	}


	public String getContent()
	{
		return fieldContent;
	}
	public void setContent( String content )
	{
		fieldContent = content;
		setLastModified( new Date() );
	}
	protected void setWritable(boolean inWritable)
	{
		fieldWritable = inWritable;
	}
	public String getOutputEncoding()
	{
		return fieldOutputEncoding;
	}
	public void setOutputEncoding(String inOutputEncoding)
	{
		fieldOutputEncoding = inOutputEncoding;
	}
}
