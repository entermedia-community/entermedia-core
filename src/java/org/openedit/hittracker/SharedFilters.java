package org.openedit.hittracker;

import java.util.List;
import java.util.Map;

import org.openedit.WebPageRequest;
import org.openedit.data.Searcher;
import org.openedit.profile.UserProfile;

public interface SharedFilters
{

	public UserProfile getUserProfile();

	public void setUserProfile(UserProfile inUserProfile);
	public Map<String,FilterNode> getAllValues(Searcher inSearcher, WebPageRequest inReq);
	//public void flagUserFilters(HitTracker inHits);
	public List getFilteredTerms(HitTracker inHits);
	public void clear(String inSearchType);

}
