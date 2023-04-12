package org.openedit.generators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.OpenEditException;
import org.openedit.WebPageRequest;
import org.openedit.error.ContentNotAvailableException;
import org.openedit.page.Page;

public class DownloadGenerator extends FileGenerator
{
	private static Log log = LogFactory.getLog(DownloadGenerator.class);

	public void generate(WebPageRequest inContext, Page contentpage, Output inOut) throws OpenEditException
	{
		String vir = contentpage.get("virtual");
		if ( !Boolean.parseBoolean(vir) )
		{
			if( !contentpage.exists() )
			{
				log.info("Missing: " +contentpage.getPath());
				throw new ContentNotAvailableException("Missing: " +contentpage.getPath(),contentpage.getPath());
			}
			else
			{
				log.debug("not found: " + contentpage);
				return; //do nothing
			}
		}
		String alternateRoot = inContext.findPathValue("downloadrootdirectory"); 
		if (alternateRoot != null && getPageManager() != null)
		{
			String path = contentpage.getPath().substring(alternateRoot.length());
			String newpathprefix = inContext.findPathValue("newpathprefix"); 
			if( newpathprefix != null)
			{
				path = newpathprefix + path;
			}
			contentpage = getPageManager().getPage(path);
		}
		
		super.generate(inContext, contentpage, inOut);
	}
}
