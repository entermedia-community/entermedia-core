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
package org.openedit.generators;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.Generator;
import org.openedit.OpenEditException;
import org.openedit.WebPageRequest;
import org.openedit.error.ContentNotAvailableException;
import org.openedit.page.Page;
import org.openedit.page.manage.PageManager;
import org.openedit.util.FileUtils;
import org.openedit.util.SimpleDateFormatPerThread;



/**
 * This generator uses file steam to process a request for a page
 *
 * @author Chris Burkey
 */
public class FileGenerator extends BaseGenerator implements Generator
{
	private static Log log = LogFactory.getLog(FileGenerator.class);
	protected PageManager fieldPageManager;

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
				setHeaders(res, contentpage);
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
			if( res != null)
			{
				String forcedownload = inContext.getRequestParameter("forcedownload");
			
				if( Boolean.parseBoolean(forcedownload))
				{
					//filename = URLEncoder.encode(filename,content.getCharacterEncoding());

					String downloadname = inContext.getRequestParameter("downloadname");
					if (downloadname == null) {
						downloadname = contentpage.getName();
					}

					String fileName = URLEncoder.encode(downloadname, "UTF-8");
					//fileName = URLDecoder.decode(fileName, "ISO8859_1");
					//inReq.getResponse().setContentType("application/x-msdownload");
				    
					//fileName=fileName.replaceAll(";", "/;");
					
					//inReq.getResponse().setHeader("Content-Disposition: attachment; filename*=us-ascii'en-us'"+ fileName);
					fileName.replace("\"", "/\"");
					//res.setHeader("Content-disposition", "attachment; filename*=utf-8''\""+ fileName +"\"");
					
					res.setHeader("Content-Type", "application/octet-stream; charset=utf-8");
					res.setHeader("Content-disposition", "attachment; filename*=utf-8''\""+ fileName +"\"");
					//res.setHeader("Content-disposition", "attachment; filename*=utf-8''\""+ fileName +"\"");
					
					//Content-Type: 'application/octet-stream; charset=utf-8'
					//Content-Disposition: attachment; filename="filename.jpg"; filename*="filename.jpg"
					//		Content-Length: <size in bytes>
					
				}
			}	
			
			long length = -1;
			if ( res != null && !contentpage.isHtml() && !contentpage.isJson() && inContext.getContentPage() == contentpage  )
			{	//we can set the length unless there is a decorator on it somehow
				length = (long)contentpage.getContentItem().getLength();
				if ( length != -1)
				{
					res.setHeader("Accept-Ranges", "bytes");
					if( start > -1)
					{	
						if( end == -1)
						{
							end = length - 1;  //1024-1 1023
						}
						res.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + length); //len is total
						long sent = end + 1 - start; // 0 1024 - 1024 length

						length = sent;
					}
					if(length > 0 && length < Integer.MAX_VALUE)
					{

						//res.setContentLength((int)length);
					}	
					else
					{
						log.info("Zero length file " + contentpage.getPath());
					}
					//res.removeHeader("Content-Length");
					//res.setHeader("Content-Length", String.valueOf(length));
				}
			}

			if ( contentpage.isBinary() )
			{
				in = streamBinary(inContext, contentpage, in, start, end, length, inOut);
			}
			else
			{
				if( length > -1)
				{
					if (length <= Integer.MAX_VALUE)
					{
						res.setContentLength((int)length);
					}
					else
					{
						//res.removeHeader("Content-Length");
					    res.addHeader("Content-Length", Long.toString(length));
					}
				}
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
			if( eof instanceof OpenEditException)
			{
				throw (OpenEditException)eof;
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
		if( length > -1 && inReq.getResponse() != null)
		{
			
			if (length <= Integer.MAX_VALUE)
			{
				inReq.getResponse().setContentLength((int)length);
			}
			else
			{
				//res.removeHeader("Content-Length");
				inReq.getResponse().addHeader("Content-Length", Long.toString(length));
			}
		}
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
	
	
	
	
	public boolean canGenerate(WebPageRequest inReq)
	{
		return true;
	}
	protected void forceDownload(WebPageRequest inReq, String inFilename)
	{
		String filename = inFilename.replace("\"", "/\"");

		if(inReq.getResponse() != null)
		{
			inReq.getResponse().setHeader("Content-disposition", "attachment; filename=\""+ filename +"\"");  //This seems to work on firefox
		}
	}


}
