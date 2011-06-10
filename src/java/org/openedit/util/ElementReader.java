package org.openedit.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

import com.openedit.OpenEditException;

/**
 * This is a class that produces XML as needed. A pull reader. This allows us to have a smaller memory footprint
 * @author cburkey
 * @deprecated This has some formatting problems so I can't recommend this approach
 */


public class ElementReader extends Reader
{
	boolean finished;
	protected Element fieldElement;
	protected LinkedList fieldUnclosedElements;
	protected XMLWriter fieldXmlWriter;
	protected StringWriter fieldStringBuffer;

	/**
	 * This is the prefered API since along with a cached XMLWriter across multiple documents
	 * @param inRoot
	 * @param inWriter
	 */
	public ElementReader(Element inRoot,XMLWriter inWriter)
	{
		setElement(inRoot);
		setXmlWriter(inWriter);
	}

	public ElementReader()
	{
	}
	public ElementReader(Element inRoot, String encoding)
	{
		setElement(inRoot);

		OutputFormat format = OutputFormat.createPrettyPrint();
    	format.setEncoding(encoding);
    	
		try
		{
			setXmlWriter( new XMLWriter(format) );
		}
		catch (UnsupportedEncodingException e)
		{
			throw new RuntimeException(e);
		}
		
	}	
	
	public void close() throws IOException
	{
		finished = true;
	}

	public int read(char[] inCbuf, int inOff, int inLen) throws IOException
	{
		//start reading things until we get to the lenght. Any extra save in the local buffer
		if( fieldUnclosedElements == null)
		{
			//This is the first time. We put the document stuff in there
			try
			{
				getXmlWriter().startDocument();
			}
			catch (SAXException e)
			{
				throw new OpenEditException(e);
			}
		}
		if( !finished && getLength() < inLen )
		{
			finished = render(getElement(), inLen, 1);
//			if( finished)
//			{
//				getXmlWriter().flush();  //this may not be needed
//			}
		}
		int copied = dumpData(inCbuf, inOff, inLen);
		return copied;
	}

	protected boolean render(Element inNode, int inRequest, int inDeep) throws IOException
	{
		int i = 0;
		if (getUnclosedElements().size() > 0)
		{
			i = ((Integer)getUnclosedElements().removeLast()).intValue();
			Element from = (Element)inNode.node(i);
			render(from,inRequest,inDeep +1);
			i = i+1;
		}
		else
		{
			getXmlWriter().writeOpen(inNode);
		}
		boolean wasjusttext = false;
		for (; i < inNode.nodeCount(); i++)
		{
			Node node = inNode.node(i);
			wasjusttext = node.getNodeType() == node.TEXT_NODE;
			if (node instanceof Element )
			{
				Element element = (Element)node;
				if( getLength() >= inRequest)
				{
					Element parent = element.getParent();
					//Stack should be empty at this point
					getUnclosedElements().addFirst(Integer.valueOf(i));
					while (parent != null )
					{
						Node child = parent;
						parent = parent.getParent();
						if( parent != null)
						{
							//calling indexOf is slow for some reason
							getUnclosedElements().addFirst(Integer.valueOf(parent.indexOf(child)));
						}
					}
					return false;
				}
				if( element.nodeCount() == 0)
				{
					//BUG: The element is not tabbed over
					getXmlWriter().write(node);					
				}
				else
				{
					indent(inDeep);
					if( !render(element, inRequest,inDeep +1) )
					{
						return false;
					}
				}
			}
			else
			{
				if(!wasjusttext)
				{
					indent(inDeep);
					getXmlWriter().write(node);
				//getBuffer().append('\n');
				}
				else
				{
					getXmlWriter().write(node);
				}
			}
		}	
		if( wasjusttext && inNode.nodeCount() == 1)
		{
			getXmlWriter().writeClose(inNode);			
		}
		else
		{
			indent(inDeep-1);
			getXmlWriter().writeClose(inNode);
		}
		return true;
	}

	private void indent(int inDeep)
	{
		getBuffer().append('\n');
		for (int j = 0; j < inDeep; j++)
		{
			getBuffer().append('\t');				
		}
	}

	protected int dumpData(char[] inBuf,int inOff, int inLen)
	{
		if( getLength() == 0)
		{
			return -1;
		}
		int copied = Math.min(inLen, getLength());
		getBuffer().getChars( 0, copied, inBuf,inOff);
		//Cut off the start and move remainder up?
		if( getLength() == copied)
		{
			getBuffer().setLength(0);
		}
		else
		{
			char[] remain = new char[getLength()-copied];
			getBuffer().getChars(copied, getLength(), remain, 0);
			getBuffer().setLength(0);
			getBuffer().append(remain);
		}
		return copied;
	}
	public Element getElement()
	{
		return fieldElement;
	}

	public void setElement(Element inElement)
	{
		fieldElement = inElement;
	}

	public XMLWriter getXmlWriter()
	{
		return fieldXmlWriter;
	}

	public void setXmlWriter(XMLWriter inXmlWriter)
	{
		fieldXmlWriter = inXmlWriter;
		fieldXmlWriter.setWriter(getStringBuffer());
	}
	protected int getLength()
	{
		return getBuffer().length();
	}
	protected StringBuffer getBuffer()
	{
		return getStringBuffer().getBuffer();
	}
	protected StringWriter getStringBuffer()
	{
		if( fieldStringBuffer == null)
		{
			fieldStringBuffer = new StringWriter();
		}
		return fieldStringBuffer;
	}

	
	protected LinkedList getUnclosedElements()
	{
		if (fieldUnclosedElements == null)
		{
			fieldUnclosedElements = new LinkedList();
		}

		return fieldUnclosedElements;
	}
	
}
