
package com.openedit.page;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.Data;
import org.openedit.repository.ContentItem;
import org.openedit.repository.RepositoryException;

import com.openedit.Generator;
import com.openedit.OpenEditException;
import com.openedit.WebPageRequest;
import com.openedit.config.Script;
import com.openedit.config.Style;
import com.openedit.error.ContentNotAvailableException;
import com.openedit.generators.Output;
import com.openedit.util.FileUtils;
import com.openedit.util.OutputFiller;
import com.openedit.util.PathUtilities;


public class Page implements Data, Comparable
{
	private static Log log = LogFactory.getLog(Page.class);
	public static final String BLANK_LAYOUT = "NOLAYOUT";
	
	protected String fieldPath;
	protected PageSettings fieldPageSettings;
	protected ContentItem fieldContentItem;
	protected long fieldOriginalyModified;
	protected Map fieldCache;
	
	public Page( String inPath, PageSettings inMetaData )
	{
		fieldPath = inPath;
		setPageSettings(inMetaData );
	}
	/**
	 * 
	 */
	public Page()
	{
	}
	public Page( Page inPage )
	{
		this( inPage.getPath(), inPage.getPageSettings() );
		fieldContentItem = inPage.getContentItem();
	}
	protected Map getCache()
	{
		if( fieldCache == null)
		{
			fieldCache = new HashMap();
		}
		return fieldCache;
	}
	public boolean isBinary()
	{
		if ( getMimeType() == null)
		{
			return false; //There are many more text formats we support
		}
		if( getMimeType().startsWith("text") )
		{
			return false;
		}
		if( getMimeType().indexOf("xml") > -1 )
		{
			return false;
		}
		return true;
		
	}
	
	public boolean isFolder()
	{
		return getContentItem().isFolder();
	}
	
	public boolean exists()
	{
		return getContentItem().exists();
	}

	public Date getLastModified()
	{
		return getContentItem().lastModified();
	}
	public long lastModified()
	{
		return getContentItem().getLastModified();
	}

	/**
	 * This is asking if the settings have been changed under it
	 * @return
	 */
	
	public boolean isCurrent()
	{
		//Content
		long time = getContentItem().getLastModified();
//		if ( time == -1)
//		{
//			return false; //is missing. This seems very wasteful
//		}
		boolean pageCurrent = time == getOriginalyModified();
		if( !pageCurrent)
		{
			return false;
		}
		boolean metaDataCurrent = getPageSettings().isCurrent();
		return metaDataCurrent;
	}
	/**
	 * DOCME
	 *
	 * @param inDateFormat DOCME
	 *
	 * @return DOCME
	 */
	public String getLastModified(String inDateFormat)
	{
		Date date = getLastModified();
		if( date == null)
		{
			return null;
		}
		SimpleDateFormat format = new SimpleDateFormat(inDateFormat);

		return format.format(date);
	}

	/**
	 * DOCME
	 *
	 * @return DOCME
	 */
	public String getMimeType()
	{
		return getPageSettings().getMimeType();
	}

	/**
	 * Get a List of page actions.  To add an action to the page just add the action to this List.
	 * Page actions are triggered each time the page is requested.
	 *
	 * @return A List of page actions
	 */
	public List getPageActions()
	{
		List copy = (List)getCache().get("pageActions");
		if( copy == null)
		{
			List actions =  getPageSettings().getPageActions();
			if( isDynamic() )
			{
				copy = actions;
			}
			else
			{
				copy = new ArrayList(actions.size());
				for (Iterator iter = actions.iterator(); iter.hasNext();)
				{
					PageAction action = (PageAction) iter.next();
					if ( action.isIncludesAll() )
					{
						copy.add(action);
					}
				}
			}
			getCache().put("pageActions",copy);
		}
		return copy;
	}
	
	
	public List getStyles()
	{
		if( isHtml() )
		{
			List styles =  getPageSettings().getStyles();
			//look for duplicate
			List copy = new ArrayList(styles.size());
			Set got = new HashSet(styles.size());
			for (Iterator iterator = styles.iterator(); iterator.hasNext();)
			{
				Style script = (Style) iterator.next();
				
				if( !got.contains(script.getId()) )
				{
					copy.add(script);
					got.add(script.getId()); //no duplicates
					//script.setHref($content.getPageSettings().replaceProperty(script.getHref()));

				}
			}
			return copy;
		}
		return null;
	}

	public List getScripts()
	{
		if( isHtml() )
		{
			List scripts =  getPageSettings().getScripts();
			//look for duplicate
			List copy = new ArrayList(scripts.size());
			HashMap ids = new HashMap(scripts.size());
			for (Iterator iterator = scripts.iterator(); iterator.hasNext();)
			{
				Script script = (Script) iterator.next();
				ids.put(script.getId(),script);
			}
			for (Iterator iterator = scripts.iterator(); iterator.hasNext();)
			{
				Script script = (Script) iterator.next();
				Script goodone = (Script)ids.get(script.getId());
				//goodone.setSrc(getPageSettings().replaceProperty(goodone.getSrc()));
				if( !copy.contains(goodone) )
				{
					copy.add(goodone);
				}
			}
			return copy;
		}
		return null;
	}
	public List getScriptPaths()
	{
		List paths = (List)getCache().get("scriptPaths");
		if( paths == null)
		{
			List scripts = getScripts();
			if( scripts != null)
			{
				paths = new ArrayList(scripts.size());
				for (Iterator iterator = scripts.iterator(); iterator.hasNext();)
				{
					Script script = (Script) iterator.next();
					paths.add(getPageSettings().replaceProperty(script.getSrc()));
				}
				getCache().put("scriptPaths",paths);
			}
		}
		return paths;
	}
	public List getStylePaths()
	{
		List paths = (List)getCache().get("stylePaths");
		if( paths == null)
		{
			List styles = getStyles();
			if( styles != null)
			{
				paths = new ArrayList(styles.size());
				for (Iterator iterator = styles.iterator(); iterator.hasNext();)
				{
					Style style = (Style) iterator.next();
					paths.add(getPageSettings().replaceProperty(style.getHref()));
				}
				getCache().put("stylePaths",paths);
			}
		}
		return paths;
	}
	
	/**
	 * Get a List of page actions.  To add an action to the page just add the action to this List.
	 * Page actions are triggered each time the page is requested.
	 *
	 * @return A List of page actions
	 */
	public List getPathActions()
	{
		List copy = (List)getCache().get("pathActions");
		if( copy == null)
		{
			List actions = getPageSettings().getPathActions();
			if( isDynamic() )
			{
				copy = actions;
			}
			else
			{
				copy = new ArrayList(actions.size());
				for (Iterator iter = actions.iterator(); iter.hasNext();)
				{
					PageAction action = (PageAction) iter.next();
					if ( action.isIncludesAll() )
					{
						copy.add(action);
					}
				}
			}
			getCache().put("pathActions",copy);
		}	
		return copy;
	}

	/**
	 * Get the request path.
	 *
	 * @return The request path
	 */
	public String getPath()
	{
		return fieldPath;
	}

	public String getDirectory()
	{
		String path = PathUtilities.extractDirectoryPath(getPath());
		// urlpath is the address the link came in on
		return path;
	}
	public String getDirectoryName()
	{
		String path = PathUtilities.extractDirectoryPath(getPath());
		// urlpath is the address the link came in on
		if( path.length()> 1)
		{
			path = PathUtilities.extractFileName(path);
		}
		return path;
	}
	public String getDirectoryRoot()
	{
		int slash = getPath().indexOf('/',1);
		if( slash > 1)
		{
			return getPath().substring(0,slash);
		}
		// urlpath is the address the link came in on
		return "";		
	}

	public String getName()
	{
		String path = PathUtilities.extractFileName(getPath());
		// urlpath is the address the link came in on
		return path;
	}
	public String getPageName()
	{
		String path = PathUtilities.extractPageName(getPath());
		return path;
	}
	public String getPageType()
	{
		String type = PathUtilities.extractPageType(getPath());
		return type;
	}
	
	/**
	 * Get the named page property using the default Locale.  If the property is not found then
	 * return null.
	 *
	 * @param name The property name
	 *
	 * @return The value or null
	 */
	public String getProperty(String name)
	{
		return getProperty(name, (Locale)null);
	}
	public String getProperty(String name, String language)
	{
		String val = null;
		if( language != null )
		{
			PageProperty property = (PageProperty) getPageSettings().getProperty(name);
			if (property != null)
			{
				val =  property.getValue(language);
			}
			else
			{
				return null;
			}
		}
		else
		{
			val =  getProperty(name,(Locale)null );
		}
		val = getPageSettings().replaceProperty(val);
		return val;
	}
	/**
	 * Get the Locale-specific value for the given named property.  If the property is not found
	 * then return null.  This method will try to find the most suitable locale by searching the
	 * property values in the following manner:
	 * 
	 * <p>
	 * language + "_" + country + "_" + variant<br> language + "_" + country<br> langauge<br> ""
	 * </p>
	 *
	 * @param name The property name
	 * @param locale The locale
	 *
	 * @return The value
	 */
	public String getProperty(String name, Locale language)
	{
		if( log.isDebugEnabled())
		{
			debug("Get property [name=" + name + ",locale=" + language + "]");
		}
		PageProperty property = (PageProperty) getPageSettings().getProperty(name);
		
		if (property != null)
		{
			String value = property.getValue(language);
			value = getPageSettings().replaceProperty(value);
			return value;
		}
		else
		{
			return null;
		}
	}	
	public void debug( String inMessage )
	{
		log.debug( inMessage );
		//System.out.println( inMessage );
	}

	/**
	 * Get the named property.  This method is equivilent to the <code>getProperty(name)</code>
	 * method.  This method is provided as a convenience to Velocity code.
	 *
	 * @param name The property name
	 *
	 * @return The value
	 */
	public String get(String name)
	{
		if(name.equals("name")){
			return getName();
		}
		if(name.equals("path")){
			return getPath();
		}
		if(name.equals("id")){
			return getId();
		}
		String value = getProperty(name);
		
		//debug("get(" + name + ") called to retrieve property");
		value = getPageSettings().replaceProperty(value);
		return value;
	}

	public boolean isPropertyTrue(String inKey)
	{
		Object val = getProperty(inKey);
		if( val != null)
		{
			return Boolean.parseBoolean(val.toString());
		}
		return false;
	}


	/**
	 * DOCME
	 *
	 * @return DOCME
	 */
	public String toString()
	{
		return getPath();
	}

	public void setPageSettings(PageSettings inSettings)
	{
		fieldPageSettings = inSettings;
	}
	public PageSettings getPageSettings()
	{
		return fieldPageSettings;
	}
	
	/**
	 * @return
	 */
	public String getAlternateContentPath()
	{
		return getPageSettings().getAlternateContentPath();
	}
	/**
	 * @return
	 */
	public List getGenerator()
	{
		return getPageSettings().getGenerators();
	}
	
	public InputStream getInputStream() throws ContentNotAvailableException
	{
		try
		{
			return getContentItem().getInputStream();
		}
		catch( RepositoryException e )
		{
			throw new ContentNotAvailableException(e.getMessage(), getPath() );
		}
	}
	/**
	 * @return
	 */
	public String getLayout()
	{
		return getPageSettings().getLayout();
	}
	
	public String getInnerLayout()
	{
		PageSettings parent = getPageSettings();
		return parent.getInnerLayoutExcludeSelf(getPath());
//		while( parent != null)
//		{
//			String layout =  parent.getInnerLayout();
//			if( layout == null || !layout.equals(getPath()))
//			{
//				return layout;
//			}
//			parent = parent.getParent();
//		}
//		return null;
//	}
	}

	public String findInnerLayout()
	{
		String il = getInnerLayout();
		if ( il == null)
		{
			return null;
		}
		if ( il.equals(BLANK_LAYOUT))
		{
			return null;
		}
		if( il.equalsIgnoreCase(getPath()))
		{
			return null;
		}
		return il;
	}

	
	public boolean hasLayout()
	{
		String layout = getLayout();
		
		return layout != null && !layout.equals(BLANK_LAYOUT);
	}
	public boolean hasInnerLayout()
	{
		String il = getInnerLayout();
		if ( il == null)
		{
			return false;
		}
		if ( il.equals(BLANK_LAYOUT))
		{
			return false;
		}
		if( il.equalsIgnoreCase(getPath()))
		{
			return false;
		}
		return true;
	}
	
	public ContentItem getContentItem()
	{
		return fieldContentItem;
	}
	public void setContentItem( ContentItem revision )
	{
		fieldContentItem = revision;
		if ( revision != null)
		{
			fieldOriginalyModified = revision.getLastModified();
		}
	}
	
	public Reader getReader() throws OpenEditException
	{
		if ( exists() )
		{
			InputStreamReader in = null;
			try
			{
				in = new InputStreamReader( getContentItem().getInputStream(), getCharacterEncoding() );
			} catch ( Exception ex )
			{
				throw new OpenEditException(ex);
			}
			return in;
		}
		else
		{
			throw new ContentNotAvailableException("No such page " + getPath(),getPath() );
		}
	}
	
	
	public Reader getReader(String inEncoding) throws OpenEditException
	{
		if ( exists() )
		{
			InputStreamReader in = null;
			try
			{
				in = new InputStreamReader( getContentItem().getInputStream(), inEncoding );
			} catch ( Exception ex )
			{
				throw new OpenEditException(ex);
			}
			return in;
		}
		else
		{
			throw new ContentNotAvailableException("No such page " + getPath(),getPath() );
		}
	}
	
	protected long getOriginalyModified()
	{
		return fieldOriginalyModified;
	}
	/* (non-javadoc)
	 * @see org.jpublish.Page#getCharacterEncoding()
	 */
	public String getCharacterEncoding()
	{
		//Its UTF-8 unless otherwise setup in the xconf's
		String encoding =  getPageSettings().getPropertyValue("encoding",null);
		if ( encoding == null)
		{
			return "UTF-8";
		}
		return encoding;
	}

	public String getContent() throws OpenEditException
	{
		StringWriter out = new StringWriter();
		Reader reader = null;
		try
		{
			reader = getReader();
			new OutputFiller().fill( reader, out);
		}
		catch (IOException ex)
		{
			log.error( ex );
			throw new OpenEditException(ex);
		}
		finally
		{
			FileUtils.safeClose(reader);
		}
		return out.toString();
	}
	/**
	 * @param inReq
	 */
	public WebPageRequest generate(WebPageRequest inReq, Output inOut) throws OpenEditException
	{
		WebPageRequest req = inReq;
		if ( inReq.getPage() != this)
		{
			req = inReq.copy(this);
		}
		if ( !req.hasRedirected() )
		{
			for (Iterator iter = getGenerator().iterator(); iter.hasNext();)
			{
				Generator gen= (Generator) iter.next();
				if ( gen.canGenerate(inReq))
				{
					gen.generate(req,this,inOut);
					break;
				}
			}
		}
		return req;
	}

	/**
	 * This will contain innerlayout details
	 * @param inWebPageContext
	 * @param inOutputStream
	 */
	public void generate(WebPageRequest inWebPageContext, Writer inOutputStream)
	{
		WebPageRequest context = null;
		if( inWebPageContext.getPage() == this  )
		{
			context = inWebPageContext.copy(this);
		}
		else
		{
			context = inWebPageContext;
		}
		context.putPageValue("content", this);
		PageStreamer streamer = inWebPageContext.getPageStreamer().copy();
		
		Output out = new Output();
		out.setWriter(inOutputStream);
		streamer.setOutput(out);

		context.putPageStreamer(streamer);
		streamer.setWebPageRequest(context);
		streamer.render();
		
	}
	
	
	public boolean isDynamic() {
		return isHtml() || isJson();
	}
	
	/**
	 * @return
	 */
	public boolean isJson() {
		String mime = getMimeType();
		if ( mime != null && mime.endsWith("json"))
		{
			return true;
		}
		return false;
	}
	
	public boolean isHtml() {
		String mime = getMimeType();
		if ( mime != null && mime.endsWith("html"))
		{
			return true;
		}
		return false;
	}
	public boolean isImage() {
		String mime = getMimeType();
		if ( mime != null && mime.startsWith("image"))
		{
			return true;
		}
		return false;
	}
	
	public boolean isVideo() {
		String mime = getMimeType();
		if ( mime != null && mime.startsWith("video"))
		{
			return true;
		}
		return false;
	}
	public boolean isDraft()
	{
		if( getContentItem().getActualPath().indexOf(".draft.") > -1 && exists() )
		{
			return true;
		}
		return false;
	}
	public Permission getPermission(String inName)
	{
		return getPageSettings().getPermission(inName);
	}
	public List getPermissions()
	{
		return getPageSettings().getPermissions();
	}
	
	public List getParentPaths()
	{
		List parents = new ArrayList();
		String path =  getPath();
		while( true )
		{
			if( path.length() == 0 || path.equals("/"))
			{
				break;
			}
			parents.add(0,path);
			path = PathUtilities.extractDirectoryPath(path);
		}
		if( parents.size() > 0)
		{	
			parents.add(0,"/");
		}
		else
		{
			parents.add("/");
		}
		Collections.sort(parents);	
		return parents;
	}
	
	public String getId()
	{
		String  id = PathUtilities.makeId(getPath());
		id = id.replace('/', '_');
		
		return id;
		
	}
	public String getParentPath()
	{
		if( getPath().equals("/"))
		{
			return null;
		}
		String path = PathUtilities.extractDirectoryPath(getPath());
		// urlpath is the address the link came in on
		if( path.equals(""))
		{
			return "/";
		}
		return path;
	}
	
	public String getSourcePath() {
	return getName();
	}
	
	public void setId(String inNewid) {
	
		
	}
	
	public void setName(String inName) {
		// TODO Auto-generated method stub
		
	}

	public void setProperty(String inId, String inValue) {
		getPageSettings().setProperty(inId, inValue);
		
	}
	public void setProperties(Map<String, String> inProperties)
	{
		getPageSettings().setProperties(inProperties);
	}
	
	public void setSourcePath(String inSourcepath) {
		// TODO Auto-generated method stub
		
	}
	public long length()
	{
		return getContentItem().getLength();
	}
	public Map getProperties() {
		return getPageSettings().getProperties();
	}
	public int compareTo(Object inO)
	{
		Page other = (Page)inO;
		return getPath().compareTo(other.getPath());
	}
	public String getText(String inKey, String inLocale)
	{
		String text = getPageSettings().getTextLabels().getAutoText(this, inKey, inLocale);
		return text;
	}

	public String replaceProperty(String inValue){
		return getPageSettings().replaceProperty(inValue);
	}
	
	public void setValues(String inKey, Collection<String> inValues)
	{
		StringBuffer values = new StringBuffer();
		for (Iterator iterator = inValues.iterator(); iterator.hasNext();)
		{
			String detail = (String) iterator.next();
			values.append(detail);
			if( iterator.hasNext())
			{
				values.append(" | ");
			}
		}
		setProperty(inKey,values.toString());
	}
}
