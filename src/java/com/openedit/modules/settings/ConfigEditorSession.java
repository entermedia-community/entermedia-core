/*
 * Created on Jun 3, 2006
 */
package com.openedit.modules.settings;

import java.util.List;

import com.openedit.modules.edit.EditSession;
import com.openedit.page.XconfConfiguration;

public class ConfigEditorSession extends EditSession
{
	protected XconfConfiguration fieldConfig;
	protected List fieldLayouts;
	
	public XconfConfiguration getConfig()
	{
		return fieldConfig;
	}
	public void setConfig(XconfConfiguration inConfig)
	{
		fieldConfig = inConfig;
	}
//	public List getLayouts()
//	{
//		return fieldLayouts;
//	}
//	public void setLayouts(List inLayouts)
//	{
//		fieldLayouts = new ArrayList();
//		for (Iterator iter = inLayouts.iterator(); iter.hasNext();)
//		{
//			Page layout = (Page) iter.next();
//			if (layout.isHtml() )
//			{
//				String name = PathUtilities.extractPageName(layout.getPath());
//				if( name.indexOf("layout") > -1 )
//				{
//					fieldLayouts.add(layout);
//				}
//			}
//		}
//		
//	}
}
