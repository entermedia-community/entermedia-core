/*
 * Created on Jan 25, 2006
 */
package com.openedit.servlet;

import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;


/**
 * Stub implementation of HttpServletResponse
 */
public class DecoratedServletResponse extends HttpServletResponseWrapper
{
	DecoratedServletOutputStream output;
	public DecoratedServletResponse(HttpServletResponse response, DecoratedServletOutputStream inOut)
	{
		super(response);
		this.output = inOut;
	}

	public void setContentLength(int len)
	{
	}
	public ServletOutputStream getOutputStream()
	{
		return this.output;
	}

	public PrintWriter getWriter()
	{
		return output.getWriter();
	}
}
