package org.openedit.xml;

import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.openedit.Data;
import org.openedit.repository.ContentItem;

import com.openedit.OpenEditException;
import com.openedit.page.Page;
import com.openedit.page.manage.PageManager;
import com.openedit.users.User;
import com.openedit.util.FileUtils;
import com.openedit.util.XmlUtil;

public class XmlArchive
{
	private static final Log log = LogFactory.getLog(XmlArchive.class);
	protected Map fieldCache;
	protected PageManager fieldPageManager;
	protected XmlUtil fieldXmlUtil;
	protected Map fieldLocks;
	
	protected Map getLocks()
	{
		if (fieldLocks == null)
		{
			synchronized (this)
			{
				if( fieldLocks == null)
				{
					fieldLocks = new HashMap();
				}
			}
		}
		return fieldLocks;
	}

	/**
	 * This is the recommended API
	 * @param inPath
	 * @param inElementName
	 * @return
	 * @throws OpenEditException
	 */
	public XmlFile getXml( String inPath, String inElementName) throws OpenEditException
	{
		return getXml(inPath,inPath,inElementName);
	}
	
	public XmlFile getXml(String inPath) throws OpenEditException
	{
		return getXml(inPath, inPath, null);
	}
	
	/**
	 * @deprecated Use the more simple getXml(String) version
	 * @param inId
	 * @param path
	 * @param inElementName
	 * @return
	 * @throws OpenEditException
	 */
	public XmlFile getXml( String inId, String path, String inElementName) throws OpenEditException
	{
		try
		{// This can be specified within the page action with a <property
			// name="xmlfile">./data.xml</property>
			XmlFile element = (XmlFile) getCache().get(inId);
			if( path.startsWith("/WEB-INF/data") )
			{
				ContentItem input = getPageManager().getRepository().get(path);
				if (element == null || element.getLastModified() != input.lastModified().getTime() )
				{
					element = load(inId, path, inElementName, input);
				}
			}
			else
			{
				Page input = getPageManager().getPage(path, true);
				if (element == null || element.getLastModified() != input.lastModified())
				{
					element = load(inId, path, inElementName, input.getContentItem());
				}

			}
			return element;
		}
		catch (Exception e)
		{
			String actual = getPageManager().getPage(path).getContentItem().getAbsolutePath();
			if (actual == null)
			{
				actual = path;
			}
			throw new OpenEditException("Path was: " + actual + " Error:  " + e.getMessage(), e);
		}
		
	}
	/**
	 * @deprecated
	 */
	public XmlFile createXmlFile( String inId, String path) throws OpenEditException
	{
		return getXml(inId,path,null);
	}
	/**
	 */
	public XmlFile loadXmlFile( String inId) throws OpenEditException
	{
		XmlFile element = (XmlFile)getCache().get(inId);
		if( element != null)
		{
			Page input = getPageManager().getPage(element.getPath(),true);
			if ( element.getLastModified() != input.lastModified() )
			{
				return null;
			}
		}
		return element;
	}
	
	protected XmlFile load(String inId, String path, String inElementName, ContentItem input) throws OpenEditException
	{
		//log.info("Loading " + path);
		boolean found = false;

		XmlFile element;
		Element  root = null;
		if( !input.exists() )
		{
			if( inElementName == null)
			{
				root = DocumentHelper.createElement("root");
			}
			else
			{
				if( inElementName.endsWith("y"))
				{
					root = DocumentHelper.createElement(inElementName.substring(0,inElementName.length() - 1) + "ies");
				}
				else
				{
					root = DocumentHelper.createElement(inElementName + "s");
				}
			}				
		}
		else
		{
			found = true;
			InputStream in = input.getInputStream();
			try
			{
				root= getXmlUtil().getXml(in,"UTF-8");
			}
			catch( OpenEditException ex )
			{
				log.error("file problem: " + path,ex);
				throw ex;
			}
			finally 
			{
				FileUtils.safeClose(in);
			}
		}
		element  = new XmlFile();
		element.setRoot(root);
		element.setExist(found);
		element.setElementName(inElementName);
		element.setPath(path);
		element.setLastModified(input.lastModified().getTime());
		element.setId(inId);

		if( getCache().size() > 1000)
		{
			getCache().clear();
		}
		getCache().put(inId, element);

		return element;
	}
	
	
	public Map getCache()
	{
		if (fieldCache == null)
		{
			fieldCache = new HashMap();
		}
		return fieldCache;
	}

	public void setCache(Map inCache)
	{
		fieldCache = inCache;
	}
	public void saveXml(Data inFile, User inUser) throws OpenEditException
	{
		saveXml((XmlFile)inFile,inUser);
	}
	public void saveXml(XmlFile inFile, User inUser) throws OpenEditException
	{
		Object lock = getLock(inFile.getPath());
		synchronized (lock)
		{
			Page page = getPageManager().getPage(inFile.getPath(), false);
	
			ContentItem tmp = getPageManager().getRepository().getStub(inFile.getPath() + ".tmp.xml");
			tmp.setMakeVersion(false);
			getXmlUtil().saveXml(inFile.getRoot(), tmp.getOutputStream(),page.getCharacterEncoding());
	
			ContentItem xmlcontent = getPageManager().getRepository().getStub(inFile.getPath());
			xmlcontent.setMakeVersion(false);
			getPageManager().getRepository().remove(xmlcontent); //might be a little faster to remove it first
			getPageManager().getRepository().move(tmp, xmlcontent);
			getPageManager().firePageModified(page);
			
			xmlcontent = getPageManager().getRepository().getStub(inFile.getPath());
			inFile.setLastModified(xmlcontent.getLastModified());
			inFile.setExist(true);
			getCache().put(inFile.getPath(),inFile);
			//log.info("Save " + inFile.getPath());
		}
	}

	private Object getLock(String inPath)
	{
		Object lock = getLocks().get(inPath);
		if( lock == null)
		{
			//create one
			synchronized (getLocks())
			{
				//double check
				lock = getLocks().get(inPath);
				if( lock == null)
				{
					lock = new Object();
					getLocks().put(inPath,lock);
				}
			}
		}
		return lock;
	}

	public PageManager getPageManager()
	{
		return fieldPageManager;
	}


	public void setPageManager(PageManager inPageManager)
	{
		fieldPageManager = inPageManager;
	}

	public XmlUtil getXmlUtil()
	{
		if (fieldXmlUtil == null)
		{
			fieldXmlUtil = new XmlUtil();
		}
		return fieldXmlUtil;
	}


	public void setXmlUtil(XmlUtil inXmlUtil)
	{
		fieldXmlUtil = inXmlUtil;
	}


	public void deleteXmlFile(XmlFile inSettings) throws OpenEditException
	{
		getCache().remove(inSettings.getId());

		Page page = getPageManager().getPage(inSettings.getPath(),true);
		page.getContentItem().setMakeVersion(false);
		getPageManager().removePage(page);
		
	}
	public void clear(String inId)
	{
		getCache().remove(inId);
		
	}


	
}
