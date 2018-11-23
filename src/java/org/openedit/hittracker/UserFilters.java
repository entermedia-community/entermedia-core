package org.openedit.hittracker;

import java.util.List;

import org.openedit.profile.UserProfile;

public interface UserFilters
{

	public UserProfile getUserProfile();

	public void setUserProfile(UserProfile inUserProfile);
	public List<FilterNode> getFilterOptions(String inSearchType, SearchQuery inSearchQuery);

}
