package com.openedit.servlet.gzip;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class GzipResponseWrapper extends HttpServletResponseWrapper {
  protected HttpServletResponse origResponse = null;
  protected ServletOutputStream stream = null;
  protected PrintWriter writer = null;

  public GzipResponseWrapper(HttpServletResponse response) {
    super(response);
    origResponse = response;
  }

  public ServletOutputStream createOutputStream() throws IOException {
    return (new GzipResponseStream(origResponse));
  }

  public void finishResponse() {
    try {
      if (writer != null) {
        writer.close();
      } else {
        if (stream != null) {
          stream.close();
        }
      }
    } catch (IOException e) {}
  }

  public void flushBuffer() throws IOException {
    stream.flush();
  }

  public ServletOutputStream getOutputStream() throws IOException {
    if (writer != null) {
      throw new IllegalStateException("getWriter() has already been called!");
    }

    if (stream == null)
      stream = createOutputStream();
    return (stream);
  }

  public PrintWriter getWriter() throws IOException {
    if (writer != null) {
      return (writer);
    }

    if (stream != null) {
      throw new IllegalStateException("getOutputStream() has already been called!");
    }

   stream = createOutputStream();
   // BUG FIX 2003-12-01 Reuse content's encoding, don't assume UTF-8
   writer = new PrintWriter(new OutputStreamWriter(stream, origResponse.getCharacterEncoding()));
   return (writer);
  }

  public void setContentLength(int length) {}
}
