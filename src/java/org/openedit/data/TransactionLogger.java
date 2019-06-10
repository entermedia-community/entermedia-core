package org.openedit.data;

import java.util.Date;

import org.openedit.Data;
import org.openedit.ModuleManager;

public class TransactionLogger
{

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



	
	
	public void logEvent(String inCatalogId, String inOperation, String inSearchType, Data inData, String inUser) {
		
		Searcher searcher = getSearcherManager().getSearcher(inCatalogId, "transactionLog");
		Data event = searcher.createNewData();
		event.setValue("operation", inOperation);
		event.setValue("searchtype", inSearchType);
		event.setValue("dataid", inData.getId());
		event.setValue("recordmodificationdate", new Date());
		event.setValue("user", inUser);
		searcher.saveData(event);
		
		
		
	}
	
	
	

}
