package org.openedit.hittracker;

import java.util.ArrayList;
import java.util.List;

public class PaginatedIdHitTracker extends ListHitTracker
{

	
	public List getPageOfHits()
	{
		int inHitsPerPage = getHitsPerPage();
		int count = (getPage() - 1) * inHitsPerPage; // pick up from here
		int max = Math.min(getList().size(), count + inHitsPerPage);
		List page = getList().subList(count, max);

		Term term = getSearchQuery().getTermByTermId("id");
		term.setValues(page.toArray(new String[page.size()]));
		
		String sort = getSearchQuery().getSortBy();
		
		HitTracker assets = getSearcher().query().ids(page).named(getHitsName()).hitsPerPage(inHitsPerPage).sort(sort).search();
		return assets.getPageOfHits();
	}
}
