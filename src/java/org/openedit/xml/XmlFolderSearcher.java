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
			String path = getPropertyDetailsArchive().getConfigurationPath("/lists"	+ "/" + inName + "/" );
			
			XmlFile composite = new XmlFile();
			composite.setPath(path);
			Element root = DocumentHelper.createElement(inName);
			composite.setRoot(root);

			List<String> children = getPageManager().getChildrenPaths(path,true);
			if( children.size() > 0)
			{
				composite.setExist(true);
				for(String child:children)
				{
					XmlFile settings = getXmlArchive().getXml(child,child,inName);
					for (Iterator iterator = settings.getRoot().elementIterator(); iterator
							.hasNext();) {
						Element row = (Element) iterator.next();
						row.setParent(null);
						root.add(row);
					}
				}
			}
			return composite;
		} catch ( OpenEditException ex)
		{
			throw new OpenEditRuntimeException(ex);
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
