package org.openedit.xml;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.openedit.Data;

import com.openedit.hittracker.HitTracker;

public class XmlHitTracker extends HitTracker
{
	protected XmlFile fieldXmlFile;
	
	public XmlHitTracker()
	{
		
	}
	public XmlHitTracker(XmlFile inXmlFile)
	{
		setXmlFile(inXmlFile);
	}

	public boolean contains(Object inHit)
	{
		return getXmlFile().getElements().contains(inHit);
	}

	public Data get(int inCount) 
	{
		Element element = (Element)getXmlFile().getElements().get(inCount);
		if( element == null)
		{
			return null;
		}
		return new ElementData(element);
	}
	//Use getByID
	public Object get(String inId) throws IOException
	{
		return getById(inId);
	}

	public Data getById(String inId)
	{
		Element element = getXmlFile().getElementById(inId);
		if( element == null)
		{
			return null;
		}
		return toData(element);
		
	}
	public int size()
	{
		return getXmlFile().size();
	}

	public Iterator iterator()
	{
		return new ElementDataIterator(getXmlFile().getElements());
	}

	public XmlFile getXmlFile()
	{
		return fieldXmlFile;
	}

	public void setXmlFile(XmlFile inXmlFile)
	{
		fieldXmlFile = inXmlFile;
		setPage(1);
	}
	public List keys()
	{
		return getXmlFile().keys();
	}
	public Data toData(Object inHit)
	{
		if( inHit instanceof Data)
		{
			return (Data)inHit;
		}
		return new ElementData(inHit);
	}
	
	public String getValue(Object inHit, String inString)
	{
		Element target = (Element)inHit;
		return target.attributeValue(inString);
		

	}

	
	
}
