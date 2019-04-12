/*
 * Created on Jan 28, 2005
 */
package org.openedit.page.manage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.openedit.Generator;
import org.openedit.OpenEditException;
import org.openedit.config.Configuration;
import org.openedit.config.Script;
import org.openedit.config.Style;
import org.openedit.generators.CompositeGenerator;
import org.openedit.generators.GeneratorWithAcceptFilter;
import org.openedit.generators.GeneratorWithMimeTypeFilter;
import org.openedit.page.Page;
import org.openedit.page.PageAction;
import org.openedit.page.PageProperty;
import org.openedit.page.PageSettings;
import org.openedit.page.Permission;
import org.openedit.page.XconfConfiguration;
import org.openedit.repository.RepositoryException;
import org.openedit.util.OutputFiller;
import org.openedit.util.PathUtilities;
import org.openedit.util.XmlUtil;
import org.openedit.util.strainer.Filter;
import org.openedit.util.strainer.FilterReader;

/**
 * @author cburkey
 *
 */
public class XConfToPageSettingsConverter
{
	private static final Log log = LogFactory.getLog(XConfToPageSettingsConverter.class);
	protected PageSettingsManager fieldPageSettingsManager;
	protected FilterReader fieldFilterReader;
	
	protected XmlUtil fieldXmlUtil = new XmlUtil();
	protected OutputFiller fieldOutputFiller = new OutputFiller();
	protected List loadActions( PageSettings inSettings,List inPageActionList ) throws OpenEditException
	{
		if ( inPageActionList.size() == 0)
		{
			return null;
		}
		List pageActions = new ArrayList(inPageActionList.size());
		Iterator pageActionElements = inPageActionList.iterator();
		while (pageActionElements.hasNext())
		{
			Configuration pageActionElement = (Configuration) pageActionElements.next();
			PageAction currentPageAction = createAction(inSettings, pageActionElement );
			
			pageActions.add( currentPageAction );
		}
		return pageActions;
	}
	protected List loadScripts( PageSettings inSettings,List inScripts ) throws OpenEditException
	{
		if ( inScripts.size() == 0)
		{
			return null;
		}
		List pageActions = new ArrayList(inScripts.size());
		Iterator pageActionElements = inScripts.iterator();
		while (pageActionElements.hasNext())
		{
			Configuration pageActionElement = (Configuration) pageActionElements.next();
			Script script = createScript(inSettings, pageActionElement );
			
			pageActions.add( script );
		}
		return pageActions;
	}

	protected List loadStyles( PageSettings inSettings,List inStyles ) throws OpenEditException
	{
		if ( inStyles.size() == 0)
		{
			return null;
		}
		List pageActions = new ArrayList(inStyles.size());
		Iterator pageActionElements = inStyles.iterator();
		while (pageActionElements.hasNext())
		{
			Configuration pageActionElement = (Configuration) pageActionElements.next();
			Style script = createStyle(inSettings, pageActionElement );
			if( script != null)
			{
				pageActions.add( script );
			}
		}
		return pageActions;
	}

	
	protected Script createScript(PageSettings inSettings, Configuration inConfigElement)
	{
		String cancel = inConfigElement.get("cancel");
		if(cancel != null && cancel.equals("true") )
		{
			return null;
		}
		Script script = new Script();
		script.setId(inConfigElement.get("id"));
		script.setSrc(inConfigElement.get("src"));
		String external = inConfigElement.get("external");
		script.setExternal(Boolean.parseBoolean(external));
		script.setPath(inSettings.getPath());
		return script;
	}
	protected Style createStyle(PageSettings inSettings, Configuration inConfigElement)
	{
		Style style = new Style();
		style.setId(inConfigElement.get("id"));
		style.setHref(inConfigElement.get("href"));
		style.setExternal(Boolean.parseBoolean(inConfigElement.get("external")));
		return style;
	}
	private PageAction createAction(PageSettings inSettings, Configuration inPageActionElement)
	{
		String actionName = inPageActionElement.getAttribute("name");
		PageAction currentPageAction = new PageAction(  actionName );
		currentPageAction.setPath(inSettings.getXConf().getPath());
		currentPageAction.setConfig( inPageActionElement );
		currentPageAction.setIncludesAll(Boolean.parseBoolean( inPageActionElement.getAttribute("alltypes") ) );
		return currentPageAction;
	}

	protected void loadAlternateContentFile( PageSettings inPageConfig, String inAlternatePath )
	{
		if ( inAlternatePath != null )
		{
			String path = PathUtilities.resolveRelativePath(inAlternatePath,inPageConfig.getPath());
			inPageConfig.setAlternateContentPath( path );
		}
	}
	protected void loadGenerators( PageSettings inPageConfig, Configuration inParentConfig) throws OpenEditException
	{ 
		if ( inParentConfig == null )
		{
			return;
		}
		List allGens = new ArrayList(2); 
		List root = inParentConfig.getChildren("generator"); //these are top level generators
		for (Iterator iter = root.iterator(); iter.hasNext();)
		{
			Configuration rootconfig = (Configuration) iter.next();
			Generator generator = createGenerator(rootconfig);
			allGens.add(generator);
		}
		inPageConfig.setGenerators(allGens);
	}
	
	protected Generator createGenerator(Configuration inRootconfig) throws OpenEditException
	{
		String name = inRootconfig.getAttribute("name");
		Generator generator = null;
		if ( name.equals("composite"))
		{
			//now add any children to a list
			List children  = inRootconfig.getChildren("generator");
			List all = new ArrayList(children.size());
			for (Iterator iter = children.iterator(); iter.hasNext();)
			{
				Configuration config = (Configuration) iter.next();
				Generator child = createGenerator(config);
				all.add(child);
			}
			CompositeGenerator composite = new CompositeGenerator();
			composite.setGenerators(all);
			generator = composite;
		}
		else
		{
			generator = getPageSettingsManager().getGenerator( name );
		}
		generator = addFilter(inRootconfig,generator);
		return generator;
	}

	protected Generator addFilter(Configuration config, Generator generator)
	{
		String types = config.getAttribute("mimetypes");
		if( types != null)
		{
			generator = new GeneratorWithMimeTypeFilter(generator,types);
		}
		String accepts = config.getAttribute("accepts");
		if ( accepts != null)
		{
			generator = new GeneratorWithAcceptFilter(generator,accepts);
		}
		return generator;
	}
	protected void loadLayout( PageSettings inPageConfig,  Configuration inLayoutConfig ) throws OpenEditException
	{
		if ( inLayoutConfig == null )
		{
			return;
		}
	
		String layoutPath = inLayoutConfig.getValue();
		if ( layoutPath == null )
		{
			inPageConfig.setLayout(Page.BLANK_LAYOUT);
			return;
		}
		layoutPath = PathUtilities.resolveRelativePath( layoutPath, inPageConfig.getPath() );
		if ( layoutPath.equals(inPageConfig.getPath()))
		{
			//dont set layout to self
			inPageConfig.setLayout(null);
			return;
		}
		inPageConfig.setLayout( layoutPath );	
	}
	
	protected void loadInnerLayout( PageSettings inPageConfig,  Configuration inInnerLayoutConfig ) throws OpenEditException
	{
		if ( inInnerLayoutConfig == null )
		{
			return;
		}
	
		String innerLayoutPath = inInnerLayoutConfig.getValue();
		if ( innerLayoutPath == null )
		{
			inPageConfig.setInnerLayout(Page.BLANK_LAYOUT);
			return;
		}
		innerLayoutPath = PathUtilities.resolveRelativePath( innerLayoutPath, inPageConfig.getPath() );
		if ( innerLayoutPath.equals(inPageConfig.getPath()))
		{
			//dont set layout to self
			inPageConfig.setInnerLayout(Page.BLANK_LAYOUT);
			return;
		}
		inPageConfig.setInnerLayout( innerLayoutPath );	
	}
	protected void loadPermissionFilters( PageSettings inPageConfig, XconfConfiguration inConfig ) throws OpenEditException
	{
		List permissions = new ArrayList();
		Filter viewf = getFilterReader().readFilterCollection( inConfig.getViewRequirements(),"view" );
		if( viewf != null)
		{
			Permission per = new Permission();
			per.setName("view");
			per.setRootFilter(viewf);
			per.setPath(inPageConfig.getPath());
			permissions.add(per);
		}
		Filter edit = getFilterReader().readFilterCollection( inConfig.getEditRequirements(),"edit" );
		if( edit != null)
		{
			Permission per = new Permission();
			per.setName("edit");
			per.setRootFilter(edit);
			per.setPath(inPageConfig.getPath());
			permissions.add(per);
		}
		for (Iterator iterator = inConfig.getChildIterator("permission"); iterator.hasNext();)
		{
			Configuration top = (Configuration) iterator.next();
			String name = top.getAttribute("name");
			Filter root = getFilterReader().readFilterCollection( top,name );
			
			Permission per = new Permission();
			per.setName(name);
			per.setRootFilter(root);
			per.setPath(inPageConfig.getPath());
			permissions.add(per);
		}
		if( permissions.size() > 0)
		{
			inPageConfig.setPermissions(permissions);
		}
	}

	protected Map loadProperties( List inPropertyList )
	{
		Map properties = new HashMap(inPropertyList.size());
		Iterator propertyElements = inPropertyList.iterator();
		while (propertyElements.hasNext())
		{
			Configuration propertyElement = (Configuration) propertyElements.next();
			String name = propertyElement.getAttribute("name");
			PageProperty property = new PageProperty(name);
			boolean hasvalue = false;
			for (Iterator iter =  propertyElement.getChildIterator("value"); iter.hasNext();)
			{
				hasvalue = true;
				Configuration val = (Configuration) iter.next();
				String locale = val.getAttribute("locale");
				property.setValue(val.getValue(), locale); //TODO: Should I pass "" if its the default locale already?
			}
			if( !hasvalue )
			{
				String value = propertyElement.getValue();
//				if( value == null)
//				{
//					value = "";
//				}
				property.setValue(value, (Locale)null);
			}
			
			properties.put(name, property);
		}
		return properties;
	}

	public PageSettingsManager getPageSettingsManager()
	{
		return fieldPageSettingsManager;
	}
	public void setPageSettingsManager(PageSettingsManager inPageSettingsManager)
	{
		fieldPageSettingsManager = inPageSettingsManager;
	}

	/**
	 * @param inPageSettings
	 * @param inUrlPath
	 */
	public void configure(PageSettings inPageSettings, String inUrlPath) throws OpenEditException
	{
		boolean contentexists =  getPageSettingsManager().getRepository().doesExist( inUrlPath );
		boolean settings = inPageSettings.exists();
		if ( !settings )
		{
			loadFallBackDirectory( inPageSettings, inUrlPath, contentexists );
			//	loadOverrideDirectory( inPageSettings, inUrlPath );		
			loadAlternativeContent(inPageSettings, inUrlPath, contentexists);
//
			return;
		}
		if( log.isDebugEnabled())
		{
			log.info( "Configure: " + inPageSettings.getPath() );
		}
		XconfConfiguration config = new XconfConfiguration( );

		Element root= null;
		try
		{
			root = fieldXmlUtil.getXml(inPageSettings.getReader(),inPageSettings.getPageCharacterEncoding());
		}
		catch (Exception e )
		{
			log.error("Could not read: " + inUrlPath );
			throw new OpenEditException(e + "path: " + inPageSettings.getPath(),e,inUrlPath);
		}
		config.populate(root);
		inPageSettings.getProperties().putAll( loadProperties( config.getProperties()));
		
		if( settings ) //Reloads it here since it was not loaded above
		{
			loadFallBackDirectory( inPageSettings, inUrlPath, contentexists );		
			loadAlternativeContent(inPageSettings, inUrlPath, contentexists);
		}
		
		loadPermissionFilters( inPageSettings, config );

		loadGenerators(inPageSettings, config);
		loadLayout( inPageSettings, config.getLayout() );
		loadInnerLayout( inPageSettings, config.getInnerLayout() );
		
		
		//TODO: Move this to a module
		//List pageActions = new ArrayList();
		//pageActions.addAll( loadValues( config.getPageValues(), "PageValue." ) );
		//pageActions.addAll( loadValues( config.getSessionValues(), "SessionValue.") );
		List pagea = loadActions( inPageSettings,config.getPageActions() );
		inPageSettings.setPageActions( pagea );
		
		List patha = loadActions(inPageSettings,config.getPathActions());
		inPageSettings.setPathActions(patha);

		inPageSettings.setScripts(loadScripts(inPageSettings,config.getScripts()));

		inPageSettings.setStyles(loadStyles(inPageSettings,config.getStyles()));

		//turns out we need this for login-path and other places I am sure
		//If there are unkown tags in the xconf then set the user defined data field

		if ( config.hasChild("product") || config.hasChild("asset") || config.hasChild("blog")) //TODO: Do we use this anymore?
		{
			inPageSettings.setUserDefinedData(config);
		}
		
		String mime = inPageSettings.getPropertyValue("mimetype", null);
		if( mime != null)
		{
			inPageSettings.setMimeType(mime);
		}
		
	}

	protected void loadAlternativeContent(PageSettings inPageSettings, String inUrlPath, boolean inContentexists) throws RepositoryException
	{
		inPageSettings.setOriginalyExistedContentPath(inContentexists);
		//Find the alternative content path if found someplace else							
		String fallback = inPageSettings.getPropertyValueFixed("fallbackcontentpath");
		if( fallback != null)
		{
			fallback = PathUtilities.resolveRelativePath(fallback, inPageSettings.getPath());
			inPageSettings.setAlternateContentPath(fallback);
		}
		else if(!inContentexists)
		{
			//Look for some content to use
			PageSettings settings = inPageSettings.getFallback();
			boolean isfolder = false;
			if( settings != null && settings.getPath().endsWith("/_site.xconf") && !inUrlPath.endsWith("_site.xconf"))
			{
				isfolder = true;
			}
			while( settings != null)
			{
				String alternativepath = settings.getPath();
				alternativepath = PathUtilities.extractDirectoryPath(alternativepath);
				if(!isfolder)
				{
					alternativepath += "/" + PathUtilities.extractFileName(inUrlPath);
				}
				boolean fallbackcontentexists = getPageSettingsManager().getRepository().doesExist(alternativepath);
				if( fallbackcontentexists )
				{
					inPageSettings.setAlternateContentPath(alternativepath);
					break;
				}
				settings = settings.getFallback();
			}
		}
		
	}

	/**
	 * @param inPageSettings
	 * @param inUrlPath
	 */
	protected void loadFallBackDirectory(PageSettings inPageSettings, String inUrlPath, boolean contentexists) throws OpenEditException
	{
		boolean inwebinfs = false;
		if ( inUrlPath.startsWith("/WEB-INF/"))
		{
			inwebinfs = true;
		}
		boolean specified = false;
		String fallBackValue = null;

		//this is a catch 22. If we don't have a 1st level fallback set it might not look for second level
		PageProperty fallBackDir  = inPageSettings.getProperty("fallbackdirectory");
		String alternativepath = null;
		if ( fallBackDir  != null && fallBackDir.getValue() != null )
		{
			fallBackValue = fallBackDir.getValue();
			//1. First is looks in mattcatalog. But there we want to use another fallback
			
			//this might be using a variable. The value for this comes from the parent
			fallBackValue = inPageSettings.replaceProperty(fallBackValue);
			fallBackValue = inPageSettings.getParent().replaceProperty(fallBackValue);
			if( fallBackValue.equals("/"))
			{
				fallBackValue = "";
			}
			if ( fallBackValue.endsWith("/")){
				throw new OpenEditException("Fall back setting must not end in slash for " + inUrlPath);
			}
			if( fallBackValue.equals("NO_FALLBACK")  )
			{
				return;
			}
			//Find out the directory we are in. 
			String thisdir = fallBackDir.getPath();
			//There is a problem here, we seem to be mixing and matching paths and the substring
//			if( !inUrlPath.startsWith(thisdir) )
//			{
//				inwebinfs = true;
//			}
//			else
//			{
				thisdir = PathUtilities.extractDirectoryPath(thisdir); //what level the path was defined
				String filepart = inUrlPath.substring(thisdir.length(),inUrlPath.length());
				alternativepath = fallBackValue + filepart; //end part might be a file name or _site.xconf
				if( alternativepath.equals(inUrlPath))
				{
					//Now sure why this happens
					log.debug(inUrlPath + " Cannot specify self as fallback directory");
					return;
				}
				specified = true;
//			}
		}
		else if(!inwebinfs)//Site wide default
		{
			fallBackValue = "/WEB-INF/base";
			alternativepath = fallBackValue + inUrlPath;
		}
		
		try
		{
			if( inwebinfs && !specified)
			{
				//log.info("Not loading fallback for " + inUrlPath);
			}
			else
			{
				PageSettings otherxconf = getPageSettingsManager().getPageSettings(alternativepath);
				//log.info("loading fallback for " + inUrlPath + " with " + alternativepath);
				inPageSettings.setFallBack(otherxconf);
//				if( specified )
//				{
//					followFallBack(inPageSettings, fallBackDir, alternativepath);
//				}
				
				
			}
		} 
		catch ( Exception ex )
		{
			log.error(ex);
			if( ex instanceof OpenEditException)
			{
				throw (OpenEditException)ex;
			}
			throw new OpenEditException(ex);
		}
	}
	/*
	protected void followFallBack(PageSettings inPageSettings, PageProperty fallBackDir, String alternativepath)
	{
		PageSettings otherxconf;
		PageProperty fallBackDirNew  = inPageSettings.getProperty("fallbackdirectory");
		String oldval = fallBackDir == null?null:fallBackDir.getValue();
		String newval = fallBackDirNew == null?null:fallBackDirNew.getValue();
		
		if( newval != null && !newval.equals( oldval ) )
		{
			//loadFallBackDirectory(inPageSettings,inUrlPath, contentexists);
			//log.info(newval);
			String defined = PathUtilities.extractDirectoryPath(fallBackDirNew.getPath()); //what level the path was defined

			String extrapathinfo = alternativepath.substring(defined.length() );
			newval = inPageSettings.replaceProperty(newval);
			newval = inPageSettings.getParent().replaceProperty(newval);
			newval = newval + extrapathinfo;
			otherxconf = getPageSettingsManager().getPageSettings(newval);
			inPageSettings.setFallBack(otherxconf);
			followFallBack(inPageSettings,fallBackDirNew,alternativepath);
		}
	}
	*/

	public FilterReader getFilterReader()
	{
		return fieldFilterReader;
	}

	public void setFilterReader(FilterReader inFilterReader)
	{
		fieldFilterReader = inFilterReader;
	}
}
