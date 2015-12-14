package org.openedit.util;

import org.openedit.OpenEditException;
import org.openedit.page.Page;
import org.openedit.page.manage.PageManager;

public class PageZipUtil extends ZipUtil
{
	public PageZipUtil (PageManager inPageManager)
	{
		setPageManager(inPageManager);
	}
	protected boolean isValidEntry ( String inPath) throws OpenEditException
	{
		if( exclude( inPath ))
		{
			return false;
		}
		Page page = getPageManager().getPage(inPath);
		String temp = page.get("excludefromzips");
		boolean excludePage = Boolean.parseBoolean(temp);
		getPageManager().clearCache(page);
		if (excludePage == true)
		{
			return false;
		}
		return true;
	}
}
