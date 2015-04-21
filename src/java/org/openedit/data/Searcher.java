package org.openedit.data;

import java.text.DateFormat;
import java.util.Collection;
import java.util.List;

import org.openedit.Data;
import org.openedit.profile.UserProfile;

import com.openedit.OpenEditException;
import com.openedit.WebPageRequest;
import com.openedit.hittracker.HitTracker;
import com.openedit.hittracker.SearchQuery;
import com.openedit.users.User;

public interface Searcher
{
	public String nextId();
	
	public abstract HitTracker cachedSearch(WebPageRequest inPageRequest, SearchQuery inQuery) throws OpenEditException;

	public abstract HitTracker loadHits(WebPageRequest inReq) throws OpenEditException;

	public abstract HitTracker loadHits(WebPageRequest inReq, String hitsname) throws OpenEditException;

	/** @deprecated **/
	public abstract DateFormat getDefaultDateFormat();

	public abstract void setDefaultDateFormat(DateFormat inDefaultDateFormat);

	public abstract HitTracker fieldSearch(WebPageRequest inReq) throws OpenEditException;

	public HitTracker fieldSearch(String attr, String value);

	public HitTracker fieldSearch(String attr, String value, String orderby); 

	public abstract SearchQuery addStandardSearchTerms(WebPageRequest inPageRequest) throws OpenEditException;

	public Data updateData(WebPageRequest inReq, String[] fields, Data data);
	
	public abstract List deselect(String inField, String[] toremove) throws OpenEditException;

	public QueryBuilder query();

	/**
	 * Use this to automatically filter a search with the .xconf that declares the action.
	 * Example: 
	 * 	 <page-action name="OrderModule.getOrdersForUser">
	 *	 	<not>
	 *			<orderstatus>completed</orderstatus>
	 *   	</not>
	 *	 </page-action>
	 * @param inReq
	 * @param search
	 * @return
	 */

	public abstract SearchQuery addActionFilters(WebPageRequest inReq, SearchQuery search);

	public abstract HitTracker loadPageOfSearch(WebPageRequest inPageRequest) throws OpenEditException;

	//Some of these may be able to become protected
	public abstract void reIndexAll() throws OpenEditException;

	public SearchQuery createSearchQuery();

	public Object searchById(String inId);

	public Data loadData(Data inHit);
	
	public Object searchByField(String inField,String inValue);

	public Data searchByQuery(SearchQuery inQuery);

//	public abstract HitTracker search(String inQuery);

	public abstract HitTracker search(SearchQuery inQuery);

//	public abstract HitTracker search(String inQuery, String inOrdering);

	public abstract String getIndexId();

	public abstract void clearIndex();

	public abstract PropertyDetailsArchive getPropertyDetailsArchive();

	public abstract void setPropertyDetailsArchive(PropertyDetailsArchive inPropertyDetailsArchive);

	public PropertyDetails getPropertyDetails();

	/**
	 * @deprecated No longer need a user passed in
	 * @param inView
	 * @param inUser
	 * @return
	 */
	public List getDetailsForView(String inView, User inUser);

	public List getDetailsForView(String inView, UserProfile inUserProfile);

	/**
	 * Do we even need this to work?
	 * @deprecated No longer need a user passed in
	 * @param inView
	 * @param inFieldName
	 * @param inUser
	 * @return
	 */
	public PropertyDetail getDetailForView(String inView, String inFieldName, User inUser);
	
	public List getProperties();

	public abstract HitTracker getAllHits(WebPageRequest inReq);

	public abstract HitTracker getAllHits();

	public abstract SearcherManager getSearcherManager();

	public abstract void setSearcherManager(SearcherManager inSearcherManager);

	/**
	 * @deprecated Use {@link #getSearchType()} instead
	 */
	public abstract String getFieldName();

	public abstract String getSearchType();

	/**
	 * @deprecated Use {@link #setSearchType(String)} instead
	 */
	public abstract void setFieldName(String inFieldName);

	public abstract void setSearchType(String inFieldName);

	public abstract String getCatalogId();

	public abstract void setCatalogId(String inCatalogId);

	public abstract void saveData(Data inData, User inUser);

	public abstract Data createNewData();

	public abstract void deleteAll(User inUser);

	public abstract void delete(Data inData, User inUser);

	public abstract void saveAllData(Collection<Data> inAll, User inUser);
	
	public PropertyDetail getDetail(String inId);
	
	public void changeSort(WebPageRequest inReq);
	
	public void addChildQuery(WebPageRequest inReq);

	public abstract void saveDetails(WebPageRequest inReq, String[] fields,	Data data, String id);

	/**
	 * @deprecated use searchByQuery
	 * @param inQ
	 * @return
	 */
	public Data uniqueResult(SearchQuery inQ);

	HitTracker searchByIds(Collection<String> inIds);

	public void updateFilters(WebPageRequest inReq);

	public void restoreSettings();
	
	public void reloadSettings();

}