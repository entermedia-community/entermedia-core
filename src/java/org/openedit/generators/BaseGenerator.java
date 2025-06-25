/*
 * Created on Jun 5, 2005
 */
package org.openedit.generators;

import java.io.EOFException;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.Generator;
import org.openedit.WebPageRequest;
import org.openedit.page.Page;
import org.openedit.users.User;
import org.openedit.util.OutputFiller;
import org.openedit.util.SimpleDateFormatPerThread;

/**
 * @author cburkey
 *
 */
public abstract class BaseGenerator implements Generator, Cloneable
{
	protected String fieldName;
	private OutputFiller fieldOutputFiller;
	
	private static Log log = LogFactory.getLog(BaseGenerator.class);

	protected OutputFiller getOutputFiller()
	{
		if ( fieldOutputFiller == null )
		{
			fieldOutputFiller = new OutputFiller();
		}
		return fieldOutputFiller;
	}

	
	public String getName()
	{
		return fieldName;
	}
	public void setName(String inName)
	{
		fieldName = inName;
	}
	//TODO: Should this check for exists? What about isBinary?
	public boolean canGenerate(WebPageRequest inReq)
	{
		return !inReq.getPage().isBinary();
	}
	public Object clone()
	{
		try
		{
			return super.clone();
		}
		catch (CloneNotSupportedException ex)
		{
			//silent, will never happen
			return null;
		}
	}
	public boolean hasGenerator(Generator inChild)
	{
		return inChild == this;
	}

//	protected void checkCors(HttpServletRequest httpReq, HttpServletResponse httpResp, User inUser)
//	{
//	      // No Origin header present means this is not a cross-domain request
//        String origin = httpReq.getHeader("Origin");
//         if (origin == null) 
//         {
// 			boolean isoptions = httpReq.getMethod().equals("OPTIONS");
//
//        	//Warning: Allows all domains
// 			if (isoptions || inUser != null	)
// 			{
// 				if( origin != null)
// 				{
// 					httpResp.setHeader("Access-Control-Allow-Origin",origin);
// 				}
// 				else
// 				{
// 					httpResp.setHeader("Access-Control-Allow-Origin","*"); //This does nothing useful
// 				}
// 			}
//        	 
//            // Return standard response if OPTIONS request w/o Origin header
//           if (isoptions)
//           {
//               httpResp.setHeader("Access-Control-Allow-Methods", VALID_METHODS);
//        	   httpResp.setHeader("Access-Control-Allow-Headers", VALID_HEADERS);
//        	   httpResp.setHeader("Access-Control-Allow-Credentials","true");
//               httpResp.setStatus(200);
//               return;
//           }
//        } else {
//            // This is a cross-domain request, add headers allowing access
//        	//if( user is logged in )
//        	//{
//            //httpResp.setHeader("Access-Control-Allow-Origin", origin);  //TODO: This is not secure at all. my JSESSION id is avilable to any site
//        	//}
//            httpResp.setHeader("Access-Control-Allow-Methods", VALID_METHODS);
//
//            String headers = httpReq.getHeader("Access-Control-Request-Headers");
//            if (headers != null)
//                httpResp.setHeader("Access-Control-Allow-Headers", headers);
//
//            // Allow caching cross-domain permission
//            httpResp.setHeader("Access-Control-Max-Age", "3600");
//        }
//	}
	
	

	protected boolean ignoreError(Throwable inWrapped)
	{
		if( inWrapped == null )
		{
			return false;
		}
		if( inWrapped instanceof EOFException )
		{
			return true;
		}
		if( inWrapped instanceof java.net.SocketTimeoutException )
		{
			return true;
		}
		if ("Closed".equals(inWrapped.getMessage()))
		{
			return true;
		}
		String message = inWrapped.toString() + inWrapped.getMessage();
		if ( message.indexOf("Broken pipe") > -1 || 
			message.indexOf("socket write error") > -1 ||
			message.indexOf("Connection reset") > -1 ) //tomcat
		{
			return true;
		}
		return ignoreError( inWrapped.getCause() );
	}
	
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
	
	protected boolean checkCache(WebPageRequest inContext, Page contentpage, HttpServletRequest req, HttpServletResponse res)
	{
		if( req != null)
		{
			String match = req.getHeader("If-None-Match");
			if (match != null)
			{
				String lasmodified = String.valueOf(contentpage.lastModified());
				if (lasmodified.equals(match))
				{
					res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					return true;
				}
			}

			String since = req.getHeader("If-Modified-Since");
			if( since != null && since.endsWith("GMT"))
			{
				//304 Not Modified
				try
				{
					Date old = getLastModFormat().parse(since);
					
					long oldtime = old.getTime() / 1000;
					long currenttime = contentpage.lastModified() / 1000;
					
					if( currenttime == oldtime)
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
	
		return false;
	}
	
	
	protected void setHeaders(HttpServletResponse res, Page contentpage)
	{
		Long lastmodified = contentpage.getLastModified().getTime();
		res.setHeader("ETag", lastmodified.toString());
		res.setHeader("Cache-Control", "max-age=0; must-revalidate");
		res.setDateHeader("Last-Modified", lastmodified);
		//long now = System.currentTimeMillis();	
		//res.setDateHeader("Expires", now + (1000 * 60 * 60 * 24 * 30 * 6 )); //sec * min * hour * 48 Hours	 six months
	}

}
