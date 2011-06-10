/*
 * Created on Jan 25, 2006
 */
package com.openedit.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;

public class DecoratedServletOutputStream extends ServletOutputStream
{
		PrintWriter writer;
		OutputStream out;
		public DecoratedServletOutputStream(PrintWriter inWriter)
		{
			this.writer = inWriter;
		}
		public DecoratedServletOutputStream(OutputStream inOut)
		{
			out = inOut;
		}
		public void write(int b) throws java.io.IOException
		{
			// This method is not used but have to be implemented
			if ( writer != null )
			{
				this.writer.write(b);
			}
			else
			{
				out.write(b);
			}
		}
		public void write(byte[] inB) throws IOException
		{
			out.write(inB);
		}
		public void write(byte[] inB, int inOff, int inLen) throws IOException
		{
			//super.write(inB, inOff, inLen);
			out.write(inB,inOff,inLen);
		}
		public void flush() throws IOException
		{
			if( writer != null)
			{
				writer.flush();
			}
			else
			{
				out.flush();
			}
		}
		
		public PrintWriter getWriter()
		{
			return writer;
		}
	
}
