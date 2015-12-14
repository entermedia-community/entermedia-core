/*
 * Created on Jun 5, 2005
 */
package org.openedit.generators;

import java.io.EOFException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openedit.Generator;
import org.openedit.WebPageRequest;
import org.openedit.util.OutputFiller;

/**
 * @author cburkey
 *
 */
public abstract class BaseGenerator implements Generator, Cloneable
{
	protected String fieldName;
	private OutputFiller fieldOutputFiller;
	public static String VALID_METHODS = "DELETE, HEAD, GET, OPTIONS, POST, PUT";

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

	protected void checkCors(HttpServletRequest httpReq, HttpServletResponse httpResp)
	{
	      // No Origin header present means this is not a cross-domain request
        String origin = httpReq.getHeader("Origin");
         if (origin == null) 
         {
        	//Warning: Allows all domains
        	 
            // Return standard response if OPTIONS request w/o Origin header
           if ("OPTIONS".equalsIgnoreCase(httpReq.getMethod())) {
                httpResp.setHeader("Allow", VALID_METHODS);
                httpResp.setStatus(200);
                return;
            }
        } else {
            // This is a cross-domain request, add headers allowing access
            httpResp.setHeader("Access-Control-Allow-Origin", origin);
            httpResp.setHeader("Access-Control-Allow-Methods", VALID_METHODS);

            String headers = httpReq.getHeader("Access-Control-Request-Headers");
            if (headers != null)
                httpResp.setHeader("Access-Control-Allow-Headers", headers);

            // Allow caching cross-domain permission
            httpResp.setHeader("Access-Control-Max-Age", "3600");
        }
	}
	
	

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

}
