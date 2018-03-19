package org.openedit.xml;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.openedit.Shutdownable;
import org.openedit.cache.CacheManager;
import org.openedit.data.BaseData;
import org.openedit.data.BaseSearcher;
import org.openedit.data.PropertyDetail;
import org.openedit.data.PropertyDetails;
import org.openedit.hittracker.DataHitTracker;
import org.openedit.hittracker.HitTracker;
import org.openedit.hittracker.SearchQuery;
import org.openedit.hittracker.Term;
import org.openedit.repository.ContentItem;
import org.openedit.users.User;
import org.openedit.util.DateStorageUtil;
import org.openedit.util.PathUtilities;

public class XmlSearcher extends BaseSearcher implements Shutdownable
{
	protected XmlArchive fieldXmlArchive;
	private static final Log log = LogFactory.getLog(XmlSearcher.class);
	protected PropertyDetails fieldDefaultDetails;
	protected CacheManager fieldCacheManager;
	protected XmlFile fieldXmlFile;
	protected long fieldEditCount = 0;
	protected Boolean fieldLazyInit ;
	
	
	
	
	

	public boolean isLazyInit()
	{
		if (fieldLazyInit == null)
		{
			fieldLazyInit = getPropertyDetails().isLazyInit();
			if(fieldLazyInit == null){
				fieldLazyInit = false;
			}
			
		}

		return fieldLazyInit;
	}

	public void setLazyInit(boolean inLazyInit)
	{
		fieldLazyInit = inLazyInit;
	}

	public CacheManager getCacheManager()
	{
		if (fieldCacheManager == null)
		{
			fieldCacheManager = new CacheManager(); //TODO: make this shared across catalogs?
		}

		return fieldCacheManager;
	}

	public void setCacheManager(CacheManager inCache)
	{
		fieldCacheManager = inCache;
	}
	

	public Object searchById(String inId)
	{
	return searchById(inId, true );
	}
	
	
	public Object searchById(String inId, boolean inCache)
	{
		if (inId != null)
		{
			SearchQuery query = createSearchQuery();
			query.addMatches("id", inId);
			HitTracker hits = search(query);
			Object hit = hits.first();
			return hit;
		}
		return null;
	}
	

	protected String cacheId()
	{
		return getCatalogId() + getSearchType();
	}

	public void reIndexAll() throws OpenEditException
	{
		getCacheManager().clear(cacheId() );
		fieldXmlFile = null;
	}
	
	public boolean passes(Element inElement, SearchQuery inQuery) throws ParseException
	{
		for (Iterator iterator = inQuery.getTerms().iterator(); iterator.hasNext();) 
		{
			Term term = (Term) iterator.next();
			if("betweendates".equals(term.getOperation()))
			{
				Date before = inQuery.getDateFormat().parse(term.getParameter("lowDate"));
				Date after = inQuery.getDateFormat().parse(term.getParameter("highDate"));
				String id = term.getDetail().getId();//effectivedate
				String date = inElement.attributeValue(id);
				if(date == null)
				{
					return false;
				}
				Date target = getDefaultDateFormat().parse(date);
				if(!(before.before(target) && after.after(target)))
				{
					return false;
				}
			} 
			else if("afterdate".equals(term.getOperation()))
			{
				Date after = inQuery.getDateFormat().parse(term.getParameter("highDate"));
				String id = term.getDetail().getId();//effectivedate
				String date = inElement.attributeValue(id);
				if(date == null)
				{
					return false;
				}
				Date target = getDefaultDateFormat().parse(date);
				if(!target.after(after))
				{
					return false;
				}
			}
			else if("beforedate".equals(term.getOperation()))
			{
				String low = term.getParameter("beforeDate");
				Date before = null;
				if( low != null)
				{
					before = inQuery.getDateFormat().parse(low);					
				}
				else
				{
					Object[] values = term.getValues();
					if( values != null && values.length > 0)
					{
						before = (Date)values[0];						
					}
				}
				String id = term.getDetail().getId();//effectivedate
				String date = inElement.attributeValue(id);
				if(date == null)
				{
					return false;
				}
				Date target = DateStorageUtil.getStorageUtil().parseFromStorage(date);
				
				if(before == null || !target.before(before))
				{
					return false;
				}
				 
			}
			else if("orgroup".equals(term.getOperation()))
			{
				String value = term.getValue();
				Object[] values = term.getValues();
				
				String attribval = inElement.attributeValue(term.getDetail().getId());
				
				if(value != null && (attribval == null || !value.contains(attribval)) )
				{
					return false;
				}
				if(values != null){
					boolean foundmatch = false;
					for (int i = 0; i < values.length; i++) {
						String val = (String) values[i];
						if(attribval != null && val.contains(attribval)){
							foundmatch =true;
						}
					}
					if(!foundmatch){
						return false;
					}
				}
			}
			else
			{
				String name = term.getDetail().getId();
				String value = term.getValue();
				if (name == null)
				{
					name = "id";
				}
				else if( name.equals("description") )
				{
					if( value != null && "*".equals(value))
					{
						return true;
					}
					name = "name"; //This is temporary until we support isKeyword
				}
				
				if( value == null && term.getDetail().isBoolean() )
				{
					value = "false";
				}
				if( value != null)
				{
					value = value.toLowerCase();
				}
				String attribval = null;
				if( "name".equals(name))
				{
					attribval = inElement.getTextTrim();
					if(attribval == null || attribval.length()==0){
						attribval = inElement.attributeValue("name");
						if(attribval != null){
							attribval = attribval.trim();
						}
					}
				}
				else
				{
					attribval = inElement.attributeValue(name);
				}
				if( attribval != null )
				{
					attribval = attribval.toLowerCase();
				}
				
				if( attribval == null && term.getDetail().isBoolean() )
				{
					attribval = "false";
				}				
				if( "not".equals( term.getOperation() ) )
				{
					if (value != null && attribval != null && ("*".equals(value) || doesMatch(term.getOperation(),attribval,value) ) )
					{
						return false;
					}
				}
				else
				{
					if (value != null && attribval != null && ("*".equals(value) || doesMatch(term.getOperation(),attribval,value) ) )
					{
						if (!inQuery.isAndTogether())
						{
							return true;
						}
					}
					else if (inQuery.isAndTogether())
					{
						return false;
					}
				}
			}
		}
		if(inQuery.isAndTogether())
		{
			return true;
		}
		return false;
	}
	
	protected boolean doesMatch(String inOperation, String inAttribval, String inValue)
	{
		if( "contains".equals(inOperation) )
		{
			return inAttribval.contains(inValue);
		}
		if(inAttribval.contains("|")){
			String[] vals = BaseData.VALUEDELMITER.split(inAttribval);
			for (int i = 0; i < vals.length; i++)
			{
				String val = vals[i];
				if(PathUtilities.match(inValue, val)){
					return true;
				}
				
			}
		}
		
		return	PathUtilities.match(inAttribval.toLowerCase(), inValue);
	}

	public String getIndexId()
	{
		XmlFile settings = getXmlFile();
		return getSearchType() + settings.getLastModified() + fieldEditCount;
	}
	/**
	 * Because of the way SearchQuery is coded, we can't get to the operation information.
	 * So, this only supports exact matching.
	 */
	public HitTracker search(SearchQuery inQuery) 
	{
		if( inQuery == null)
		{
			return null;
		}
		HitTracker hits = (HitTracker) getCacheManager().get(cacheId(), inQuery.toQuery() + inQuery.getSortBy());
		if(hits != null)
		{
			if( log.isDebugEnabled() )
			{
				log.debug("Cached search " + getSearchType() + " " + inQuery.toQuery() + " (sorted by " + inQuery.getSortBy() + ") found " + hits.size() + " in " + getCatalogId());
			}
			return hits;
		}
		XmlFile settings = getXmlFile(); 
		
		List results = new ArrayList();
		
		if (settings.isExist()) 
		{
			for (Iterator iterator = settings.getElements().iterator(); iterator.hasNext();) 
			{
				Element element = (Element) iterator.next();
				try
				{
					if (passes(element, inQuery))
					{
						//log.info(element.asXML());
						results.add(new ElementData(element, getPropertyDetails()));					
					}
				}
				catch (ParseException e)
				{
					throw new OpenEditRuntimeException(e);
				}
			}
		}
		else
		{
			log.info("Xml does not exist " + settings.getPath());
		}
		
		sortResults(inQuery, results);
		
		hits = new DataHitTracker();
		hits.setSearcher(this);
		hits.setSearchQuery(inQuery);
		hits.setIndexId(getSearchType() + settings.getLastModified());
		hits.addAll(results);
//		if( getCache().size() > 500)
//		{
//			clearIndex();
//		}
		getCacheManager().put(cacheId(),inQuery.toQuery() + inQuery.getSortBy(), hits);
		//if( log.isDebugEnabled() )
		{
			log.debug("Search " + getSearchType() + " " + inQuery.toQuery() + " (sorted by " + inQuery.getSortBy() + ") found " + hits.size());
		}

		return hits;
	}

	private void sortResults(SearchQuery inQuery, List results)
	{
		final List sorts = inQuery.getSorts();
		if (!sorts.isEmpty() )
		{
			ElementSorter sorter = new ElementSorter(sorts);
			Collections.sort(results,sorter);
			
		}
	}
	
	protected XmlFile getXmlFile()
	{
		if( fieldXmlFile == null )
		{
			synchronized (this)
			{
				if( fieldXmlFile == null )
				{
					fieldXmlFile = loadXmlFile();
				}
			}
		}
		return fieldXmlFile;
	}
	public void setXmlFile(XmlFile inFile)
	{
		fieldXmlFile = inFile;
	}
	protected XmlFile loadXmlFile()
	{
		try
		{
			String inName = getSearchType();
			String path = getPropertyDetailsArchive().findConfigurationFile("/lists"
				+ "/" + inName + ".xml");
			//No sure why we do this. 
			PropertyDetails details = getPropertyDetailsArchive().getPropertyDetailsCached(inName);
			if( details ==  null)
			{
				inName = "property";
			}
			inName = inName.replace('/','_'); //if we load up foldername/name, we can't have a slash in the xml
			XmlFile settings = getXmlArchive().getXml(path,path,inName);
			return settings;
		} catch ( OpenEditException ex)
		{
			throw new OpenEditRuntimeException(ex);
		}
	}

	public XmlArchive getXmlArchive()
	{
		return fieldXmlArchive;
	}

	public void setXmlArchive(XmlArchive inXmlArchive)
	{
		fieldXmlArchive = inXmlArchive;
	}

	public SearchQuery createSearchQuery()
	{
		SearchQuery query = new SearchQuery();
		query.setPropertyDetails(getPropertyDetails());
		query.setCatalogId(getCatalogId());
		query.setSearcherManager(getSearcherManager());
		query.setResultType(getSearchType());
		return query;
	}

	public void saveData(Data inData, User inUser)
	{
		//If this element is manipulated then the instance is the same
		//No need to read it ElementData data = (ElementData)inData;
		XmlFile settings = getXmlFile();
		String path = "/WEB-INF/data/" + getCatalogId() + "/lists" + "/" + getSearchType() + ".xml";

		settings.setPath(path);

		Element element = null;
		if( inData.getId() == null)
		{
			inData.setId( String.valueOf( new Date().getTime() ));
		}
		else
		{
			element = settings.getElementById(inData.getId());
		}
		if( element == null )
		{
			//New element
			element = settings.getRoot().addElement(settings.getElementName());
			element.addAttribute("id", inData.getId());
		}
		if( inData instanceof ElementData)
		{
			ElementData data = (ElementData)inData;
			List attributes = data.getElement().attributes();
			element.setAttributes(attributes);
			//element.setText(inData.getName());
			//existing row exists
			element.setContent(data.getElement().content());
		}
		else
		{
			element.clearContent();
			element.setAttributes(null);
			
			ElementData data = new ElementData(element, getPropertyDetails());
			data.setId(inData.getId());
			data.setName(inData.getName());
			data.setSourcePath(inData.getSourcePath());
			for (Iterator iterator = inData.keySet().iterator(); iterator.hasNext();)
			{
				String key	= (String) iterator.next();
				data.setValue(key, inData.getValue(key));
			}
		}
		
		log.info("Saved to "  + settings.getPath());
		getXmlArchive().saveXml(settings, inUser);
		
		clearIndex();
		
	}
	
	public void saveAllData(Collection inAll, User inUser){
		String path = "/WEB-INF/data/" + getCatalogId() + "/lists"
				+ "/" + getSearchType() + ".xml";
		saveAllData(inAll, inUser, path);
	}
	
	public void saveAllData(Collection inAll, User inUser, String path)
	{
		XmlFile settings = getXmlFile();
		
		settings.setPath(path);
		for (Iterator iterator = inAll.iterator(); iterator.hasNext();)
		{
			Data data = (Data) iterator.next();
			if( data.getId() == null)
			{
				//TODO: Use counter
				data.setId( String.valueOf( new Date().getTime() ));
			}
			if( data instanceof ElementData)
			{
				ElementData edata = (ElementData)data;
				if( edata.getElement().getParent() == null)
				{
					settings.getRoot().add(edata.getElement());
					continue;
				}
				if( edata.getElement().getParent() == settings.getRoot())
				{
					continue;
				}
			}
			Element contained = settings.getRoot().elementByID(data.getId()); //This only works for upper case ID
			if( contained == null )
			{
				Element newone = settings.addNewElement();
				List details = getProperties();

				for (Iterator iterator2 = details.iterator(); iterator2.hasNext();)
				{
					PropertyDetail field = (PropertyDetail) iterator2.next();
					String value = data.get(field.getId());
					if( "name".equals(field.getId()) )
					{
						newone.setText(value);
					}
					else
					{
						newone.addAttribute(field.getId(), value);
					}
				}	
			}
		}
		clearIndex();
		log.info("Saved to "  + settings.getPath());
		getXmlArchive().saveXml(settings, inUser);
	}

	
	public Data createNewData()
	{
		XmlFile settings = getXmlFile();

		Element newone = DocumentHelper.createElement(settings.getElementName());
		ElementData data = new ElementData(newone, getPropertyDetails());		
		return data;
	}
	public List getIndexProperties()
	{
		PropertyDetails details = getPropertyDetailsArchive().getPropertyDetailsCached(getSearchType());
		if( details == null || details.size() == 0)
		{
			return getDefaultDetails().findIndexProperties();
		}
		return details.findIndexProperties();
	}
	
	
	public List getSearchProperties(User inUser)
	{
		List details = getDetailsForView(getSearchType() + "/" + getSearchType() + "search", inUser);
		if (details == null || details.size() == 0)
		{
			return getDefaultDetails().findIndexProperties();
		}
		return details;
	}
	
	
	public List getProperties()
	{
		PropertyDetails details = getPropertyDetailsArchive().getPropertyDetailsCached(getSearchType());
		if( details == null || details.size() == 0)
		{
			return getDefaultDetails().getDetails();
		}
		return details.getDetails();
	}

	
	public PropertyDetails getPropertyDetails() {
		
		PropertyDetails details = getPropertyDetailsArchive().getPropertyDetailsCached(getSearchType());
		if( details == null || details.size() == 0)
		{
			return getDefaultDetails();
		}
		return details;
	}
	public PropertyDetails getDefaultDetails()
	{
		if( fieldDefaultDetails == null)
		{
			//fake one
			PropertyDetails details = new PropertyDetails(getPropertyDetailsArchive(),getSearchType());
			PropertyDetail id = new PropertyDetail();
			id.setIndex(true);
			id.setStored(true);
			id.setName("Id");
			id.setId("id");
			id.setEditable(true);
			id.setIndex(true);
			id.setStored(true);
			
			details.addDetail(id);

			id = new PropertyDetail();
			id.setIndex(true);
			id.setStored(true);
			id.setName("Name");
			id.setId("name");
			id.setEditable(true);
			details.addDetail(id);
			
			fieldDefaultDetails = details;

		}
		return fieldDefaultDetails;
	}

	public void deleteAll(User inUser)
	{
		String path = "/WEB-INF/data/" + getCatalogId() + "/lists" + "/" + getSearchType() + ".xml";
		ContentItem item = getXmlArchive().getPageManager().getRepository().getStub(path);
		getXmlArchive().getPageManager().getRepository().remove(item);
		clearIndex();
	}
	public void delete(Data inData, User inUser)
	{
		XmlFile settings = getXmlFile();
		//TODO: Save this file to the data directory not the app
		String path = "/WEB-INF/data/" + getCatalogId() + "/lists/" + getSearchType() + ".xml";
		settings.setPath(path);
		
		Element record = settings.getElementById(inData.getId());
		if( record != null)
		{
			settings.getRoot().remove(record);
			getXmlArchive().saveXml(settings, inUser);
		}
		clearIndex();
	}

	public void clearIndex()
	{
		fieldEditCount++;
		synchronized (this)
		{
			fieldXmlFile = null; //reload it each time?
			getCacheManager().clear(cacheId());
		}
	}
	public void shutdown()
	{
		clearIndex();
	}
	
	
	
	
}
	
	