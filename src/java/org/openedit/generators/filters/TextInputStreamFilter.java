package org.openedit.generators.filters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class TextInputStreamFilter extends InputStream
{
	protected BufferedReader in;
	String lastLine = null;
	int lastLineIndex;
	int lastLineLength;
	byte[] slack;
	private int begin;
	String encoding;

	//TranslationFilter(InputStream inIn, Page inPage, String inLocale)
	public TextInputStreamFilter(InputStream inIn, String inEncoding) throws UnsupportedEncodingException
	{
		in = new BufferedReader(new InputStreamReader(inIn, inEncoding));
		encoding = inEncoding;
	}
	public TextInputStreamFilter(BufferedReader inIn, String inEncoding) throws UnsupportedEncodingException
	{
		in = inIn;
		encoding = inEncoding;
	}
	
	public synchronized int read() throws IOException
	{
		if (in == null)
		{
			throw new IOException("Stream Closed");
		}

		byte result;
		if (slack != null && begin < slack.length)
		{
			result = slack[begin];
			if (++begin == slack.length)
			{
				slack = null;
			}
		}
		else
		{
			byte[] buf = new byte[1];
			if (read(buf, 0, 1) <= 0)
			{
				result = -1;
			}
			result = buf[0];
		}

		if (result < -1)
		{
			result += 256;
		}

		return result;
	}

	public synchronized int read(byte[] b, int off, int len) throws IOException
	{
		if (in == null)
		{
			throw new IOException("Stream Closed");
		}

		while (slack == null)
		{
			char[] buf = new char[len]; // might read too much
			int n = readFromLine(buf);
			if (n == -1)
			{
				return -1;
			}
			if (n > 0)
			{
				slack = new String(buf, 0, n).getBytes(encoding);
				begin = 0;
			}
		}

		if (len > slack.length - begin)
		{
			len = slack.length - begin;
		}

		System.arraycopy(slack, begin, b, off, len);

		if ((begin += len) >= slack.length)
		{
			slack = null;
		}

		return len;
	}

	public int readFromLine(char[] output) throws IOException
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
		int size = Math.min(output.length, lastLineLength - lastLineIndex); //half the bytes?
		lastLine.getChars(lastLineIndex, lastLineIndex + size, output,0);
		lastLineIndex = lastLineIndex + size;
		if( lastLineIndex == lastLineLength)
		{
			lastLine = null; //ran out of string
		}
		return size;
		//take the char[] and copy into buffer
	}

	protected StringBuffer replace(String inLastLine)
	{
		
		return null;
	}
	
	
	
}
