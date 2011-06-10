/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package com.openedit;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * This is the superclass of all exceptions thrown from Open Edit.
 *
 * @author Eric Galluzzo
 */
public class OpenEditException extends OpenEditRuntimeException implements Serializable
{
	private static final long serialVersionUID = 1630227379714618008L;
	
	private static final Log log = LogFactory.getLog(OpenEditException.class);
	protected String fieldPathWithError;

	public OpenEditException()
	{
		this("No Error Entered");
	}
	public OpenEditException(String inMsg)
	{
		this(inMsg, (Throwable) null);
	}

	public OpenEditException(String inMsg, String inPath)
	{
		this(inMsg, null, inPath);
	}

	public OpenEditException(String inMsg, Throwable inRootCause, String inPath)
	{
		super(inMsg, inRootCause);

		if (inRootCause instanceof OpenEditException)
		{
			log.error("Should not wrap an exception of type OpenEditException ");
		}
		setPathWithError(inPath);
	}

	public OpenEditException(String inMsg, Throwable inRootCause)
	{
		this(inMsg, inRootCause, null);
	}

	public OpenEditException(Throwable inRootCause, String inPath)
	{
		this(inPath + " " + inRootCause.getMessage(), inRootCause, inPath);
	}

	public OpenEditException(Throwable inRootCause)
	{
		this(inRootCause.getMessage(), inRootCause);
	}

	/**
	 * DOCME
	 *
	 * @return DOCME
	 */
	public String getMessage()
	{
		String message = super.getMessage();

		if ((message == null) || (message.length() == 0))
		{
			if (getCause() != null)
			{
				return getCause().getMessage();
			}
			else
			{
				return "No error message";
			}
		}
		else
		{
			return message;
		}
	}

	/**
	 * Sets the fieldPathWithError.
	 *
	 * @param fieldPathWithError The fieldPathWithError to set
	 */
	public void setPathWithError(String fieldPathWithError)
	{
		this.fieldPathWithError = fieldPathWithError;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return String
	 */
	public String getPathWithError()
	{
		return fieldPathWithError;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	public String toStackTrace()
	{
		StringWriter out = new StringWriter();

		if (getCause() != null)
		{
			getCause().printStackTrace(new PrintWriter(out));
		}
		else
		{
			printStackTrace(new PrintWriter(out));
		}

		return out.toString();
	}
}
