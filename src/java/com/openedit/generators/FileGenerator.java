/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

/*
 * (c) Copyright 2002 eInnovation Inc.
 * All Rights Reserved.
 */
package com.openedit.generators;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openedit.Generator;
import com.openedit.OpenEditException;
import com.openedit.WebPageRequest;
import com.openedit.error.ContentNotAvailableException;
import com.openedit.page.Page;
import com.openedit.page.manage.PageManager;
import com.openedit.util.FileUtils;
import com.openedit.util.SimpleDateFormatPerThread;



/**
 * This generator uses file steam to process a request for a page
 *
 * @author Chris Burkey
 */
public class FileGenerator extends BaseGenerator implements Generator
{
	private static Log log = LogFactory.getLog(FileGenerator.class);
	protected PageManager fieldPageManager;
	protected SimpleDateFormatPerThread fieldLastModFormat;
	
	public SimpleDateFormatPerThread getLastModFormat() 
	{
		if( fieldLastModFormat  == null)
		{
			//Tue, 05 Jan 2010 14:20:51 GMT  -- just english
			fieldLastModFormat = new SimpleDateFormatPerThread("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
			//log.info( fieldLastModFormat.format(new Date()) );
		}
		return fieldLastModFormat;
	}
	public PageManager getPageManager()
	{
		return fieldPageManager;
	}
	public void setPageManager(PageManager inPageManager)
	{
		fieldPageManager = inPageManager;
	}
	public void generate( WebPageRequest inContext, Page inPage, Output inOut ) throws OpenEditException
	{
		Page contentpage = inPage;

		HttpServletResponse res = inContext.getResponse();
		HttpServletRequest req = inContext.getRequest();
		if( res != null && req != null)
		{
			checkCors( req, res);
		}
		long start = -1;
		InputStream in = null;
		try
		{
			in = contentpage.getInputStream();
			if( in == null) 
			{
				String vir = contentpage.get("virtual");
				if ( !Boolean.parseBoolean(vir) )
				{
					log.info("Missing: " +contentpage.getPath());
					throw new ContentNotAvailableException("Missing: " +contentpage.getPath(),contentpage.getPath());
				}
				else
				{
					log.debug("not found: " + contentpage);
					return; //do nothing
				}
			}
			//only bother if we are the content page and not in development
			if ( res != null && inContext.getContentPage() == contentpage  )
			{
				boolean cached = checkCache(inContext, contentpage, req, res);
				if( cached )
				{
					return;
				}
			}
			//sometimes we can specify the length of the document
			//long length = -1;
			long end = -1;
			if( req != null && inContext.getContentPage() == contentpage )
			{
				//might need to seek
				String range = req.getHeader("Range");
				if( range != null && range.startsWith("bytes="))
				{
					//will need to seek
					int cutoff = range.indexOf("-");
					start = Long.parseLong( range.substring(6,cutoff));
					cutoff++;// 0 based
					if( range.length() > cutoff) // a trailing - means everything left
					{
						end = Long.parseLong( range.substring(cutoff));
					}
					//log.info("Requested " + range);
					res.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
				}
			}
			
			
			long length = -1;
			if ( res != null && !contentpage.isHtml() && inContext.getContentPage() == contentpage  )
			{	//we can set the length unless there is a decorator on it somehow
				length = (long)contentpage.getContentItem().getLength();
				if ( length != -1)
				{
					if( start > -1)
					{	
						if( end == -1)
						{
							end = length - 1;  //1024-1 1023
						}
						res.setHeader("Accept-Ranges", "bytes");
						res.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + length); //len is total
						long sent = end + 1 - start; // 0 1024 - 1024 length

						length = sent;
					}
					res.setContentLength((int)length);
				}
			}

			if ( contentpage.isBinary() )
			{
				in = streamBinary(inContext, contentpage, in, start, end, length, inOut);
			}
			else
			{
				InputStreamReader reader = null;
				if ( contentpage.getCharacterEncoding() != null )
				{
					reader = new InputStreamReader( in, contentpage.getCharacterEncoding() );
				}
				else
				{
					reader = new InputStreamReader( in );
				}
				//If you get an error about content length then your character encoding is not correct. Use UTF-8
				//maybe we need to write with the correct encoding then the files should match
				getOutputFiller().fill(reader, inOut.getWriter());
			}
		}
		catch ( Exception eof )
		{
			if( ignoreError(eof))
			{
				//log.error(eof); ignored
				return;
			}
			if( in == null)
			{
				log.error("Could not load " + contentpage.getPath());
			}
			throw new OpenEditException(eof);
		}
		finally
		{
			FileUtils.safeClose(in);
		}
		
	}
	
	protected InputStream streamBinary(WebPageRequest inReq, Page inPage, InputStream in, long start, long end, long length, Output inOut) throws IOException
	{
		OutputStream outs = inOut.getStream();
		if( start > -1)
		{
			BufferedInputStream buffer = new BufferedInputStream(in);
			long did = buffer.skip(start);
			if( did != start)
			{
				throw new OpenEditException("Could not seek to start");
			}
			in = buffer;
		}
		if( end != -1)
		{
			getOutputFiller().fill(in, outs, length);
		}
		else
		{					
			getOutputFiller().fill(in, outs);
		}
		return in;
	}
	
	protected boolean checkCache(WebPageRequest inContext, Page contentpage, HttpServletRequest req, HttpServletResponse res)
	{
		long now = System.currentTimeMillis();			
		boolean cache = true;
		String nocache = inContext.getRequestParameter("cache");
		if( nocache != null ) 
		{
			cache = Boolean.parseBoolean(nocache);
		}
		else
		{
			//is this recenlty modified?
			//3333333recent99  + 24 hours (mil * sec * min * hours) will be more than now
			cache = contentpage.getLastModified().getTime() + (1000 * 60 * 60 * 24 ) < now;
		}
		if( cache && req != null)
		{
			String since = req.getHeader("If-Modified-Since");
			if( since != null && since.endsWith("GMT"))
			{
				//304 Not Modified
				try
				{
					Date old = getLastModFormat().parse(since);
					if( !contentpage.getLastModified().after(old))
					{
						//log.info("if since"  + since);
						res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
						return true;
					}
				}
				catch( Exception ex)
				{
					log.error(since);
				}
			}
			
		}
		res.setDateHeader("Last-Modified",contentpage.getLastModified().getTime());
		
		if(  cache )
		{
			res.setDateHeader("Expires", now + (1000 * 60 * 60 * 24 )); //sec * min * hour * 48 Hours				
		}
		else
		{
			res.setDateHeader("Expires", now - (1000 * 60 * 60 * 24)); //expired 24 hours ago
		}
		return false;
	}
	public boolean canGenerate(WebPageRequest inReq)
	{
		return true;
	}
}
