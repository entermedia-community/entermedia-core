package org.openedit.xml;

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
import com.openedit.hittracker.SearchQuery;
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

	protected XmlFile loadXml()
	{
		try
		{
			String inName = getSearchType();
			//getConfigurationPath
			XmlFile composite = new XmlFile();
			composite.setPath("/WEB-INF/data/" + getCatalogId() + "/lists/" + inName);
			Element root = DocumentHelper.createElement(inName);
			composite.setRoot(root);

			List<String> children = getPageManager().getChildrenPaths(composite.getPath(),false);
			if( children.size() > 0)
			{
				composite.setExist(true);
				loadChildren(inName, children, root, false);
			}
			List<String> children2 = getPageManager().getChildrenPaths("/" + getCatalogId() + "/data" + "/lists/" + inName,true);
			if( children2.size() > 0)
			{
				composite.setExist(true);
				boolean existing = children.size() > 0;
				loadChildren(inName, children2, root, existing);
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
			XmlFile settings = getXmlArchive().getXml(child,child,inName);
			for (Iterator iterator = settings.getRoot().elementIterator(); iterator
					.hasNext();) 
			{
				Element row = (Element) iterator.next();
				if( !noDups || getElementById(root,row.attributeValue("id")) == null)
				{
					row.setParent(null);
					root.add(row);
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
	
	public HitTracker search(SearchQuery inQuery)
	{
		HitTracker hits = (HitTracker) getCache().get(inQuery.toQuery() + inQuery.getSortBy());
		if(hits != null)
		{
			return hits;
		}

		return super.search(inQuery);
	}
	
	public void saveData(Data inData, User inUser)
	{
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
	
}
