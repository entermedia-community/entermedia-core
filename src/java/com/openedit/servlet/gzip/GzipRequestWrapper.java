package com.openedit.servlet.gzip;

import java.io.IOException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class GzipRequestWrapper extends HttpServletRequestWrapper {
	protected HttpServletRequest origRequest = null;
	protected ServletInputStream stream = null;

	public GzipRequestWrapper(HttpServletRequest request) {
		super(request);
		this.origRequest = request;
	}

	public ServletInputStream getInputStream() throws IOException {
		if (stream == null)
			stream = createInputStream();
		return (stream);
	}

	public ServletInputStream createInputStream() throws IOException {
		return (new GzipRequestStream(origRequest));
	}
}