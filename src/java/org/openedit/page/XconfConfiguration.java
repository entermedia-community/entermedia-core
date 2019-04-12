/*
 * Created on Oct 15, 2004
 */
package org.openedit.page;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.openedit.OpenEditException;
import org.openedit.OpenEditRuntimeException;
import org.openedit.config.Configuration;
import org.openedit.config.XMLConfiguration;
import org.openedit.util.XmlUtil;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class XconfConfiguration extends XMLConfiguration
{
	
	public XconfConfiguration() 
	{
		
	}

	public String getViewRequirementsXml()
	{
		XMLConfiguration conf = (XMLConfiguration)getViewRequirements();
		if( conf == null)
		{
			return null;
		}
		return asInnerXml("view-requirements");
	}
	public String getEditRequirementsXml()
	{
		XMLConfiguration conf = (XMLConfiguration)getEditRequirements();
		if( conf == null)
		{
			return null;
		}
		return asInnerXml("edit-requirements");
	}

	private String asInnerXml(String inName)
	{
		Element root = DocumentHelper.createElement(inName);
		XMLConfiguration conf = (XMLConfiguration)getChild(inName);
		conf.appendXml(conf,root);		
		StringWriter text = new StringWriter();
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding("UTF-8");
		XMLWriter out = new XMLWriter(text, format);
		try
		{
			out.write((Element)root.elementIterator().next());
		}
		catch (IOException ex)
		{
			throw new OpenEditRuntimeException(ex);
		}
		String xml = text.toString();
		return xml;

	}


	public Configuration getViewRequirements()
	{
		return getChild( "view-requirements" );
	}
	
	public Configuration getEditRequirements()
	{
		return getChild( "edit-requirements" );
	}
	
//	public String getContentFile()
//	{
//		return getChildValue( "contentfile" );
//	}
	public String getOverrideDirectory()
	{
		return getChildValue( "overridedirectory" );
	}
	
	public List getGenerators()
	{
		return getChildren("generator");
	}
	
	public Configuration getLayout()
	{
		return getChild("layout");
	}
	
	public Configuration getInnerLayout()
	{
		return getChild("inner-layout");
	}
	public Configuration getProperty(String inId)
	{
		Configuration config = selectSingleNode("//property[@name='" + inId + "']");
		return config;
	}
	
	public List getProperties()
	{
		return getChildren("property");
	}
	
	public List getPageActions()
	{
		return getChildren( "page-action" );
	}
	public List getPathActions()
	{
		return getChildren( "path-action" );
	}

	public List getScripts()
	{
		return getChildren( "script" );
	}

	public List getStyles()
	{
		return getChildren( "style" );
	}

	//More stuff
	protected String fieldWorkingDir;
	protected String fieldPagePath;
	public static final String VIEW_REQ = "view-requirements";
	public static final String WRITE_REQ = "edit-requirements";
	protected Configuration fieldConfig;
	protected List fieldStandardSettings;
	

	public void setConfig(Configuration inConfig)
	{
		fieldConfig = inConfig;
	}
	public String get(String inKey)
	{
		Configuration child = getChild(inKey);
		if (child != null)
		{
			return child.getValue();
		}
		return null;
	}
	public String getGeneratorChildValue(String inChildName)
	{
		//find this generator and get the name out
		Configuration prop =  selectSingleNode("//generator/" + inChildName);
		if (prop != null)
		{
			return prop.getValue();
		}
		return null;
	}

//	public boolean checkedAttrib(String inName, String inKey)
//	{
//		if (propertyValue( inKey) != null)
//		{
//			return true;
//		}
//		return false;
//	}
	public boolean checkedText(String inName, String inText)
	{
		for (Iterator iter = getChildIterator(inName); iter.hasNext();)
		{
			Configuration element = (Configuration) iter.next();
			if (inText.equalsIgnoreCase(element.getValue()))
			{
				return true;
			}
		}
		return false;
	}
	public String propertyValue( String inKey )
	{
		String val = null;
		for (Iterator iter = getChildIterator("property"); iter.hasNext();)
		{
			Configuration element = (Configuration) iter.next();
			if (inKey.equalsIgnoreCase(element.getAttribute("name")))
			{
				val = element.getValue();
				if( val == null )
				{
					//This is used for non languages based look ups
					for (Iterator iterator = element.getChildIterator("value"); iterator.hasNext();)
					{
						Configuration valconf = (Configuration) iterator.next();
						val = valconf.getValue();
						break;
					}
				}
			}
		}
		return val;
		
	}
	public boolean isTrueProperty(String inKey)
	{
		String value = propertyValue(inKey);
		if ( value != null )
		{
			return value.equalsIgnoreCase("true");
		}
		return  false;
	}
	public boolean isEmptyProperty(String inKey)
	{
		String value = propertyValue(inKey);
		if ( value != null )
		{
			return false;
		}
		return  true;
	}
	public boolean isFalseProperty(String inKey)
	{
		String value = propertyValue(inKey);
		if ( value != null )
		{
			return value.equalsIgnoreCase("false");
		}
		return  false;
	}
	public boolean isBlankLayout()
	{
		Configuration layout = getChild("layout");
		if ( layout != null )
		{
			if( layout.getValue() == null || 
				layout.getValue().length() == 0 ||
				Page.BLANK_LAYOUT.equals(layout.getValue()) )
			{
				return true;
			}
		}
		return false;
	}
	public boolean isBlankInnerLayout()
	{
		Configuration layout = getChild("inner-layout");
		if ( layout != null )
		{
			if( layout.getValue() == null || 
				layout.getValue().length() == 0 ||
				Page.BLANK_LAYOUT.equals(layout.getValue()) )
			{
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns the list of page actions as XML in UTF-8 encoding.
	 * 
	 * @return  An XML fragment
	 */
	public String listPageActions()
	{
		return outerXml("page-action");
	}
	
	public List getAllProperties()
	{
		List props = new ArrayList();
		for (Iterator iter = getChildIterator("property"); iter.hasNext();)
		{
			Configuration element = (Configuration) iter.next();
			props.add(element.getAttribute("name"));
		}
		props.remove("title");
		props.remove("keywords");
		props.remove("description");
		if( props.size() > 0)
		{
			props.add(0,"description");
		}
		else
		{
			props.add("description");
		}
		props.add(0,"keywords");
		props.add(0,"title");
		return props;
	}
	public List getUserProperties()
	{
		List props = new ArrayList();
		for (Iterator iter = getAllProperties().iterator(); iter.hasNext();)
		{
			String name  = (String) iter.next();
			if ( !getStandardSettings().contains(name) )
			{
				props.add(name);
			}
				
		}
		return props;
	}
	/**
	 * Method removeProperty.
	 * @param name
	 */
	public void removeProperty(String name)
	{
		//TODO how do we get the attribute out of here with XPATH
		Configuration prop = (Configuration) selectSingleNode("//property[@name='" + name + "']");
		if (prop != null)
		{
			prop.getParent().removeChild(prop);
		}
	}
	/**
	 * Method saveProperty.
	 * @param name
	 * @param value
	 */
	public void saveProperty(String name, String value, String inLocale)
	{
		//If its already here just update it. Otherwise add it
		//if value is null remove property altogether
		if (value == null || value.length() == 0)
		{
			removeProperty(name);
		}
		else
		{
			Configuration prop =
				(Configuration) selectSingleNode("//property[@name='" + name + "']");
			if( inLocale != null && inLocale.length() == 0)
			{
				inLocale = null;
			}
			if (prop == null)
			{
				prop = addChild("property");
				prop.setAttribute("name", name);
				if( inLocale == null )
				{
					prop.setValue(value);
					return;
				}
			}
			boolean existing = false;
			String defaultval = prop.getValue();
			//clear default prop
			if( defaultval != null)
			{
				Configuration inline = prop.addChild("value");
				inline.setValue(defaultval);
				prop.setValue(null);
			}
			
			for (Iterator iter = prop.getChildIterator("value"); iter.hasNext();)
			{
				Configuration  valueconfig = (Configuration ) iter.next();
				String vlocale = valueconfig.getAttribute("locale");
				if( inLocale == null && vlocale == null)
				{
					valueconfig.setValue(value);
					existing = true;
					break;
				}
				if( vlocale != null && vlocale.equals(inLocale))
				{
					valueconfig.setValue(value);
					existing = true;
					break;					
				}
			}
			if( !existing)
			{
				//add new
				Configuration valueconf = prop.addChild("value");
				valueconf.setAttribute("locale", inLocale);
				valueconf.setValue(value);
			}
		}
	}
	/**
	 * @param xpath
	 * @return
	 */
	private Configuration selectSingleNode(String xpath)
	{
		if ( xpath == null)
		{
			return null;
		}
//		property[@name='" + name + "']
		//TODO: Implement this soon
		if( xpath.indexOf('[') == -1)
		{
			//generator/stylesheet
			//then this must be a simple look up
			String[] path = xpath.split("\\/");
			Configuration parent = this; //TODO: go to root level
			for (int i = 0; i < path.length; i++)
			{
				if ( path[i].trim().length() == 0)
				{
					continue;
				}
				Configuration child = parent.getChild(path[i]);
				if ( child == null)
				{
					return parent; 
				}
				parent = child;
			}
		}
		else
		{
			String child = xpath.substring(2,xpath.indexOf('['));
			String attrib = xpath.substring(xpath.indexOf('@') + 1,xpath.indexOf('='));
			String value = xpath.substring(xpath.indexOf('\'') + 1,xpath.lastIndexOf('\''));
			for (Iterator iter = getChildren(child).iterator(); iter.hasNext();)
			{
				Configuration element = (Configuration) iter.next();
				if ( value.equals( element.getAttribute(attrib)))
					{
					return element;
					}
			}
		}
		return null;
	}

	public boolean isMissing(String inConfiguration)
	{
		Configuration elem = getChild(inConfiguration);
		if (elem == null )
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	public void saveConfiguration(String inName, String inText)
	{
		
		if ( inText != null && inText.trim().length() == 0)
		{
			inText = null;
		}
		
		Configuration prop = getChild(inName);
		if (prop != null)
		{
			if ( inText != null)
			{
				prop.setValue(inText);
			}
			else
			{
				removeChild(prop);
			}
		}			
		else if ( inText != null)
		{
			addChild(inName).setValue(inText);
		}
	}
	
	/**
	 * Method removeConfigurations.
	 * @param string
	 */
	public void removeConfigurations(String inName)
	{
		for (Iterator iter = getChildren(inName).iterator(); iter.hasNext();)
		{
			Configuration elem = (Configuration) iter.next();
			elem.getParent().removeChild(elem);
		}
	}
	
	protected String outerXml(String inField)
	{
		StringWriter wri = new StringWriter();
		for (Iterator iter = getChildIterator(inField); iter.hasNext();)
		{
			XMLConfiguration read = (XMLConfiguration) iter.next();
			wri.write(read.toXml("UTF-8"));
		}
		return wri.toString();
	}

	protected String innerXml(String inField)
	{
		Configuration read = getChild(inField);
		if (read != null)
		{
			StringWriter wri = new StringWriter();

			for (Iterator iter = read.getChildren().iterator(); iter.hasNext();)
			{
				XMLConfiguration child = (XMLConfiguration) iter.next();
				wri.write(child.toXml("UTF-8"));
			}
			return wri.toString();
		}
		return null;
	}
	public String getWritePermissions()
	{
		return innerXml(WRITE_REQ);
	}
	/*
	 * This allows someone to reset the value in XML
	 */
	public void setReadPermissions(String inXML) throws OpenEditException
	{
		resetValueWithXml(VIEW_REQ, inXML);
	}
	protected void resetValueWithXml(String inField, String inXML) throws OpenEditException
	{
		try
		{
			Configuration read = getChild(inField);
			if (read != null)
			{
				removeChild(read);
			}
			//			
			if (inXML != null && inXML.length() > 0)
			{
				appendXml("<" + inField + ">\n" + inXML + "\n</" + inField + ">");
			}
		}
		catch (Exception ex)
		{
			throw new OpenEditException(ex);
		}
	}
	public void setAllActions(String inXML) throws OpenEditException
	{
			removeConfigurations("page-action");
			appendXml(inXML);
	}

	/**
	 * @param inFilename
	 */
	public void removeScriptAction(String inFilename)
	{
		//make sure its not already in there
		for (Iterator iter = getChildIterator("page-action"); iter.hasNext();)
		{
			Configuration element = (Configuration) iter.next();
			if ("Script.run".equals(element.getAttribute("name")))
			{
				for (Iterator iterator = element.getChildIterator("property"); iterator.hasNext();)
				{
					Configuration prop = (Configuration) iterator.next();
					if ("code".equals(prop.getAttribute("name"))
						&& inFilename.equals(prop.getValue()))
					{
						removeChild(element);
					}
				}
			}
		}

	}

	public void appendScriptAction(String inCode) throws OpenEditException
	{
		removeScriptAction(inCode);
		Configuration top = addChild("page-action");
		top.setAttribute("name", "Script.run");
		Configuration prop = top.addChild("property");
		prop.setAttribute("name", "code");
		prop.setValue(inCode);
	}
	protected void appendXml(String inXML) throws OpenEditException
	{
		if (inXML != null && inXML.length() > 0)
		{
			StringReader in = new StringReader("<tmp>" + inXML + "</tmp>");
			XMLConfiguration tmpconfig = new XMLConfiguration();
			Element child = new XmlUtil().getXml(in, "UTF-8");
			tmpconfig.populate(child);
			for (Iterator iter = tmpconfig.getChildren().iterator(); iter.hasNext();)
			{
				Configuration element = (Configuration) iter.next();
				addChild(element);
			}
		}
	}

	public void setWritePermissions(String inXML) throws OpenEditException
	{
		resetValueWithXml(WRITE_REQ, inXML);
	}
	public boolean isEmpty()
	{
		return 
			getChildren() == null
			|| getChildren().size() == 0;
	}


//
//	public void setContentFile(String inContentFile)
//	{
//		saveConfiguration("contentfile",inContentFile);
//	}

	/**
	 * 
	 */
	public void removeAllProperties()
	{
		List props = new ArrayList();
		for (Iterator iter = getChildIterator("property"); iter.hasNext();)
		{
			Configuration element = (Configuration) iter.next();
			props.add(element );
		}
		for (Iterator iter = props.iterator(); iter.hasNext();)
		{
			Configuration element = (Configuration) iter.next();
			removeChild(element);
			
		}

	}

	public List getStandardSettings()
	{
		if ( fieldStandardSettings == null)
		{	//TODO: Move to external config
			fieldStandardSettings = new ArrayList();
//			fieldStandardSettings.add("title");
//			fieldStandardSettings.add("keywords");
//			fieldStandardSettings.add("description");
			fieldStandardSettings.add("editable");  
			fieldStandardSettings.add("showToolbar");
			fieldStandardSettings.add("saveasxhtml");  
			//fieldStandardSettings.add("contentfile");
			fieldStandardSettings.add("encoding");
			fieldStandardSettings.add("mimetype");
			fieldStandardSettings.add("overridedirectory");
			fieldStandardSettings.add("virtual");  
		}
		return fieldStandardSettings;
	}
	public void setStandardSettings(List inStandardSettings)
	{
		fieldStandardSettings = inStandardSettings;
	}

	/**
	 * @param inString
	 */
	public void removeElements(String inString)
	{
		Configuration config = getChild(inString);
		while(config != null )
		{
			removeChild(config);
			config = getChild(inString);
		}
	}

}
