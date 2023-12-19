/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

/*--

 Copyright (C) 2001-2002 Anthony Eden.
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions, and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions, and the disclaimer that follows
    these conditions in the documentation and/or other materials
    provided with the distribution.

 3. The name "JPublish" must not be used to endorse or promote products
    derived from this software without prior written permission.  For
    written permission, please contact me@anthonyeden.com.

 4. Products derived from this software may not be called "JPublish", nor
    may "JPublish" appear in their name, without prior written permission
    from Anthony Eden (me@anthonyeden.com).

 In addition, I request (but do not require) that you include in the
 end-user documentation provided with the redistribution and/or in the
 software itself an acknowledgement equivalent to the following:
     "This product includes software developed by
      Anthony Eden (http://www.anthonyeden.com/)."

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR(S) BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.

 For more information on JPublish, please see <http://www.jpublish.org/>.

 */
package org.openedit.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.openedit.OpenEditException;


/**
 * Utility class for building URLs.
 *
 * @author Anthony Eden
 */
public class URLUtilities
{
	/** The URL path separator. */
	public static final String URL_PATH_SEPARATOR = "/";
	private static Log log = LogFactory.getLog(URLUtilities.class);

	protected static Pattern VALIDUTF8 = null;
	/**
	 * Construct a new URLUtilities class which can use the given request and response objects to
	 * build URLs.
	 */
	private HttpServletRequest fieldRequest;
	private HttpServletResponse fieldResponse;

	public URLUtilities(
		HttpServletRequest request, HttpServletResponse response)
	{
		this.fieldRequest = request;
		this.fieldResponse = response;
	}
	
	/**
	 * The only non-buggy way to get a file name is to look at the full URL then chop off the
	 * context to make it a relative URL
	 *
	 * @return /index.html
	 */
	public static String getPathWithoutContext(String inContext, String fullpath, String inDefault)
	{
	    String nameOnly = fullpath;
	    if (fullpath.startsWith(inContext)){
	        nameOnly = fullpath.substring(inContext.length());
	    }	    

		if (nameOnly.equals("/") || (nameOnly.length() == 0))
		{
			nameOnly += inDefault;
		}
		else if (nameOnly.indexOf('.') == -1)
		{
			if ( !nameOnly.endsWith("/"))
			{			
				nameOnly += '/';
			}
			nameOnly += inDefault;
		}

		return nameOnly;
	}

	/**
	 * This includes the webapp name but not the page name
	 *
	 * @return http://www.acme.com/webapp/
	 */
	public String buildBasePath(String path)
	{
		if( fieldRequest == null)
		{
			return null;
		}
		StringBuffer ctx = fieldRequest.getRequestURL();
		String servername = ctx.substring(0, ctx.indexOf("/", 7)); //just the server name

		if (path.lastIndexOf('/') > -1)
		{
			path = path.substring(0, path.lastIndexOf('/'));

			return servername + path + "/";
		}
		else
		{
			return servername + "/";
		}
	}
	
	/**
	 * This is the server name only 
	 *
	 * returns http://www.acme.com/
	 */
	public String buildRoot()
	{
		if( fieldRequest == null)
		{
			return null;
		}

		StringBuffer ctx = fieldRequest.getRequestURL();
		String servername = ctx.substring(0, ctx.indexOf("/", 8));

		return servername + "/";
	}
	/**
	 * This is the server name  and webapp  
	 *
	 * returns http://www.acme.com/webapp
	 */
	public String buildAppRoot()
	{
	
		String server = buildRoot();
		if(!server.endsWith("/")){
			server = server + "/";
		}
		String app = relativeHomePrefix();
		if(app.startsWith("/")){
			app = app.substring(1);
		}
		return server+ app;
		
	}

	/**
	 * Build an HTTPS (Secure Socket Layer) method relative to the application context  using the
	 * given path.
	 *
	 */
	public String buildSecure(String path)
	{
		return buildSecure(path, 0);
	}

	/**
	 * Build an HTTPS (Secure Socket Layer) method relative to the application context  using the
	 * given path.  This version of the <code>buildSecure</code> method  allows you to specify the
	 * port number.  A port number of 0 will cause the port  argument to be ignored.
	 *
	 * @param path The path
	 * @param port The port
	 *
	 * @return DOCME
	 */
	public String buildSecure(String path, int port)
	{
		return build(path, "https", port);
	}

	/**
	 * Build an HTTP URL relative to the application context using the given path.
	 *
	 * @param path The path
	 *
	 * @return DOCME
	 */
	public String buildStandard(String path)
	{
		return buildStandard(path, 0);
	}

	/**
	 * Build an HTTP URL relative to the application context using the given path.   This version
	 * of the <code>buildStandard</code> method allows you to specify  the port number.  A port
	 * number of 0 will cause the port argument to be ignored.
	 *
	 * @param path The path
	 * @param port The port
	 *
	 * @return DOCME
	 */
	public String buildStandard(String path, int port)
	{
		return build(path, "http", port);
	}

	public static String escapeUtf8(String inCode)
	{
		if( VALIDUTF8 == null )
		{
			VALIDUTF8 = Pattern.compile("[^\\u0009\\u000a\\u000d\\u0020-\\uD7FF\\uE000-\\uFFFD]", Pattern.MULTILINE);
		}
		String clean = VALIDUTF8.matcher(inCode).replaceAll("");
		return clean;
		//s/.*?((?:[\t\n\r\x20-\x7E])+|(?:\xD0[\x90-\xBF])+|(?:\xD1[\x80-\x8F])+|(?:\xC3[\x80-\xBF])+|).*?/$1/sg;
	}
	
	/**
	 * Percent-encode the given String.  This method delegates to the URLEncoder.encode() method.
	 *
	 * @param s The String to encode
	 * @deprecated use urlEscape
	 * @return The encoded String
	 *
	 * @see java.net.URLEncoder
	 */
	public static String encode(String s)
	{
		if (s == null)
		{
			return null;
		}

		String encoded = URLEncoder.encode(s);
		encoded = encoded.replace("%2F", "/");
		encoded = encoded.replace(" ", "%20");
		return encoded;
	}
	private static String encodeParamVal(String value) {
	    try
		{
			return URLEncoder.encode(value, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new OpenEditException(e);
		}
	}
	public static String urlEscape(String rawurl)
	{
//		gen-delims  = ":"  "/"  "?"  "#"  "["  "]" "@"
//	
//			     sub-delims  = "!" / "$" / "&" / "'" / "(" / ")"
//			                 / "*" / "+" / "," / ";" / "="
		if(rawurl  == null) {
			return null;
		}
		
//		URI uri = URI.create(rawurl);
//		String returned = uri.toExternalForm();
//		return returned;
		String host = null;
			String path = null;
			String query = null;
			if( rawurl.startsWith("/") )
			{
				path = rawurl;
			}
			else
			{
				int slash = rawurl.indexOf("/",8);
				if( slash > -1)
				{
					host = rawurl.substring(0,slash);
					path = rawurl.substring(slash);
				}
				else
				{
					path = rawurl;
				}
			}
			int quest = path.indexOf("?");
			if( quest > -1)
			{
				query = path.substring(quest + 1);
				path = path.substring(0,quest);
			}
						
			path = fixPath(path);
			StringBuffer finalurl = new StringBuffer();
			if( host != null )
			{
				finalurl.append( host);
			}
			finalurl.append(path);
			if( query != null)
			{
				String[] params = query.split("&");
				StringBuffer out = new StringBuffer();
				for (int i = 0; i < params.length; i++)
				{
					if( i > 0)
					{
						out.append("&");
					}
					String valuepair =params[i]; 
					int cutoff = valuepair.indexOf("=");
					if( cutoff > -1)
					{
						String key = valuepair.substring(0,cutoff);
						out.append(key);
						out.append("=");
						if( cutoff >= 1)
						{
							String value = valuepair.substring(cutoff + 1);
							out.append(encodeParamVal(value));
						}
					}
				}
				finalurl.append("?" + out.toString());
			}
			return finalurl.toString();
	 }
	
	public static String fixPath(String inPath)
	{
		// path = UriUtils.encodePath(path, "UTF-8");
		
		//Ian says we need spaces in here
		final String PATHVALUES = "';:?#[]@+ \"\\^{|}<>~`%"; //:?@[] \"%-.<>\\^_`{|}~";

		
//		byte[] encoded = inPath.getBytes("UTF-8");
//		Integer.toHexString(encoded);
//		
		//Escaper 
		//String result = UrlEscapers.urlPathSegmentEscaper().escape(inPath);
	    StringBuilder result = new StringBuilder(inPath.length() +1);
	   
	    for(int i=0; i<inPath.length();++i) 
	    {
			char c = inPath.charAt(i);
			
	    	if(i < inPath.length()-1 && Character.isSurrogatePair(c, inPath.charAt(i+1))) 
	    	{
				// if so, the codepoint must be stored on a 32bit int as char is only 16bit
				int codePoint = inPath.codePointAt(i);
				// show the code point and the char
				//System.out.println(String.format("%6d:%s", codePoint, new String(new int[]{codePoint}, 0, 1)));
				byte[] allbytes = new String(new int[]{codePoint}, 0, 1).getBytes(StandardCharsets.UTF_8);
				Formatter formatter = new Formatter();
				for (byte b : allbytes) 
				{
	                formatter.format("%%%02X", b);
	            }
				result.append(formatter.toString());
				++i;
			}
	    	else if (PATHVALUES.indexOf(c) != -1 ) 
	        { 
	            result.append("%" + Integer.toHexString(c).toUpperCase());
	        }
	    	else if ( c > 128)
	    	{
				byte[] allbytes = new String(new int[]{c}, 0, 1).getBytes(StandardCharsets.UTF_8);
				Formatter formatter = new Formatter();
				for (byte b : allbytes) 
				{
	                formatter.format("%%%02X", b);
	            }
				result.append(formatter.toString());
	    	}
	    	else if ( c < 32)
	        {
	    		//ASCII 0 to 31 ()
	    		result.append('%');
	    		result.append(toHex(c / 16));
	    		result.append(toHex(c % 16));
	    	}
	    	else {
	    		result.append(c);
	    	}
	    }
		return result.toString();
	}
	private static char toHex(int ch)
	{
	    return (char)(ch < 10 ? '0' + ch : 'A' + ch - 10);
	}
   public static String utf8encode(int codepoint) 
   {
	   	String inChar = new String(new int[]{codepoint}, 0, 1);
        byte[] bytes = inChar.getBytes(StandardCharsets.UTF_8);
        
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%%02X", b);
        }
        String encodedHex = formatter.toString().toUpperCase();
        return encodedHex;
        
    }
	public static String decode(String s)
	{
		if (s == null)
		{
			return null;
		}

		return URLDecoder.decode(s);
	}
	public static String decodePath(String s)
	{
		if (s == null)
		{
			return null;
		}

		String decoded = URLDecoder.decode(s);
		return decoded;
				
	}


	/**
	 * Build an HTTP URL relative to the application context using the given path. This is a path
	 * such as /path/myfile.html but is encoded
	 * If you want to unencoded path use $content.path or getOriginalPath()
	 *
	 * @return /webapp/path/myfile.html
	 */
	public String getOriginalUrl()
	{
		if( fieldRequest == null)
		{
			return null;
		}

		String path = fieldRequest.getRequestURI();
		String home = relativeHomePrefix();
		path = path.substring(home.length());
		return encode( path );
	}
	/**
	 * This is the path that the browser is on.
	 * /sub/index page.html
	 * @return
	 */
	public String getOriginalPath()
	{
		if( fieldRequest == null)
		{
			return null;
		}

		String requestedPath = getRequest().getRequestURI();
		try
		{
			requestedPath = URLDecoder.decode(requestedPath, "UTF-8");
			//getRequest().getQueryString()
		}
		catch (UnsupportedEncodingException ex)
		{
			log.error( ex );
		}
	
		String contextPath = getRequest().getContextPath();
	    if ( requestedPath.startsWith( contextPath ) )
	    {
	        requestedPath = requestedPath.substring(contextPath.length());
	    }
	    return requestedPath;
	}


	/**
	 * Build an HTTP URL relative to the application context using the given path. This is a path
	 * such as http://servername/webapp/path/myfile.html
	 *
	 * @return /webapp/path/myfile.html
	 */
	public String requestPath()
	{
		if( fieldRequest == null)
		{
			return null;
		}

		StringBuffer ctx = fieldRequest.getRequestURL();
		String requestPath = ctx.substring(ctx.indexOf("/", 7)); //just the path

		return requestPath;
	}
	/**
	 * Is the full path with arguments included
	 * /webappname/sub/index.html?test=1234
	 */
	
	public String requestPathWithArguments()
	{
		if( fieldRequest == null)
		{
			return null;
		}

		String path = fieldRequest.getRequestURI();
		if ( fieldRequest.getQueryString() != null && fieldRequest.getQueryString().length() > 0)
		{
			path = path + "?" + fieldRequest.getQueryString();
		}
		return path;
	}
	/**
	 * Is the full path with arguments included
	 * /sub/index.html?test=1234
	 */
	
	public String requestPathWithArgumentsNoContext()
	{
		if( fieldRequest == null)
		{
			return null;
		}

		String path = fieldRequest.getRequestURI();
		if ( fieldRequest.getQueryString() != null && fieldRequest.getQueryString().length() > 0)
		{
			path = path + "?" + fieldRequest.getQueryString();
		}
		String home = relativeHomePrefix();
		path = path.substring(home.length());
		return path;
	}
	/**
	 * Report the site name, e.g. http://www.openeditpro.com
	 * 
	 * @return The site's root URL
	 */
	public String siteRoot()
	{
		if( fieldRequest == null)
		{
			return null;
		}

		StringBuffer ctx = fieldRequest.getRequestURL();
		String siteRoot = ctx.substring( 0, ctx.indexOf("/", 8) ); //8 comes from https://
		return siteRoot;
	}
	public static String xmlEscapeWithWrap(String inStr)
	{
		return xmlEscapeWithWrap(inStr, 100);
	}
	public static String xmlEscapeWithWrap(String inStr, int inWrap)
	{
			if( inStr == null)
			{
				return null;
			}
			String inCode = xmlEscape(inStr);
			int LINE_LENGTH = inWrap;
			StringBuffer sb = new StringBuffer();
			int linecount = 0;
			boolean nextSpace = false;
			
			for ( int n = 0; n < inCode.length(); n++ )
			{
				char c = inCode.charAt( n );
				linecount++;
				if ( linecount > LINE_LENGTH)
				{
					nextSpace = true;
				}
				
				switch ( c )
				{
					case '\n':
					{
						sb.append( "\n" );
						linecount = 0;
						nextSpace = false;
						break;
					}
					case '\r':
					{
						break;
					}
				default:
					if(  nextSpace && c == ' ' )	
					{
						sb.append( "\n" );
						nextSpace = false;
						linecount = 0;
					}
					sb.append( c );
				}
			}
			return sb.toString();
	}
	/**
	 * A simple hack to escape XML, stolen from Jakarta commons XmlUtils
	 *
	 * @param inStr
	 *
	 * @return String
	 */
	public static String xmlEscape(String inStr)
	{
		if ( inStr == null )
		{
			return null;
		}
		//TODO: Deal with [ ] and 
		/*
		 *  gen-delims  = ":" / "/" / "?" / "#" / "[" / "]" / "@"

      sub-delims  = "!" / "$" / "&" / "'" / "(" / ")"
                  / "*" / "+" / "," / ";" / "="
		 */
		StringBuffer output = new StringBuffer(inStr.length() + 50);
		for (int i = 0; i < inStr.length(); i++)
		{
			char c = inStr.charAt(i);
			switch (c)
			{
			case '&':
				//can you just blindly replace any & since it might be part of &apos;?
				//IE seems to espace the & for some reason inStr = inStr.replaceAll("'", "&apos;");		
				//inStr = inStr.replaceAll("&", "&amp;");
				output.append("&amp;");
				break;
			case '<':
				output.append("&lt;");
				break;
			case '>':
				output.append("&gt;");
				break;
			case '\"':
				output.append("&quot;");
				break;
			case '\'':
				output.append("&apos;");
				break;
			default:
				output.append(c);
				break;
			}
		}
		return output.toString();
	}

	public static String textEscape(String inStr)
	{
		if ( inStr == null )
		{
			return null;
		}
		StringBuffer output = new StringBuffer(inStr.length() + 50);
		for (int i = 0; i < inStr.length(); i++)
		{
			char c = inStr.charAt(i);
			switch (c)
			{
			case '\n':
				//can you just blindly replace any & since it might be part of &apos;?
				//IE seems to espace the & for some reason inStr = inStr.replaceAll("'", "&apos;");		
				//inStr = inStr.replaceAll("&", "&amp;");
				output.append(" ");
				break;
			case '\r':
				output.append(" ");
				break;
			case '\'':
				break;
			case '\"':
				output.append("&quot;");
				break;
			default:
				output.append(c);
				break;
			}
		}
		return output.toString();
	}

	public static String xmlUnescape(String inStr)
	{
		if ( inStr == null )
		{
			return null;
		}
		//can you just blindly replace any & since it might be part of &apos;?
		inStr = inStr.replaceAll("&amp;","&");

		inStr = inStr.replaceAll("&lt;", "<");
		inStr = inStr.replaceAll("&gt;", ">");
		inStr = inStr.replaceAll("&quot;", "\"");

		//IE seems to espace the & for some reason inStr = inStr.replaceAll("'", "&apos;");		
		return inStr;
	}

	/**
	 * If I am located in /webapp/demo/test.html my prefix would be /demo/ to get back to the base
	 * /webapp level
	 *
	 * The rule is you can tack on $home  + "/somepage.html" without getting //somepage.html
	 *
	 *
	 * @return Object
	 */
	public String relativeHomePrefix()
	{
		if( fieldRequest == null)
		{
			return null;
		}

		String rootdir = fieldRequest.getContextPath();

		if ((rootdir != null) && (rootdir.length() > 0))
		{
			if ( rootdir.endsWith("/"))
			{
				rootdir = rootdir.substring(0,rootdir.length() - 1);
			}
			return rootdir;
		}
		else
		{
			return "";
		}

	}

	/**
	 * Build a URL using the given path, protocol and port.  The path will be relative to the
	 * current context.
	 *
	 * @param path The path
	 * @param protocol (i.e. http or https)
	 * @param port The port (0 to ignore the port argument)
	 *
	 * @return The URL as a String
	 */
	protected String build(String path, String protocol, int port)
	{
		if( fieldRequest == null)
		{
			return null;
		}

		String serverName = fieldRequest.getServerName();
		String contextPath = fieldRequest.getContextPath();

		//log.debug("Server name: " + serverName);
		//log.debug("Context path: " + contextPath);

		if (!contextPath.endsWith(URL_PATH_SEPARATOR))
		{
			contextPath = contextPath + URL_PATH_SEPARATOR;
		}

		if (path.startsWith(URL_PATH_SEPARATOR))
		{
			path = path.substring(1);
		}

		String requestPath = contextPath + path;
		//log.debug("Request path: " + requestPath);

		StringBuffer buffer = new StringBuffer();
		buffer.append(protocol);
		buffer.append("://");
		buffer.append(serverName);

		int realPort = fieldRequest.getServerPort();

		if (port > 0)
		{
			realPort = port;
		}

		if (
			(realPort > 0) &&
				!((protocol.equals("http") && (realPort == 80)) ||
				(protocol.equals("https") && (realPort == 443))))
		{
			buffer.append(":");
			buffer.append(realPort);
		}

		if (!requestPath.startsWith(URL_PATH_SEPARATOR))
		{
			buffer.append(URL_PATH_SEPARATOR);
		}

		buffer.append(requestPath);

		//log.debug("URL: '" + buffer + "'");

		return buffer.toString();
	}
	public PathUtilities getPathUtilities()
	{
		return new PathUtilities();
	}

	public HttpServletResponse getResponse()
	{
		return fieldResponse;
	}

	public void setResponse(HttpServletResponse inResponse)
	{
		fieldResponse = inResponse;
	}

	public HttpServletRequest getRequest()
	{
		return fieldRequest;
	}

	public void setRequest(HttpServletRequest inRequest)
	{
		fieldRequest = inRequest;
	}
	
	public static HttpClientBuilder createTrustingHttpClient() {

		try
		{
			  HttpClientBuilder builder = HttpClientBuilder.create();
		        SSLContext sc = SSLContext.getInstance("SSL");
		        sc.init(null, getTrustingManager(), new java.security.SecureRandom());
			    SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sc, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			    builder.setSSLSocketFactory(sslConnectionFactory);
	
			    Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
			            .register("https", sslConnectionFactory)
			            .build();
	
			    HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
	
			    builder.setConnectionManager(ccm);
	
			    return builder;
		}
		catch( Exception ex)
		{
			throw new OpenEditException(ex);
		}

	        
//	        ClientConnectionManager manager = httpClient.getConnectionManager();
//	        manager.getSchemeRegistry().register(new Scheme("https", 443, factory));
//	        httpclient.getConnectionManager().getSchemeRegistry().register(sch);
//	        return httpclient;

	}
	 private static TrustManager[] getTrustingManager() {
	        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
	            @Override
	            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }

	            @Override
	            public void checkClientTrusted(X509Certificate[] certs, String authType) {
	                // Do nothing
	            }

	            @Override
	            public void checkServerTrusted(X509Certificate[] certs, String authType) {
	                // Do nothing
	            }

	        } };
	        return trustAllCerts;
	    }
	 
	 private static final Pattern urlPattern = Pattern.compile(
		        "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
		                + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
		                + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
		        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
	 
	public static String escapeMessage(String inMessage) {
		return escapeMessage(inMessage,-1);
	}
	 
	public static String escapeMessage(String inMessage, int maxchars) {
		if (inMessage == null) {
			return null;
		}
		String escaped = URLUtilities.xmlEscape(inMessage);
		escaped = escaped.replaceAll("&lt;br&gt;", "<br>");
		escaped = escaped.replaceAll("&lt;i&gt;", "<i>");
		escaped = escaped.replaceAll("&lt;/ir&gt;", "</i>");
		escaped = escaped.replaceAll("&lt;b&gt;", "<b>");
		escaped = escaped.replaceAll("&lt;/b&gt;", "</b>");
		escaped = escaped.replaceAll("&lt;p&gt;", "<p>");
		escaped = escaped.replaceAll("&lt;/p&gt;", "</p>");

		  Matcher m = urlPattern.matcher(escaped);
		  StringBuffer sb = new StringBuffer(escaped.length());
		  while (m.find()) {
		   // String text = m.group(1);
		    String text = m.group(0).trim();
		    if( maxchars > -1 && maxchars < escaped.length() )  //Not gonna fit
		    {
		    	m.appendReplacement(sb,text);
		    	continue;
		    }
		    // ... possibly process 'text' ...
		    StringBuffer link = new StringBuffer();
		    
		    link.append(" <a href=\"");
		    link.append(text);
		    link.append("\">");
		    link.append(text);
		    link.append("</a>");
		    m.appendReplacement(sb, Matcher.quoteReplacement(link.toString()));
		  }
		  m.appendTail(sb);
		  
		  if( maxchars > -1 && maxchars < sb.length())
		  {
			  String cutoff = sb.toString().substring(0,maxchars);
			  String[] types = new String[]{"div","b","i","span","a"};
			  for (int i = 0; i < types.length; i++)
			  {
				  cutoff = stripTags(cutoff, types[i]);
			  }
			  if( cutoff.contains("<"))
			  {
				for (int i = cutoff.length()-1; i >= 0; i--)
				{
					//Loop from the end make sure we have > before any <
					if( cutoff.charAt(i)  == '>')
					{
						break;
					}
					if( cutoff.charAt(i) == '<')
					{
						cutoff = cutoff + "/>";
						break;
					}
				}
			  }
//			  if( cutoff.contains("<a ") && !cutoff.contains("</a>"))
//			  {
//				  cutoff = cutoff.replace("<a ", "") + " \"</span>";
//			  }
			  
			  
			  return cutoff + "...";
		  }
		  
		  //Suppoort new lines and tabs and spaces?
//		  String replaced = sb.toString().replace("\\n", "<br>");
//		  //replaced = replaced.replace("/n", "<br>");
//		  replaced = replaced.replace("	", "&nbsp;&nbsp;&nbsp;&nbsp;");
//		  return replaced;
		  return sb.toString();
		
	}

	public static String stripTags(String inHtml, String tag) 
	{
		String[] opentag = inHtml.split("<" + tag);
		String[] closetag = inHtml.split("</" + tag);
		
		for (int i = 0; i < closetag.length - closetag.length; i++)
		{
			inHtml = inHtml + "</" + tag;
		}
		return inHtml;
	}
	
	public String getDomain()
	{
		return parseDomain(buildRoot());
	}

	public String getSubDomain()
	{
		return parseSubDomain(buildRoot());
	}
	public static String parseSubDomain(String base)
	{
			// string off start
			String basestring = parseDomain(base);
			//total Domain
			String[] parts  = basestring.split("\\.");
			if( parts.length < 3)
			{
				return null;
			}
			return parts[0];

	}

	protected static String parseDomain(String base)
	{
		String basestring = base.substring(base.lastIndexOf("//") + 2,
				base.length());
		int port = basestring.indexOf(":");
		if( port > -1)
		{
			basestring = basestring.substring(0,port);
		}
		
		int nextslash = basestring.indexOf("/");
		if( nextslash > -1)
		{
			basestring = basestring.substring(0,nextslash);
		}
		basestring = basestring.toLowerCase();
		return basestring;
	}

	public static String dash(String inName)
	{
		String text = xmlEscape(inName);
		text = text.replaceAll(" ","-").replaceAll("&amp;","-");
		return text;
	}
	
}
