package org.openedit.data;

import org.openedit.CatalogEnabled;
import org.openedit.Data;
import org.openedit.ModuleManager;

public class TransactionLogger implements CatalogEnabled
{

	protected String fieldCatalogId;
	protected ModuleManager fieldModuleManager;
	
	protected SearcherManager getSearcherManager()
	{
		return (SearcherManager) getModuleManager().getBean("searcherManager");
	}
	
	
	
	public ModuleManager getModuleManager()
	{
		return fieldModuleManager;
	}



	public void setModuleManager(ModuleManager inModuleManager)
	{
		fieldModuleManager = inModuleManager;
	}



	public String getCatalogId()
	{
		return fieldCatalogId;
	}

	public void setCatalogId(String inCatalogId)
	{
		fieldCatalogId = inCatalogId;
	}
	
	public void logEvent(String inOperation, String inSearchType, Data inData) {
		
		Searcher searcher = getSearcherManager().getSearcher(getCatalogId(), "transactionLog");
		Data event = searcher.createNewData();
		event.setValue("operation", inOperation);
		event.setValue("searchtype", inSearchType);
		event.setValue("dataid", inData.getId());
		
		searcher.saveData(event);
		
		
		
	}
	
	
	

}
