package org.openedit.hittracker;

import java.util.List;
import java.util.Map;

import org.openedit.WebPageRequest;
import org.openedit.profile.UserProfile;

public interface UserFilters
{

	public UserProfile getUserProfile();

	public void setUserProfile(UserProfile inUserProfile);
	public List<FilterNode> getFilterOptions(HitTracker inHits, WebPageRequest inReq);
	public Map<String,FilterNode> getFilterValues(HitTracker inHits, WebPageRequest inReq);
	public List<FilterNode> getFilterOptions(HitTracker inHits);
	public Map<String,FilterNode> getFilterValues(HitTracker inHits);
	public void clear(String inSearchType);
	public void flagUserFilters(HitTracker inHits);
	public List getFilteredTerms(HitTracker inHits);

}
