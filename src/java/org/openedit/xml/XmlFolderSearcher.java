package org.openedit.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.openedit.Data;
import org.openedit.OpenEditException;
import org.openedit.OpenEditRuntimeException;
import org.openedit.hittracker.HitTracker;
import org.openedit.page.Page;
import org.openedit.page.manage.PageManager;
import org.openedit.users.User;

public class XmlFolderSearcher extends XmlSearcher
{
	private static final Log log = LogFactory.getLog(XmlFolderSearcher.class);
	protected PageManager fieldPageManager;
	
	public PageManager getPageManager()
	{
		return fieldPageManager;
	}

	public void setPageManager(PageManager inPageManager)
	{
		fieldPageManager = inPageManager;
	}
	
	protected XmlFile loadXmlFile()
	{
		try
		{
			String inName = getSearchType();
			//getConfigurationPath
			XmlFile composite = new XmlFile();
			
			String rootpath = "/WEB-INF/data/" + getCatalogId() + "/lists/";
			composite.setPath(rootpath + inName + ".xml");

			long inLastModified = getPageManager().getRepository().getStub(composite.getPath()).lastModified().getTime();
			composite.setLastModified(inLastModified);
			
			Element root = DocumentHelper.createElement(inName);
			composite.setRoot(root);

			XmlFile parent = super.loadXmlFile();
			if( parent.isExist())
			{
				for (Iterator iterator = parent.getRoot().elementIterator(); iterator
						.hasNext();) 
				{
					Element row = (Element) iterator.next();
					row.setParent(null);
					root.add(row);
				}
			}
			List<String> children = getPageManager().getChildrenPaths(rootpath,false);
			if( children.size() > 0)
			{
				composite.setExist(true);
				loadChildren(inName, children, root, true);
			}
			List<String> children2 = getPageManager().getChildrenPaths("/" + getCatalogId() + "/data" + "/lists/" + inName + "/",true);
			if( children2.size() > 0)
			{
				composite.setExist(true);
				loadChildren(inName, children2, root, true);
			}
//			HitTracker hits = getSearcherManager().getList(
//					getCatalogId(), "dataextensions");
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
				String deleted = element.attributeValue("recordstatus");
				if( !Boolean.parseBoolean(deleted))
				{
					return element;
				}
			}
		}
		return null;
	}

	public void deleteAll(User inUser)
	{
//		String path = "/WEB-INF/data/" + getCatalogId() + "/lists"
//				+ "/" + getSearchType() + "/custom.xml";
//		Page page = getPageManager().getPage(path);
//		getPageManager().removePage(page);
		HitTracker all = getAllHits();
		for (Iterator iterator = all.iterator(); iterator.hasNext();)
		{
			Data data = (Data)iterator.next();
			delete(data,inUser);
		}
		clearIndex();
		fieldXmlFile = null; //reload
		getXmlFile();

	}
	
	public void delete(Data inData, User inUser)
	{
		saveData(inData,inUser,true);
	}
	public void saveData(Data inData, User inUser)
	{
		saveData(inData,inUser,false);
	}	
	public void saveData(Data inData, User inUser, boolean delete)
	{
		if( delete )
		{
			inData.setProperty("recordstatus", "deleted");
		}
		else
		{
			inData.setProperty("recordstatus", null);
		}

		//If this element is manipulated then the instance is the same
		//No need to read it ElementData data = (ElementData)inData;
		String path = "/WEB-INF/data/" + getCatalogId() + "/lists"
		+ "/" + getSearchType() + ".xml";
		XmlFile settings = getXmlArchive().getXml(path);

		
		updateOrAddElement(settings, inData);

		
		clearIndex();
		log.info("Saved to "  + settings.getPath());
		getXmlArchive().saveXml(settings, inUser);
		
	}

	public void saveAllData(Collection inAll, User inUser){
		String path = "/WEB-INF/data/" + getCatalogId() + "/lists"
				+ "/" + getSearchType() + ".xml";
		saveAllData(inAll, inUser, path);
	}

	public void saveAllData(Collection inAll, User inUser, String path)
	{
		//If this element is manipulated then the instance is the same
		//No need to read it ElementData data = (ElementData)inData;
		XmlFile settings = getXmlArchive().getXml(path);

		for (Iterator iterator = inAll.iterator(); iterator.hasNext();)
		{
			Data data = (Data) iterator.next();
			data.setProperty("recordstatus", null);
			updateOrAddElement(settings, data);
		}
		
		
		clearIndex();
		log.info("Saved to "  + settings.getPath());
		getXmlArchive().saveXml(settings, inUser);
	}	
	
}
