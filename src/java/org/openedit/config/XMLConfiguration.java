/*
 * Created on Oct 26, 2004
 */
package org.openedit.config;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.openedit.OpenEditRuntimeException;
import org.openedit.util.XmlUtil;

/**
 * @author cburkey
 *
 */
public class XMLConfiguration implements Configuration
{
	private static final Log log = LogFactory.getLog(XMLConfiguration.class);
	protected HashMap fieldAttributes;
	protected String fieldText;
	protected String fieldName;
	protected List fieldChildren;
	protected Configuration fieldParent;
	
	/**
	 * 
	 */
	public XMLConfiguration()
	{
	}

	public XMLConfiguration(String inName)
	{
		setName(inName);
	}
	
	/**
	 * @param inRootElement
	 */
	public void populate(Element inRootElement)
	{
		setName(inRootElement.getName());
		setValue(inRootElement.getText());
		
		for (Iterator iter = inRootElement.attributeIterator(); iter.hasNext();)
		{
			Attribute attrib = (Attribute) iter.next();
			addAttribute(attrib.getName(),attrib.getValue());
		}
		
		for (Iterator iter = inRootElement.elementIterator(); iter.hasNext();)
		{
			Element	 element = (Element) iter.next();
			
			addChild(new XMLConfiguration(element));
		}

	}

	/**
	 * @param inConfiguration
	 */
	public Configuration addChild(Configuration inConfiguration)
	{
		if ( fieldChildren == null)
		{
			fieldChildren = new ArrayList();
		}
		fieldChildren.add( inConfiguration);
		inConfiguration.setParent(this);
		return inConfiguration;
	}

	/**
	 * @param inName
	 * @param inValue
	 */
	private void addAttribute(String inName, String inValue)
	{
		if ( fieldAttributes == null)
		{
			fieldAttributes = new HashMap();
		}
		fieldAttributes.put( inName, inValue);
	}
	
	/**
	 * @param inChild
	 */
	public XMLConfiguration(Element inChild)
	{
		populate(inChild);
	}

	public XMLConfiguration(Reader inRead)
	{
		readXML( inRead);
	}

	/* (non-javadoc)
	 * @see com.anthonyeden.lib.config.Configuration#getChildren(java.lang.String)
	 */
	public List getChildren(String inString)
	{
		//this is annoying. We are going to create fake dom4j nodes
		List hits = new ArrayList(realChildren().size());
		for (Iterator iter = realChildren().iterator(); iter.hasNext();)
		{
			XMLConfiguration element = (XMLConfiguration) iter.next();
			if ( inString.equals( element.getName()) )
			{
				hits.add( element);
			}
		}
		
		
		return hits;
	}


	/* (non-javadoc)
	 * @see com.anthonyeden.lib.config.Configuration#getAttribute(java.lang.String)
	 */
	public String get(String inKey)
	{
		return getAttribute(inKey);
	}
	public String getAttribute(String inString)
	{
		return (String)getAttributes().get( inString);
	}
	public void setAttribute(String inKey, String inValue)
	{
		getAttributes().put(inKey,inValue);
	}
	/* (non-javadoc)
	 * @see com.anthonyeden.lib.config.Configuration#getValue()
	 */
	public String getValue()
	{
		return fieldText;
	}
	public void setValue( String inValue)
	{
		fieldText = inValue;
		if ( fieldText != null)
		{
			fieldText = fieldText.trim();
			if ( fieldText.length() == 0)
			{
				fieldText = null;
			}
		}
	}

	/* (non-javadoc)
	 * @see com.anthonyeden.lib.config.Configuration#getChild(java.lang.String)
	 */
	public Configuration getChild(String inString)
	{
		for (Iterator iter = realChildren().iterator(); iter.hasNext();)
		{
			XMLConfiguration config = (XMLConfiguration) iter.next();
			if ( inString.equals( config.getName()  ) )
			{
				return config;
			}
		}
		return null;
	}
	protected List realChildren()
	{
		if (fieldChildren == null)
		{
			fieldChildren = new ArrayList();
			
		}

		return fieldChildren;
	}

	/* (non-javadoc)
	 * @see com.anthonyeden.lib.config.Configuration#getChildren()
	 */
	public List getChildren()
	{

		return realChildren();
		
	}

	/* (non-javadoc)
	 * @see com.anthonyeden.lib.config.Configuration#getChildValue(java.lang.String)
	 */
	public String getChildValue(String inString)
	{
		Configuration config = getChild(inString);
		if ( config != null)
		{
			return config.getValue();
		}
		return null;
	}

	/**
	 * @return
	 */
	public List getAttributeNames()
	{
		Set attribs = getAttributes().keySet();
		List names = new ArrayList(attribs.size());
		
		for (Iterator iter = attribs.iterator(); iter.hasNext();)
		{
			String a = (String) iter.next();
			names.add( a );
		}
		return names;
	}


	public HashMap getAttributes()
	{
		if (fieldAttributes == null)
		{
			fieldAttributes = new HashMap(2);
		}
		return fieldAttributes;
	}
	public void setAttributes(HashMap inAttributes)
	{
		fieldAttributes = inAttributes;
	}
	public String getName()
	{
		return fieldName;
	}
	public void setName(String inName)
	{
		fieldName = inName;
	}

	/**
	 * @param inString
	 * @return
	 */
	public Configuration addChild(String inString)
	{
		XMLConfiguration config = new XMLConfiguration(inString);
		addChild(config);
		return config; 
	}

	/**
	 * @param inString
	 * @return
	 */
	public Iterator getChildIterator(String inString)
	{
		return getChildren(inString).iterator();
	}
	/**
	 * @param inElement
	 */
	public void removeChild(Configuration inElement)
	{
		getChildren().remove(inElement);
	}

	public Configuration getParent()
	{
		return fieldParent;
	}
	public void setParent(Configuration inParent)
	{
		fieldParent = inParent;
	}
	public String toString()
	{
		return toXml("UTF-8");
	}
	/**
	 * Returns this configuration as an XML document with the specified
	 * encoding.
	 * 
	 * @param inEncoding  The encoding
	 * 
	 * @return  An XML document
	 */
	public String toXml( String inEncoding )
	{
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement(getName());
		appendXml(this,root);
		StringWriter text = new StringWriter();
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding(inEncoding);
		XMLWriter out = new XMLWriter(text, format);
		try
		{
			out.write(doc);
		}
		catch (IOException ex)
		{
			throw new OpenEditRuntimeException(ex);
		}
		return text.toString();
	}
	public Document asXml()
	{
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement(getName());
		appendXml(this,root);
		return doc;
	}

	
	/**
	 * @param inConfiguration
	 * @param inRoot
	 */
	public void appendXml(XMLConfiguration inConfiguration, Element inElement)
	{
		for (Iterator iter = inConfiguration.getAttributeNames().iterator(); iter.hasNext();)
		{
			String id = (String) iter.next();
			inElement.addAttribute(id,inConfiguration.getAttribute(id));
		}		
		for (Iterator iter = inConfiguration.getChildren().iterator(); iter.hasNext();)
		{
			XMLConfiguration child = (XMLConfiguration) iter.next();
			appendXml(child,inElement.addElement(child.getName()));
		}
		if ( inConfiguration.getValue() != null && inConfiguration.getValue().trim().length() > 0)
		{
			inElement.setText(inConfiguration.getValue());
		}
	}
	public boolean hasChildren()
	{
		return fieldChildren != null && getChildren().size() > 0;
	}

	/**
	 * @param inString
	 * @return
	 */
	public boolean hasChild(String inString)
	{
		return getChild(inString) != null;
	}
	/**
	 * Method should be avoided due to slow performance without shared SaxReader
	 * @param inReader
	 */
	public void readXML(Reader inReader)
	{
		XmlUtil util = new XmlUtil();
		Element root = util.getXml(inReader, "UTF-8");
		populate(root);
	}
}
