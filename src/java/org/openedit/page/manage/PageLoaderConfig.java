package org.openedit.page.manage;

import org.openedit.config.Configuration;

public class PageLoaderConfig
{
	protected Configuration fieldXmlConfig;
	public Configuration getXmlConfig()
	{
		return fieldXmlConfig;
	}
	public void setXmlConfig(Configuration inXmlConfig)
	{
		fieldXmlConfig = inXmlConfig;
	}
}
