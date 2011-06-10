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
public class SessionValueModule extends BaseModule
{
	public void loadSessionVariable( WebPageRequest inContext ) throws Exception
	{
		PageAction inAction = inContext.getCurrentAction();
		String key = inAction.getConfig().getAttribute("bean");
		Object bean = inContext.getSessionValue(key);
		if( bean != null)
		{
			return;
		}
		String className = inAction.getConfig().getAttribute("class");
		if ( className != null)
		{
			SessionTool classTool = (SessionTool)inContext.getPageValue( PageRequestKeys.CLASSTOOL );
			if( classTool == null)
			{
				classTool = new SessionTool(inContext,getModuleManager());
				inContext.putSessionValue(PageRequestKeys.CLASSTOOL, classTool);
			}

			inContext.putPageValue( key, classTool.construct( key, className ) );
			return;
		}
		else
		{
			bean = getBeanFactory().getBean(key);
			if ( bean != null)
			{
				inContext.putSessionValue(key,bean);
				return;
			}
		}
		throw new OpenEditException("No such bean " + key);
	}
}
