package com.openedit.hittracker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openedit.Data;
import org.openedit.data.PropertyDetail;
import org.openedit.util.DateStorageUtil;

import com.openedit.OpenEditException;

public abstract class HitTracker implements Serializable, Collection
{
	protected int fieldPage = 0;
	protected int fieldHitsPerPage = 12;
	protected int fieldCurrentHit;
	protected Set fieldSelections;
	protected SearchQuery fieldSearchQuery;
	protected boolean fieldUseRandom;
	protected List fieldRandomLookup;
	protected transient String fieldIndexId; //cause this index to invalidate
	protected String fieldResultType;
	protected String fieldDataSource;
	protected List fieldCurrentPage;
	protected int fieldMaxPageListing = 10; //used for page listing
	

	public HitTracker()
	{

	}

	public String getResultType()
	{
		return getSearchQuery().getResultType();
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

	public Object getById(String inId)
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

	public List<Data> getPageOfHits()
	{
		if( fieldCurrentPage == null)
		{
			setPage(1);
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
		return fieldHitsPerPage;
	}

	public void setHitsPerPage(int inHitsPerPage)
	{
		if (inHitsPerPage > 0 && inHitsPerPage != fieldHitsPerPage)
		{
			fieldCurrentPage = null;
			fieldHitsPerPage = inHitsPerPage;
			if (getPage() * inHitsPerPage > getTotal())
			{
				setPage(1);
			}
			else
			{
				setPage(getPage());
			}
		}
	}

	public int getPage()
	{
		if( fieldPage == 0)
		{
			setPage(1);
		}
		return fieldPage;
	}

	public void setPage(int inPage)
	{
		if( fieldPage != inPage || fieldCurrentPage == null)
		{
			fieldPage = inPage;
			
			int inHitsPerPage = getHitsPerPage();
			List page = new ArrayList();
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
			String[] inputs = query.getInputs(inKey);
			if (inputs != null)
			{
				for (int i = 0; i < inputs.length; i++)
				{
					if (inputs[i] != null)
					{
						if (inputs[i].equals(inValue))
						{
							return true;
						}
					}
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

	public String toString(Data inHit)
	{
		if (inHit instanceof Data)
		{
			return inHit.toString();
		}
		else
		{
			String name = getValue(inHit, "name");
			if (name == null)
			{
				name = getValue(inHit, "shortdescription");
			}
			if (name == null)
			{
				name = getValue(inHit, "id");
			}

			return name;
		}
	}

	public void clear()
	{
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
	
	public List getSelectedHits(){
		ArrayList hits = new ArrayList();
		for (Iterator iterator = getSelections().iterator(); iterator.hasNext();)
		{
			Integer hit = (Integer) iterator.next();
			hits.add(get(hit.intValue()));
			
		}
		return hits;
	}
	
	public Set getSelections()
	{
		if (fieldSelections == null)
		{
			fieldSelections = new HashSet();

		}

		return fieldSelections;
	}

	public void setSelections(Set inSelections)
	{
		fieldSelections = inSelections;
	}

	public void addSelection(int hit)
	{
		getSelections().add(new Integer(hit));
	}

	
	public void removeSelection(int hit)
	{
		
		getSelections().remove(new Integer(hit));
	}

	public boolean isSelected(int hit)
	{
		
		return getSelections().contains(new Integer(hit));

	}

	public boolean isSelectedOnPage(int count)
	{

		int bottom = (getPage() - 1) * getHitsPerPage(); // this is the start of
		int index = bottom + count; // the offset
		
		boolean selected = getSelections().contains(new Integer(index));
		return selected;

	}

	public void toggleSelected(int inCount)
	{
		if (isSelected(inCount))
		{
			removeSelection(inCount);
		}
		else
		{
			addSelection(inCount);
		}

	}

	public void selectAll()
	{
		
		for (int count = 0; count < size(); count++)
		{
			
			
			addSelection(count);
			

		}

	}

	public void deselectAll()
	{
		setSelections(null);

	}

	public void selectCurrentPage() throws Exception
	{
		deselectAll();
		List page = getPageOfHits();
		int bottom = (getPage() - 1) * getHitsPerPage(); // this is the start of
		

		for (int count = 0; count < page.size(); count++)
		{
			int index = bottom + count; // the offset
			
			addSelection(index);
			

		}

	}
	
	public String getSessionId()
	{
		return getSearchQuery().getSessionId();
	}
	
	public String getHitsName()
	{
		return getSearchQuery().getHitsName();
	}
	
	public void setHitsName(String inHitsname)
	{
		getSearchQuery().setHitsName(inHitsname);
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
		int current = indexOf(inId);
		if(current != -1){
			if(current < size()-1){
				return get(current +1);
			}
		}
		return null;
	}
	public Object previousById(String inId){
		int current = indexOf(inId);
		if(current != -1){
			if(current > 0){
				return get(current -1);
			}
		}
		return null;
	}
	
	private int indexOf(String inId)
	{
			for (int i = 0; i < size(); i++)
			{
				Data hit = get(i);
				String id = getValue(hit, "id");
				if( id.equals(inId))
				{
					return i;
				}
			}
			return -1;
			//throw new OpenEditException("getById Not implemented");
		
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
	
	public boolean isAllSelected(){
		return getSelectedHits().size() == getTotal();
	}
	
	public Data findRow(String inField, String inValue)
	{
		if(inValue == null || inField == null)
		{
			return null;
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
}
