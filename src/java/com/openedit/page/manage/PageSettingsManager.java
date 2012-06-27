/*
 * Created on Jul 21, 2004
 */
package com.openedit.page.manage;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.repository.ContentItem;
import org.openedit.repository.Repository;
import org.openedit.repository.RepositoryException;

import com.openedit.Generator;
import com.openedit.ModuleManager;
import com.openedit.OpenEditException;
import com.openedit.OpenEditRuntimeException;
import com.openedit.generators.FailGenerator;
import com.openedit.page.PageSettings;
import com.openedit.util.PathUtilities;

/**
 * @author Matthew Avery, mavery@einnovation.com
 * 
 * Can we come up with a better name for this class.... please!?
 * 
 * Extending PageManager is a shortcut, not a valid case for inheritance.
 * It is possible that PageManager and MetaDataConfigurator have a common
 * abstract superclass.
 */
public class PageSettingsManager 
{
	//public static final String DEFAULT_PATH = "_default.xconf";
	public static final String SITE_PATH = "_site.xconf";
	private static final Log log = LogFactory.getLog(PageSettingsManager.class);
	
	protected Map fieldGeneratorMap;
	protected ModuleManager fieldModuleManager;
	protected Generator fieldDefaultGenerator;
	protected Repository fieldRepository;
	protected Map fieldCache;
	protected MimeTypeMap fieldMimeTypeMap;

	protected XConfToPageSettingsConverter fieldXconfReader;
	protected PageSettingsToXconfWriter fieldPageSettingsWriter;
	protected TextLabelManager fieldTextLabelManager;
	
	public PageSettings getPageSettings( String inPath ) throws OpenEditException
	{
		PageSettings page = (PageSettings) getCache().get( inPath );
		if ( page != null )
		{
			if( page.isCurrent()) //This is slow but is only run in the initial creation. After that it is seldom run
			{
				return page;				
			}
		}
		page = createPageSettings( inPath );
		return page;
	}

	protected Map getCache()
	{
		if( fieldCache == null)
		{
			//HARD means even if the object goes out of scope we still keep it in the hashmap
			//until the memory runs low then things get dumped randomly
			fieldCache = new ReferenceMap(ReferenceMap.HARD,ReferenceMap.SOFT);
		}
		return fieldCache;
	}
	public Generator getGenerator( String inName ) throws OpenEditException
	{
		String id = inName;
		Generator gen =  (Generator)getGeneratorMap().get( id );
		if ( gen == null)
		{
			if(getModuleManager().contains(inName))
			{
				gen = (Generator)getModuleManager().getBean(inName);
			}
			else
			{
				gen = new FailGenerator(inName);
			}
			getGeneratorMap().put(id,gen);
		}
		return gen;
	}
	
	protected PageSettings createPageSettings( String inUrlPath ) throws OpenEditException
	{
		//This managager deals with xconf files only
		String xconfPath = inUrlPath;
		xconfPath = toXconfPath(xconfPath);
		
		//debug( "Getting page meta data for " + inMetaDataPath );
		xconfPath = PathUtilities.resolveRelativePath( xconfPath, "/" );

		PageSettings settings = new PageSettings();
		settings.setTextLabels(getTextLabelManager());
		ContentItem content = null;
		try
		{
			content = getRepository().get(xconfPath);
			settings.setXConf( content );
		}
		catch (RepositoryException ex)
		{
			log.error( ex );
			throw new OpenEditException(ex);
		}
		//As long as it does not end with _default.xconf nor _site.xconf ir must be a real page
		if ( !xconfPath.equals("/_site.xconf") && !xconfPath.equals("/WEB-INF/base/_site.xconf"))
		{
			String path = PathUtilities.extractDirectoryPath( xconfPath ); //go up a level
			if ( xconfPath.endsWith("/_site.xconf"))
			{
				path = PathUtilities.extractDirectoryPath( path ); //go up a another level
			}
			PageSettings parent = getPageSettings( path + "/" + SITE_PATH );
			settings.setParent(parent);
		}	
		if( !inUrlPath.endsWith(".xconf") )
		{
			String mimeType = getMimeTypeMap().getPathMimeType( inUrlPath );
			settings.setMimeType(mimeType);
		}
		getXconfReader().configure( settings, inUrlPath );
//		if( getCache().size() > 2000)
//		{
//			getCache().clear(); //This seems a bit harsh
//		}
		getCache().put( inUrlPath, settings );
		return settings;
	}

	public String toXconfPath(String xconfPath) 
	{
		if( xconfPath.endsWith(".xconf"))
		{
			return xconfPath;
		}
		if( xconfPath.endsWith("/"))
		{
			return xconfPath + SITE_PATH;
		}
		if( xconfPath.indexOf(".") == -1)
		{
			//might be a folder
			try
			{
				ContentItem item = getRepository().get(xconfPath);
				if( item.isFolder())
				{
					return xconfPath + "/" + SITE_PATH;
				}
			} 
			catch ( RepositoryException ex)
			{
				throw new OpenEditRuntimeException(ex);
			}
		}
		String path = PathUtilities.extractPagePath( xconfPath );
		xconfPath = path + ".xconf";
		return xconfPath;
	}
	public MimeTypeMap getMimeTypeMap()
	{
		return fieldMimeTypeMap;
	}

	public void setMimeTypeMap( MimeTypeMap mimeTypeMap )
	{
		fieldMimeTypeMap = mimeTypeMap;
	}

	public Map getGeneratorMap()
	{
		if ( fieldGeneratorMap == null )
		{
			fieldGeneratorMap = new HashMap();
		}
		return fieldGeneratorMap;
	}
	public void setGeneratorMap( Map generators )
	{
		fieldGeneratorMap = generators;
	}
	public Repository getRepository()
	{
		return fieldRepository;
	}
	
	public void setRepository(Repository inRepository)
	{
		fieldRepository = inRepository;
	}

	protected ModuleManager getModuleManager()
	{
		return fieldModuleManager;
	}
	public void setModuleManager(ModuleManager inPluginManager)
	{
		fieldModuleManager = inPluginManager;
	}
	/**
	 * @param inXconf
	 */ 
	public void saveSetting(PageSettings inSetting) throws OpenEditException
	{
			ContentItem item = getPageSettingsWriter().createXConf(inSetting);
			saveSetting(item);
	}
	public void saveSetting(ContentItem inXconf) throws OpenEditException
	{
		try
		{
			getRepository().put(inXconf);
		}
		catch (RepositoryException ex)
		{
			log.error( ex );
			throw new OpenEditException(ex);
		}		
		getCache().remove(inXconf.getPath()); //this also should remove the *.html version
	}
	public void clearCache()
	{
		getCache().clear();
	}
	protected XConfToPageSettingsConverter getXconfReader()
	{
		return fieldXconfReader;
	}
	public void setXconfReader(XConfToPageSettingsConverter inConverter)
	{
		fieldXconfReader = inConverter;
	}
	protected PageSettingsToXconfWriter getPageSettingsWriter()
	{
		return fieldPageSettingsWriter;
	}
	public void setPageSettingsWriter(PageSettingsToXconfWriter inPageSettingsWriter)
	{
		fieldPageSettingsWriter = inPageSettingsWriter;
	}

	/**
	 * @param inPath such as index.xconf
	 */
	public void clearCache(String inPath) 
	{
		getCache().remove(inPath);
		if( !inPath.endsWith(".xconf"))
		{
			String path = toXconfPath(inPath);
			getCache().remove(path);
		}
	}
	public TextLabelManager getTextLabelManager()
	{
		return fieldTextLabelManager;
	}

	public void setTextLabelManager(TextLabelManager inTextLabelManager)
	{
		fieldTextLabelManager = inTextLabelManager;
	}


}
