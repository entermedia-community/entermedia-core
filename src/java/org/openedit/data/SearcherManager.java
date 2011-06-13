package org.openedit.data;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.xml.XmlArchive;

import com.openedit.ModuleManager;
import com.openedit.OpenEditRuntimeException;
import com.openedit.hittracker.HitTracker;

public class SearcherManager
{
	private static final Log log = LogFactory.getLog(SearcherManager.class);
	
	protected ModuleManager fieldModuleManager;
	protected Map fieldCache;
	protected XmlArchive fieldXmlArchive;
	
	
		
	//A fieldName can be product or orderstatus. If there is no orderstatus searcher then we use an XML lookup for this catalog. 
	public Searcher getSearcher(String inCatalogId, String inFieldName)
	{
		if( inCatalogId == null)
		{
			throw new OpenEditRuntimeException("Catalog id is required");
		}
		//look in map for existing
		String id = inCatalogId + inFieldName;
		
		Searcher searcher = (Searcher)getCache().get(id);
		if( searcher == null )
		{
			if( inFieldName == null)
			{
				return null;
			}
			String beanName = null;
			if( getModuleManager().contains(inCatalogId,inFieldName + "Searcher") )
			{
				beanName = inFieldName + "Searcher";
				searcher = (Searcher)getModuleManager().getBean(inCatalogId,beanName);
			}
			else
			{
				if( inFieldName.endsWith("Log"))
				{
					beanName = "dynamicLogSearcher";					
				}
				else
				{
					beanName = "dynamicSearcher";  //typically an XmlSearcher or XmlFileSearcher
				}
				searcher = (Searcher)getModuleManager().getBean(beanName);
				if(log.isDebugEnabled())
				{
					log.debug("Searcher not found creating dynamic instance ");
				}
			}
			searcher.setCatalogId(inCatalogId);
			searcher.setSearchType(inFieldName); //This may be product or orderstatus
			//set the data
			PropertyDetailsArchive newarchive = getPropertyDetailsArchive(inCatalogId);
			searcher.setPropertyDetailsArchive(newarchive);
			searcher.setSearcherManager(this);
			if(log.isDebugEnabled())
			{
				log.debug("Created New Searcher: Catalog = " + searcher.getCatalogId() + "SearchType = " + searcher.getSearchType() + "Searcher = " + searcher.getClass() );
			}
			getCache().put(id, searcher);
		}
		//log.debug("return " + id + " " + searcher);
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

	public Object getData(String inCatalogId, String inSearchType, String inId)
	{
		if( inId == null)
		{
			return null;
		}
		Searcher searcher = getSearcher(inCatalogId, inSearchType);
		Object data = searcher.searchById(inId);
		return data;
	}

	public HitTracker getList(PropertyDetail inDetail)
	{
		return getList(inDetail.getListCatalogId(), inDetail.getListId());
	}

	public HitTracker getList(String inCatalogId, String inFieldName)  
	{
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
	
	public void removeFromCache(String inCatalogId, String inSearchType)
	{
		getCache().remove(inCatalogId + inSearchType);
		getModuleManager().clearBean(inCatalogId, inSearchType);
	}
}
