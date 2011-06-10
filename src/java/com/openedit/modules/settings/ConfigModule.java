/*
 * Created on Jan 5, 2005
 */
package com.openedit.modules.settings;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.repository.filesystem.StringItem;

import com.openedit.OpenEditException;
import com.openedit.WebPageRequest;
import com.openedit.config.Configuration;
import com.openedit.modules.edit.BaseEditorModule;
import com.openedit.modules.translations.Language;
import com.openedit.modules.translations.Translation;
import com.openedit.page.Page;
import com.openedit.page.PageSettings;
import com.openedit.page.XconfConfiguration;

/**
 * @author cburkey
 *
 */
public class ConfigModule extends BaseEditorModule
{
	private static final Log log = LogFactory.getLog(ConfigModule.class);
	
	public void saveProperties(WebPageRequest inReq) throws OpenEditException
	{
		ConfigEditorSession session = readConfig(inReq);
		PageSettings settings = session.getEditPage().getPageSettings();
		
		// Add the user home
		String[] fields = inReq.getRequestParameters("field");
		for (int i = 0; i < fields.length; i++)
		{
			String value = inReq.getRequestParameter(fields[i] + ".value");
			settings.setProperty(fields[i], value);
		}
		getPageManager().saveSettings(session.getEditPage());
	}
	
	public ConfigEditorSession readConfig(WebPageRequest inReq) throws OpenEditException
	{
		try
		{
			//The GUI needs to send us in the path to the xconf file somehow
			String path = inReq.getRequestParameter("editPath");
			if( path == null)
			{
				return null;
			}
			Page webPage = getPageManager().getPage(path);
			String editPath = webPage.getPageSettings().getXConf().getPath(); //get .xconf path
			
			Page editPage = getPageManager().getPage(editPath,true);

			XconfConfiguration config = new XconfConfiguration();
			//find the correct xconf for this type
			ConfigEditorSession session = (ConfigEditorSession)inReq.getPageValue("configeditsession");
			if( session == null)
			{
				session = new ConfigEditorSession();
			}
			session.setEditPage(editPage);
			PageSettings settings = editPage.getPageSettings();

			if( settings.exists() )
			{				
				config.readXML(settings.getReader());
			}
			else
			{
				config.setName("page");
			}
			String windowname = inReq.getRequestParameter("parentName");
			if( windowname == null && session != null && session.getEditPath() == editPath )
			{
				//reload the data and return
				session.setConfig(config);
				return session;
			}
			
			session.setConfig(config);
			
			session.setParentName(windowname);
			
			String origURL = inReq.getRequestParameter("origURL");
			session.setOriginalUrl(origURL);

			inReq.putPageValue("configeditsession", session);
			
			Translation trans = (Translation) inReq.getPageValue("translations");
			if( trans != null)
			{
				for (Iterator iter = config.getAllProperties().iterator(); iter.hasNext();)
				{
					String id = (String) iter.next();
					Configuration propconfig = config.getProperty(id);
					if( propconfig != null)
					{
						for (Iterator iterator = propconfig.getChildIterator("value"); iterator.hasNext();)
						{
							Configuration val = (Configuration) iterator.next();
							String local = val.getAttribute("locale");
							if( local != null)
							{
								Language lang = trans.getLanguage(local);
								if ( lang == null)
								{
									lang = new Language();
									lang.setName(local);
									lang.setId(local);
									trans.addLanguage(lang);
								}
							}
						}
					}
				}
			}
			return session;
		}
		catch (Throwable ex)
		{
			throw new OpenEditException(ex);
		}
	}
	public void saveConfigChanges(WebPageRequest inReq) throws Exception
	{
		//they already selected the Xconf from a menu
		ConfigEditorSession session = readConfig(inReq);

		XconfConfiguration config = session.getConfig();
		
		HtmlToXconfReader converter = new HtmlToXconfReader();
		converter.setAdvancedMode(inReq.getUser().hasPermission("oe.edit.settings.advanced"));
		converter.saveChangesToConfig(inReq, config );
		
		//If there was no config there before and its still empty then dont bother saving a new config
		if ( config.isEmpty())
		{
			//delete it
			getPageManager().removePage(session.getEditPage()); //delete xconf
		}
		else
		{
			
			String xml = config.toXml(session.getEditPage().getCharacterEncoding());
			StringItem content = new StringItem(
				session.getEditPath(), xml ,session.getEditPage().getCharacterEncoding());
			content.setAuthor(inReq.getUser().getUserName());
			content.setMessage("Edited settings");
			Page site = getPageManager().getPage(session.getEditPath());
			site.setContentItem(content);
			getPageManager().putPage(site);	
		}
	}

}
