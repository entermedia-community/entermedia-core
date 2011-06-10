
package com.openedit.util;

import org.openedit.repository.ContentItem;

import com.openedit.config.XMLConfiguration;
import com.openedit.page.PageSettings;
import com.openedit.users.User;

public class TextReplacer extends PathProcessor
{
	protected String fieldFind;
	protected String fieldReplace;
	
	public TextReplacer(String inFind, String inReplace)
	{
		fieldFind = inFind;
		fieldReplace = inReplace;
	}
	public void processFile(ContentItem inContent, User inUser)
	{
		PageSettings settings = getPageManager().getPageSettingsManager().getPageSettings(inContent.getPath());
		XMLConfiguration conf = (XMLConfiguration) settings.getUserDefined(fieldFind);
		if(conf != null)
		{
			settings.getUserDefinedData().removeChild(conf);
			conf.setName(fieldReplace);
			settings.getUserDefinedData().addChild(conf);
			getPageManager().getPageSettingsManager().saveSetting(settings);
		}
	}
}
