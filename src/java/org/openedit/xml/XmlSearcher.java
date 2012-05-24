package org.openedit.xml;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.openedit.Data;
import org.openedit.data.BaseSearcher;
import org.openedit.data.PropertyDetail;
import org.openedit.data.PropertyDetails;
import org.openedit.util.DateStorageUtil;

import com.openedit.OpenEditException;
import com.openedit.OpenEditRuntimeException;
import com.openedit.WebPageRequest;
import com.openedit.hittracker.DataHitTracker;
import com.openedit.hittracker.HitTracker;
import com.openedit.hittracker.SearchQuery;
import com.openedit.hittracker.Term;
import com.openedit.users.User;
import com.openedit.util.PathUtilities;

public class XmlSearcher extends BaseSearcher
{
	protected XmlArchive fieldXmlArchive;
	private static final Log log = LogFactory.getLog(XmlSearcher.class);
	protected PropertyDetails fieldDefaultDetails;
	protected Map fieldCache;
	protected Map fieldIdcache;
	protected long fieldLastModified = 0;
	
	public long getLastModified()
	{
		return fieldLastModified;
	}

	public void setLastModified(long inLastModified)
	{
		fieldLastModified = inLastModified;
	}

	public Map getCache()
	{
		if (fieldCache == null)
		{
			fieldCache = new HashMap();
			
		}

		return fieldCache;
	}

	public void setCache(Map inCache)
	{
		fieldCache = inCache;
	}
	

	public Object searchById(String inId)
	{
		if (inId != null)
		{
			Object hit = getIdCache().get(inId);
			if (hit != null)
			{
				return hit;
			}
			SearchQuery query = createSearchQuery();
			query.addMatches("id", inId);
			//TODO: Search for only one then stop
			HitTracker hits = search(query);
			if (hits.size() > 0)
			{
				hit = hits.get(0);
				if(getIdCache().size()>100)
				{
					getIdCache().clear();
				}
				getIdCache().put(inId, hit);
				return hit;
			}
		}
		return null;
	}

	public HitTracker getAllHits(WebPageRequest inReq) 
	{
		SearchQuery query = createSearchQuery();
		query.addMatches("id","*");
		query.setCatalogId(getCatalogId());
		HitTracker tracker = search(query);
		if (inReq != null)
		{
			String hitsname = inReq.findValue("hitsname");
			if (hitsname != null)
			{
				query.setHitsName(hitsname);
			} 
			inReq.putSessionValue(tracker.getSessionId(), tracker);
			inReq.putPageValue(tracker.getHitsName(), tracker);
		}
		return tracker;
	}

	public void reIndexAll() throws OpenEditException
	{
		
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
				String low = term.getParameter("lowDate");
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
			else
			{
				String name = term.getDetail().getId();
				if (name == null)
				{
					name = "id";
				}
				String value = term.getValue();
				if( value != null)
				{
					value = value.toLowerCase();
				}
				String attribval = null;
				if( "name".equals(name))
				{
					attribval = inElement.getTextTrim();
				}
				else
				{
					attribval = inElement.attributeValue(name);
				}
				
				if (attribval !=null &&(value.equals("*") || PathUtilities.match(attribval.toLowerCase(), value)))
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
		if(inQuery.isAndTogether())
		{
			return true;
		}
		return false;
	}
	
	public String getIndexId()
	{
		XmlFile settings = loadXml();
		return getSearchType() + settings.getLastModified();
	}
	/**
	 * Because of the way SearchQuery is coded, we can't get to the operation information.
	 * So, this only supports exact matching.
	 */
	public HitTracker search(SearchQuery inQuery) 
	{
		XmlFile settings = loadXml();
		checkCache(settings.getLastModified());

		HitTracker hits = (HitTracker) getCache().get(inQuery.toQuery() + inQuery.getSortBy());
		if(hits != null)
		{
			if( log.isDebugEnabled() )
			{
				log.debug("Cached search " + getSearchType() + " " + inQuery.toQuery() + " (sorted by " + inQuery.getSortBy() + ") found " + hits.size() + " in " + getCatalogId());
			}
			return hits;
		}
		
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
						results.add(new ElementData(element));					
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
		hits.setSearchQuery(inQuery);
		hits.setIndexId(getSearchType() + settings.getLastModified());
		hits.addAll(results);
		//checkCache(settings.getLastModified());
		getCache().put(inQuery.toQuery() + inQuery.getSortBy(), hits);
		if( log.isDebugEnabled() )
		{
			log.debug("Search " + getSearchType() + " " + inQuery.toQuery() + " (sorted by " + inQuery.getSortBy() + ") found " + hits.size());
		}

		return hits;
	}

	protected void checkCache(long fileModDate)
	{
		if(fileModDate > getLastModified() )
		{
			getCache().clear();
			setLastModified(fileModDate);
		} 
		else if( getCache().size() > 1000)
		{
			getCache().clear();
			setLastModified(fileModDate);
		}
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
	
	protected XmlFile loadXml()
	{
		try
		{
			String inName = getSearchType();
			String path = getPropertyDetailsArchive().getConfigurationPath("/lists"
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
		XmlFile settings = loadXml();
		String path = "/WEB-INF/data/" + getCatalogId() + "/lists"
		+ "/" + getSearchType() + ".xml";

		settings.setPath(path);
		ElementData data = (ElementData)inData;
		if( data.getElement().getParent() == null)
		{
			settings.getRoot().add(data.getElement());
			//getdata.getElement();
		}
		if( data.getId() == null)
		{
			//TODO: Use counter
			data.setId( String.valueOf( new Date().getTime() ));
		}
		clearIndex();
		log.info("Saved to "  + settings.getPath());
		getXmlArchive().saveXml(settings, inUser);
		
	}
	
	public void saveAllData(Collection inAll, User inUser)
	{
		XmlFile settings = loadXml();
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
		XmlFile settings = loadXml();

		Element newone = DocumentHelper.createElement(settings.getElementName());
		ElementData data = new ElementData(newone);		
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
	public List getStoredProperties()
	{
		PropertyDetails details = getPropertyDetailsArchive().getPropertyDetailsCached(getSearchType());
		if( details == null || details.size() == 0)
		{
			return getDefaultDetails().findStoredProperties();
		}
		return details.findStoredProperties();
	}
	
	
	public List getSearchProperties(User inUser)
	{
		List details = getDetailsForView(getSearchType() + "/" + getSearchType() + "search", inUser);
		if (details == null || details.size() == 0)
		{
			return getDefaultDetails().findStoredProperties();
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
			PropertyDetails details = new PropertyDetails();
			PropertyDetail id = new PropertyDetail();
			id.setIndex(true);
			id.setStored(true);
			id.setText("Id");
			id.setId("id");
			id.setEditable(true);
			id.setIndex(true);
			id.setStored(true);
			
			details.addDetail(id);

			id = new PropertyDetail();
			id.setIndex(true);
			id.setStored(true);
			id.setText("Name");
			id.setId("name");
			id.setEditable(true);
			details.addDetail(id);
			
			fieldDefaultDetails = details;

		}
		return fieldDefaultDetails;
	}

	public void deleteAll(User inUser)
	{
		HitTracker all = getAllHits();
		XmlFile settings = loadXml();
		for (Iterator iterator = all.iterator(); iterator.hasNext();)
		{
			Data object = (Data)iterator.next();
			Element record = settings.getElementById(object.getId());
			if( record != null)
			{
				settings.getRoot().remove(record);
			}
		}
		getXmlArchive().saveXml(settings, inUser);
		clearIndex();
	}
	public void delete(Data inData, User inUser)
	{
		XmlFile settings = loadXml();
		Element record = settings.getElementById(inData.getId());
		if( record != null)
		{
			settings.getRoot().remove(record);
			getXmlArchive().saveXml(settings, inUser);
		}
		clearIndex();
	}

	protected Map getIdCache()
	{
		if (fieldIdcache == null)
		{
			fieldIdcache = new HashMap();
		}
		return fieldIdcache;
	}
	
	public void clearIndex()
	{
		getCache().clear();
		getIdCache().clear();
	}
	
}
	
	