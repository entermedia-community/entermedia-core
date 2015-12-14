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
package org.openedit.page.manage;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.openedit.OpenEditRuntimeException;
import org.openedit.util.FileUtils;


/**
 * Data structure representing a MIME type map.
 *
 * @author Anthony Eden
 * @author Matthew Avery, mavery@einnovation.com
 */
public class BaseMimeTypeMap implements MimeTypeMap
{
	protected String fieldDefaultMimeType;
	protected Map fieldInternalMap;
	protected File fieldRootDirectory;
	
	public BaseMimeTypeMap()
	{
		fieldDefaultMimeType = "application/octet-stream";
	}

	/**
     * @see org.openedit.page.manage.MimeTypeMap#setDefaultMimeType(java.lang.String)
     */
	public void setDefaultMimeType(String inMimeType)
	{
		fieldDefaultMimeType = inMimeType;
	}

	/**
     * @see org.openedit.page.manage.MimeTypeMap#getDefaultMimeType()
     */
	public String getDefaultMimeType()
	{
		return fieldDefaultMimeType;
	}

	/**
     * @see org.openedit.page.manage.MimeTypeMap#getMimeType(java.lang.String)
     */
	public String getMimeType(String inExtension)
	{
		if ( inExtension == null )
		{
			return getDefaultMimeType();
		}
		String mimeType = (String) get(inExtension.toLowerCase());

		if (mimeType == null)
		{
			mimeType = getDefaultMimeType();
		}

		return mimeType;
	}

	/**
     * @see org.openedit.page.manage.MimeTypeMap#getPathMimeType(java.lang.String)
     */
	public String getPathMimeType(String path)
	{
		return getMimeType(getExtension(path));
	}

	protected String getExtension(String inPath)
	{
		String result = null;

		if (inPath != null)
		{
			int index = inPath.lastIndexOf('.');

			if (index > -1)
			{
				result = inPath.substring(index + 1);
			}
		}

		return result;
	}

	public void addMappings(String mimeType, String exts)
	{
		String ext;

		for (StringTokenizer st = new StringTokenizer(exts, ","); st.hasMoreTokens();)
		{
			ext = st.nextToken();
			put(ext, mimeType);
		}
	}
	
	public Map getInternalMap()
	{
		if ( fieldInternalMap == null )
		{
			fieldInternalMap = new HashMap();
			loadMimeTypes(fieldInternalMap);
		}
		return fieldInternalMap;
	}
	
	protected void loadMimeTypes(Map inInternalMap)
	{
		//first load the internal mimetypes.properties
		ClassLoader loader = getClass().getClassLoader();
		if( loader == null)
		{
			loader = ClassLoader.getSystemClassLoader();
		}

		InputStream in = loader.getResourceAsStream("mimetypes.properties");
		if ( in == null)
		{
			throw new OpenEditRuntimeException("Could not load mimetypes");
		}
			Properties values = new Properties();
			try
			{
				values.load(in);
				inInternalMap.putAll(values);
			} 
			catch ( Exception ex)
			{
				throw new OpenEditRuntimeException(ex);
			}
			finally
			{
				FileUtils.safeClose(in);
			}
		//check load up any additional ones from the WEB-INF/mimytypes.properties location
		File props2 = new File( getRootDirectory(), "/WEB-INF/mimetypes.properties");
		if ( props2.exists())
		{
			values = new Properties();
			try
			{
				in = new FileInputStream(props2);
				values.load(in);
				inInternalMap.putAll(values);
			} 
			catch ( Exception ex)
			{
				throw new OpenEditRuntimeException(ex);
			}
			finally
			{
				FileUtils.safeClose(in);
			}
		}
	}

	public void setInternalMap( Map internalMap )
	{
		fieldInternalMap = internalMap;
	}
	/**
     * @see org.openedit.page.manage.MimeTypeMap#get(java.lang.Object)
     */
	public Object get( Object key )
	{
		return getInternalMap().get( key );
	}
	/**
     * @see org.openedit.page.manage.MimeTypeMap#put(java.lang.Object, java.lang.Object)
     */
	public Object put( Object arg0, Object arg1 )
	{
		return getInternalMap().put( arg0, arg1 );
	}

	public File getRootDirectory()
	{
		return fieldRootDirectory;
	}

	public void setRootDirectory(File inRootDirectory)
	{
		fieldRootDirectory = inRootDirectory;
	}
}
