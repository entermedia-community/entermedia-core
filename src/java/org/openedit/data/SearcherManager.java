package org.openedit.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.Data;
import org.openedit.ModuleManager;
import org.openedit.MultiValued;
import org.openedit.OpenEditException;
import org.openedit.cache.CacheManager;
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
	protected CacheManager fieldCacheManager;
	
	
	public CacheManager getCacheManager()
	{
		return fieldCacheManager;
	}


	public void setCacheManager(CacheManager inCacheManager)
	{
		fieldCacheManager = inCacheManager;
	}

	public Searcher getSearcher(String inCatalogId, String inSearchtypeid)
	{
		Searcher searcher = loadSearcher(inCatalogId,inSearchtypeid, true);
		return searcher;
	}

	//A fieldName can be product or orderstatus. If there is no orderstatus searcher then we use an XML lookup for this catalog. 
	public Searcher loadSearcher(String inCatalogId, String inFieldName,boolean init)
	{
		if( inCatalogId == null)
		{
			throw new OpenEditException("Catalog id is required");
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
					if(!finalcatalogid.equals(inCatalogId))
					{
						searcher = (Searcher)getCache().get(finalcatalogid + "|" + inFieldName);
						if( searcher != null )
						{
							getCache().put(id, searcher); //make sure we store both versions since they are the same searcher
							return searcher;
						}
					}
					//TODO: Index name is assets_catalog_lists or assets_catalog_asset  
					//TODO: Go read module.xml to see if this is its own index or not
					boolean created = getNodeManager(finalcatalogid).connectCatalog(finalcatalogid);
					
					PropertyDetailsArchive newarchive = getPropertyDetailsArchive(finalcatalogid);
					searcher = loadSearcher(newarchive, inFieldName);
					searcher.setCatalogId(finalcatalogid);
					searcher.setSearchType(inFieldName); //This may be product or orderstatus
					//set the data
					searcher.setPropertyDetailsArchive(newarchive);
					searcher.setSearcherManager(this);
					if( init )
					{
						searcher.initialize();
					}
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
		try
		{
			//If this is failing you are missing the base/system/configuration folder
			NodeManager manager = (NodeManager)getModuleManager().getBean(inFinalcatalogid,"nodeManager");
			return manager;
		}
		catch( Exception ex)
		{
			throw new OpenEditException("Couldnot resolve " + inFinalcatalogid, ex);
		}
	}
	protected synchronized Searcher loadSearcher(PropertyDetailsArchive newarchive, String inFieldName)
	{
		String inCatalogId = newarchive.getCatalogId();
		Searcher searcher;
		//Check the properites
		PropertyDetails details = newarchive.getPropertyDetailsCached(inFieldName);
		
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
		if (inDetail == null)
		{
			return null;
		}
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
	public String getDataLabel(Data inData,PropertyDetail inDetail)
	{
		if(inData == null || inDetail == null){
			return null;
		}
		Object value = 	inData.get(inDetail.getId());
		if( value == null)
		{
			return null;
		}
		if( inDetail.isList())
		{
			//Lookup the value
			Data lookup = getCachedData(inDetail.getListCatalogId(), inDetail.getListId(), value.toString());
			return lookup.getName();
		}
		else
		{
			return value.toString();
		}
		
	}
	public Object getListData(PropertyDetail inDetail, String inValue)
	{
		return getData(inDetail.getListCatalogId(), inDetail.getListId(), inValue);
	}
	public String getValue(Data inParent,PropertyDetail inDetail)
	{
		if(inParent == null || inDetail == null){
			return null;
		}
		String mask = inDetail.get("rendermask");
		if( mask == null )
		{
			mask = inDetail.get("mask"); //Legacy. Remove by 2023
		}
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
	public Collection getUniqueValues(Searcher inSearcher, HitTracker inHits, String inColumn, String startsWith)
	{
		if(inHits != null){
			inHits.enableBulkOperations();
		}
		Set	 results = new HashSet();
		startsWith = startsWith.toLowerCase();
		for (Iterator iterator = inHits.iterator(); iterator.hasNext();)
		{
			Data data = (Data) iterator.next();
				//	We need a list of searchable (keyword fields)
			Object obj = data.getValue(inColumn);
			if( obj instanceof Collection)
			{
				Collection values = (Collection)obj;
				for (Iterator iterator2 = values.iterator(); iterator2.hasNext();)
				{
					String val = (String) iterator2.next();
					if( val.toLowerCase().startsWith(startsWith))
					{
						results.add( val);
					}
				}
			}
			else
			{
				String val = (String) obj;
				if(val != null) {
					/*if( val.toLowerCase().startsWith(startsWith))
					{
						results.add( val);
					}*/
					
					//could break it by spaces but regex:
				    Pattern pattern = Pattern.compile("(?i)\\b\\S*"+startsWith+"\\S*");
				    Matcher matcher = pattern.matcher(val);

				    while (matcher.find()) {
				        //return matcher.group(1);
				    	String output =  matcher.group(0);
				    	results.add(output);
				    }
				}
			}
			if( results.size() > 100)
			{
				break;
			}
		}
		List<String> sorted = new ArrayList( results);
		Collections.sort(sorted);
		return sorted;
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
	public String getLabel(PropertyDetail inDetail, Data inData, String inLocale)
	{
		if( inData == null || inDetail == null)
		{
			return null;
		}
		Searcher listsearcher = getSearcher(inDetail.getListCatalogId(),inDetail.getListId() );
		String mask = listsearcher.getPropertyDetails().getRender();
		String val = null;
		if( mask != null)
		{
			val =  getValue(inDetail.getCatalogId(),mask,inData.getProperties());
		}
		else
		{
			String name = inData.getName(inLocale);
			
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
	public Collection getValues(String inCatalogId, String inMask,Map inValues)
	{
		String value = getValue(inCatalogId,inMask,inValues);
		if( value == null || value.isEmpty() )
		{
			return Collections.emptyList();
		}
		String[] values = MultiValued.VALUEDELMITER.split(value);
		return Arrays.asList(values);
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
	public Data getCachedData(String inCatalogId, String inSearchType, String inId)
	{
		if( inId == null)
		{
			return null;
		}
		Data found = (Data)getCacheManager().get("sm-data" + inCatalogId, inSearchType + "/" + inId);
		if( found == CacheManager.NULLDATA)
		{
			return null;
		}
		if( found != null)
		{
			return found;
		}
		Searcher searcher = getSearcher(inCatalogId, inSearchType);
		Object data = searcher.searchById(inId);
		if(data == null)
		{
			getCacheManager().put("sm-data" + inCatalogId, inSearchType + "/" + inId, CacheManager.NULLDATA);
			return null;
		}
		getCacheManager().put("sm-data" + inCatalogId, inSearchType + "/" + inId, data);
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

	/**
	 * TODO: Cache this list, check the indexid tho for local edits
	 * @param inCatalogId
	 * @param inFieldName
	 * @return
	 */
	public HitTracker getList(String inCatalogId, String inFieldName)  
	{
		if( inFieldName == null)
		{
			return null;
		}
		//If this is not my searcher type then use the manager to get the  correct search
		Searcher searcher = getSearcher(inCatalogId, inFieldName);
		HitTracker found = (HitTracker)getCacheManager().get("sm" + inCatalogId, inFieldName);
		if( found == null || !searcher.getIndexId().equals(found.getIndexId()))
		{
			String sortfield = null;
			if( searcher.getPropertyDetails().getDetail("ordering") != null )
			{
				sortfield = "ordering";
			}
			else if( searcher.getPropertyDetails().getDetail("numberval") != null )
			{
				sortfield = "numberval";
			}
			else if( searcher.getPropertyDetails().getDetail("name") != null )
			{
				sortfield = "name";
			}
			found = searcher.query().all().sort(sortfield).search();
			found.setHitsPerPage(100);
			getCacheManager().put("sm" + inCatalogId, inFieldName, found);
		}	
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
		getCacheManager().clearAll();
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
		if(catalogdata != null && catalogdata.get("catalogid") != null)
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
	public Collection reloadLoadedSettings(String inCatalogid)
	{
		Collection<Searcher> tables = listLoadedSearchers(inCatalogid);
		
		List types = new ArrayList();
		for (Iterator iterator = tables.iterator(); iterator.hasNext();)
		{
			Searcher searcher = (Searcher) iterator.next();
			if (searcher instanceof Reloadable)
			{
				if( !searcher.getSearchType().contains("$searcher.getSearchType()") )
				{
					searcher.reloadSettings();
					
					types.add(searcher.getSearchType());
				}	
			}
		}
		return types;
	}

	
	public void resetAlternative(){
		for (Iterator iterator = getCache().keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			Searcher toclear = (Searcher) getCache().get(key);
			toclear.setAlternativeIndex(null);
			
		}
	}


	public boolean isData(Object inVal)
	{
		if( inVal instanceof Data)
		{
			return true;
		}
		return false;
	}

	public Searcher getExistingSearcher(String inCatalogId, String inSearchType)
	{
		PropertyDetailsArchive archive = getPropertyDetailsArchive(inCatalogId);
		List searchtypes = archive.getSearchTypes();
		boolean found = searchtypes.contains(inSearchType);
		if( found )
		{
			return getSearcher(inCatalogId, inSearchType);
		}
		return null;
	}
	
}
