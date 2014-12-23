package org.openedit.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.entermedia.locks.LockManager;
import org.openedit.Data;
import org.openedit.xml.XmlArchive;

import com.openedit.ModuleManager;
import com.openedit.OpenEditRuntimeException;
import com.openedit.hittracker.HitTracker;
import com.openedit.util.Replacer;

public class SearcherManager
{
	private static final Log log = LogFactory.getLog(SearcherManager.class);
	
	protected ModuleManager fieldModuleManager;
	protected Map fieldCache;
	protected XmlArchive fieldXmlArchive;
    public LockManager getLockManager() {
		return fieldLockManager;
	}
	public void setLockManager(LockManager inLockManager) {
		fieldLockManager = inLockManager;
	}

	protected LockManager fieldLockManager;
    
	//A fieldName can be product or orderstatus. If there is no orderstatus searcher then we use an XML lookup for this catalog. 
	public Searcher getSearcher(String inCatalogId, String inFieldName)
	{
		if( inCatalogId == null)
		{
			throw new OpenEditRuntimeException("Catalog id is required");
		}
		//look in map for existing
		String id = inCatalogId + "|" + inFieldName;
		
		Searcher searcher = (Searcher)getCache().get(id);
		if( searcher == null )
		{
			synchronized (this)
			{
				searcher = (Searcher)getCache().get(id);
				if( searcher == null )
				{
					PropertyDetailsArchive newarchive = getPropertyDetailsArchive(inCatalogId);
					if( inFieldName == null)
					{
						return null;
					}
					searcher = loadSearcher(newarchive, inFieldName);
					searcher.setCatalogId(inCatalogId);
					searcher.setSearchType(inFieldName); //This may be product or orderstatus
					//set the data
					searcher.setPropertyDetailsArchive(newarchive);
					searcher.setSearcherManager(this);
					if(log.isDebugEnabled())
					{
						log.debug("Created New Searcher: Catalog = " + searcher.getCatalogId() + "SearchType = " + searcher.getSearchType() + "Searcher = " + searcher.getClass() );
					}
					getCache().put(id, searcher);
				}
			}
		}
		//log.debug("return " + id + " " + searcher);
		return searcher;
	}
	protected synchronized Searcher loadSearcher(PropertyDetailsArchive newarchive, String inFieldName)
	{
		String inCatalogId = newarchive.getCatalogId();
		Searcher searcher;
		String beanName = inFieldName + "Searcher";
		if( !getModuleManager().contains(inCatalogId,inFieldName + "Searcher") )
		{
			if( inFieldName.endsWith("Log"))
			{
				beanName = "dynamicLogSearcher";					
			}
			else
			{
				//Check the properites
				//searchertype
				PropertyDetails details = newarchive.getPropertyDetails(inFieldName);
				beanName = details.getBeanName();
				if( beanName == null )
				{
					beanName = "listSearcher";
				}
			}
		}
		searcher = (Searcher)getModuleManager().getBean(inCatalogId, beanName, false);
		if(log.isDebugEnabled())
		{
			log.debug("Searcher not found creating dynamic instance ");
		}
//			if( inCatalogId.length() == 0 && "catalogs".equals(inFieldName))
//			{
//				XmlSearcher xml = (XmlSearcher)searcher;
//			}
		log.info("Loaded " + inFieldName + " with " + beanName);
		return searcher;
	}
	public PropertyDetailsArchive getPropertyDetailsArchive(String inCatalogId)
	{
		PropertyDetailsArchive 	newarchive = (PropertyDetailsArchive)getModuleManager().getBean(inCatalogId, "propertyDetailsArchive"); //Not a singleton
		return newarchive;
	}
	public Searcher getSearcher(String inCatalogId, String inExternalCatalogId, String inFieldName)  
	{
		if( inExternalCatalogId != null)
		{
			inCatalogId = inExternalCatalogId;
		}
		return getSearcher(inCatalogId, inFieldName);
	}
	/**
	 * 
	 * @param inCatalogId
	 * @param inDetail
	 * @deprecated use getListSearcher($detail)
	 * @return
	 */
	public Searcher getSearcher(String inCatalogId, PropertyDetail inDetail)  
	{
		//We may get passed in an external catalog and field
		String field = inDetail.getId();
		if( inDetail.getExternalId() != null)
		{
			field = inDetail.getExternalId();
		}
		return getSearcher(inDetail.getListCatalogId(), field);
	}
	public Searcher getListSearcher(PropertyDetail inDetail)  
	{
		//We may get passed in an external catalog and field
		return getSearcher(inDetail.getListCatalogId(), inDetail.getListId() );
	}
	public Object getListData(PropertyDetail inDetail, String inValue)
	{
		return getData(inDetail.getListCatalogId(), inDetail.getListId(), inValue);
	}
	public String getValue(Data inParent,PropertyDetail inDetail)
	{
		if(inParent == null){
			return null;
		}
		String mask = inDetail.get("render");
		String val = null;
		if( mask != null)
		{
			val = getValue(inDetail.getCatalogId(),mask,inParent.getProperties());
		}
		else
		{
			val = inParent.get(inDetail.getId());
		}
		return val;
	}
	public String getLabel(Searcher inSearcher, Data inData)
	{
		if(inData == null){
			return null;
		}
		String val = null;
		String mask = inSearcher.getPropertyDetails().getRender();
		if( mask != null)
		{
			val =  getValue(inSearcher.getCatalogId(),mask,inData.getProperties());
		}
		else
		{
			String name = inData.getName();
			
			if(name != null)
			{
				val = name;
			}
			else
			{
				val = inData.getId();
			}
			
		}
		return val;
	}
	
	public String getLabel(PropertyDetail inDetail, Data inData)
	{
		if(inData == null){
			return null;
		}
		String mask = inDetail.get("render");
		String val = null;
		if( mask != null)
		{
			val = getValue(inDetail.getCatalogId(),mask,inData.getProperties());
		}
		else
		{
			Searcher listsearcher = getSearcher(inDetail.getListCatalogId(),inDetail.getListId() );
			mask = listsearcher.getPropertyDetails().getRender();
			if( mask != null)
			{
				val =  getValue(inDetail.getCatalogId(),mask,inData.getProperties());
			}
			else
			{
				String name = inData.getName();
				
				if(name != null)
				{
					val = name;
				}
				else
				{
					val = inData.getId();
				}
				
			}
		}
		return val;
	}
	
	public String getValue(String inCatalogId, String inMask,Map inValues)
	{
		if( inMask == null)
		{
			return null;
		}
		Replacer replacer = getReplacer(inCatalogId);
		
		String val = replacer.replace(inMask, inValues);
		if( val.startsWith("$") && val.equals(inMask) )
		{
			return "";
		}
		return val; 
	}
	
	public Replacer getReplacer(String inCatalogId)
	{
		return (Replacer)getModuleManager().getBean(inCatalogId, "replacer");
	}
	public Data getData(String inCatalogId, String inSearchType, String inId)
	{
		if( inId == null)
		{
			return null;
		}
		Searcher searcher = getSearcher(inCatalogId, inSearchType);
		Object data = searcher.searchById(inId);
		return (Data)data;
	}
	public Data getData(PropertyDetail inDetail, String inId)
	{
		if( inId == null)
		{
			return null;
		}
		Searcher searcher = getSearcher(inDetail.getListCatalogId(), inDetail.getListId());
		Object data = searcher.searchById(inId);
		return (Data)data;
	}

	public HitTracker getList(PropertyDetail inDetail)
	{
		return getList(inDetail.getListCatalogId(), inDetail.getListId());
	}

	public HitTracker getList(String inCatalogId, String inFieldName)  
	{
		if( inFieldName == null)
		{
			return null;
		}
		//If this is not my searcher type then use the manager to get the  correct search
		Searcher searcher = getSearcher(inCatalogId, inFieldName);
		HitTracker found = searcher.getAllHits();
		return found;
	}

	
	public ModuleManager getModuleManager()
	{
		return fieldModuleManager;
	}

	public void setModuleManager(ModuleManager inModuleManager)
	{
		fieldModuleManager = inModuleManager;
	}
	public void clear()
	{
		for (Iterator iterator = getCache().keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			String[] vals = key.split("|");
			getModuleManager().clearBean(vals[0], vals[1]);
		}
		
		getCache().clear();
	}
	protected Map getCache()
	{
		if( fieldCache == null)
		{
			fieldCache = new HashMap();
		}
		return fieldCache;
	}
	public XmlArchive getXmlArchive()
	{
		return fieldXmlArchive;
	}
	public void setXmlArchive(XmlArchive inXmlArchive)
	{
		fieldXmlArchive = inXmlArchive;
	}

	public FilteredTracker makeFilteredTracker()
	{
		return new CompositeFilteredTracker();
	}
	
	public void removeFromCache(String inCatalogId, String inFieldName)
	{
		getCache().remove(inCatalogId + "|" +  inFieldName);
		
		String beanName = null;
		if( getModuleManager().contains(inCatalogId,inFieldName + "Searcher") )
		{
			beanName = inFieldName + "Searcher";
		}
		else
		{
			if( inFieldName.endsWith("Log"))
			{
				beanName = "dynamicLogSearcher";					
			}
			else
			{
				PropertyDetails details = getPropertyDetailsArchive(inCatalogId).getPropertyDetails(inFieldName);
				beanName = details.getBeanName();
				if( beanName == null )
				{
					beanName = "dynamicSearcher";
				}
			}
		}
		getModuleManager().clearBean(inCatalogId, beanName);
	}
	/*
	public Collection getCatalogs()
	{
		XmlFile file = getCatalogXml();
		XmlSearcher searcher = (XmlSearcher)getSearcher("", "catalogs");
		searcher.setXmlFile(file);
		return searcher.getAllHits();
	}
	protected XmlFile getCatalogXml()
	{
		XmlFile file = getXmlArchive().getXml("/WEB-INF/data/catalogs.xml");
		if( !file.isExist() )
		{
			file = getXmlArchive().getXml("/WEB-INF/data/media/lists/catalogs.xml"); //legacy
		}
		return file;
	}
	public Element getCatalog(String inId)
	{
		XmlFile file = getCatalogXml();
		Element element = file.getElementById(inId);
		return element;
	}
	public void saveCatalog(String inId, String inName)
	{
		XmlFile file = getCatalogXml();
		Element element = file.getElementById(inId);
		if( element == null )
		{
			element = file.createElement();
			file.add(element);
		}
		element.addAttribute("id", inId);
		element.setText(inName);
		getXmlArchive().saveXml(file, null);
	}
	*/
}
