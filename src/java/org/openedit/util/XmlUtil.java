/*
 * Created on May 19, 2005
 */
package org.openedit.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.util.PerThreadSingleton;
import org.openedit.OpenEditException;
import org.openedit.OpenEditRuntimeException;
import org.openedit.config.XMLConfiguration;
import org.openedit.repository.ContentItem;

/**
 * @author cburkey
 *
 */
public class XmlUtil
{
	protected PerThreadSingleton fieldReaderPool;
	protected XmlWriterPool fieldWriterPool;

	public Element getXml(File inFile, String inEncode )
	{
		try
		{
			return getXml(new FileReader(inFile), inEncode);
		}
		catch (Exception ex)
		{
			throw new RuntimeException(inFile.toString(), ex);
		}
	}
	public Element getXml(ContentItem inItem, String inEncoding)
	{
		Element root = getXml(inItem.getInputStream(),inEncoding);
		return root;
	}	
	public Element getXml(InputStream inXmlReader, String inEncoding)
	{
		try
		{
			if( inEncoding == null)
			{
				inEncoding = "UTF-8";
			}
			return getXml(new InputStreamReader(inXmlReader,inEncoding), inEncoding );
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	public Element getXml(Reader inXmlReader, String inEncode)
	{
		SAXReader reader = getReader();
		try
		{
			reader.setEncoding(inEncode);
			reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

			Document document = reader.read(inXmlReader);
			Element root = document.getRootElement();
			return root;
		}
		catch ( Exception ex)
		{
			throw new OpenEditRuntimeException(ex.getMessage(), ex);
		}
		finally
		{
			FileUtils.safeClose(inXmlReader);
		}
	}
	public Element getXml(String inXml, String inEncode)
	{
		StringReader reader = new StringReader(inXml);
		return getXml(reader,inEncode);

	}
	
	public void saveXml(Element inRoot, Writer inWriter, String inEncoding)
	{
		if( inRoot.getDocument() != null)
		{
			saveXml(inRoot.getDocument(), inWriter, inEncoding);
			return;
		}
		try
		{
			XMLWriter writer = getWriter(inEncoding);
			writer.setWriter(inWriter);
			writer.write(inRoot);
		}
		catch ( Exception ex)
		{
			throw new RuntimeException(ex.getMessage(), ex);
		}
		finally
		{
			FileUtils.safeClose(inWriter);
		}
	}
	
	public void saveXml(Document inRoot, Writer inWriter, String inEncoding)
	{
		try
		{
			XMLWriter writer = getWriter(inEncoding);
			writer.setWriter(inWriter);
			writer.write(inRoot);
		}
		catch ( Exception ex)
		{
			throw new RuntimeException(ex.getMessage(), ex);
		}
		finally
		{
			FileUtils.safeClose(inWriter);
		}
	}

	
	public void saveXml(Document inRoot, OutputStream inStream, String inEncoding)
	{

		try
		{
			Writer inWriter = new OutputStreamWriter(inStream,inEncoding);
		saveXml(inRoot,inWriter,inEncoding);
	}
	catch ( Exception ex )
	{
		throw new RuntimeException(ex);
	}
	}

	public void saveXmlConfiguration(XMLConfiguration inConfig, File inFile)
	{
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement(inConfig.getName());
		inConfig.appendXml(inConfig,root);
		saveXml( doc,inFile);
	}
	
	/**
	 * @param inStockQuoteDocument
	 * @param inFile
	 */
	public void saveXml(Document inStockQuoteDocument, File inFile)
	{
		try
		{
			saveXml(inStockQuoteDocument, new FileWriter(inFile), "UTF-8");
		}
		catch ( Exception ex )
		{
			throw new RuntimeException(ex);
		}
	}
	public String xmlEscape(String inCode)
	{
		return URLUtilities.xmlEscape(inCode);
	}

	public SAXReader getReader()
	{
		return (SAXReader)getReaderPool().instance();
	}
	public XMLWriter  getWriter(String inEncoding)
	{
		return getWriterPool().instance(inEncoding);
	}

	public PerThreadSingleton getReaderPool()
	{
		if (fieldReaderPool == null)
		{
			fieldReaderPool = new PerThreadSingleton();
			fieldReaderPool.setSingletonClassName(SAXReader.class.getName());
		}
		return fieldReaderPool;
	}

	public XmlWriterPool getWriterPool()
	{
		if (fieldWriterPool == null)
		{
			fieldWriterPool = new XmlWriterPool();
		}
		return fieldWriterPool;
	}

	public void saveXml(XMLConfiguration inConfig, Writer inOut, String inCharacterEncoding)
	{
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement(inConfig.getName());
		inConfig.appendXml(inConfig,root);
		saveXml(root, inOut, inCharacterEncoding);
		
	}

	public void saveXml(Element inRoot, OutputStream inOutputStream, String inCharacterEncoding)
	{
		try
		{
			XMLWriter writer = getWriter(inCharacterEncoding);
			writer.setOutputStream(inOutputStream);
			if( inRoot.getDocument() != null )
			{
				writer.write(inRoot.getDocument());
			}
			else
			{
				writer.write(inRoot);
			}
		}
		catch ( Exception ex)
		{
			throw new OpenEditException(ex.getMessage(), ex);
		}
		finally
		{
			FileUtils.safeClose(inOutputStream);
		}
		
	}

	public Element getElementById(Element inElement, String inId)
	{
		if(inElement == null || inId == null)
		{
			return null;
		}
		
		for(Iterator i = inElement.elementIterator(); i.hasNext();)
		{
			Element e = (Element) i.next();
			if(inId.equals(e.attributeValue("id")))
			{
				return e;
			}
		}
		
		return null;
	}
}
