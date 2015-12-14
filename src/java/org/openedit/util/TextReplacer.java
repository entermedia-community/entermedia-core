
package org.openedit.util;

import org.openedit.config.XMLConfiguration;
import org.openedit.page.PageSettings;
import org.openedit.repository.ContentItem;
import org.openedit.users.User;

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
