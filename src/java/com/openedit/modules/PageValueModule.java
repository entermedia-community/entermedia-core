/*
 * Created on Dec 6, 2004
 */
package com.openedit.modules;

import com.openedit.OpenEditException;
import com.openedit.WebPageRequest;
import com.openedit.page.PageAction;
import com.openedit.page.PageRequestKeys;
import com.openedit.util.SessionTool;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class PageValueModule extends BaseModule
{
	public void loadPageVariable( WebPageRequest inContext ) throws Exception
	{
		PageAction inAction = inContext.getCurrentAction();
		String key = inAction.getConfig().getAttribute("bean");

		String className = inAction.getConfig().getAttribute("class");
		if ( className != null)
		{
			SessionTool classTool = getLoader(inContext);
			inContext.putPageValue( key, classTool.construct( key, className ) );
			return;
		}
		else
		{
			Object bean = getBeanFactory().getBean(key);
			if ( bean != null)
			{
				inContext.putPageValue(key,bean);
				return;
			}
		}
		throw new OpenEditException("No such bean " + key);
	}

	public SessionTool getLoader(WebPageRequest inContext)
	{
		SessionTool classTool = (SessionTool)inContext.getPageValue( PageRequestKeys.CLASSTOOL );
		if( classTool == null)
		{
			classTool = new SessionTool(inContext,getModuleManager());
			inContext.putSessionValue(PageRequestKeys.CLASSTOOL, classTool);
		}
		return classTool;
	}
}
