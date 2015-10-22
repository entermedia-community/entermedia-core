/*
 Copyright (c) 2003 eInnovation Inc. All rights reserved

 This library is free software; you can redistribute it and/or modify it under the terms
 of the GNU Lesser General Public License as published by the Free Software Foundation;
 either version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU Lesser General Public License for more details.
 */

package com.openedit.page.manage;

import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.entermedia.cache.CacheManager;
import org.openedit.repository.CompoundRepository;
import org.openedit.repository.ContentItem;
import org.openedit.repository.OutputStreamItem;
import org.openedit.repository.ReaderItem;
import org.openedit.repository.RepositoryException;

import com.openedit.OpenEditException;
import com.openedit.PageAccessListener;
import com.openedit.WebPageRequest;
import com.openedit.page.Page;
import com.openedit.page.PageSettings;
import com.openedit.users.User;
import com.openedit.util.PathUtilities;

/**
 * The PageManager is a central access point for locating pages. Pages are
 * loaded and cached automatically. The cache will check the file's last
 * modification time and will update if the stored time does not match the file
 * system's time.
 * 
 * @author Matt Avery, mavery@einnovation.com
 */
public class PageManager
{
	private static Log log = LogFactory.getLog( PageManager.class );

	protected Map fieldPageAccessListeners;
	protected CacheManager fieldCacheManager;
	protected CompoundRepository fieldRepository;
	protected PageSettingsManager fieldSettingsManager;
	private static final String CACHEID = PageManager.class.getName();
	public PageManager()
	{
		log.debug("create page manager instance");
	}
	public Page getPage(String inPath, WebPageRequest inReq) throws OpenEditException
	{
		//this gets the page for this user
		boolean checkCurrent = Boolean.parseBoolean( inReq.findValue("reload") );
		Page real = getPage(inPath, checkCurrent);
		return getPage(real,checkCurrent,inReq);
	}
	/**
	 * This checks for pages in this order:
	 * 1. Draft page in the language directory
	 * 2. Page in the language directory
	 * 3. Draft in the real directory
	 * 4. Original file in the real directory
	 * @param inPage
	 * @param inReq
	 * @return
	 * @throws OpenEditException
	 */
	public Page getPage(Page inPage, WebPageRequest inReq) throws OpenEditException
	{
		//boolean checkCurrent = inReq.getUser() != null; //this no longer works since we do not have a user yet
		Boolean checkCurrent = (Boolean)inReq.getPageValue("reloadpages");
		if( checkCurrent == null)
		{
			checkCurrent = Boolean.FALSE;
		}
		return getPage( inPage, checkCurrent, inReq);
	}
	public Page getPage(Page inPage, boolean inCheckCurrent, WebPageRequest inReq) throws OpenEditException
	{
		String inPath = inPage.getPath();
		//log.info("getPage" + inPath);
		boolean useDraft = true;
		User user = inReq.getUser();
		if ( user == null )
		{
			useDraft = false;
		}

		if ( useDraft && !user.hasProperty("oe.edit.draftmode") )
		{
			useDraft = false;
		}
		if( useDraft && inPage.isDraft() )
		{
			useDraft = false;
		}
		if( useDraft )
		{
			String draftedits = inPage.get("oe.edit.draftedits"); //This is the opposite of oe.edit.directedits
			if (  draftedits != null && !Boolean.parseBoolean(draftedits) )
			{
				useDraft = false;
			}
		}		
		boolean multipleLang = false;
		String savein = inReq.getPageProperty("usemultiplelanguages");
		if (  savein != null )
		{
			multipleLang = Boolean.parseBoolean(savein);
		}
		String selectedcode = inReq.getLanguage();
		String rootdir = "/translations/" + selectedcode; //TODO: Make configurable
		if( multipleLang )
		{
			if( selectedcode == null || selectedcode.equals("default") )
			{
				multipleLang = false;
			}
			if( inPath.startsWith("/translations/") )
			{
				//strip off the begining
				rootdir = inPath.substring(0,rootdir.length());
				inPath = inPath.substring(rootdir.length(),inPath.length());
				multipleLang = true;
			}
		}
		Page foundPage = null;
		if ( useDraft)
		{
			String dp = PathUtilities.createDraftPath(inPath );
			if( multipleLang )
			{
					Page translated = getPage( rootdir + dp,inCheckCurrent);
					if( translated.exists() ) //Does the draft exists?
					{
						foundPage = translated;
					}
					else
					{
						translated = getPage( rootdir + inPath, inCheckCurrent); //Does the page exists in the /translations directory?
						if( translated.exists() )
						{
							foundPage = translated;
						}
						else
						{
							//Does the page exists in the home directory
							//log.info("trans oath " + inPath);
						}
					}
			}
			if( foundPage == null )
			{
				if( getRepository().doesExist(dp) )
				{
					Page draft = getPage(dp,inCheckCurrent);
					foundPage = draft;
				}
			}
		}
		else if( multipleLang )
		{
			Page translated = getPage( rootdir + inPath,inCheckCurrent);
			if( translated.exists() )
			{
				foundPage = translated;
			}
		}
		if( foundPage == null)
		{
			return inPage;
		}
		else
		{
			return foundPage;
		}
	}
	public  Page getPage( String inPath ) throws OpenEditException
	{
		return getPage(inPath,false);
	}
	/**
	 * Get a Page instance from the given path. If no page can be found then
	 * this method will throw a FileNotFoundException.
	 * 
	 * TODO: This method needs to be smarter.  If I request page.html and I have
	 * content page.xml, I still need to return a Page object with both
	 * requested mime-type (text/html) and content mime-type (text/xml)
	 * 
	 * @param path
	 *            The page path
	 * 
	 * @return The Page
	 * 
	 * @throws OpenEditException
	 *             Any Exception
	 */
	public  Page getPage( String inPath, boolean inCheckDates) throws OpenEditException
	{
		if ( inPath == null)
		{
			return null;
		}
		String fullPath = PathUtilities.buildRelative( inPath, "/" );
		
		Page page = (Page) getCacheManager().get( CACHEID, fullPath );
		boolean reloadPage = false;
		if ( page != null )
		{
				if ( originalPathChanged(page) )
				{
					//if the fullpath alternative has been added then we need to blow away this page and its settings
					reloadPage = true;
				}
				else if ( !inCheckDates || page.isCurrent() )
				{
					firePageRequested( page );
					return page;
				}
				else
				{
					reloadPage = true;
				}
		}
		synchronized( getCacheManager() )
		{	//lock down the config until we can configure the thing 
			page = (Page) getCacheManager().get( CACHEID, fullPath );
			if ( page == null || reloadPage ) //check for other threads that where waiting
			{
				if ( reloadPage)
				{
					getPageSettingsManager().clearCache(fullPath);  //in case alternative file shows up
				}
				page = createPage( fullPath );
				getCacheManager().put(CACHEID, fullPath, page );
				firePageRequested( page );
			}
		}
		return page;
	}

	private boolean originalPathChanged(Page inPage) throws OpenEditException
	{
		//we only need to check for changes when we are using an alternative content path
		if( inPage.getAlternateContentPath() != null)
		{
			//go check the original path to make sure it has not appeared or been removed
			boolean doesExist = getRepository().doesExist( inPage.getPath() );
			boolean changed = (doesExist != inPage.getPageSettings().isOriginalyExistedContentPath() );
			return changed;
		}
		return false;
	}

	protected Page createPage( String inPath ) throws OpenEditException
	{
		PageSettings settings = getPageSettingsManager().getPageSettings( inPath );
		Page page = new Page( inPath, settings );
		populatePage( page );
		return page;
	}

	protected void populatePage( Page inPage ) throws OpenEditException
	{
		ContentItem revision = getContentRevision( inPage );
		inPage.setContentItem( revision );
	}

	protected  ContentItem getContentRevision( Page inPage ) throws RepositoryException
	{
		String path = inPage.getPath();
		if (inPage.getAlternateContentPath() != null)  //TODO: This should be done in the settings object
		{
			path = inPage.getAlternateContentPath();
		}
		ContentItem revision = getRepository().getStub( path );
		return revision;
	}
	
	public PageSettingsManager getPageSettingsManager()
	{
		return fieldSettingsManager;
	}
	public void setPageSettingsManager( PageSettingsManager inManager)
	{
		fieldSettingsManager = inManager;
	}

	public void copyPage( Page inSourcePage, Page inDestinationPage ) throws RepositoryException
	{
		if (inSourcePage.exists())
		{
			String makeversion = inDestinationPage.getProperty("makeversion");
			if (makeversion != null)
			{
				boolean ver = Boolean.parseBoolean(makeversion);
				inDestinationPage.getContentItem().setMakeVersion(ver);
			}
			ContentItem item = inDestinationPage.getContentItem();
			item.setPath(inDestinationPage.getPath());
			
			getRepository().copy( inSourcePage.getContentItem(),  item);
		}
		else
		{
			throw new RepositoryException("No such page to copy " + inSourcePage);
		}
		//If we take xconfs when we copy the we best keep the contentfile set
		/*		if (inSourcePage.getPageSettings().exists())
		{
			//TODO: Flatten down xconf a little
			getRepository().copy( inSourcePage.getPageSettings().getXConf(),
					inDestinationPage.getPageSettings().getXConf() );
			//
		}
*/		firePageAdded( inDestinationPage );
	}

	public void movePage( Page inSource, Page inDestination ) throws RepositoryException
	{
		if (inSource.exists())
		{
			ContentItem item = inDestination.getContentItem(); //Use new path
			item.setPath(inDestination.getPath());
			String makeversion = inDestination.getProperty("makeversion");
			item.setMakeVersion(Boolean.parseBoolean(makeversion));
			getRepository().move( inSource.getContentItem(),  item);
		}
		else
		{
			throw new RepositoryException("No such page to move " + inSource);
		}
		firePageRemoved( inSource );
		firePageAdded( inDestination ); //this event will invalidate the folder in the file manager
	}

	public void removePage( Page inPage ) throws OpenEditException
	{
		String makeversion = inPage.getProperty("makeversion");
		if (makeversion != null)
		{
			boolean ver = Boolean.parseBoolean(makeversion);
			inPage.getPageSettings().getXConf().setMakeVersion(ver);
			inPage.getContentItem().setMakeVersion(ver);
		}
		inPage.getContentItem().setPath(inPage.getPath());
		getRepository().remove( inPage.getContentItem() );
		//getRepository().remove( inPage.getPageSettings().getXConf() );
		getCacheManager().remove( CACHEID, inPage.getPath() );
		firePageRemoved( inPage );
	}

	public void putPage( Page inPage ) throws OpenEditException
	{
		//Kind of hackish. Guess we need to look on the disk to be sure but that could be slow
		ContentItem oldItem = inPage.getContentItem();
		boolean existing = oldItem.exists();
		String makeversion = inPage.getProperty("makeversion");
		if (makeversion != null)
		{
			boolean ver = Boolean.parseBoolean(makeversion);
			oldItem.setMakeVersion(ver);
		}
		getRepository().put( oldItem );
		clearCache(inPage);
		
		//we want to get the recent version back from disk
		if ( oldItem.isMakeVersion() && oldItem.lastModified() == null ) //might be a StringItem for example
		{
			//Load up a fresh item from the disk drive since we uploaded
			ContentItem reloadedItem = getRepository().get( inPage.getPath() );
			if ( oldItem.getMessage() != null && reloadedItem.getMessage() == null )
			{
				reloadedItem.setAuthor(oldItem.getAuthor());
				reloadedItem.setMessage(oldItem.getMessage());
				reloadedItem.setType(oldItem.getType());
				reloadedItem.setVersion(oldItem.getVersion());
			}
			reloadedItem.setMakeVersion(oldItem.isMakeVersion());
			
			inPage.setContentItem( reloadedItem );
		}

		if (existing)
		{
			firePageModified( inPage );
		}
		else
		{
			firePageAdded( inPage );
		}
	}
	public void saveSettings(Page inPage)
	{
		getPageSettingsManager().saveSetting(inPage.getPageSettings());
		firePageModified(inPage);
		clearCache(inPage);
	}
	protected CacheManager getCacheManager()
	{
		return fieldCacheManager;
	}
	public void setCacheManager(CacheManager inCacheManager)
	{
		fieldCacheManager = inCacheManager;
	}

	public CompoundRepository getRepositoryManager()
	{
		return fieldRepository;
	}
	public CompoundRepository getRepository()
	{
		return getRepositoryManager();
	}

	public void setRepository( CompoundRepository repository )
	{
		fieldRepository = repository;
	}

	public void firePageAdded( Page inPage )
	{
		for ( Iterator iter = getPageAccessListeners().keySet().iterator(); iter.hasNext(); )
		{
			PageAccessListener listener = (PageAccessListener) iter.next();
			listener.pageAdded( inPage );
		}
	}

	public void firePageModified( Page page )
	{
		for ( Iterator iter = getPageAccessListeners().keySet().iterator(); iter.hasNext(); )
		{
			PageAccessListener listener = (PageAccessListener) iter.next();
			listener.pageModified( page );
		}
	}

	public void firePageRemoved( Page inPage )
	{
		for ( Iterator iter = getPageAccessListeners().keySet().iterator(); iter.hasNext(); )
		{
			PageAccessListener listener = (PageAccessListener) iter.next();
			listener.pageRemoved( inPage );
		}
	}

	public void firePageRequested( Page inPage )
	{
		if( getPageAccessListeners().size() > 4) //Our defaults dont count since they dont do anything
		{
			for ( Iterator iter = getPageAccessListeners().keySet().iterator(); iter.hasNext(); )
			{
				PageAccessListener listener = (PageAccessListener) iter.next();
				listener.pageRequested( inPage );
			}
		}
	}

	protected Map getPageAccessListeners()
	{
		if (fieldPageAccessListeners == null)
		{
			//HARD means even if the object goes out of scope we still keep it in the hashmap
			//until the memory runs low then things get dumped randomly
			fieldPageAccessListeners = new HashMap();
		}

		return fieldPageAccessListeners;
	}
	public void setSettingsManager( PageSettingsManager metaDataConfigurator )
	{
		fieldSettingsManager = metaDataConfigurator;
	}
	
	public void addPageAccessListener( PageAccessListener inListener )
	{
		getPageAccessListeners().put( inListener, this );
	}
	
	public void removePageAccessListener( PageAccessListener inListener )
	{
		getPageAccessListeners().remove( inListener );
	}
	public void clearCache()
	{
		synchronized( getCacheManager() )
		{
			getCacheManager().clear(CACHEID);
			getPageSettingsManager().clearCache();
		}
	}

	/**
	 * @param inPage
	 */
	public void clearCache(Page inPage)
	{
		clearCache(inPage.getPath());
	}
	public void clearCache(String inPath)
	{
		if( inPath != null )
		{
			synchronized( getCacheManager() )
			{
				getCacheManager().remove(CACHEID, inPath);
				getPageSettingsManager().clearCache(inPath);
			}
		}
	}
	public void saveContent(Page inPage, User inUser, String inContent, String inMessage) throws OpenEditException
	{
		saveContent(inPage, inUser, new StringReader(inContent), inMessage);
	}
	
	public void saveContent(Page inPage, User inUser, Reader inReader, String inMessage)
	{
		ReaderItem item = new ReaderItem(inPage.getPath(),inReader,inPage.getCharacterEncoding());
		item.setMessage(inMessage);
		item.setAuthor(inUser.getUserName());
		inPage.setContentItem(item);
		putPage(inPage);
	}	

	public OutputStream saveToStream(Page inPage, User inUser, String inMessage)
	{
		OutputStreamItem item = new OutputStreamItem(inPage.getPath());
		item.setMessage(inMessage);
		if(inUser != null){
			//could be null
			item.setAuthor(inUser.getUserName());
		}
		boolean previous = inPage.getContentItem().isMakeVersion();
		item.setMakeVersion(previous);
		inPage.setContentItem(item);
		putPage(inPage);  //This sets the outputstream for us
		return item.getOutputStream();
	}	
	public OutputStream saveToStream(Page inPage)
	{
		OutputStreamItem item = new OutputStreamItem(inPage.getPath());
		inPage.setContentItem(item);
		putPage(inPage);  //This sets the outputstream for us
		return item.getOutputStream();		
	}
	
	public List getChildrenPaths(String inUrl) throws RepositoryException
	{
		return getChildrenPaths(inUrl, false);
	}
	public List getChildrenPathsSorted(String inUrl) throws RepositoryException
	{
		List paths = getChildrenPaths(inUrl, false);
		Collections.sort(paths, new Comparator()
			{
				public int compare(Object inO1, Object inO2) 
				{
					return inO1.toString().toLowerCase().compareTo(inO2.toString().toLowerCase());
				}
		}); 
		return paths;
	}
	/**
	 * @deprecated see getChildrenPaths
	 * @param inUrl
	 * @return
	 * @throws RepositoryException
	 */
	public List getChildrenNames(String inUrl) throws RepositoryException
	{
		return getChildrenPaths(inUrl);
	}
	public ContentItem getLatestVersion(String inPath) throws RepositoryException
	{
		return getRepository().getLastVersion(inPath);
	}	
	public List getVersions(String inPath) throws RepositoryException
	{
		return getRepository().getVersions(inPath);
	}
	public List getChildrenPaths(String inPath, boolean inIncludeFallBack)
	{
		List all = getRepository().getChildrenNames(inPath);
		if( inIncludeFallBack)
		{
			PageSettings settings = getPageSettingsManager().getPageSettings(inPath);
			settings = settings.getFallback();
			while( settings != null)
			{
				String dirparent = PathUtilities.extractDirectoryPath(settings.getPath());
				List morechildren = getRepository().getChildrenNames(dirparent );
				all.addAll(morechildren);
				settings = settings.getFallback();
			}
		}
		return all;
	}
}