package org.openedit.xml;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.entermedia.locks.Lock;
import org.entermedia.locks.LockManager;
import org.entermedia.locks.MemoryLockManager;
import org.openedit.Data;
import org.openedit.repository.ContentItem;

import com.openedit.ModuleManager;
import com.openedit.OpenEditException;
import com.openedit.page.Page;
import com.openedit.page.manage.PageManager;
import com.openedit.users.User;
import com.openedit.util.FileUtils;
import com.openedit.util.XmlUtil;

public class XmlArchive
{
	private static final Log log = LogFactory.getLog(XmlArchive.class);
	protected PageManager fieldPageManager;
	protected XmlUtil fieldXmlUtil;
	protected ModuleManager fieldModuleManager;
	

	public ModuleManager getModuleManager()
	{
		return fieldModuleManager;
	}

	public void setModuleManager(ModuleManager inModuleManager)
	{
		fieldModuleManager = inModuleManager;
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
			XmlFile	element = null;
			if( path.startsWith("/WEB-INF/data") )
			{
				ContentItem input = getPageManager().getRepository().get(path);
				element = load(inId, path, inElementName, input);
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
	 * @deprecated use getXml(String)
	 */
	public XmlFile loadXmlFile( String inId) throws OpenEditException
	{
		return getXml(inId);
	}
	public long getLastModified( String inPath) throws OpenEditException
	{
		if( inPath.startsWith("/WEB-INF/data") )
		{
			ContentItem input = getPageManager().getRepository().get(inPath);
			return input.getLastModified();
		}
		else
		{
			return getPageManager().getPage(inPath).lastModified();
		}
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

		return element;
	}

	public void saveXml(XmlFile inFile, User inUser, Lock lock) throws OpenEditException
	{
		
		//Lock lock = getLockManager().lock("system", inFile.getPath(), null ); //this will retry 10 times then timeout and throw an exception
	
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
				//log.info("Save " + inFile.getPath());
	
	}
	
	public void saveXml(XmlFile inFile, User inUser) throws OpenEditException
	{
		
		Lock lock = getLockManager().lock(inFile.getPath(), null ); //this will retry 10 times then timeout and throw an exception
		try
		{
			saveXml(inFile, inUser,lock);
				//log.info("Save " + inFile.getPath());
		}
		finally
		{
			
			getLockManager().release(lock);
		}
	}
	
	
	public LockManager getLockManager(String inCatalogId)
	{
		LockManager manager = (LockManager)getModuleManager().getBean(inCatalogId,"lockManager");
		return manager;
	}
	

	protected LockManager getLockManager()
	{
		return getLockManager("system");
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
		Page page = getPageManager().getPage(inSettings.getPath(),true);
		page.getContentItem().setMakeVersion(false);
		getPageManager().removePage(page);
		
	}



	
}
