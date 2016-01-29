package org.openedit.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.Data;
import org.openedit.ModuleManager;
import org.openedit.OpenEditRuntimeException;
import org.openedit.hittracker.HitTracker;
import org.openedit.locks.LockManager;
import org.openedit.node.NodeManager;
import org.openedit.util.Replacer;

public class SearcherManager
{
	private static final Log log = LogFactory.getLog(SearcherManager.class);
	
	protected ModuleManager fieldModuleManager;
	protected Map fieldCache;
	protected HashMap fieldShowLogs;
	
	//A fieldName can be product or orderstatus. If there is no orderstatus searcher then we use an XML lookup for this catalog. 
	public Searcher getSearcher(String inCatalogId, String inFieldName)
	{
		if( inCatalogId == null)
		{
			throw new OpenEditRuntimeException("Catalog id is required");
		}
		if ( inFieldName == null )
		{
			return null;
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
					String finalcatalogid = resolveCatalogId(inCatalogId, inFieldName);
					if( !finalcatalogid.equals(inCatalogId))
					{
						searcher = (Searcher)getCache().get(finalcatalogid + "|" + inFieldName);
						if( searcher != null )
						{
							return searcher;
						}
					}
					//TODO: Look in the cache again for the target Searcher
					getNodeManager(finalcatalogid).connectCatalog(finalcatalogid);
					PropertyDetailsArchive newarchive = getPropertyDetailsArchive(finalcatalogid);
//					if( inFieldName == null)
//					{
//						return null;
//					}
					
					searcher = loadSearcher(newarchive, inFieldName);
					searcher.setCatalogId(finalcatalogid);
					searcher.setSearchType(inFieldName); //This may be product or orderstatus
					//set the data
					searcher.setPropertyDetailsArchive(newarchive);
					searcher.setSearcherManager(this);
					searcher.initialize();
					if(log.isDebugEnabled())
					{
						log.debug("Created New Searcher: Catalog = " + searcher.getCatalogId() + "SearchType = " + searcher.getSearchType() + "Searcher = " + searcher.getClass() );
					}
					getCache().put(id, searcher);
					if( !finalcatalogid.equals(inCatalogId))
					{
						getCache().put(finalcatalogid + "|" + inFieldName, searcher); //make sure we store both versions since they are the same searcher
					}
					if( id.equals("catalogsettings") )
					{
						Data defaultval = (Data)searcher.searchById("log_all_searches");
						if( defaultval != null )
						{
							setShowSearchLogs(inCatalogId, Boolean.parseBoolean(defaultval.get("value")));
						}
					}
				}
			}
		}
		//log.debug("return " + id + " " + searcher);
		return searcher;
	}
	protected NodeManager getNodeManager(String inFinalcatalogid)
	{
		return (NodeManager)getModuleManager().getBean(inFinalcatalogid,"nodeManager");
	}
	protected synchronized Searcher loadSearcher(PropertyDetailsArchive newarchive, String inFieldName)
	{
		String inCatalogId = newarchive.getCatalogId();
		Searcher searcher;
		//Check the properites
		PropertyDetails details = newarchive.getPropertyDetails(inFieldName);
		
		String beanName = null;
		if( getModuleManager().contains(inCatalogId,inFieldName + "Searcher") ) //this might be a lookup
		{
			beanName = inFieldName + "Searcher";
		}	
		else
		{
				beanName = details.getBeanName(); //Once item is saved it always uses dataSearcher for the type
				if( beanName == null)
				{
					if( inFieldName.endsWith("Log"))
					{
						beanName = "dynamicLogSearcher";					
					}
					else
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
		//log.info("Loaded " + inFieldName + " with " + beanName);
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
	
	public Collection<Searcher> listLoadedSearchers(String inCatalogId)
	{
		List<Searcher> loaded = new ArrayList<Searcher>();
		
		for (Iterator iterator = getCache().values().iterator(); iterator.hasNext();)
		{
			Searcher searcher = (Searcher) iterator.next();
			if( searcher.getCatalogId().equals(inCatalogId))
			{
				loaded.add(searcher);
			}
		}
		return loaded;
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
	
	
	
	public String resolveCatalogId(String inCatalogId, String inSearchType)
	{
		if("searchtypes".equals(inSearchType)){
			return inCatalogId;
		}
		if(inSearchType == null){
			return inCatalogId;
		}
		Searcher typeSearcher = getSearcher(inCatalogId, "searchtypes");
		if( typeSearcher.getPropertyDetails().size() == 0) //Not used in the system catalog
		{
			if( inSearchType.equals("user") || inSearchType.equals("group"))
			{
				return "system";
			}
			return inCatalogId;
		}
		Data catalogdata = (Data)typeSearcher.searchById(inSearchType);
		if(catalogdata != null)
		{
			return catalogdata.get("catalogid");
		}
		else
		{
			if( inSearchType.equals("user") || inSearchType.equals("group"))
			{
				return "system";
			}
		}
		return inCatalogId;
		
	}
	
	public  QueryBuilder query(String inCatalogId, String inSearchType)
	{
		Searcher searcher = getSearcher(inCatalogId, inSearchType);
		if( searcher == null)
		{
			return null;
		}
		return searcher.query();
	}

	public LockManager getLockManager(String inCatalogId)
	{
		LockManager manager = (LockManager)getModuleManager().getBean(inCatalogId,"lockManager");
		return manager;
	}

	public boolean getShowSearchLogs(String inCatalogId)
	{
		Boolean found = (Boolean)getShowLogs().get(inCatalogId);
		if( found == null)
		{
			found = false;
			getShowLogs().put(inCatalogId, found);
		}
		return found;
	}
	protected Map getShowLogs()
	{
		if (fieldShowLogs == null)
		{
			fieldShowLogs = new HashMap();
		}
		return fieldShowLogs;
	}
	public void setShowSearchLogs(String inCatalogId, boolean inValue)
	{
		getShowLogs().put(inCatalogId, inValue);
	}

	
}
