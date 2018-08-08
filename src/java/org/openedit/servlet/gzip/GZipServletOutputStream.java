package org.openedit.servlet.gzip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

public class GZipServletOutputStream extends ServletOutputStream
{
	  private GZIPOutputStream    gzipOutputStream = null;

	  public GZipServletOutputStream(OutputStream output)
	        throws IOException {
	    super();
	    this.gzipOutputStream = new GZIPOutputStream(output);
	  }

	  @Override
	  public void close() throws IOException {
	    this.gzipOutputStream.close();
	  }

	  @Override
	  public void flush() throws IOException {
	    this.gzipOutputStream.flush();
	  }

	  @Override
	  public void write(byte b[]) throws IOException {
	    this.gzipOutputStream.write(b);
	  }

	  @Override
	  public void write(byte b[], int off, int len) throws IOException {
	    this.gzipOutputStream.write(b, off, len);
	  }

	  @Override
	  public void write(int b) throws IOException {
	     this.gzipOutputStream.write(b);
	  }

	public boolean isReady()
	{
		// TODO Auto-generated method stub
		return true;
	}

	public void setWriteListener(WriteListener inArg0)
	{
		// TODO Auto-generated method stub
		
	}
}