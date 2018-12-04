package org.openedit.hittracker;

import java.util.List;
import java.util.Map;

import org.openedit.data.Searcher;
import org.openedit.profile.UserProfile;

public interface UserFilters
{

	public UserProfile getUserProfile();

	public void setUserProfile(UserProfile inUserProfile);
	public List<FilterNode> getFilterOptions(HitTracker inHits);
	public Map<String,FilterNode> getFilterValues(HitTracker inHits);
	public void clear(String inSearchType);
	public void flagUserFilters(HitTracker inHits);

}
