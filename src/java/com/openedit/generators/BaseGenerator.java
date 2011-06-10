/*
 * Created on Jun 5, 2005
 */
package com.openedit.generators;

import java.io.EOFException;

import com.openedit.Generator;
import com.openedit.WebPageRequest;
import com.openedit.util.OutputFiller;

/**
 * @author cburkey
 *
 */
public abstract class BaseGenerator implements Generator, Cloneable
{
	protected String fieldName;
	private OutputFiller fieldOutputFiller;

	protected OutputFiller getOutputFiller()
	{
		if ( fieldOutputFiller == null )
		{
			fieldOutputFiller = new OutputFiller();
		}
		return fieldOutputFiller;
	}

	
	public String getName()
	{
		return fieldName;
	}
	public void setName(String inName)
	{
		fieldName = inName;
	}
	//TODO: Should this check for exists? What about isBinary?
	public boolean canGenerate(WebPageRequest inReq)
	{
		return !inReq.getPage().isBinary();
	}
	public Object clone()
	{
		try
		{
			return super.clone();
		}
		catch (CloneNotSupportedException ex)
		{
			//silent, will never happen
			return null;
		}
	}
	public boolean hasGenerator(Generator inChild)
	{
		return inChild == this;
	}


	protected boolean ignoreError(Throwable inWrapped)
	{
		if( inWrapped == null )
		{
			return false;
		}
		if( inWrapped instanceof EOFException )
		{
			return true;
		}
		String message = inWrapped.toString() + inWrapped.getMessage();
		
		if ( message.indexOf("Broken pipe") > -1 || 
			message.indexOf("socket write error") > -1 ||
			message.indexOf("Connection reset") > -1 )	 //tomcat			
		{
			return true;
		}
	
		return ignoreError( inWrapped.getCause() );
	}

}
