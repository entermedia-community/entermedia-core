package org.openedit.generators.filters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.openedit.page.Page;

public class TranslationFilter extends java.io.Reader
{
	protected BufferedReader in;
	String lastLine = null;
	int lastLineIndex;
	int lastLineLength;
	String encoding;
	
	// Multi-line marker support
	protected static final String OPEN_MARKER = "[[";
	protected static final String CLOSE_MARKER = "]]";
	protected StringBuilder multiLineBuffer = null;
	
	// Translation support
	protected Page page;
	protected String locale;

	public TranslationFilter(Page inPage, String inParams) 
	{
		Reader inIn = inPage.getReader();
		if( inIn instanceof BufferedReader)
		{
			in = (BufferedReader)inIn;
		}
		else
		{
			in = new BufferedReader(inIn);
		}
		encoding = inPage.getCharacterEncoding();
		page = inPage;
		int index = inParams.indexOf("&locale=");
		locale = inParams.substring(index + "&locale=".length(), inParams.length());
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
			lastLine = readAndProcessLines();
			lastLineIndex = 0;
			if( lastLine == null)
			{
				return -1;
			}
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
	
	/**
	 * Reads lines and handles multi-line blocks delimited by [[ and ]].
	 * Accumulates content between markers before calling replace().
	 */
	protected String readAndProcessLines() throws IOException
	{
		StringBuffer result = new StringBuffer();
		
		while(true)
		{
			String line = in.readLine();
			if(line == null)
			{
				// End of stream - process any remaining buffered content
				if(multiLineBuffer != null)
				{
					StringBuffer processed = replace(multiLineBuffer.toString());
					result.append(processed);
					multiLineBuffer = null;
				}
				return result.length() > 0 ? result.toString() : null;
			}
			
			if(multiLineBuffer != null)
			{
				// We're inside a multi-line block, look for closing marker
				int closeIndex = line.indexOf(CLOSE_MARKER);
				if(closeIndex >= 0)
				{
					// Found closing marker - complete the multi-line block
					multiLineBuffer.append(line.substring(0, closeIndex + CLOSE_MARKER.length()));
					StringBuffer processed = replace(multiLineBuffer.toString());
					result.append(processed);
					
					// Process remainder of line after closing marker
					String remainder = line.substring(closeIndex + CLOSE_MARKER.length());
					if(remainder.length() > 0)
					{
						// Check if remainder has another opening marker
						int openIndex = remainder.indexOf(OPEN_MARKER);
						if(openIndex >= 0)
						{
							// Process content before the marker
							if(openIndex > 0)
							{
								StringBuffer beforeMarker = replace(remainder.substring(0, openIndex));
								result.append(beforeMarker);
							}
							// Start new multi-line buffer
							multiLineBuffer = new StringBuilder();
							multiLineBuffer.append(remainder.substring(openIndex));
						}
						else
						{
							multiLineBuffer = null;
							StringBuffer remainderProcessed = replace(remainder);
							result.append(remainderProcessed);
						}
					}
					else
					{
						multiLineBuffer = null;
					}
					result.append('\n');
					return result.toString();
				}
				else
				{
					// No closing marker yet, keep accumulating
					multiLineBuffer.append(line).append('\n');
				}
			}
			else
			{
				// Not in a multi-line block, look for opening marker
				int openIndex = line.indexOf(OPEN_MARKER);
				if(openIndex >= 0)
				{
					// Check if closing marker is on the same line
					int closeIndex = line.indexOf(CLOSE_MARKER, openIndex + OPEN_MARKER.length());
					if(closeIndex >= 0)
					{
						// Both markers on same line - process normally
						StringBuffer processed = replace(line);
						processed.append('\n');
						return processed.toString();
					}
					else
					{
						// Opening marker without closing - start multi-line buffer
						if(openIndex > 0)
						{
							// Process content before the marker first
							StringBuffer beforeMarker = replace(line.substring(0, openIndex));
							result.append(beforeMarker);
						}
						multiLineBuffer = new StringBuilder();
						multiLineBuffer.append(line.substring(openIndex)).append('\n');
						// Continue reading to find closing marker
					}
				}
				else
				{
					// No markers - process single line normally
					StringBuffer processed = replace(line);
					processed.append('\n');
					return processed.toString();
				}
			}
		}
	}

	protected StringBuffer replace(String inLastLine)
	{
		int bracket = inLastLine.indexOf("[[");  
		if( bracket == -1)
		{
			return new StringBuffer(inLastLine);
		}
		//look for [[ and get the property to replace it with
		StringBuffer done = new StringBuffer(inLastLine.length() + 20);
		int start = 0;
		char[] line = inLastLine.toCharArray();
		while( bracket != -1 )
		{
			int end = inLastLine.indexOf("]]",bracket);
			if( end != -1 )
			{
				String key = inLastLine.substring(bracket + 2,end);
				String value = page.getText(key, locale);
				
				done.append(line,start,bracket - start); //everything up to this point
				done.append(value);
				start = end + 2;
				bracket = inLastLine.indexOf("[[",start);
			}
			else
			{
				done.append(line,start,line.length);				
				start = line.length;
				break; //no closing ]]
			}
		}
		if( start < line.length)
		{
			done.append(line,start,line.length - start);
		}
		return done;
	}

	public void close() throws IOException
	{
		in.close();
		in = null;
	}
	
}
