package org.openedit.hittracker;

import java.util.ArrayList;
import java.util.List;

public class PaginatedIdHitTracker extends ListHitTracker
{
	public PaginatedIdHitTracker()
	{
		fieldPage = -1;
	}
	
	@Override
	public boolean hasChanged(HitTracker inTracker)
	{
		return false;
	}
	
	@Override
	public void setPage(int inPageOneBased)
	{
		if( inPageOneBased == -1)
		{
			return;
		}
		if( fieldPage != inPageOneBased )
		{
			fieldCurrentPage = null;
			fieldPage = inPageOneBased;

			int inHitsPerPage = getHitsPerPage();
			int count = (getPage() - 1) * inHitsPerPage; // pick up from here
			int max = Math.min(getList().size(), count + inHitsPerPage);
			List page = getList().subList(count, max);

			Term term = getSearchQuery().getTermByTermId("id");
			term.setValues(page.toArray(new String[page.size()]));
			
			String sort = getSearchQuery().getSortBy();
			
			HitTracker assets = getSearcher().getSearcherManager().getSearcher(getSearcher().getCatalogId(),"asset").query().ids(page).hitsPerPage(inHitsPerPage).sort(sort).search();
			fieldCurrentPage = assets.getPageOfHits();
			
		}
	}
	
	
	public List getPageOfHits()
	{
		setPage(1); //?
		return fieldCurrentPage;
	}
}
