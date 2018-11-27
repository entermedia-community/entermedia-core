package org.openedit.hittracker;

import java.util.List;
import java.util.Map;

import org.openedit.data.Searcher;
import org.openedit.profile.UserProfile;

public interface UserFilters
{

	public UserProfile getUserProfile();

	public void setUserProfile(UserProfile inUserProfile);
	public List<FilterNode> getFilterOptions(Searcher inSearcher, SearchQuery inSearchQuery);
	public Map<String,FilterNode> getFilterValues(Searcher inSearcher, SearchQuery inQuery);
	public void clear(String inSearchType) ;
	public void addFilterOptions(Searcher inSearcher, SearchQuery inQuery, List<FilterNode> inFilters);

}
