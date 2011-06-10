package com.openedit.page.manage;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.openedit.repository.ContentItem;
import org.openedit.repository.ReaderItem;

import com.openedit.OpenEditException;
import com.openedit.config.Configuration;
import com.openedit.page.Page;
import com.openedit.page.PageAction;
import com.openedit.page.PageProperty;
import com.openedit.page.PageSettings;
import com.openedit.page.Permission;
import com.openedit.page.XconfConfiguration;
import com.openedit.util.XmlUtil;
import com.openedit.util.strainer.FilterWriter;

/**
 * @author cburkey
 *
 */
public class PageSettingsToXconfWriter
{
	private static final Log log = LogFactory.getLog(PageSettingsToXconfWriter.class);
	protected FilterWriter fieldFilterWriter;
	
	protected XmlUtil fieldXmlUtil = new XmlUtil();
	/**
	 * @param inSetting
	 * @return
	 */
	public ContentItem createXConf(PageSettings inSetting) throws OpenEditException
	{
		//need to make an xconf string
		XconfConfiguration config = new XconfConfiguration();
		config.setName("page");
		//start by reading old xconf since there could be extra info in it
		if (inSetting.getXConf().exists())
		{
			config.populate(fieldXmlUtil.getXml(inSetting.getXConf().getInputStream(),inSetting.getPageCharacterEncoding()));
		}
		//now update the config 
		saveProperties(inSetting, config);
		saveLayout(inSetting, config);
		saveInnerLayout(inSetting, config);
		savePermissions(inSetting, config);
		savePathActions(inSetting, config);
		savePageActions(inSetting, config);
		saveUserDefinedData(inSetting, config);
		StringWriter out = new StringWriter();
		Document root = config.asXml();
		fieldXmlUtil.saveXml(root, out, inSetting.getPageCharacterEncoding());
		
		ReaderItem results = new ReaderItem(inSetting.getPath(), new StringReader(out.toString()), inSetting.getPageCharacterEncoding());
		return results;
	}

	protected void saveUserDefinedData(PageSettings inSettings, XconfConfiguration inConfig)
	{
		if( inSettings.getUserDefinedData() != null)
		{
			for( Object o: inSettings.getUserDefinedData().getChildren())
			{
				inConfig.addChild((Configuration) o);
			}
		}
	}
	
	protected void saveProperties(PageSettings inProperties, XconfConfiguration inConfig)
	{
		//Updated to exsting properties
		inConfig.removeAllProperties();
	
		for (Iterator iter = inProperties.getProperties().values().iterator(); iter.hasNext();)
		{
			PageProperty element = (PageProperty) iter.next();
			for (Iterator iterator = element.getValues().keySet().iterator(); iterator.hasNext();)
			{
				String lang = (String) iterator.next();
				String val = (String)element.getValues().get(lang);
				inConfig.saveProperty(element.getName(), val, lang);				
			}
		}
//		inConfig.removeProperty("encoding");
//		if( inProperties.getFieldPageCharacterEncoding() != null)
//		{
//			inConfig.saveProperty("encoding",inProperties.getFieldPageCharacterEncoding(), null);
//		}
	}
	protected void saveLayout(PageSettings inPageSetting, XconfConfiguration inConfig)
	{
		//Save the template
		String layout = inPageSetting.getFieldLayout();
		inConfig.removeConfigurations("layout");
		if (layout != null)
		{
			if ( Page.BLANK_LAYOUT.equals( layout ) )
			{
				//add a blank layout
				inConfig.addChild("layout");
			}
			else
			{
				inConfig.addChild("layout").setValue(layout);
			}
		}
	}
	protected void saveInnerLayout(PageSettings inPageSetting, XconfConfiguration inConfig)
	{
		//Save the template
		String innerLayout = inPageSetting.getFieldInnerLayout();
		if ( innerLayout != null && !innerLayout.equals(""))
		{
			inConfig.removeConfigurations("inner-layout");
			inConfig.addChild("inner-layout").setValue(innerLayout);
		}
	}
		/** Handle this one day
		inConfig.removeElements("generator");


		//the choices are velocity-xslt xslt velocity jsp 
		if ( inPageSetting.getFieldGenerator() != null)
		{
			inPageSetting.getFieldGenerator();
			for (Iterator iter = .iterator(); iter.hasNext();)
			{
				Generator gen = (Generator) iter.next();
				//TODO: if ( gen instanceof NestedGenerator) handle nested and mimetypes
				
				String generator = gen.getName();
	
				XMLConfiguration currentGen = (XMLConfiguration) inConfig.addChild("generator");
				currentGen.setAttribute("name", generator);
				//TODO: currentGen.setAttribute("mimetypes", generator.);
			}
		}
		*/
//	protected void saveContentFile(PageSettings inPageSetting, XconfConfiguration inConfig)
//	{
//		//content file 
//		String contentfile = (String) inPageSetting.getFieldAlternativeContentPath();
//
//		inConfig.setContentFile(contentfile);
//	}

	
	protected void savePermissions(PageSettings inPageSetting, XconfConfiguration inConfig)
	{
		inConfig.removeConfigurations("permission");
		inConfig.removeConfigurations("edit-requirements");
		inConfig.removeConfigurations("view-requirements");


		//the choices are velocity-xslt xslt velocity jsp 
		if ( inPageSetting.getFieldPermissions() != null)
		{
			for (Iterator iter = inPageSetting.getFieldPermissions().iterator(); iter.hasNext();)
			{
				Permission	per = (Permission) iter.next();
				getFilterWriter().writeFilterCollection(per, inConfig);
			}
		}
	}

		public FilterWriter getFilterWriter()
		{
			if (fieldFilterWriter == null)
			{
				fieldFilterWriter = new FilterWriter();
			}
			return fieldFilterWriter;
		}

		public void setFilterWriter(FilterWriter inFilterWriter)
		{
			fieldFilterWriter = inFilterWriter;
		}

		protected void savePathActions(PageSettings inPageSetting, XconfConfiguration inConfig)
		{
			inConfig.removeConfigurations("path-action");

			if ( inPageSetting.getFieldPathActions() != null)
			{
				for (Iterator iter = inPageSetting.getFieldPathActions().iterator(); iter.hasNext();)
				{
					PageAction	action = (PageAction) iter.next();
					Configuration config = action.getConfig();
					config.setName("path-action");
					inConfig.addChild(config);
				}
			}
		}
		protected void savePageActions(PageSettings inPageSetting, XconfConfiguration inConfig)
		{
			inConfig.removeConfigurations("page-action");

			if ( inPageSetting.getFieldPageActions() != null)
			{
				for (Iterator iter = inPageSetting.getFieldPageActions().iterator(); iter.hasNext();)
				{
					PageAction	action = (PageAction) iter.next();
					Configuration config = action.getConfig();
					config.setName("page-action");
					inConfig.addChild(config);
				}
			}
		}

		
	
}
