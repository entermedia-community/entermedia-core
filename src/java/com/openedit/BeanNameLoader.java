package com.openedit;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.entermedia.cache.CacheManager;
import org.openedit.xml.XmlArchive;
import org.openedit.xml.XmlFile;

import com.openedit.hittracker.HitTracker;
import com.openedit.hittracker.SearchQuery;
import com.openedit.page.Page;
import com.openedit.page.PageSettings;
import com.openedit.page.manage.PageManager;
import com.openedit.util.PathUtilities;

public class BeanNameLoader
{
	private static final Log log = LogFactory.getLog(BeanNameLoader.class);
	protected PageManager fieldPageManager;
	protected CacheManager fieldCacheManager;
	protected XmlArchive fieldXmlArchive;
	
	public CacheManager getCacheManager()
	{
		return fieldCacheManager;
	}

	public void setCacheManager(CacheManager inCacheManager)
	{
		fieldCacheManager = inCacheManager;
	}

	public XmlArchive getXmlArchive()
	{
		return fieldXmlArchive;
	}

	public void setXmlArchive(XmlArchive inXmlArchive)
	{
		fieldXmlArchive = inXmlArchive;
	}

	public PageManager getPageManager()
	{
		return fieldPageManager;
	}

	public void setPageManager(PageManager inPageManager)
	{
		fieldPageManager = inPageManager;
	}
	
	public String findName(String inCatalogId, String inId)
	{
		String cacheid = "beanLoader" + inCatalogId;
		String  hit = (String)getCacheManager().get(cacheid, inId);
		if (hit != null)
		{
			//return hit;
		}
		String name = null;
		
//		XmlFile files = loadXmlFile(inCatalogId); //Never cached
//		Element found = files.getElementById(inId);
//		if( found != null)
//		{
//			name = found.attributeValue("bean");
//		}
//		if( name == null)
//		{
			name = legacyLoad(inCatalogId,inId);
//		}
		if( name == null)
		{
			name = inId;
		}
		getCacheManager().put(cacheid,inId, name);
		return name;
	}
	
	protected XmlFile loadXmlFile(String inCatalogId)
	{
		try
		{
			String inName = "bean";
			//getConfigurationPath
			XmlFile composite = new XmlFile();
			
			String rootpath = "/WEB-INF/data/" + inCatalogId + "/lists/" + inName ;
			composite.setPath(rootpath + "/custom.xml");

			long inLastModified = getPageManager().getRepository().getStub(composite.getPath()).lastModified().getTime();
			composite.setLastModified(inLastModified);
			
			Element root = DocumentHelper.createElement(inName);
			composite.setRoot(root);

			List<String> children = getPageManager().getChildrenPaths(rootpath,false);
			if( children.size() > 0)
			{
				composite.setExist(true);
				loadChildren(inName, children, root, true);
			}
			List<String> children2 = getPageManager().getChildrenPaths("/" + inCatalogId + "/data" + "/lists/" + inName + "/",true);
			if( children2.size() > 0)
			{
				composite.setExist(true);
				boolean existing = children.size() > 0;
				loadChildren(inName, children2, root, true);
			}
//			HitTracker hits = getSearcherManager().getList(
//					inCatalogId, "dataextensions");
//			for (Iterator iterator = hits.iterator(); iterator
//					.hasNext();) {
//				Data hit = (Data) iterator.next();
//				String catalogid = hit.get("catalogid");
//
//				List<String> children3 = getPageManager().getChildrenPaths("/" + catalogid + "/data" + "/lists/" + inName + "/",true);
//				if( children3.size() > 0)
//				{
//					composite.setExist(true);
//					boolean existing = children2.size() > 0;
//					if( !existing )
//					{
//						existing = children.size() > 0;
//					}
//					loadChildren(inName, children3, root, existing);
//				}
//				
//			}
			
			//remove deleted
			for (Iterator iterator = new ArrayList(composite.getElements()).iterator(); iterator.hasNext();)
			{
				Element element = (Element) iterator.next();
				if ( "deleted".equals( element.attributeValue("recordstatus") ) )
				{
					composite.getRoot().remove(element);
				}
			}
			
			return composite;
		} catch ( OpenEditException ex)
		{
			throw new OpenEditRuntimeException(ex);
		}
	}

	protected void loadChildren(String inName, List<String> children, Element root, boolean noDups) 
	{
		for(String child:children)
		{
			if(child.endsWith(".xml"))
			{
				XmlFile settings = getXmlArchive().getXml(child,child,inName);
				for (Iterator iterator = settings.getRoot().elementIterator(); iterator
						.hasNext();) 
				{
					Element row = (Element) iterator.next();
					Element existing = getElementById(root,row.attributeValue("id"));
					if( !noDups || existing == null )
					{
						row.setParent(null);
						root.add(row);
					}
				}
			}
		}
	}
	protected Element getElementById(Element root, String inEid)
	{
		if( inEid == null)
		{
			return null;
		}
		for (Iterator iter = root.elementIterator(); iter.hasNext();)
		{
			Element element = (Element) iter.next();
			String id = element.attributeValue("id");
			if ( inEid.equals(id))
			{
				return element;
			}
		}
		return null;
	}


	protected String legacyLoad(String inCatalogId, String inBeanName)
	{
		String beanName = null;
	
		String parentlocation = "/" + inCatalogId + "/configuration/beans.xml";
		Page page = getPageManager().getPage(parentlocation);
		
		if(page.exists())
		{
			XmlFile file = getXmlArchive().getXml(page.getPath());
			Element field = file.getElementById(inBeanName);
			if( field != null)
			{
				//log.info("BEANS FOUND: " + field + ", with bean: " + field.attributeValue("bean"));
				beanName = field.attributeValue("bean");
			}
		}
		return beanName;
	}	
	
	
}
