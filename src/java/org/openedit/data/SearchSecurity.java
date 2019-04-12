package org.openedit.data;

import org.openedit.WebPageRequest;
import org.openedit.hittracker.SearchQuery;

public interface SearchSecurity
{
	public SearchQuery attachSecurity(WebPageRequest inPageRequest, Searcher inSearcher, SearchQuery inQuery);
}