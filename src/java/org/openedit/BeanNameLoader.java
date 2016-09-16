package org.openedit;


import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.openedit.cache.CacheManager;
import org.openedit.page.Page;
import org.openedit.page.manage.PageManager;
import org.openedit.xml.XmlArchive;
import org.openedit.xml.XmlFile;

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
		String cacheid =  inCatalogId + inId;
		String  hit = (String)getCacheManager().get("beanNameloader", cacheid);
		if (hit != null)
		{
			return hit;
		}
		String name = legacyLoad(inCatalogId,inId);
		if( name == null)
		{
			name = inId;
		}
		getCacheManager().put("beanNameloader",cacheid, name);
		return name;
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
		
		if(!page.exists())
		{
			page = getPageManager().getPage("/system/configuration/beans.xml");
		}
		
		XmlFile file = getXmlArchive().getXml(page.getPath());
		Element field = file.getElementById(inBeanName);
		if( field != null)
		{
			//log.info("BEANS FOUND: " + field + ", with bean: " + field.attributeValue("bean"));
			beanName = field.attributeValue("bean");
		}
		else
		{
			String type = file.getRoot().attributeValue("basedatatype");
			if( type == null)
			{
				type = "elastic";
			}
			file = getXmlArchive().getXml("/system/configuration/" + type + ".xml");
			field = file.getElementById(inBeanName);
			if( field != null)
			{
				//log.info("BEANS FOUND: " + field + ", with bean: " + field.attributeValue("bean"));
				beanName = field.attributeValue("bean");
			}
		}
		
		return beanName;
	}	
	
	
}
