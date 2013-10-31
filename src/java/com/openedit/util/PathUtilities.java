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
package com.openedit.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Utility class for working with request paths.
 *
 * @author Anthony Eden
 */
public final class PathUtilities
{
	private static final Log log = LogFactory.getLog(PathUtilities.class);
	private static final String WILDCARD = "*";

	/**
	 * Internal constructor.
	 */
	public PathUtilities()
	{
		// no op
	}

	/**
	 * Get the parent of the given path.
	 *
	 * @param path The path for which to retrieve the parent
	 *
	 * @return The parent path. /sub/sub2/index.html -> /sub/sub2 If the given path is the root path ("/" or ""), return a blank string.
	 */
	public static String extractDirectoryPath(String path)
	{
		if ((path == null) || path.equals("") || path.equals("/"))
		{
			return "";
		}

		int lastSlashPos = path.lastIndexOf('/');

		if (lastSlashPos >= 0)
		{
			return path.substring(0, lastSlashPos); //strip off the slash
		}
		else
		{
			return ""; //we expect people to add  + "/somedir on their own
		}
	}
	
	public static String extractOSDirectoryPath(String path)
	{
		String sep = File.separator;
		if ((path == null) || path.equals("") || path.equals(sep))
		{
			return "";
		}

		int lastSlashPos = path.lastIndexOf(sep);

		if (lastSlashPos >= 0)
		{
			return path.substring(0, lastSlashPos); //strip off the slash
		}
		else
		{
			return ""; //we expect people to add  + "/somedir on their own
		}
	}
	
	/**
	 * Get the name of the parent of the given path.
	 *
	 * @param path The path for which to retrieve the parent
	 *
	 * @return The parent path's name. /sub/sub2/index.html -> sub2 If the given path is the root path ("/" or ""), return a blank string.
	 */
	public static String extractDirectoryName(String path)
	{
		if ((path == null) || path.equals("") || path.equals("/"))
		{
			return "";
		}
		
		String dirpath = extractDirectoryPath(path);
		String name = extractFileName(dirpath);
		return name;
	}

	/**
	 * Builds a path that might be within or one above a parent path String ret =
	 * buildRelative("../junk.txt","/a/b/c/something.html") Will return /a/b/junk.txt This code
	 * only returns a changed endPart if it starts with a dot .
	 *
	 * @param endPart
	 * @param fullParentPath
	 *
	 * @return
	 */
	public static String buildRelative(String endPart, String fullParentPath)
	{
		String basepath = null;
		if ( fullParentPath != null)
		{
			fullParentPath = fullParentPath.replace('\\','/');
		}
		//TODO: Make this work with random number of dots
		
		//if we are dealing with relative path then we need to make sure the basepath ends with a /
		if (endPart.startsWith(".") && (fullParentPath != null) && !fullParentPath.endsWith("/"))
		{
			//so with this function /examples/benchmark -> /examples/benchmark/
			//is this parent a directory or a file, lets check for a .
			int lastslash = fullParentPath.lastIndexOf("/");
			int lastperiod = fullParentPath.lastIndexOf(".");

			if (lastslash > lastperiod)
			{
				//must be a directory				
				basepath = fullParentPath;
			}
		}

		if (basepath == null)
		{
			basepath = PathUtilities.extractDirectoryPath(fullParentPath);
		}
		if ( basepath == null )
		{
			basepath = "";
		}

		String relative = endPart;

		if (endPart.startsWith(".."))
		{
			//strip the filename
			if (basepath.endsWith("/"))
			{
				basepath = basepath.substring(0, basepath.length() - 1);
			}

			basepath = basepath.substring(0, basepath.lastIndexOf("/"));
			relative = basepath + endPart.substring(2);
		}
		else if (endPart.startsWith("."))
		{
			relative = basepath + endPart.substring(1);
		}
		if ( !relative.startsWith("/"))
		{
			relative = "/" + relative;
		}
		return relative;
	}

	/**
	 * Extract the page name from the given path.  The page name is the  name of the file in the
	 * path without its suffix.
	 * i.e. /subpath/index.html -> index
	 *
	 * @param path The request path
	 *
	 * @return The page name
	 */
	public static String extractPageName(String path)
	{
		if ( path == null)
		{
			return null;
		}
		String newpath = path.replace('\\','/');
		int start = newpath.lastIndexOf("/");
		if ( start == -1)
		{
			start = 0;
		}
		else
		{
			start++; //to remove slash
		}
		int dotIndex = newpath.lastIndexOf(".");
		if(dotIndex > 0 && start > dotIndex)
		{
			return newpath;
		}
		if (dotIndex == -1 )
		{
			return newpath.substring(start);
		}
		else
		{
			return newpath.substring(start, dotIndex);
		}
	}

	/**
	 * Extract the page path from the given request path.  This method will return the path from
	 * the page root to the page descriptor file.
	 *
	 * /some/sub/dir/test.html -> /some/sub/dir/test
	 *
	 * @param path The request path
	 * @see extractDirectory
	 * @return The page path
	 */
	public static String extractPagePath(String path)
	{
		if ( path != null && path.length() > 0)
		{
			int lastDot = path.lastIndexOf(".");
			if( lastDot >= 1)
			{
				String pagePath = path.substring(0,lastDot);		
				return pagePath;
			}
		}
		return path;
	}

	/**
	 * Return the page type extracting it from the path.  For example: index.html would return
	 * "html" as the page type.  If the type cannot be determined then this method returns null.
	 *
	 * @param path The path
	 *
	 * @return The page type
	 */
	public static String extractPageType(String path)
	{
		return extractPageType(path,false);
	}
	public static String extractPageType(String path, boolean toLower)
	{
		if (path == null)
		{
			return null;
		}
		
		int dotIndex = path.lastIndexOf(".");

		if (dotIndex == -1)
		{
			return null;
		}

		String pageType = path.substring(dotIndex + 1);

		if( toLower )
		{
			return pageType.toLowerCase();
		}
		return pageType;
	}

	/**
	 * Match a path which may contain a wildcard.
	 *
	 * @param requestPath The request path submitted by the client
	 * @param exPath The match path with * wildcard
	 *
	 * @return DOCME
	 */
	public static boolean match(String requestPath, String wildcardPath)
	{
		//  *somestuffhereverylong*  != stuff
		if( wildcardPath.length() - 2 > requestPath.length())
		{
			return false;
		}
		
		//log.debug("match(" + requestPath + "," + exPath + ")");
		int wildcardIndex = wildcardPath.indexOf(WILDCARD);

		if (wildcardIndex == -1)
		{
			return requestPath.equalsIgnoreCase(wildcardPath);
		}
		else if( wildcardPath.charAt(0) == '*' && wildcardPath.charAt(wildcardPath.length()-1) == '*' )
		{
			String path = wildcardPath.substring(1,wildcardPath.length()-1);
			return requestPath.indexOf(path) > -1;
		}
		else if (wildcardIndex == (wildcardPath.length() - 1)) //ends with *
		{
				//log.debug("Wildcard appears at end of match path.");
				String checkString = wildcardPath.substring(0, wildcardPath.length() - 1);

				//  /stuff/* -> /stuff     /stuff/abc* != /stuff/ab
				
				if( checkString.charAt(checkString.length()-1) == '/')
				{
					checkString = checkString.substring(0,checkString.length() - 1);
				}
				//log.debug("String after wildcard removed: " + checkString);
				boolean answer = requestPath.startsWith(checkString);

				//log.debug("Does " + requestPath + " start with " + checkString + "? " + answer);
				return answer;
		}
		else if( wildcardPath.charAt(0) == '*')
		{
			String checkString = wildcardPath.substring(1);

			//log.debug("String after wildcard removed: " + checkString);
			boolean answer = requestPath.endsWith(checkString);
			return answer;
		}
		else
		{
			//log.debug("Wildcard appears in the middle of the string");
			String preMatch = wildcardPath.substring(0, wildcardIndex);
			String postMatch = wildcardPath.substring(wildcardIndex + 1);

			return requestPath.startsWith(preMatch) && requestPath.endsWith(postMatch);
		}
	}

	/**
	 * resolve a relative URL string against an absolute URL string.
	 *
	 * This method was adapted from the CalCom library at http://www.calcom.de
	 *
	 * <p>the absolute URL string is the start point for the
	 * relative path.</p>
	 *
	 * <p><b>Example:</b></p>
	 * <pre>
	 *   relative path:  ../images/test.jpg
	 *   absolute path:  /eigene dateien/eigene bilder/
	 *   result:         /eigene dateien/images/test.jpg
	 * </pre>
	 *
	 * @param relPath  The relative URL string to resolve.  Unlike the Calcom version, this may be
	 *				   an absolute path, if it starts with "/".
	 * @param absPath  The absolute URL string to start at.  Unlike the CalCom version, this may be a filename
	 *				   rather than just a path.
	 *
	 * @return the absolute URL string resulting from resolving relPath against absPath
	 * 
	 * @author Ulrich Hilger
	 * @author CalCom
	 * @author <a href="http://www.calcom.de">http://www.calcom.de</a>
	 * @author <a href="mailto:info@calcom.de">info@calcom.de</a>
	 * @author Dennis Brown (eInnovation)
	 */
	public static String resolveRelativePath(String relPath, String absPath)
	{
		//This might be a path with no / in it such as files.html
		
//		if( !relPath.startsWith("."))
//		{
//			return relPath;
//		}
		//	if relative path is really absolute, then ignore absPath (eInnovation change)
		if ( relPath.startsWith( "/" ) || relPath.startsWith( "$" ) ) //$ is for variables ${innerlayout}
		{
			return relPath;
		}

		String newAbsPath = absPath;
		String newRelPath = relPath;
		if (absPath.endsWith("/"))
		{
			newAbsPath = absPath.substring(0, absPath.length() - 1);
		}
		else
		{
			//	absPath ends with a filename, remove it (eInnovation change)
			int lastSlashIndex = absPath.lastIndexOf('/');
			if ( lastSlashIndex >= 0 )
			{
				newAbsPath = absPath.substring( 0, lastSlashIndex );
			}
			else
			{
				newAbsPath = "";
			}
		}

		int relPos = newRelPath.indexOf("../");
		while (relPos > -1)
		{
			newRelPath = newRelPath.substring(relPos + 3);
			int lastSlashInAbsPath = newAbsPath.lastIndexOf( "/" );
			if ( lastSlashInAbsPath >= 0 )
			{
				newAbsPath = newAbsPath.substring(0, newAbsPath.lastIndexOf("/"));
			}
			else
			{
				//	eInnovation change: fix potential exception
				newAbsPath = "";
			}
			relPos = newRelPath.indexOf("../");
		}
		String returnedPath;
		if (newRelPath.startsWith("/"))
		{
			returnedPath = newAbsPath + newRelPath;
		}
		else
		{
			returnedPath = newAbsPath + "/" + newRelPath;
		}


		//	remove any "." references to current directory (eInnovation change)
		//	For example:
		//		"./junk" becomes "junk"
		//		"/./junk" becomes "/junk"
		//		"junk/." becomes "junk"
		while ( returnedPath.endsWith( "/." ) )
		{
			returnedPath = returnedPath.substring( 0, returnedPath.length() - 2 );
		}
		do
		{
			int dotSlashIndex = returnedPath.lastIndexOf( "./" );
			if ( dotSlashIndex < 0 )
			{
				break;
			}
			else if ( dotSlashIndex == 0 || returnedPath.charAt( dotSlashIndex - 1 ) != '.' )
			{
				String firstSubstring;
				if ( dotSlashIndex > 0 )
				{
					firstSubstring = returnedPath.substring( 0, dotSlashIndex );
				}
				else
				{
					firstSubstring = "";
				}
				String secondSubstring;
				if ( dotSlashIndex + 2 < returnedPath.length() )
				{
					secondSubstring = returnedPath.substring( dotSlashIndex + 2, returnedPath.length() );
				}
				else
				{
					secondSubstring = "";
				}
				returnedPath = firstSubstring + secondSubstring;
			}
		} while ( true );

		return returnedPath;
	}

	/**
	 * Pass in /sub/dir/path.html returns path.html
	 * @param inPath
	 * @return
	 */
	public static String extractFileName(String path) {

		if ( path == null)
		{
			return null;
		}
		if( path.endsWith("/"))
		{
			path = path.substring(0,path.length() - 1);
		}
		if( path.isEmpty() )
		{
			return "";
		}
		String newpath = path.replace('\\','/');
		int start = newpath.lastIndexOf("/");
		if ( start == -1)
		{
			start = 0;
		}
		else
		{
			start = start + 1;
		}
		String pageName = newpath.substring(start, newpath.length());

		return pageName;
	}

	public static String createDraftPath(String inPath)
	{
		if ( inPath != null)
		{
			if( !inPath.contains(".draft."))
			{
				String root = PathUtilities.extractPagePath(inPath);
				String p = root + ".draft." + PathUtilities.extractPageType(inPath);
				return p;
			}
		}
		return inPath;
	}
	
	public static String createLivePath(String inDraftPath)
	{
		if ( inDraftPath != null)
		{
			if( inDraftPath.contains(".draft."))
			{
				return inDraftPath.replace(".draft", "");
			}
		}
		return inDraftPath;
	}
	/**
	 * @deprecated Use extract ID
	 * @param inText
	 * @return
	 */
	public static String makeId(String inText)
	{
		String id = inText;
		id = id.replace("/","_");		
		id = id.replace("\\","_");		
		id = id.replace(".","_");		
		id = id.replace(" ","_");		
		if( id.charAt(0) == '_')
		{
			id = id.substring(1,id.length());
		}
		return id;
	}
	public static String extractId( String inName)
	{
		return extractId(inName,true);
	}
	public static String extractId( String inName, boolean inAllowUnderstores)
	{
		StringBuffer out = new StringBuffer(inName.length());
		for (int i = 0; i < inName.length(); i++)
		{
			char c = inName.charAt(i);
			if( Character.isLetterOrDigit(c) )
			{
					out.append(c);
			}
			else if( inAllowUnderstores )
			{
				 if( c == '_' )
				 {
					out.append(c);
				 }
				 else if( c == ' ')
				 {
					 out.append("_");
				 }
			}
		}
		String result = out.toString().toLowerCase();
		result = URLUtilities.escapeUtf8(result);
		//CVS fails to save this. Should use the HEX number
		/*		result = result.replace('á', 'a');
		result = result.replace('é', 'e');
		result = result.replace('í', 'i');
		result = result.replace('ó', 'o');
		result = result.replace('ú', 'u');
*/
		return result;
	}

	public static Map extractArguments(String inArgs)
	{
		String[] args = inArgs.split("&");
		Map arguments = new HashMap(args.length);
		
		for (int i = 0; i < args.length; i++)
		{
			String[] pairs = args[i].split("=");						
			if( pairs.length > 0)
			{
				String[] values = (String[])arguments.get(pairs[0]);
				if( values == null)
				{
					values = new String[1];
				}
				else
				{
					String[] newvalues = new String[values.length + 1];
					System.arraycopy(values,0, newvalues,0, values.length);
					values = newvalues;
				}
				if( pairs.length > 1)
				{
					values[values.length -1] = pairs[1];
				}
				else
				{
					values[values.length -1] = null;
				}
				arguments.put(pairs[0], values);
			}
		}

		return arguments;
	}
	

}
