package com.openedit.servlet.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

public class GzipRequestStream extends ServletInputStream {

	protected InputStream zipInput = null;
	protected HttpServletRequest request = null;

	public GzipRequestStream(HttpServletRequest request) {
		this.request = request;
		try {
			InputStream is = request.getInputStream();
			zipInput = new GZIPInputStream(is);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void close() throws IOException {
		zipInput.close();
	}

	public int read() throws IOException {
		return zipInput.read();
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return zipInput.read(b, off, len);
	}
}