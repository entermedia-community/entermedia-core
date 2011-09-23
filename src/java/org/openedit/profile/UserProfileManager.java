package org.openedit.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.Data;
import org.openedit.data.Searcher;
import org.openedit.data.SearcherManager;

import com.openedit.OpenEditException;
import com.openedit.WebPageRequest;
import com.openedit.hittracker.ListHitTracker;

public class UserProfileManager
{
	protected SearcherManager fieldSearcherManager;
	private static final Log log = LogFactory.getLog(UserProfileManager.class);

	public UserProfile getDefaultProfile(String inCatId)
	{
		Searcher searcher = getSearcherManager().getSearcher(inCatId, "userprofile");
		UserProfile	baseProfile = (UserProfile)searcher.searchById("default");
		return baseProfile;
	}

	public SearcherManager getSearcherManager()
	{
		return fieldSearcherManager;
	}

	public void setSearcherManager(SearcherManager inSearcherManager)
	{
		fieldSearcherManager = inSearcherManager;
	}

	public UserProfile loadUserProfile(WebPageRequest inReq, String inCatalogId, String inUserName)
	{
		if( inUserName == null )
		{
			return null;
		}
		UserProfile userprofile = (UserProfile)inReq.getPageValue("userprofile");
		if( userprofile != null)
		{
			return userprofile;
		}
		String id = inCatalogId + "userprofile" + inUserName;
		
		userprofile = (UserProfile)inReq.getSessionValue(id);
		if( userprofile != null && inUserName.equals(userprofile.getUserId()) )
		{
			//check searcher cache?
			inReq.putPageValue("userprofile", userprofile); 

			return userprofile;
		}
		if(inCatalogId == null)
		{
			return null;
		}
		Searcher searcher = getSearcherManager().getSearcher(inCatalogId, "userprofile");
		userprofile = (UserProfile)searcher.searchByField("userid", inUserName);
		if( userprofile == null)
		{
			userprofile = (UserProfile)searcher.createNewData();
			userprofile.setProperty("userid", inUserName);
			if( inUserName.equals("admin"))
			{
				userprofile.setProperty("settingsgroup","administrator");
			}
			userprofile.setSourcePath(inUserName);
			userprofile.setCatalogId(inCatalogId);
			searcher.saveData(userprofile, inReq.getUser());
		}
		userprofile.setSourcePath(inUserName);
		userprofile.setCatalogId(inCatalogId);
		
		
		List ok = new ArrayList();
		List okUpload = new ArrayList();
		
		//check the parent first, then the appid
		String appid = inReq.findValue("parentapplicationid");
		if( appid == null)
		{
			appid = inReq.findValue("applicationid");
		}
		
		Collection catalogs = getSearcherManager().getSearcher(appid, "catalogs").getAllHits();
		
		for (Iterator iterator = catalogs.iterator(); iterator.hasNext();)
		{
			Data cat = (Data) iterator.next();
			//MediaArchive archive = getMediaArchive(cat.getId());
			WebPageRequest catcheck = inReq.getPageStreamer().canDoPermissions("/" + cat.getId());
			Boolean canview = (Boolean)catcheck.getPageValue("canview");
			if( canview != null && canview)
			{
				ok.add(cat);
			}
			Boolean canupload = (Boolean)catcheck.getPageValue("canupload");
			if(canupload != null && canupload)
			{
				okUpload.add(cat);
			}
		}
		userprofile.setCatalogs(new ListHitTracker(ok));
		userprofile.setUploadCatalogs(new ListHitTracker(okUpload));

		
		if (inReq.getUserName().equals(userprofile.getUserId()))
		{
			inReq.putSessionValue(id, userprofile);
		}
		
		inReq.putPageValue("userprofile", userprofile); 
		return userprofile;
	}

	public void saveUserProfile(UserProfile inUserProfile)
	{
		Searcher searcher = getSearcherManager().getSearcher(inUserProfile.getCatalogId(), "userprofile");
		if( inUserProfile.getSourcePath() == null )
		{
			throw new OpenEditException("user profile source path is null");
		}
		searcher.saveData(inUserProfile, null);
	}
}
