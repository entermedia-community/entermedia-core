package org.openedit.hittracker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.CatalogEnabled;
import org.openedit.Data;
import org.openedit.OpenEditException;
import org.openedit.data.PropertyDetail;
import org.openedit.data.Searcher;
import org.openedit.util.DateStorageUtil;

public abstract class HitTracker<T> implements Serializable, Collection, CatalogEnabled
{
	private static final Log log = LogFactory.getLog(HitTracker.class);
	protected boolean fieldAllSelected;
	protected Collection<String> fieldSelections;
	protected int fieldPage = 1;
	protected int fieldHitsPerPage = -1;
	protected int fieldCurrentHit;
	protected SearchQuery fieldSearchQuery;
	protected boolean fieldUseRandom;
	protected List fieldRandomLookup;
	protected transient String fieldIndexId; //cause this index to invalidate
	protected String fieldResultType;
	protected String fieldDataSource;
	protected String fieldHitsName;	
	protected List fieldCurrentPage;
	protected int fieldMaxPageListing = 10; //used for page listing
	protected Searcher fieldSearcher;
	protected boolean fieldShowOnlySelected;
	protected String fieldTempSessionId;
	protected List<FilterNode> fieldFilterOptions;
	protected boolean fieldUseServerCursor;
	protected long fieldSearchTime;
	
	public void enableBulkOperations()
	{
		setUseServerCursor(true);
		setHitsPerPage(1000);
		setIndexId(getIndexId() + System.currentTimeMillis());
	}
	public boolean isUseServerCursor()
	{
		return fieldUseServerCursor;
	}
	public void setUseServerCursor(boolean inUseServerCursor)
	{
		fieldUseServerCursor = inUseServerCursor;
	}
	public HitTracker()
	{
		fieldSearchTime = System.currentTimeMillis();
	}
	public HitTracker(Searcher inSearcher)
	{
		this();
		setSearcher(inSearcher);
	}
	protected void setAllSelected(boolean inSelectAll)
	{
		fieldAllSelected = inSelectAll;
		
		if(inSelectAll && !isUseServerCursor()) {
			clear();
			setUseServerCursor(inSelectAll);

		} else {
			setUseServerCursor(inSelectAll);
		}

	}

	public boolean isShowOnlySelected()
	{
		return fieldShowOnlySelected;
	}
	public void setShowOnlySelected(boolean inShowOnlySelected)
	{
		fieldShowOnlySelected = inShowOnlySelected;
	}

	public String getResultType()
	{
		return getSearchQuery().getResultType();
	}

	public String getSearchType()
	{
		return getSearcher().getSearchType();
	}
	/*
	public List getPartOfPageOfHits(int inStart, int inEnd) throws Exception
	{
		List page = new ArrayList();
		int count = (getPage() - 1) * getHitsPerPage(); // this is the start of
		// the count
		count = count + inStart; // tack on the offset
		for (int i = inStart; i < getHitsPerPage() && i < inEnd; i++)
		{
			if (count < getTotal())
			{
				page.add(get(count));
				count++;
			}
		}
		return page;
	}
	*/
	
	/** 
	 * This is the absolute position of a result 
	 * @param count
	 * @return
	 */
	public int indexOf(int count)
	{

		int bottom = (getPage() - 1) * getHitsPerPage(); // this is the start of
		return bottom + count; // the offset
	}

	public abstract Data get(int count);

	public Data getById(String inId)
	{
		for (int i = 0; i < size(); i++)
		{
			Data hit = get(i);
			String id = getValue(hit, "id");
			if( id.equals(inId))
			{
				return hit;
			}
		}
		return null;
		//throw new OpenEditException("getById Not implemented");
	}
	public Collection<String> getSourcePaths()
	{
		List paths = new ArrayList();
		for (Iterator iterator = iterator(); iterator.hasNext();)
		{
			Data	data = (Data) iterator.next();
			paths.add(data.getSourcePath());
		}
		return paths;
	}
	public List<Data> getPageOfHits()
	{
		if( fieldCurrentPage == null)
		{
			int inHitsPerPage = getHitsPerPage();
			List page = new ArrayList(inHitsPerPage);
			int count = (getPage() - 1) * inHitsPerPage; // pick up from here
			fieldCurrentPage = page;
			int total = size();
			for (int i = 0; i < inHitsPerPage; i++)
			{
				if (count < total)
				{
					page.add(get(count));
					count++;
				}
				else
				{
					break;
				}
			}
		}
		return fieldCurrentPage;
	}
	public Collection getLast(int inCount)
	{
		List page = new ArrayList(inCount);
		int total = size() - 1;
		for (int i = 0; i < inCount; i++)
		{
			int index = total - i;
			if( index > -1)
			{
				page.add(get(index));
			}
			else
			{
				break;
			}
		}
		return page;
	}
	
	public List getPageInRows(int inColCount) throws Exception
	{
		List page = getPageOfHits();

		// Now break up the page into rows by dividing the count they wanted
		double rowscount = (double) page.size() / (double) inColCount;

		List rows = new ArrayList();
		for (int i = 0; i < rowscount; i++)
		{
			int start = i * inColCount;
			int end = i * inColCount + inColCount;
			List sublist = page.subList(start, Math.min(page.size(), end));
			rows.add(sublist);
		}
		return rows;
	}

	public int getHitsPerPage()
	{
		if( fieldHitsPerPage > -1)
		{
			return fieldHitsPerPage;
		}
		if( fieldSearchQuery != null)
		{
			return getSearchQuery().getHitsPerPage();
		}
		return 15;
	}

	public void setHitsPerPage(int inHitsPerPage)
	{
		if (inHitsPerPage > 0 && inHitsPerPage != fieldHitsPerPage)
		{
			clear();
			fieldHitsPerPage = inHitsPerPage;
			if( fieldPage > 1)
			{
				setPage(1);
			}
		}
	}
	/**
	 * One based
	 * @return
	 */
	public int getPage()
	{
		return fieldPage;
	}
	public void setPageByIndex(int inIndex)
	{
		if( inIndex < getHitsPerPage() )
		{
			setPage(1);
		}
		else
		{
			int page = size() / inIndex;
			setPage(page);
		}
	}
	public void setPage(int inPageOneBased)
	{
		if( fieldPage != inPageOneBased )
		{
			fieldCurrentPage = null;
			fieldPage = inPageOneBased;
		}
			
	}	
	public int getMaxPageListing()
	{
		return fieldMaxPageListing;
	}

	public void setMaxPageListing(int inMaxPageListing)
	{
		fieldMaxPageListing = inMaxPageListing;
	}

	/**
	 * @deprecated use iterator()
	 */
	public Iterator getAllHits()
	{
		return iterator();
	}

	public abstract Iterator iterator();

	public int getCurrentHit()
	{
		return fieldCurrentHit;
	}

	public void setCurrentHit(int inCurrentHit)
	{
		fieldCurrentHit = inCurrentHit;
	}

	public boolean containsById(String inId)
	{
		return getById(inId) != null;
	}

	public abstract boolean contains(Object inHit);

	public int getTotal()
	{
		return size();
	}

	public abstract int size();

	public int getTotalPages()
	{
		double pages = (double) getTotal() / (double) getHitsPerPage();
		if (pages % 1 > 0)
		{
			pages++;
		}
		return (int) pages;
	}

	public Integer nextPage()
	{
		int page = getPage() + 1;
		if (page > getTotalPages())
		{
			return null;
		}
		else
		{
			return new Integer(page);
		}
	}

	public Integer prevPage()
	{
		int page = getPage() - 1;
		if (page < 1)
		{
			return null;
		}
		else
		{
			return new Integer(page);
		}
	}

	public Integer getPageStart()
	{
		if (getTotal() == 0)
		{
			return null;
		}
		int start = (getPage() - 1) * getHitsPerPage();
		return new Integer(start + 1);
	}

	public Integer getPageEnd()
	{
		if (getTotal() == 0)
		{
			return null;
		}
		int start = getPage() * getHitsPerPage();
		if (start > getTotal())
		{
			return new Integer(getTotal());
		}
		return new Integer(start);

	}

	public SearchQuery getSearchQuery()
	{
		if (fieldSearchQuery == null)
		{
			fieldSearchQuery = new SearchQuery();
		}
		return fieldSearchQuery;
	}

	public String getQuery()
	{
		if (getSearchQuery() != null)
		{
			return getSearchQuery().toQuery();
		}
		return null;
	}

	public List linkRange()
	{
		int totalPages = getTotalPages();
		int page = getPage();
		int start = 1;

		if (page < getMaxPageListing() / 2) // under the first 5 records
		{
			start = 1;// - getMaxPageListing()/2;
		}
		else if (page + getMaxPageListing() / 2 + 1 >= totalPages) // near
		// the
		// end +
		// 1 for
		// the
		// selected
		// one
		{
			start = 1 + totalPages - getMaxPageListing(); // Make it start 10
			// from the end
			start = Math.max(1, start); // dont go below 1
		}
		else
		{
			start = 1 + page - getMaxPageListing() / 2;
		}

		int count = Math.min(totalPages, getMaxPageListing()); // what is
		// higher the
		// total count
		// or 10
		List hits = new ArrayList(count);
		for (int i = 0; i < count; i++)
		{
			hits.add(new Integer(start + i));
		}
		return hits;
	}

	public List linksBefore()
	{
		List range = linkRange();
		int i = 0;
		for (; i < range.size(); i++)
		{
			Integer in = (Integer) range.get(i);
			if (in.intValue() >= getPage())
			{
				break;
			}
		}

		return range.subList(0, i);
	}

	public List linksAfter()
	{
		if (getTotalPages() == getPage())
		{
			return Collections.EMPTY_LIST;
		}
		List range = linkRange();
		if (range.size() == 1) // Only one hit
		{
			return Collections.EMPTY_LIST;
		}
		int start = 0;
		for (int i = 0; i < range.size(); i++)
		{
			Integer in = (Integer) range.get(i);
			if (in.intValue() > getPage())
			{
				start = i;
				break;
			}
		}
		return range.subList(start, range.size());

	}

	/**
	 * @deprecated Use getSearchQuery().getInput("nameof field")
	 */
	public String getUserQuery()
	{
		return getSearchQuery().getInput("description");
	}

	public String getInput(String inKey)
	{
		SearchQuery query = getSearchQuery();
		if (query != null)
		{
			return query.getInput(inKey);
		}
		return null;
	}

	public boolean wasInput(String inKey, String inValue)
	{
		SearchQuery query = getSearchQuery();

		if (query != null && inValue != null && inKey != null)
		{
			Collection inputs = query.getInputs(inKey);
			if (inputs != null)
			{
				if( inputs.contains(inValue) )
				{
					return true;
				}
			}
		}

		return false;
	}

	public String getOrdering()
	{
		if (getSearchQuery() == null)
		{
			return null;
		}
		return getSearchQuery().getSortBy();
	}

	public String getIndexId()
	{
		return fieldIndexId;
	}

	public void setIndexId(String inIndexCounter)
	{
		fieldIndexId = inIndexCounter;
	}

	public String getFriendlyQuery()
	{
		SearchQuery query = getSearchQuery();
		if (query != null)
		{
			return getSearchQuery().toFriendly();
		}
		return null;
	}

	public void setSearchQuery(SearchQuery inQuery)
	{

		fieldSearchQuery = inQuery;
	}

	public boolean isEmpty()
	{
		return size() == 0;
	}

	// Remaining API are not implemented
	public List keys()
	{
		// All the ID's we can find
		return null;
	}

	public String highlight(Object inDoc, String inField)
	{
		return "Not implemented";
	}
	public String getValue(Object inHit, String inString)
	{
		return getValue((Data)inHit, inString);
	}
	public String getValue(Data inHit, String inString)
	{
		return inHit.get(inString);
	}
	//Use SearchResultsData.getValues
//	public Collection getValues(Object inHit, String inString)
//	{
//		String val =  getValue((Data)inHit, inString);
//		if( val == null)
//		{
//			return Collections.EMPTY_LIST;
//		}
//		String[] vals = null;
//		Collection collection = null;
//		if( val.contains("|") )
//		{
//			vals = MultiValued.VALUEDELMITER.split(val);
//		}
//		else
//		{
//			vals = new String[] { val };
//		}
//		collection = Arrays.asList(vals);
//		//if null check parent
//		return collection;
//	}

//	public String toString(Data inHit)
//	{
//		if (inHit instanceof Data)
//		{
//			return inHit.toString();
//		}
//		else
//		{
//			String name = getValue(inHit, "name");
//			if (name == null)
//			{
//				name = getValue(inHit, "shortdescription");
//			}
//			if (name == null)
//			{
//				name = getValue(inHit, "id");
//			}
//
//			return name;
//		}
//	}

	public void clear()
	{
		fieldCurrentPage = null;
	}

	public boolean containsAll(Collection arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean remove(Object inO)
	{
		return false;
	}

	public boolean removeAll(Collection arg0)
	{
		return false;
	}

	public boolean retainAll(Collection arg0)
	{
		return false;
	}

	public Object[] toArray()
	{
		Object[] all = new Object[size()];
		return toArray(all);
	}

	public Object[] toArray(Object[] all)
	{
		Iterator iter = iterator();
		int c = 0;
		while( iter.hasNext() )
		{
			all[c] = iter.next();
			c++;
		}
		return all;
	}

	public boolean add(Object arg0)
	{
		return false;
	}

	public boolean addAll(Collection arg0)
	{
		return false;
	}

	public Date getDateValue(Data inHit, String inField)
	{
		String value = getValue(inHit, inField);
		if( value == null)
		{
			return null;
		}
		SearchQuery q = getSearchQuery();
		if( q != null)
		{
			PropertyDetail detail = q.getPropertyDetails().getDetail(inField);
			Date date = DateStorageUtil.getStorageUtil().parseFromStorage(value);
			return date;
		}
		throw new OpenEditException("Search Query must be set");

	}

	
	@Deprecated public Collection getSelectedHits(){
		return getSelectedHitracker();
	}
	
	public HitTracker getSelectedHitracker()
	{
//		if( isAllSelected() )
//		{
//			return this;
//		}		
		if( getSessionId().startsWith("selected") )
		{
			return this;
		}

		HitTracker selecteddata = getSearcher().search(getSearchQuery());
		if( isAllSelected() )
		{
			//rerun the search
			selecteddata.selectAll();
		}
		else
		{
			selecteddata.setSelections(getSelections());
			selecteddata.setShowOnlySelected(true);
		}
//		else
//		{
//			ListHitTracker lhits = new ListHitTracker();	
//			lhits.setSessionId("selected" + getSessionId() );
//			hits = lhits;
			
//		SelectedHitsTracker hits = new SelectedHitsTracker(this);
//		selecteddata.setHitsName("selected" + getHitsName() + selecteddata.size() );
		selecteddata.setSessionId("selected" + getSessionId());
		return selecteddata;
	}
//
//	public Collection<Data> getSelectedHits()
//	{
//		if( isAllSelected() )
//		{
//			return this;
//		}
//		return getSelectedHitracker();
//	}
	public boolean hasMultipleSelections()
	{
		if( isAllSelected() )
		{
			return true;
		}

		if( fieldSelections != null && fieldSelections.size() > 1 )
		{
			return true;
		}
		return false;
	}
	
	public int getSelectionSize()
	{
		if( isAllSelected() )
		{
			return size();
		}
		if( fieldSelections != null )
		{
			return fieldSelections.size();
		}
		return 0;
	}
	public boolean hasSelections()
	{
		if( isAllSelected() )
		{
			return true;
		}
		if( fieldSelections != null && fieldSelections.size() > 0 )
		{
			return true;
		}
		return false;
	}
	
	public Collection<String> getSelections()
	{
		if (fieldSelections == null)
		{
			fieldSelections = new ArrayList<String>();
		}
		return fieldSelections;
	}

	public void setSelections(Collection<String> inSelections)
	{
		fieldSelections = inSelections;
	}

	public void addSelection(String inId)
	{
		if( inId == null)
		{
			return;
		}
		if(!getSelections().contains(inId)){
			getSelections().add(inId);
		}
	}
	
	public void removeSelection(String inId)
	{
		if( isAllSelected() )
		{
			setAllSelected(false);
			getSelections().clear();
			
			//TODO: Get one page worth?
			//add everything into the selections 
			for (Iterator iterator = iterator(); iterator.hasNext();)
			{
				Data data = (Data) iterator.next();
				getSelections().add(data.getId());
			}
		}
		getSelections().remove(inId);
	}

	public boolean isSelected(String inId)
	{
		if( isAllSelected() )
		{
			return true;
		}
		return getSelections().contains(inId);

	}

	/**
	 * @deprecated Call isSelected instead
	 * @param inId
	 * @return
	 */
	
	public boolean isSelectedOnPage(String inId)
	{
//		int bottom = (getPage() - 1) * getHitsPerPage(); // this is the start of
//		int index = bottom + count; // the offset
//		
//		boolean selected = getSelections().contains(new Integer(index));
//		return selected;
		return isSelected(inId);
	}

	public void toggleSelected(String inId)
	{
		if (isSelected(inId))
		{
			removeSelection(inId);
		}
		else
		{
			addSelection(inId);
		}

	}

	public void selectAll()
	{
		
		setAllSelected(true);
	}

	public void deselectAll()
	{
		getSelections().clear();
		fieldAllSelected = false;
		setUseServerCursor(false);

	}
	public String getFirstSelected()
	{
		if( hasSelections() )
		{
			if( isAllSelected())
			{
				return get(0).getId();
			}
			String first = getSelections().iterator().next();
			if( first != null)
			{
				return first;
			}
		}
		return null;
	}
	
	public void deselectCurrentPage() throws Exception
	{
		//deselectAll();
		List page = getPageOfHits();
		for (Iterator iterator = page.iterator(); iterator.hasNext();) 
		{
			Data row = (Data) iterator.next();
			removeSelection(row.getId());
		}

	}
	public boolean isPageSelected() throws Exception
	{
		//deselectAll();
		List page = getPageOfHits();
		for (Iterator iterator = page.iterator(); iterator.hasNext();) 
		{
			Data row = (Data) iterator.next();
			if( !isSelected(row.getId()) )
			{
				return false;
			}
		}
		return true;
	}
	
	public void selectCurrentPage() throws Exception
	{
		//deselectAll();
		List page = getPageOfHits();
		for (Iterator iterator = page.iterator(); iterator.hasNext();) 
		{
			Data row = (Data) iterator.next();
			addSelection(row.getId());
		}

	}
	
	public String getSessionId()
	{
		if( fieldTempSessionId != null)
		{
			return fieldTempSessionId;
		}
		return getSearchQuery().getSessionId();
	}
	
	public String getHitsName()
	{
		if( fieldHitsName != null) //This may be not needed
		{
			return fieldHitsName;
		}
		return getSearchQuery().getHitsName();
	}
	
	public void setHitsName(String inHitsname)
	{
		fieldHitsName = inHitsname;
	}
	
	public String getCatalogId()
	{
		return getSearchQuery().getCatalogId();
	}
	
	public void setCatalogId(String inCatalogid)
	{
		getSearchQuery().setCatalogId(inCatalogid);
	}

	public int indexOf(String inCatalogId, String inId)
	{
		if( inCatalogId == null)
		{
			inCatalogId = getCatalogId();
		}
		if( inId == null)
		{
			return -1;
		}
		for(int i = 0; i < size(); i++)
		{
			Data hit = get(i);
			String catalogId = getValue(hit, "catalogid");
			String id = getValue(hit, "id");
			if(inCatalogId.equals(catalogId) && inId.equals(id))
			{
				return i;
			}
		}
		return -1;
	}
	public int getPageForIndexLocation(int inIndex)
	{
		int page =   inIndex / getHitsPerPage();
		page++;
		return page;
	}
	public Object previous(int inIndex)
	{
		if(inIndex == -1)
		{
			return null;
		}
		if(inIndex == 0)
		{
			return null;
		}
		return get(inIndex - 1);
	}
	
	public int previousIndex(String inIndex){
		int index = Integer.parseInt(inIndex);
		if(index <= 1)
		{
			return -1;
		}
		return index - 1;
	}
	
	public Object previous(String inCatalogId, String inId)
	{
		int indexOfCurrent = indexOf(inCatalogId, inId);
		return previous(indexOfCurrent);
	}
	public Object first()
	{
		if( size() == 0)
		{
			return null;
		}
		return get(0);
	}
	public Object next(int inIndex)
	{
		if(inIndex == -1)
		{
			return null;
		}
		if(inIndex >= size() - 1)
		{
			return null;
		}
		return get(inIndex + 1);
	}
	
	public int nextIndex(String inIndex){
		int index = Integer.parseInt(inIndex);
		if(index >= size()){
			return -1;
		}
		return index + 1;
	}
	
	public Object next(String inCatalogId, String inId)
	{
		int indexOfCurrent = indexOf(inCatalogId, inId);
		return next(indexOfCurrent);
	}

	public Object nextById(String inId){
		int current = indexOfId(inId);
		if(current != -1){
			if(current < size()-1){
				return get(current +1);
			}
		}
		return null;
	}
	public Object previousById(String inId){
		int current = indexOfId(inId);
		if(current != -1){
			if(current > 0){
				return get(current -1);
			}
		}
		return null;
	}
	
	public int indexOfId(String inId)
	{
		if( inId == null || inId.startsWith("multiedit:") || inId.trim().isEmpty() )
		{
			return -1;
		}
		int found = findIdOnPage(inId,getPage());
		if( found > -1)
		{
			return found;
		}
		if( getTotalPages() > getPage() )
		{
			//Look one after
			found = findIdOnPage(inId,getPage() + 1);
			if( found > -1)
			{
				return found;
			}
		}
		//Look one before
		if( getPage() > 1 )
		{
			found = findIdOnPage(inId,getPage() -1);
			if( found > -1)
			{
				return found;
			}
		}
	
		return -1;
		
	}

	protected int findIdOnPage(String inId, int inPage)
	{
		int size = size();
		int start = (inPage-1) * getHitsPerPage();
		int end = (inPage) * getHitsPerPage();
		end = Math.min(size, end);
		for (int i = start; i < end; i++)
		{
			Data hit = get(i);
			String id = getValue(hit, "id");
			if( id.equals(inId))
			{
				return i;
			}
		}
		return -1;
	}
	
	public String getDataSource() {
		return fieldDataSource;
	}

	public void setDataSource(String inDataSource) {
		fieldDataSource = inDataSource;
	}
	
	public int parseInt(Object inValue)
	{
		if( inValue == null)
		{
			return 0;
		}
		String text = String.valueOf(inValue);
		if( text.length() == 0)
		{
			return 0; 
		}
		if( Character.isDigit(text.charAt(0)))
		{
			return Integer.parseInt(text);
		}
		return 0;
	}
	
	public boolean isAllSelected()
	{
		return fieldAllSelected;
	}
	public Data findData(String inField, String inValue)
	{
		if(inValue == null || inField == null)
		{
			return null;
		}
		for (Data hit: getPageOfHits() )
		{
			if(inValue.equals(hit.get(inField)))
			{
				return hit;
			}			
		}
		for (int i = 0; i < size(); i++)
		{
			Data hit = get(i);
			if(inValue.equals(hit.get(inField)))
			{
				return hit;
			}
		}
		return null;
	}
	public int findRow(String inField, String inValue)
	{
		if(inValue == null || inField == null)
		{
			return -1;
		}
		for (int i = 0; i < size(); i++)
		{
			Data hit = get(i);
			if(inValue.equals(hit.get(inField)))
			{
				return i;
			}
		}
		return -1;
	}

	public void loadPreviousSelections(HitTracker inOld) 
	{
		setSelections(inOld.getSelections());
		//setAllSelected(inOld.isAllSelected()); This is dangerous behaviour - 
		//if I've done a search and had 10 hits and selected all, I don't want to still have all selected when I then do another search for totally different assets
	}

	public Searcher getSearcher()
	{
		return fieldSearcher;
	}

	public void setSearcher(Searcher inSearcher)
	{
		fieldSearcher = inSearcher;
	}

	protected void setSessionId(String inSessionId)
	{
		fieldTempSessionId = inSessionId;
	}
	public List<FilterNode> getFilterOptions()
	{
		if(fieldFilterOptions == null){
			fieldFilterOptions = loadFacetsFromResults();
		}
		return fieldFilterOptions;
	
	}
	public void setFilterOptions(List <FilterNode> filters){
		fieldFilterOptions = filters;
	}

	protected List loadFacetsFromResults() 
	{
		// TODO Auto-generated method stub
		return new ArrayList(); //this is load code
	}

	public FilterNode findFilterNode(String inType)
	{
		List <FilterNode> nodes = getFilterOptions();
		if( nodes != null)
		{
			for (Iterator iterator = nodes.iterator(); iterator.hasNext();)
			{
				FilterNode filterNode = (FilterNode) iterator.next();
				if( filterNode.getId().endsWith(inType))
				{
					return filterNode;
				}
			}
		}
		return null;
	}
//	public void selectFilters(List selected)
//	{
//		List topnodes = getFilters();
//		
//		if (topnodes != null)//Assettype, colour, 
//		{
//			for (Iterator iterator = topnodes.iterator(); iterator.hasNext();)
//			{
//				FilterNode node = (FilterNode) iterator.next();
//				for (Iterator iterator2 = node.getChildren().iterator(); iterator2.hasNext();)
//				{
//					FilterNode child = (FilterNode) iterator2.next();
//					if (selected.contains(child.getId()))
//					{
//						child.setSelected(true);//values						
//					}
//					else
//					{
//						child.setSelected(false);
//					}
//				}
//			}
//		}
//
//	}

//	public boolean hasSelectedFilters()
//	{
//
//		List topnodes = getFilters();
//		for (Iterator iterator = topnodes.iterator(); iterator.hasNext();)
//		{
//			FilterNode node = (FilterNode) iterator.next();
//			for (Iterator iterator2 = node.getChildren().iterator(); iterator2.hasNext();)
//			{
//				FilterNode child = (FilterNode) iterator2.next();
//				if (child.isSelected())
//				{
//					return true;
//				}
//			}
//		}
//		return false;
//	}

//	public void refreshFilters() throws Exception
//	{
//		// TODO Auto-generated method stub
//		
//	}

	public boolean isChildFacetSelected(FilterNode inNode){
		List selectedfilters = getSearchQuery().getFilters();
		for (Iterator iterator = selectedfilters.iterator(); iterator.hasNext();)
		{
			FilterNode selected = (FilterNode) iterator.next();
			if(selected.getId().equals(inNode.getId())){
				return true;
			}
			
		}
		return false;
		
	}
	public void invalidate()
	{
		setIndexId(getIndexId() + 1);
	}
	public void refresh()
	{
		// TODO Auto-generated method stub
		
	}
	public boolean isRecentSearch()
	{
		long now = System.currentTimeMillis();
		long min5 = 1000L*60l*5l;
		return now - fieldSearchTime < min5;
	}	
}

