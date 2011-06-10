package org.openedit.generators.filters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public abstract class TextReaderFilter extends java.io.Reader
{
	protected BufferedReader in;
	String lastLine = null;
	int lastLineIndex;
	int lastLineLength;
	String encoding;

	public TextReaderFilter(Reader inIn, String inEncoding) 
	{
		if( inIn instanceof BufferedReader)
		{
			in = (BufferedReader)inIn;
		}
		else
		{
			in = new BufferedReader(inIn);
		}
		encoding = inEncoding;
	}
	
	public int read(char[] b, int off, int len) throws IOException
	{
		if (in == null)
		{
			throw new IOException("Stream Closed");
		}
		int n = readFromLine(b,off,len);
		return n;
	}

	public int readFromLine(char[] output, int off, int len) throws IOException
	{
		if( lastLine == null)
		{
			lastLine = in.readLine();
			lastLineIndex = 0;
			if( lastLine == null)
			{
				return -1;
			}
			StringBuffer buffer = replace(lastLine);
			buffer.append('\n');
			lastLine = buffer.toString();
			lastLineLength = lastLine.length();
		}
		int size = Math.min(len, lastLineLength - lastLineIndex);
		lastLine.getChars(lastLineIndex, lastLineIndex + size, output,off);
		lastLineIndex = lastLineIndex + size;
		if( lastLineIndex == lastLineLength)
		{
			lastLine = null; //ran out of string
		}
		return size;
		//take the char[] and copy into buffer
	}

	protected abstract StringBuffer replace(String inLastLine);

	public void close() throws IOException
	{
		in.close();
		in = null;
	}
	
}
