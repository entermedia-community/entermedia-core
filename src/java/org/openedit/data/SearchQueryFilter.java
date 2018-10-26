package org.openedit.data;

import org.openedit.WebPageRequest;
import org.openedit.hittracker.SearchQuery;

public interface SearchQueryFilter
{
	public SearchQuery attachFilter(WebPageRequest inPageRequest, Searcher inSearcher, SearchQuery inQuery);
}