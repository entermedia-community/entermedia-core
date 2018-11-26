package org.openedit.hittracker;

import java.util.List;

import org.openedit.profile.UserProfile;

public interface UserFilters
{

	public UserProfile getUserProfile();

	public void setUserProfile(UserProfile inUserProfile);
	public List<FilterNode> getFilterOptions(String inSearchType, SearchQuery inSearchQuery);
	public void clear(String inSearchType) ;
	public void addFilterOptions(String inSearchType, SearchQuery inQuery, List<FilterNode> inFilters);

}
