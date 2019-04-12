/*
 * Created on Dec 6, 2004
 */
package org.openedit.modules;

import org.openedit.OpenEditException;
import org.openedit.WebPageRequest;
import org.openedit.page.PageAction;
import org.openedit.page.PageRequestKeys;
import org.openedit.util.SessionTool;

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
			bean = getBeanLoader().getBean(key);
			if ( bean != null)
			{
				inContext.putSessionValue(key,bean);
				return;
			}
		}
		throw new OpenEditException("No such bean " + key);
	}
}
