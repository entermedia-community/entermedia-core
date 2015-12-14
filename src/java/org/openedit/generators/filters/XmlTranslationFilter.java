package org.openedit.generators.filters;

import org.openedit.ModuleManager;
import org.openedit.data.SearcherManager;
import org.openedit.modules.translations.TranslationSearcher;
import org.openedit.page.Page;
import org.openedit.page.manage.PageManager;

public class XmlTranslationFilter extends TextReaderFilter
{
	protected Page page;
	protected String locale;
	protected ModuleManager fieldModuleManager;
	protected PageManager fieldPageManager;
	protected SearcherManager fieldSearcherManager;
	
	
	public SearcherManager getSearcherManager() {
		return (SearcherManager) getModuleManager().getBean("searcherManager");
	}
	public void setSearcherManager(SearcherManager inSearcherManager) {
		fieldSearcherManager = inSearcherManager;
	}
	public ModuleManager getModuleManager() {
		return fieldModuleManager;
	}
	public void setModuleManager(ModuleManager inModuleManager) {
		fieldModuleManager = inModuleManager;
	}
	public PageManager getPageManager() {
		return fieldPageManager;
	}
	public void setPageManager(PageManager inPageManager) {
		fieldPageManager = inPageManager;
	}
	public XmlTranslationFilter(Page inPage, String inParams)
	{
		super(inPage.getReader(),inPage.getCharacterEncoding());
		page = inPage;
		int index = inParams.indexOf("&locale=");
		locale = inParams.substring(index + "&locale=".length(), inParams.length());
	}
	public StringBuffer replace(String inLastLine)
	{
		int bracket = inLastLine.indexOf("[[");
		if( bracket == -1)
		{
			return new StringBuffer(inLastLine);
		}
		//look for [[ and get the property to replace it with
		StringBuffer done = new StringBuffer(inLastLine.length() + 20);
		int start = 0;
		char[] line = inLastLine.toCharArray();
		while( bracket != -1 )
		{
			int end = inLastLine.indexOf("]]",bracket);
			if( end != -1 )
			{
				String key = inLastLine.substring(bracket + 2,end);
				String catalogid = page.getProperty("translationsid");
				if(catalogid == null){
					catalogid = "translations";
				}
				TranslationSearcher searcher = (TranslationSearcher) getSearcherManager().getSearcher(catalogid, "translation");
				
				boolean translate = page.isPropertyTrue("auto_translate" + "_" + locale);
				String value = (String) searcher.getEntryForLocale(locale, key, translate);
				
				done.append(line,start,bracket - start); //everything up to this point
				done.append(value);
				start = end + 2;
				bracket = inLastLine.indexOf("[[",start);
			}
			else
			{
				done.append(line,start,line.length);				
				start = line.length;
				break; //no closing ]]
			}
		}
		if( start < line.length)
		{
			done.append(line,start,line.length - start);
		}
		return done;
	}
}
