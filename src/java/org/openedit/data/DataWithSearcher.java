package org.openedit.data;

import java.util.Collection;
import java.util.Date;

import org.openedit.Data;
import org.openedit.util.DateStorageUtil;

public class DataWithSearcher {

	
	protected SearcherManager fieldSearcherManager;
	protected String fieldCatalogId;
	public String getCatalogId() {
		return fieldCatalogId;
	}

	public void setCatalogId(String inCatalogId) {
		fieldCatalogId = inCatalogId;
	}

	public String getSearchType() {
		return fieldSearchType;
	}

	public void setSearchType(String inSearchType) {
		fieldSearchType = inSearchType;
	}

	protected String fieldSearchType;
	protected Data fieldData;
	
	
	public DataWithSearcher(SearcherManager inSearcherManager, String inCatalogId, String inSearchType, Data inData) {
		setCatalogId(inCatalogId);
		setSearcherManager(inSearcherManager);
		setData(inData);
		setSearchType(inSearchType);
	}

	public SearcherManager getSearcherManager() {
		return fieldSearcherManager;
	}

	public void setSearcherManager(SearcherManager inSearcherManager) {
		fieldSearcherManager = inSearcherManager;
	}


	public Data getData() {
		return fieldData;
	}

	public void setData(Data inData) {
		fieldData = inData;
	}

	public Object getChildValue(String inChildField) 
	{
		Searcher searcher = getSearcherManager().getExistingSearcher(getCatalogId(), getSearchType());
		PropertyDetail detail = searcher.getPropertyDetails().getDetail(inChildField);
		Object othervalue = getData().getValue(inChildField);
		if( othervalue != null)
		{
			if(detail.isList())
			{
				if( searcher != null)
				{
					if( othervalue instanceof Collection)
					{
						Collection others = (Collection)othervalue;
						if( others.isEmpty())
						{
							return null;
						}
						othervalue = others.iterator().next();
					}
					Data childdata = getSearcherManager().getCachedData(detail.getCatalogId(),detail.getListId(), othervalue.toString());
					if( childdata != null)
					{
						DataWithSearcher newval = new DataWithSearcher(getSearcherManager(),detail.getCatalogId(),detail.getListId(),childdata);
						return newval;
					}
				}
			}
			else if(detail.isDate() && !(othervalue instanceof Date)) //Is this used?
			{
				Date date = DateStorageUtil.getStorageUtil().parseFromStorage(othervalue.toString());
				//TODO: Support .year
				
				return date;
			}
			
		}
		return othervalue;
	}


}
