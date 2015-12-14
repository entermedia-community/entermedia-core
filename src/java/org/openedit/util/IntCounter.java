/*
 * Created on Mar 13, 2006
 */
package org.openedit.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.openedit.OpenEditRuntimeException;

public class IntCounter
{
	protected File fieldCounterFile;
	protected String fieldLabelName = "idCount";
	
	public synchronized int incrementCount() 
	{
		try
		{
			int i = getIdCounter();
			i++;
			saveCount(i);
			return i;
		} catch (IOException ex)
		{
			throw new OpenEditRuntimeException(ex);
		}
	}

	/**
	 * @throws IOException
	 */
	public synchronized void saveCount(int inCount) throws IOException
	{
		File tmp = new File(getCounterFile().getParentFile(), ".tmp" );
		FileOutputStream out = new FileOutputStream(tmp);
		try
		{
			Properties fieldCustomerIdCounter = new Properties();
			fieldCustomerIdCounter.setProperty(getLabelName(),String.valueOf(inCount));
			fieldCustomerIdCounter.store(out,"");
		}
		finally
		{
			out.close();
		}
		getCounterFile().delete();
		if( !tmp.renameTo(getCounterFile()) )
		{
			throw new IOException("Could not update counter file " + getCounterFile().getAbsolutePath());
		}		
	}
	protected synchronized int getIdCounter() throws IOException, IllegalStateException
	{
		Properties fieldCustomerIdCounter = new Properties();
		if ( !getCounterFile().exists() )
		{
			saveCount(100);
		}
		
		FileInputStream in = new FileInputStream( getCounterFile() );
		try
		{
			fieldCustomerIdCounter.load( in );
		}
		finally
		{
			in.close();
		}
		
		String count = fieldCustomerIdCounter.getProperty( getLabelName() );
		if ( count == null )
		{
			throw new IllegalStateException( "Could not find valid ID counter " + getCounterFile() + " with label " + getLabelName() );
		}
	
		int i = Integer.parseInt( count );
		return i;
	}

	public File getCounterFile()
	{
		return fieldCounterFile;
	}

	public void setCounterFile(File inCounterFile)
	{
		fieldCounterFile = inCounterFile;
		if( inCounterFile != null)
		{
			inCounterFile.getParentFile().mkdirs();
		}
	}

	public String getLabelName()
	{
		return fieldLabelName;
	}

	public void setLabelName(String inLabelName)
	{
		fieldLabelName = inLabelName;
	}

	
	
}
