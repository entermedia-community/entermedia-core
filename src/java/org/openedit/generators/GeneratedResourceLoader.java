package org.openedit.generators;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.util.ExtProperties;
import org.openedit.ModuleManager;
import org.openedit.OpenEditException;
import org.openedit.generators.filters.TextReaderFilter;
import org.openedit.generators.filters.TranslationFilter;
import org.openedit.generators.filters.XmlTranslationFilter;
import org.openedit.page.Page;
import org.openedit.page.manage.PageManager;
import org.openedit.servlet.OpenEditEngine;
import org.openedit.util.ReaderInputStream;

/**
 * We need to clean the paths all the time except when actually returning. Yes
 * This needs to adjust the inputstream as it is passed out
 * 
 * @author cburkey
 *
 */


public class GeneratedResourceLoader extends ResourceLoader
{
	protected PageManager fieldPageManager;
	protected ModuleManager fieldModuleManager;
	

	public ModuleManager getModuleManager() {
		if (fieldModuleManager == null)
		{
			OpenEditEngine engine = (OpenEditEngine) rsvc.getApplicationAttribute("openEditEngine");

			fieldModuleManager = engine.getModuleManager();
			
		}
		return fieldModuleManager;
	}

	public void setModuleManager(ModuleManager inModuleManager) {
		fieldModuleManager = inModuleManager;
	}

	/**
	 * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#init(org.apache.commons.collections.ExtendedProperties)
	 */
	public void init(ExtendedProperties configuration)
	{
		//if (log.isTraceEnabled())
		{
		//	log.trace("FileResourceLoader : initialization starting.");
		}
	}

	/**
	 * Get an InputStream so that the Runtime can build a template with it.
	 * @override
	 * 
	 * @param templateName
	 *            name of template to get
	 * @return InputStream containing the template
	 * @throws ResourceNotFoundException
	 *             if template not found in the file template path.
	 */
	public InputStream getResourceStream(String templateName) throws ResourceNotFoundException
	{
		//ok we need to know what filters are being used
		int index = templateName.indexOf("?filter="); // need the locale and filter type ?filter=translate&locale=en
		String path = templateName;
		if (index == -1)
		{
			Page page = getPageManager().getPage(path);
			return page.getInputStream();
		}
		path = templateName.substring(0,index);
		Page page = getPageManager().getPage(path);
		int amp = templateName.indexOf("&",index);
		if( amp == -1)
		{
			amp = templateName.length();
		}
		String filter = templateName.substring(index + "?filter=".length(),amp);
		if( filter.equals("translation")) //TODO: Use Spring. Check performance tho
		{
			TextReaderFilter reader = new TranslationFilter(page,templateName);
			return new ReaderInputStream(reader, page.getCharacterEncoding());
		} 
		else if(filter.equals("xmltranslation")) //this seems slow
		{
			XmlTranslationFilter reader = new XmlTranslationFilter(page,templateName);
			reader.setPageManager(getPageManager());
			reader.setModuleManager(getModuleManager());
			
			return new ReaderInputStream(reader, page.getCharacterEncoding());
		}
		//not supported
		throw new OpenEditException("filter "  + filter + " not supported");
	}

	/**
	 * Overrides superclass for better performance.
	 * 
	 * @since 1.6
	 */
	public boolean resourceExists(String name)
	{
		if (name == null)
		{
			return false;
		}
		name = clean(name);
		return getPageManager().getRepository().doesExist(name);
	}

	public String clean(String inPath)
	{
		int index = inPath.indexOf("?filter="); // need the locale and filter type ?filter=translate&locale=en
		String template = inPath;
		if (index != -1)
		{
			template = template.substring(0, index);
		}
		
		return template;
	}

	public boolean isSourceModified(Resource resource)
	{
		String path = resource.getName();
		path = clean(path);
		Page page = getPageManager().getPage(path);
		boolean modified = (page.lastModified() != resource.getLastModified());
		
		return modified;
	}

	/**
	 * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getLastModified(org.apache.velocity.runtime.resource.Resource)
	 */
	public long getLastModified(Resource resource)
	{
		String path = resource.getName();
		path = clean(path);
		Page page = getPageManager().getPage(path);
		return page.lastModified();
	}


	public PageManager getPageManager()
	{
		if (fieldPageManager == null)
		{
			OpenEditEngine engine = (OpenEditEngine) rsvc.getApplicationAttribute("openEditEngine");

			fieldPageManager = engine.getPageManager();
			
		}
		return fieldPageManager;
	}

	public void setPageManager(PageManager inPageManager)
	{
		fieldPageManager = inPageManager;
	}

	@Override
	public Reader getResourceReader(String name, String encoding) throws ResourceNotFoundException
	{
		// TODO Auto-generated method stub
		InputStream stream = getResourceStream(name);
		try
		{
			return buildReader(stream,encoding);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			throw new OpenEditException(e);
		}
	}

	@Override
	public void init(ExtProperties inArg0)
	{
		// TODO Auto-generated method stub
		
	}

}
