/*
 * Created on Jun 6, 2006
 */
package com.openedit.modules.edit;

import com.openedit.page.Page;

public class EditSession
{
	protected Page fieldEditPage;
	protected String fieldOriginalUrl;
	protected String fieldParentName;
	
	public Page getEditPage()
	{
		return fieldEditPage;
	}
	public void setEditPage(Page inEditPage)
	{
		fieldEditPage = inEditPage;
	}
	public String getEditPath()
	{
		return getEditPage().getPath();
	}
	public String getOriginalUrl()
	{
		return fieldOriginalUrl;
	}
	public void setOriginalUrl(String inOriginalUrl)
	{
		fieldOriginalUrl = inOriginalUrl;
	}
	public String getParentName()
	{
		return fieldParentName;
	}
	public void setParentName(String inParentName)
	{
		fieldParentName = inParentName;
	}

}
