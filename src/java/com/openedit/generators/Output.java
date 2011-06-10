/*
 * Created on May 20, 2006
 */
package com.openedit.generators;

import java.io.OutputStream;
import java.io.Writer;

public class Output
{
	Writer fieldWriter;
	OutputStream fieldStream;
	
	public Output()
	{
		
	}
	
	public Output(Writer inOut, OutputStream inStream)
	{
		fieldWriter = inOut;
		fieldStream = inStream;
	}
	public OutputStream getStream()
	{
		return fieldStream;
	}
	public void setStream(OutputStream inStream)
	{
		fieldStream = inStream;
	}
	public Writer getWriter()
	{
		return fieldWriter;
	}
	public void setWriter(Writer inWriter)
	{
		fieldWriter = inWriter;
	}
	
}
