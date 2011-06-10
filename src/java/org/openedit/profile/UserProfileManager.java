package org.openedit.profile;

import java.util.HashMap;
import java.util.Map;

import org.openedit.data.Searcher;
import org.openedit.data.SearcherManager;

import com.openedit.WebPageRequest;

public class UserProfileManager
{
	protected SearcherManager fieldSearcherManager;

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
		}
		userprofile.setSourcePath(inUserName);
		userprofile.setCatalogId(inCatalogId);
		
		
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
		searcher.saveData(inUserProfile, null);
	}
}
