package org.openedit.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.openedit.ModuleManager;
import org.openedit.hittracker.SearchQuery;
import org.openedit.hittracker.Term;
import org.openedit.page.Page;
import org.openedit.page.manage.PageManager;
import org.openedit.users.User;
import org.openedit.users.UserManager;
import org.openedit.util.PathUtilities;
import org.openedit.xml.XmlArchive;
import org.openedit.xml.XmlFile;

public class SearchQueryArchive 
{
	private static final Log log = LogFactory.getLog(SearchQueryArchive.class);
	protected PageManager fieldPageManager;
	protected XmlArchive fieldXmlArchive;
	protected DateFormat fieldDateFormat = new SimpleDateFormat();
	protected SearcherManager fieldSearcherManager;

	public PageManager getPageManager() {
		return fieldPageManager;
	}

	public void setPageManager(PageManager inPageManager) {
		fieldPageManager = inPageManager;
	}

	public XmlArchive getXmlArchive() {
		return fieldXmlArchive;
	}

	public void setXmlArchive(XmlArchive inXmlArchive) {
		fieldXmlArchive = inXmlArchive;
	}

	public UserManager getUserManager() {
		return fieldUserManager;
	}

	public void setUserManager(UserManager inUserManager) {
		fieldUserManager = inUserManager;
	}

	public Map getCache() {
		return fieldCache;
	}

	public void setCache(Map inCache) {
		fieldCache = inCache;
	}

	public ModuleManager getModuleManager() {
		return fieldModuleManager;
	}

	public void setModuleManager(ModuleManager inModuleManager) {
		fieldModuleManager = inModuleManager;
	}

	protected UserManager fieldUserManager;
	protected Map fieldCache;
	protected ModuleManager fieldModuleManager;

	public void saveQuery(String inCatalogId, SearchQuery inQuery, String id, User inUser) {
		String path = "/WEB-INF/data/" + inCatalogId + "/savedqueries/" + id + ".xml";
		XmlFile file = getXmlArchive().getXml(path, "query");
		Element root = inQuery.toXml();
		Element inputs = root.addElement("inputs");
		for (Iterator iterator = inQuery.getProperties().keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			Collection values = inQuery.getInputs(key);
			Element input = inputs.addElement("input");
			input.addAttribute("key", key);
			if( values != null )
			{
				for (Iterator iterator2 = values.iterator(); iterator2.hasNext();)
				{
					String val = (String) iterator2.next();
					input.addElement("value").setText(val);					
				}
			}
		}
		file.setRoot(root);
		getXmlArchive().saveXml(file, inUser);

	}

	public SearcherManager getSearcherManager() {
		return fieldSearcherManager;
	}

	public void setSearcherManager(SearcherManager inSearcherManager) {
		fieldSearcherManager = inSearcherManager;
	}

	public SearchQuery loadQuery(String inCatalogId, String inSearchType, String inId, User inUser) throws Exception {
		// inQuery = the query to populate (of the appropriate type for the
		// search we're doing
		Searcher searcher = getSearcherManager().getSearcher(inCatalogId, inSearchType);
		String path = "/WEB-INF/data/" + inCatalogId + "/savedqueries/" + inId + ".xml";
		Page page = getPageManager().getPage(path);
		if(!page.exists())
		{
			path = "/" + inCatalogId + "/data/savedqueries/" + inId + ".xml";
		}
		XmlFile file = getXmlArchive().getXml(path, "query");
		Element root = file.getRoot();
		SearchQuery query = searcher.createSearchQuery();
		loadQuery(query, inCatalogId, inId, inUser, searcher, root);

		return query;

	}

	protected void loadQuery(SearchQuery query, String inCatalogId, String inId, User inUser, Searcher searcher, Element root) throws ParseException
	{
		String description = root.attributeValue("description");
		query.setDescription(description);
		String name = root.attributeValue("name");
		query.setName(name);
		query.setId(inId);
		String or = root.attributeValue("ortogether");
		if( Boolean.parseBoolean(or))
		{
			query.setAndTogether(false);
		}
		else
		{
			query.setAndTogether(true);
		}
		
		Element catalogs = root.element("catalogs");
		if (catalogs != null) {
			for (Iterator iterator = catalogs.elementIterator("catalog"); iterator.hasNext();) {
				Element cat = (Element) iterator.next();
				String catid = cat.attributeValue("id");
				query.addCatalog(catid);
			}
		}
		for (Iterator iterator = root.elementIterator("term"); iterator.hasNext();) {
			Element term = (Element) iterator.next();
			String op = term.attributeValue("op");
			String realop = term.attributeValue("realop");
			String val = term.attributeValue("val");
			String field = term.attributeValue("id");
			
			String catalogid = term.attributeValue("catalogid");
			if( catalogid == null)
			{
				catalogid = inCatalogId;
			}
			String view = term.attributeValue("view");
			String searchtype = term.attributeValue("searchtype");
			if( searchtype == null)
			{
				searchtype = "asset"; //Was not being set by albums
			}
				
			PropertyDetail detail = 
				getSearcherManager().getPropertyDetailsArchive(catalogid).getDataProperty(searchtype, view, field, inUser);
		
			if(detail == null)
			{
				detail = getSearcherManager().getSearcher(catalogid, searchtype).getDetail(field);
			}
			if(detail == null)
			{
				// create a virtual one?
				detail = new PropertyDetail();
				detail.setId(field);
				detail.setView(view);
				detail.setCatalogId(catalogid);
				detail.setSearchType(searchtype);
			//	continue;
			}
			
			
			query.setProperty(field, val);
			Term t = null;
			if ("matches".equals(op)) {
				t = query.addMatches(detail, val);
			} else if ("exact".equals(op)) {
				t = query.addExact(detail, val);
			} else if ("startswith".equals(op)) {
				t = query.addStartsWith(detail, val);
			} else if ("not".equals(op)) {
				t = query.addNot(detail, val);
			} else if ("afterdate".equals(op)) {
				Date after = query.getDateFormat().parse(val);
				query.setProperty("datedirection" + field, "after");
				t = query.addAfter(detail, after);
			} else if ("betweendates".equals(op)) {
				String low = term.attributeValue("afterDate");
				String high = term.attributeValue("beforeDate");
				Date lowdate = query.getDateFormat().parse(low);
				Date highdate = query.getDateFormat().parse(high);
				t = query.addBetween(detail, lowdate, highdate);
				query.setProperty(field + ".before", low);
				query.setProperty(field + ".after", high);
			} else if ("beforedate".equals(op)) {
				Date before = query.getDateFormat().parse(val);
				query.setProperty("datedirection" + field, "before");
				t = query.addBefore(detail, before);
			} else if ("orgroup".equals(op)) {
				t = query.addOrsGroup(detail, val);
			} else if ("notgroup".equals(op)) {
				t = query.addNots(detail, val);
			} else if ("categoryfilter".equals(op)) { //not used?
				List categories = new ArrayList();
				for (Iterator iterator2 = term.elementIterator("categories"); iterator2.hasNext();) {
					String category = (String) iterator2.next();
					categories.add(category);
				}
				query.addCategoryFilter(categories);
			}
			else if ("betweennumbers".equals(op)) 
			{
				String low = term.attributeValue("lowval");
				String high = term.attributeValue("highval");
				t = query.addBetween(detail, Long.parseLong(low), Long.parseLong( high));
			}
			else if ("greaterthannumber".equals(op)) 
			{
				t = query.addGreaterThan(detail, Long.parseLong(val));
			}
			else if ("lessthannumber".equals(op)) {
				t = query.addLessThan(detail, Long.parseLong(val));
			}
			else if ("equastonumber".equals(op)) {
				t = query.addExact(detail, Long.parseLong(val));
			}
			else
			{
				log.error("Operation not recognized: " + op);
			}
			if (t != null && realop != null)
			{
				t.addParameter("op", realop);
			}
		}
		int count = 0;
		for (Iterator iterator = root.elementIterator("query"); iterator.hasNext();)
		{
			Element	element = (Element) iterator.next();
			SearchQuery child = searcher.createSearchQuery();
			loadQuery(child, inCatalogId, inId + count++, inUser, searcher, element);
			query.addChildQuery(child);
		}
		readInputs(root.element("inputs"), query);
	}

	protected void readInputs(Element inInputs, SearchQuery inQuery)
	{
		if (inInputs == null)
		{
			return;
		}
		for(Iterator i = inInputs.elementIterator("input"); i.hasNext();)
		{
			Element input = (Element) i.next();
			String key = input.attributeValue("key");
			ArrayList valueList = new ArrayList();
			for(Iterator valiterator = input.elementIterator("value"); valiterator.hasNext();)
			{
				valueList.add(((Element)valiterator.next()).getText());
			}
			String[] values = new String[valueList.size()];
			valueList.toArray(values);
			inQuery.setPropertyValues(key, values);
		}
	}
	
	public void deleteQuery(String inCatalogId, String inId, User inUser) throws Exception {
		// inQuery = the query to populate (of the appropriate type for the
		// search we're doing

		String path = "/WEB-INF/data/" + inCatalogId + "/savedqueries/" + inId + ".xml";
		Page page = getPageManager().getPage(path);
		getPageManager().removePage(page);

	}

	public DateFormat getDateFormat() {
		return fieldDateFormat;
	}

	public void setDateFormat(DateFormat inDateFormat) {
		fieldDateFormat = inDateFormat;
	}

	public List loadSavedQueryList(String inCatalogId, String inFieldName, User inUser) throws Exception {
		String path = "/WEB-INF/data/" + inCatalogId + "/savedqueries/";
		Page rootfolder = getPageManager().getPage(path);
		List paths = getPageManager().getChildrenPaths(path);
		paths.addAll(getPageManager().getChildrenPaths("/" + inCatalogId + "/data/savedqueries/",true));
		List allQueries = new ArrayList();
		Set ids = new HashSet();
		for (Iterator iterator = paths.iterator(); iterator.hasNext();) 
		{
			String target = (String) iterator.next();
			if (target.endsWith(".xml")) {
				String id = PathUtilities.extractPageName(target);
				if( !ids.contains(id))
				{
					SearchQuery query = loadQuery(inCatalogId, inFieldName, id, inUser);
					allQueries.add(query);
					ids.add(id);
				}
			}
		}
		Collections.sort(allQueries);
		return allQueries;

	}

}
