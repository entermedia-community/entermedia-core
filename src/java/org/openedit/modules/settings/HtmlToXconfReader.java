/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package org.openedit.modules.settings;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openedit.OpenEditException;
import org.openedit.WebPageRequest;
import org.openedit.config.Configuration;
import org.openedit.config.XMLConfiguration;
import org.openedit.page.Page;
import org.openedit.page.XconfConfiguration;


/**
 * This action saves the configuration file for the page with the path given by the
 * <samp>path</samp> request parameter, where the configuration file's content is given by the
 * <samp>content</samp> request parameter.
 *
 * @author Eric Galluzzo
 */
public class HtmlToXconfReader 
{
	protected boolean fieldAdvancedMode;
	
	public void saveChangesToConfig(WebPageRequest inReq, XconfConfiguration inConfig) throws OpenEditException
	{
		Map props = inReq.getParameterMap();
		
		//Updated to exsting properties
		Configuration oldeditable = inConfig.getProperty("editable");
		inConfig.removeAllProperties();
		
		saveProperties(inReq, inConfig);

		if ( isAdvancedMode() )
		{
			saveStandard(props, inConfig);
			saveLayout(props, inConfig);
			savePermissions( props, inConfig);
		}
		else
		{
			if ( oldeditable != null)
			{
				inConfig.saveProperty("editable",oldeditable.getValue(),null);
			}
		}
		//saveGenerators(props, inConfig);
	}

	protected void savePermissions(Map inProps, XconfConfiguration inConfig)
	{
		String view = (String)inProps.get("view-requirements");
		inConfig.removeElements("view-requirements");
		if( view != null && view.trim().length() > 0)
		{
			Configuration nconf = inConfig.addChild("view-requirements");
			StringReader read = new StringReader(view);
			nconf.addChild(new XMLConfiguration(read) );
		}

		String edit = (String)inProps.get("edit-requirements");
		inConfig.removeElements("edit-requirements");
		if( edit != null && edit.trim().length() > 0)
		{
			Configuration nconf = inConfig.addChild("edit-requirements");
			StringReader read = new StringReader(edit);
			nconf.addChild(new XMLConfiguration(read) );
		}
	}

//	protected void saveContentFile(Map inProperties, XconfConfiguration inConfig)
//	{
//		//content file
//		String contentfile = (String) inProperties.get("contentfile");
//
//		inConfig.setContentFile(contentfile);
//	}

	protected void saveGenerators(Map inProperties, XconfConfiguration inConfig)
	{
		//generator 
		inConfig.removeElements("generator");

		String generatorKey = "generator";

		//the chices are velocity-xslt xslt velocity jsp 
		String generator = (String) inProperties.get(generatorKey);

		if ((generator == null) || (generator.length() == 0) || "default".equals(generator))
		{
			return;
		}
		else
		{
			XMLConfiguration currentGen = (XMLConfiguration)inConfig.addChild("generator");
			String xslt = (String) inProperties.get(generatorKey + ".xslt");

			currentGen.setAttribute("name", generator);
			
			
			//TODO: We lost support for compound genertors
		}
	}

	protected void saveNewProperty(WebPageRequest inReq, XconfConfiguration inConfig, int inCount)
	{
		//We need two things, The name and the value
		String name = inReq.getRequestParameter("newproperty." + inCount + ".name");

		String value = inReq.getRequestParameter("newproperty." + inCount + ".value");

		if ((value != null) && (value.length() != 0))
		{
			String locale = inReq.getRequestParameter("newproperty." + inCount + ".language");
			inConfig.saveProperty(name, value, locale);
		}
		else
		{
			inConfig.removeProperty(name);
		}
	}

	protected void saveProperties(WebPageRequest inReq, XconfConfiguration inConfig)
	{
		
		Map props = inReq.getParameterMap();


		Set set = props.keySet();
		List keys = new ArrayList(set);
//		Collections.sort(keys, new Comparator() {
//			public int compare(Object inO1, Object inO2)
//			{
//				String one = (String)inO1;
//				String two = (String)inO2;
//				if( one.startsWith("property") && two.startsWith("property") && one.endsWith(".name") && two.endsWith(".name"))
//				{
//					String count1 = one.substring("property".length() + 1, one.lastIndexOf("."));
//					String count2 = two.substring("property".length() + 1, two.lastIndexOf("."));
//					return new Integer(count1).compareTo(new Integer(count2));
//				}
//				return 0;
//			}
//		});
		
		for (int i = 0; i < keys.size(); i++)
		{
			String propname = inReq.getRequestParameter("property." + i + ".name");
			if( propname != null && propname.length() > 0 )
			{				
				String valuetag = "property." + i + ".value";
				String[] values = inReq.getRequestParameters(valuetag);
				String langtag = "property." + i + ".language";
				String[] locales = inReq.getRequestParameters(langtag);
				
				if( locales == null && values.length > 0)
				{
					inConfig.saveProperty(propname, values[0], null);
				}
				else
				{
					if ((values != null) && (values.length > 0))
					{
						for (int v = 0; v < values.length; v++)
						{
							String value = values[v];
							if( value != null && value.length() > 0)
							{
								if( value.equals("on"))
								{
									value = "true";
									
								}
								String locale = locales[v];
								inConfig.saveProperty(propname, value, locale);
							}
						}
					}
				}
			}
		}
		//Now loop over new properties looking for any properties that start with newprop..
		saveNewProperty(inReq, inConfig, 1);
		saveNewProperty(inReq, inConfig, 2);
	}

	protected void saveLayout(Map inProperties, XconfConfiguration inConfig)
	{
		//Save the template
		String layout = (String) inProperties.get("layout");
		inConfig.removeConfigurations("layout");

		if ( layout != null && layout.equals(Page.BLANK_LAYOUT))
		{
			inConfig.addChild("layout");
		}
		else if (layout != null && layout.length() > 0)
		{   
			inConfig.addChild("layout").setValue(layout);
		}
		
		String innerlayout = (String) inProperties.get("inner-layout");
		inConfig.removeConfigurations("inner-layout");
		if ( innerlayout != null && innerlayout.equals(Page.BLANK_LAYOUT))
		{
			inConfig.addChild("inner-layout");
		}
		else if (innerlayout != null && innerlayout.length() > 0)
		{   
			inConfig.addChild("inner-layout").setValue(innerlayout);
		}
		String customlayout = (String) inProperties.get("custominnerlayout");
		if( customlayout != null && customlayout.length() > 0)
		{
			inConfig.removeConfigurations("inner-layout");
			if( customlayout.startsWith("/WEB-INF/base"))
			{
				customlayout = customlayout.substring("/WEB-INF/base".length());
			}
			inConfig.addChild("inner-layout").setValue(customlayout);
		}
		
	}
	
	protected void saveStandard(Map inProperties, XconfConfiguration inConfig)
	{
		String editable = (String) inProperties.get("editable");
		if ( editable != null && editable.length() > 0)
		{
			if( editable.equals("ok"))
			{
				editable = "true";
			}
			inConfig.saveProperty("editable",editable,null);
		}
		
	}

	public boolean isAdvancedMode()
	{
		return fieldAdvancedMode;
	}

	public void setAdvancedMode(boolean inAdvancedMode)
	{
		fieldAdvancedMode = inAdvancedMode;
	}

}
