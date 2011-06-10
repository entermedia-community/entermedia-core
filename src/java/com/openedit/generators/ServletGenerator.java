package com.openedit.generators;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openedit.OpenEditException;
import com.openedit.WebPageRequest;
import com.openedit.page.Page;

/**
 * This repository implements a JSP compiler using the {@link NamedDispatcher}
 * class in the standard servlet API.
 */
public class ServletGenerator extends BaseGenerator
{

	public ServletGenerator()
	{
		super();

	}

	protected static final Log log = LogFactory.getLog(ServletGenerator.class);


	public boolean canGenerate(WebPageRequest inReq)
	{
		return true;
	}

	/*
	 * Overloaded method to execute Servlet.
	 * 
	 * Add to openedit.xml: <bean id="servlet"
	 * class="com.ackdev.openedit.generators.ServletGenerator"/>
	 * 
	 * Add following contents to /servlet/_site.xconf <page> <generator
	 * name="servlet" /> </page>
	 * 
	 * 
	 * 
	 * (non-Javadoc)
	 * 
	 * @see com.openedit.Generator#generate(com.openedit.WebPageRequest,
	 *      com.openedit.page.Page, com.openedit.generators.Output)
	 */
	public void generate(WebPageRequest inContext, Page inPage, Output inOut) throws OpenEditException
	{
		log.debug("Executing servlet filter " + inPage.getPath());
		HttpServletRequest httpRequest = inContext.getRequest();
		HttpServletResponse httpResponse = inContext.getResponse();
		DecoratedServletResponse response = new DecoratedServletResponse(httpResponse, new DecoratedServletOutputStream(inOut));

		FilterChain chain = (FilterChain) httpRequest.getAttribute("servletchain"); // Set
																					// in
																					// com.openedit.servlet.OpenEditFilter.java
		try
		{
			chain.doFilter(httpRequest, response);
		}
		catch (IOException e)
		{
			log.error(e);
		}
		catch (ServletException e)
		{
			log.error(e);
			throw new OpenEditException(e);
		}
	}

	/**
	 * Stub implementation of HttpServletResponse
	 */
	class DecoratedServletResponse extends HttpServletResponseWrapper
	{
		DecoratedServletOutputStream output;

		public DecoratedServletResponse(HttpServletResponse response, DecoratedServletOutputStream inOut)
		{
			super(response);
			this.output = inOut;

		}

		// Keep this stream open since we want to keep feeding data to the
		// stream
		public boolean isCommitted()
		{
			return false;
		}

		public void setContentLength(int len)
		{
			log.debug("setContentLength(int len) " + len);
		}

		public void setContentType(java.lang.String type)
		{
			log.debug("type:" + type);
			super.setContentType(type);
		}

		// this is for text
		public PrintWriter getWriter()
		{
			PrintWriter out = new PrintWriter(output.getOutput().getWriter())
			{
			};

			return out;
		}

		// This is binary
		public ServletOutputStream getOutputStream() throws IOException
		{

			return output;

		}

		public void reset()
		{
		}

		public void resetBuffer()
		{
		}

	}

	class DecoratedServletOutputStream extends ServletOutputStream
	{
		Output output;

		public DecoratedServletOutputStream(Output inOut)
		{
			this.output = inOut;

		}

		public void write(int b) throws java.io.IOException
		{
			// This method is not used but has to be implemented
			// this.writer.write(b);
			output.getStream().write(b);

		}

		public void write(byte b[]) throws IOException
		{
			write(b, 0, b.length);

		}

		public void write(byte b[], int off, int len) throws IOException
		{
			// System.out.println("writing...");
			output.getStream().write(b, off, len);

		}

		public void close() throws IOException
		{

			super.close();
		}

		public void flush() throws IOException
		{
			output.getStream().flush();

		}

		public Output getOutput()
		{

			return output;
		}

	}

}
