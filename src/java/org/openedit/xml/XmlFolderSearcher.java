package org.openedit.xml;

import java.util.Iterator;
import java.util.List;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.openedit.OpenEditException;
import com.openedit.OpenEditRuntimeException;
import com.openedit.hittracker.HitTracker;
import com.openedit.hittracker.SearchQuery;
import com.openedit.page.manage.PageManager;

public class XmlFolderSearcher extends XmlSearcher
{
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
				if( !noDups || settings.getElementById(row.attributeValue("id")) == null)
				{
					row.setParent(null);
					root.add(row);
				}
			}
		}
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
}
