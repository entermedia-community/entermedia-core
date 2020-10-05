package org.openedit.hittracker;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.openedit.Data;
import org.openedit.data.Searcher;

public class HitTrackerWrapper extends HitTracker {

	protected HitTracker fieldParentTracker;
	
	public HitTrackerWrapper(HitTracker inParent)
	{
		fieldParentTracker = inParent;
	}
	
	public void enableBulkOperations() {
		fieldParentTracker.enableBulkOperations();
	}

	public boolean isUseServerCursor() {
		return fieldParentTracker.isUseServerCursor();
	}

	public boolean isShowOnlySelected() {
		return fieldParentTracker.isShowOnlySelected();
	}

	public List linkRange() {
		return fieldParentTracker.linkRange();
	}

	public List linksBefore() {
		return fieldParentTracker.linksBefore();
	}

	public List linksAfter() {
		return fieldParentTracker.linksAfter();
	}

	public boolean isEmpty() {
		return fieldParentTracker.isEmpty();
	}

	public List keys() {
		return fieldParentTracker.keys();
	}

	public boolean isSelected(String inId) {
		return fieldParentTracker.isSelected(inId);
	}

	public boolean isSelectedOnPage(String inId) {
		return fieldParentTracker.isSelectedOnPage(inId);
	}

	public Object previous(int inIndex) {
		return fieldParentTracker.previous(inIndex);
	}

	public int previousIndex(String inIndex) {
		return fieldParentTracker.previousIndex(inIndex);
	}

	public Object previous(String inCatalogId, String inId) {
		return fieldParentTracker.previous(inCatalogId, inId);
	}

	public Object next(int inIndex) {
		return fieldParentTracker.next(inIndex);
	}

	public int nextIndex(String inIndex) {
		return fieldParentTracker.nextIndex(inIndex);
	}

	public Object next(String inCatalogId, String inId) {
		return fieldParentTracker.next(inCatalogId, inId);
	}

	public Object nextById(String inId) {
		return fieldParentTracker.nextById(inId);
	}

	public Object previousById(String inId) {
		return fieldParentTracker.previousById(inId);
	}
/*
	public int pageOfId(String inId) 
	{
		int parentpage = fieldParentTracker.pageOfId(inId);
		int parenthitsperpage = fieldParentTracker.getHitsPerPage();
		
	}
*/
	public int parseInt(Object inValue) {
		return fieldParentTracker.parseInt(inValue);
	}

	public boolean isAllSelected() {
		return fieldParentTracker.isAllSelected();
	}

	public void loadPreviousSelections(HitTracker inOld) {
		fieldParentTracker.loadPreviousSelections(inOld);
	}

	public boolean isChildFacetSelected(FilterNode inNode) {
		return fieldParentTracker.isChildFacetSelected(inNode);
	}

	public void invalidate() {
		fieldParentTracker.invalidate();
	}

	public void refresh() {
		fieldParentTracker.refresh();
	}

	public boolean isRecentSearch() {
		return fieldParentTracker.isRecentSearch();
	}

	public boolean isInputEquals(String inDetail, String inValue) {
		return fieldParentTracker.isInputEquals(inDetail, inValue);
	}

	public boolean isSortedBy(String inKey) {
		return fieldParentTracker.isSortedBy(inKey);
	}

	public Stream parallelStream() {
		return fieldParentTracker.parallelStream();
	}

	public void setShowOnlySelected(boolean inShowOnlySelected) {
		fieldParentTracker.setShowOnlySelected(inShowOnlySelected);
	}

	public void setCurrentHit(int inCurrentHit) {
		fieldParentTracker.setCurrentHit(inCurrentHit);
	}

	public boolean wasInput(String inKey, String inValue) {
		return fieldParentTracker.wasInput(inKey, inValue);
	}

	public boolean remove(Object inO) {
		return fieldParentTracker.remove(inO);
	}

	public boolean removeAll(Collection arg0) {
		return fieldParentTracker.removeAll(arg0);
	}

	public boolean removeIf(Predicate filter) {
		return fieldParentTracker.removeIf(filter);
	}

	public void setUseServerCursor(boolean inUseServerCursor) {
		fieldParentTracker.setUseServerCursor(inUseServerCursor);
	}

	public Data getById(String inId) {
		return fieldParentTracker.getById(inId);
	}

	public Collection getLast(int inCount) {
		return fieldParentTracker.getLast(inCount);
	}

	public Iterator getAllHits() {
		return fieldParentTracker.getAllHits();
	}

	public int getCurrentHit() {
		return fieldParentTracker.getCurrentHit();
	}

	public boolean containsById(String inId) {
		return fieldParentTracker.containsById(inId);
	}

	public String getInput(String inKey) {
		return fieldParentTracker.getInput(inKey);
	}

	public String getIndexId() {
		return fieldParentTracker.getIndexId();
	}

	public String getFriendlyQuery() {
		return fieldParentTracker.getFriendlyQuery();
	}

	public void clear() {
		fieldParentTracker.clear();
	}

	public boolean containsAll(Collection arg0) {
		return fieldParentTracker.containsAll(arg0);
	}

	public boolean add(Object arg0) {
		return fieldParentTracker.add(arg0);
	}

	public boolean addAll(Collection arg0) {
		return fieldParentTracker.addAll(arg0);
	}

	public Date getDateValue(Data inHit, String inField) {
		return fieldParentTracker.getDateValue(inHit, inField);
	}

	public void addSelection(String inId) {
		fieldParentTracker.addSelection(inId);
	}

	public void deselectAll() {
		fieldParentTracker.deselectAll();
	}

	public String getFirstSelected() {
		return fieldParentTracker.getFirstSelected();
	}

	public void deselectCurrentPage() throws Exception {
		fieldParentTracker.deselectCurrentPage();
	}

	public String getCatalogId() {
		return fieldParentTracker.getCatalogId();
	}

	public Object first() {
		return fieldParentTracker.first();
	}

	public String getDataSource() {
		return fieldParentTracker.getDataSource();
	}

	/*
	public int findRow(String inField, String inValue) {
		return fieldParentTracker.findRow(inField, inValue);
	}
	*/
	
	public FilterNode findFilterNode(String inType) {
		return fieldParentTracker.findFilterNode(inType);
	}

	public HitTracker copy() {
		return fieldParentTracker.copy();
	}

	public boolean equals(Object obj) {
		return fieldParentTracker.equals(obj);
	}

	public Data findData(String inField, String inValue) {
		return fieldParentTracker.findData(inField, inValue);
	}

	public void forEach(Consumer arg0) {
		fieldParentTracker.forEach(arg0);
	}

	public String getResultType() {
		return fieldParentTracker.getResultType();
	}

	public Data getRandomHit() {
		return fieldParentTracker.getRandomHit();
	}

	public String getSearchType() {
		return fieldParentTracker.getSearchType();
	}

	public int indexOf(int count) {
		return fieldParentTracker.indexOf(count);
	}

	public Collection getSourcePaths() {
		return fieldParentTracker.getSourcePaths();
	}

	public int getMaxPageListing() {
		return fieldParentTracker.getMaxPageListing();
	}

	public int getTotal() {
		return fieldParentTracker.getTotal();
	}

	public SearchQuery getSearchQuery() {
		return fieldParentTracker.getSearchQuery();
	}

	public String getQuery() {
		return fieldParentTracker.getQuery();
	}

	public String getUserQuery() {
		return fieldParentTracker.getUserQuery();
	}

	public String getOrdering() {
		return fieldParentTracker.getOrdering();
	}

	public void setIndexId(String inIndexCounter) {
		fieldParentTracker.setIndexId(inIndexCounter);
	}

	public void setSearchQuery(SearchQuery inQuery) {
		fieldParentTracker.setSearchQuery(inQuery);
	}

	public String highlight(Object inDoc, String inField) {
		return fieldParentTracker.highlight(inDoc, inField);
	}

	public String getValue(Object inHit, String inString) {
		return fieldParentTracker.getValue(inHit, inString);
	}

	public String getValue(Data inHit, String inString) {
		return fieldParentTracker.getValue(inHit, inString);
	}

	public boolean retainAll(Collection arg0) {
		return fieldParentTracker.retainAll(arg0);
	}

//	public Object[] toArray() {
//		return fieldParentTracker.toArray();
//	}
//
//	public Object[] toArray(Object[] all) {
//		return fieldParentTracker.toArray(all);
//	}

	public Collection getSelectedHits() {
		return fieldParentTracker.getSelectedHits();
	}

	public HitTracker getSelectedHitracker() {
		return fieldParentTracker.getSelectedHitracker();
	}

	public boolean hasMultipleSelections() {
		return fieldParentTracker.hasMultipleSelections();
	}

	public int getSelectionSize() {
		return fieldParentTracker.getSelectionSize();
	}

	public boolean hasSelections() {
		return fieldParentTracker.hasSelections();
	}

	public Collection getSelections() {
		return fieldParentTracker.getSelections();
	}

	public void setSelections(Collection inSelections) {
		fieldParentTracker.setSelections(inSelections);
	}

	public void removeSelection(String inId) {
		fieldParentTracker.removeSelection(inId);
	}

	public void toggleSelected(String inId) {
		fieldParentTracker.toggleSelected(inId);
	}

	public void selectAll() {
		fieldParentTracker.selectAll();
	}

	public String getSessionId() {
		return fieldParentTracker.getSessionId();
	}

	public void setHitsName(String inHitsname) {
		fieldParentTracker.setHitsName(inHitsname);
	}

	public void setCatalogId(String inCatalogid) {
		fieldParentTracker.setCatalogId(inCatalogid);
	}

	public int indexOf(String inCatalogId, String inId) {
		return fieldParentTracker.indexOf(inCatalogId, inId);
	}

	public int indexOfId(String inId) {
		return fieldParentTracker.indexOfId(inId);
	}

	public void setDataSource(String inDataSource) {
		fieldParentTracker.setDataSource(inDataSource);
	}

	public Searcher getSearcher() {
		return fieldParentTracker.getSearcher();
	}

	public void setSearcher(Searcher inSearcher) {
		fieldParentTracker.setSearcher(inSearcher);
	}

	public void setSessionId(String inSessionId) {
		fieldParentTracker.setSessionId(inSessionId);
	}


	/*
	public void setHitsPerPageHeight(String pageHeight, int inRowHeight) {
		fieldParentTracker.setHitsPerPageHeight(pageHeight, inRowHeight);
	}

	public String idOnPreviousPage() {
		return fieldParentTracker.idOnPreviousPage();
	}

	public String idOnThisPage() {
		return fieldParentTracker.idOnThisPage();
	}

	public String idOnNextPage() {
		return fieldParentTracker.idOnNextPage();
	}
*/
	public boolean hasChanged(HitTracker inTracker) {
		return fieldParentTracker.hasChanged(inTracker);
	}

//	public Spliterator spliterator() {
//		return fieldParentTracker.spliterator();
//	}

//	public Stream stream() {
//		return fieldParentTracker.stream();
//	}
//
//	public Object[] toArray(IntFunction generator) {
//		return fieldParentTracker.toArray(generator);
//	}

	public String toString() {
		return fieldParentTracker.toString();
	}

	protected HitTracker getParent()
	{
		return fieldParentTracker;
	}
	
	@Override
	public Data get(int count) 
	{
		return getParent().get(count);
	}

	@Override
	public Iterator iterator() {
		return getParent().iterator();
	}

	@Override
	public boolean contains(Object inHit) {
		return getParent().contains(inHit);
	}

	@Override
	public int size() {
		return getParent().size();
	}

	@Override
	public String getHitsName() {
		return getParent().getHitsName();
	}

	@Override
	public Map getActiveFilterValues()
	{
		return getParent().getActiveFilterValues();
	}

}
