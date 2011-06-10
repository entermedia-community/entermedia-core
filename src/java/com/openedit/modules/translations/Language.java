/*
 * Created on Dec 6, 2005
 */
package com.openedit.modules.translations;

import java.util.Date;

import com.openedit.OpenEditException;
import com.openedit.page.Page;
import com.openedit.page.manage.PageManager;


public class Language
{
	protected String fieldName; //friendly text
	protected String fieldId; //locale
	protected String fieldRootDirectory;
	
	protected PageManager fieldPageManager;
	
	public String getId()
	{
		return fieldId;
	}
	public void setId(String inId)
	{
		fieldId = inId;
	}
	public String getName()
	{
		return fieldName;
	}
	public void setName(String inName)
	{
		fieldName = inName;
	}
	public boolean isCurrent(String inPath) throws OpenEditException
	{
		Page trans = getPage(inPath);
		Date here = trans.getLastModified();
		if ( here.getTime() == 0)
		{
			return false;
		}		
		Page rootLevel = getPageManager().getPage(inPath);
		if ( rootLevel.getContentItem().getPath().equals(trans.getContentItem().getPath()))
		{
			return false; //this is the fallback directory
		}
		
		Date current = rootLevel.getLastModified();
		boolean ok = current.before(here) || current.equals(here); //if the root page is older than the now we are ok
		return ok;
	}
	public Page getPage(String inPath) throws OpenEditException
	{
		return getPageManager().getPage(getRootDirectory() + inPath);
	}
	public PageManager getPageManager()
	{
		return fieldPageManager;
	}
	public void setPageManager(PageManager inPageManager)
	{
		fieldPageManager = inPageManager;
	}
	public String getRootDirectory()
	{
		return fieldRootDirectory;
	}
	public void setRootDirectory(String inRootDirectory)
	{
		fieldRootDirectory = inRootDirectory;
	}
	public String toString()
	{
		return getId() + " " + getName();
	}
}
