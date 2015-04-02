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

import com.openedit.OpenEditException;
import com.openedit.OpenEditRuntimeException;
import com.openedit.hittracker.HitTracker;
import com.openedit.page.Page;
import com.openedit.page.manage.PageManager;
import com.openedit.users.User;

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
			
			String rootpath = "/WEB-INF/data/" + getCatalogId() + "/lists/" + inName ;
			composite.setPath(rootpath + "/custom.xml");
			
			Element root = DocumentHelper.createElement(inName);
			composite.setRoot(root);

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
				boolean existing = children.size() > 0;
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
				return element;
			}
		}
		return null;
	}

	public void deleteAll(User inUser)
	{
		String path = "/WEB-INF/data/" + getCatalogId() + "/lists"
				+ "/" + getSearchType() + "/custom.xml";
		Page page = getPageManager().getPage(path);
		getPageManager().removePage(page);
		clearIndex();
		HitTracker all = getAllHits();
		for (Iterator iterator = all.iterator(); iterator.hasNext();)
		{
			Data data = (Data)iterator.next();
			delete(data,inUser);
		}

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
		+ "/" + getSearchType() + "/custom.xml";
		XmlFile settings = getXmlArchive().getXml(path);
		ElementData data = (ElementData)inData;
		
		Element alreadyhere = settings.getElementById(data.getId());
		if( alreadyhere != null)
		{
			settings.getRoot().remove(alreadyhere);
		}
		data.getElement().setParent(null);
		settings.getRoot().add(data.getElement());
		
		if( data.getId() == null)
		{
			//TODO: Use counter
			data.setId( String.valueOf( new Date().getTime() ));
		}
		clearIndex();
		log.info("Saved to "  + settings.getPath());
		getXmlArchive().saveXml(settings, inUser);
		
	}
	
	public void saveAllData(Collection inAll, User inUser, String path)
	{
		for (Iterator iterator = inAll.iterator(); iterator.hasNext();)
		{
			Data data = (Data) iterator.next();
			saveData(data,inUser,false);
		}
	}	
	
}
