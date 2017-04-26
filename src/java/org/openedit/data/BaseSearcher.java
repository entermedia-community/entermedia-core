package org.openedit.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.print.attribute.standard.MediaPrintableArea;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.entermediadb.location.GeoCoder;
import org.entermediadb.location.Position;
import org.openedit.Data;
import org.openedit.ModuleManager;
import org.openedit.MultiValued;
import org.openedit.OpenEditException;
import org.openedit.WebPageRequest;
import org.openedit.config.Configuration;
import org.openedit.event.WebEvent;
import org.openedit.event.EventManager;
import org.openedit.hittracker.GeoFilter;
import org.openedit.hittracker.HitTracker;
import org.openedit.hittracker.SearchQuery;
import org.openedit.hittracker.Term;
import org.openedit.locks.LockManager;
import org.openedit.modules.translations.LanguageMap;
import org.openedit.profile.UserProfile;
import org.openedit.users.User;
import org.openedit.util.DateStorageUtil;
import org.openedit.util.URLUtilities;

import groovy.json.JsonSlurper;

public abstract class BaseSearcher implements Searcher, DataFactory
{
	private static final Log log = LogFactory.getLog(BaseSearcher.class);
	protected DateFormat fieldDefaultDateFormat;
	protected String fieldSearchType;
	protected String fieldCatalogId;
	protected static final String delim = ":";
	protected PropertyDetailsArchive fieldPropertyDetailsArchive;
	protected SearcherManager fieldSearcherManager;
	protected EventManager fieldEventManager;
	protected boolean fieldFireEvents = false;
	protected SearchQueryFilter fieldSearchQueryFilter;
	protected boolean fieldAllowRemoteDetails = false;
	protected String fieldAlternativeIndex;
	protected ModuleManager fieldModuleManager;
	protected String fieldNewDataName;
	
	protected boolean fieldForceBulk = false;
	

	public boolean isForceBulk()
	{
		return fieldForceBulk;
	}

	public void setForceBulk(boolean inForceBulk)
	{
		fieldForceBulk = inForceBulk;
	}

	
	public String getAlternativeIndex()
	{
		return fieldAlternativeIndex;
	}

	public void setAlternativeIndex(String inAlternativeIndex)
	{
		fieldAlternativeIndex = inAlternativeIndex;
	}
	
	public boolean isAllowRemoteDetails()
	{
		return fieldAllowRemoteDetails;
	}

	public void setAllowRemoteDetails(boolean inAllowRemoteDetails)
	{
		fieldAllowRemoteDetails = inAllowRemoteDetails;
	}

	public SearchQueryFilter getSearchQueryFilter()
	{
		if (fieldSearchQueryFilter == null)
		{
			fieldSearchQueryFilter = (SearchQueryFilter) getModuleManager().getBean(getCatalogId(), "searchQueryFilter");
		}
		return fieldSearchQueryFilter;
	}

	public void setSearchQueryFilter(SearchQueryFilter inScriptedSearchFilter)
	{
		fieldSearchQueryFilter = inScriptedSearchFilter;
	}

	@Override
	public boolean initialize()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public ModuleManager getModuleManager()
	{
		return fieldModuleManager;
	}

	public void setModuleManager(ModuleManager inModuleManager)
	{
		fieldModuleManager = inModuleManager;
	}

	public String getNewDataName()
	{
		if (fieldNewDataName == null)
		{
			fieldNewDataName = getPropertyDetails().getClassName();
		}
		return fieldNewDataName;
	}

	public void setNewDataName(String inDataClassName)
	{
		fieldNewDataName = inDataClassName;
	}

	public BaseSearcher()
	{
		super();
	}

	public QueryBuilder query()
	{
		QueryBuilder builder = new QueryBuilder();
		builder.setSearcher(this);
		return builder;
	}

	/*
	 * This is the main search method
	 */
	public HitTracker cachedSearch(WebPageRequest inPageRequest, SearchQuery inQuery) throws OpenEditException
	{
		if (inQuery == null)
		{
			return null;
		}
		if (log.isDebugEnabled())
		{
			log.debug("checking: " + getCatalogId() + " " + inQuery.toFriendly());
		}
		addShowOnly(inPageRequest, inQuery);

		inPageRequest.putPageValue("searcher", this);
		HitTracker tracker = null;

		if (inQuery.isEmpty())
		{
			return null;
		}

		if (inQuery.getHitsName() == null)
		{
			String hitsname = inPageRequest.findValue("hitsname");
			if (hitsname == null)
			{
				hitsname = "hits";
			}
			inQuery.setHitsName(hitsname);
		}
		tracker = (HitTracker) inPageRequest.getSessionValue(inQuery.getSessionId());

		boolean runsearch = false;

		String cache = inPageRequest.getRequestParameter("cache");
		if(cache != null && !Boolean.parseBoolean("cache")){
			runsearch = true;
		}
		
		if (tracker == null)
		{
			runsearch = true;
		}
		if (!runsearch && tracker != null)
		{
			String clear = inPageRequest.getRequestParameter(getSearchType() + "clearresults");
			if (Boolean.parseBoolean(clear))
			{
				runsearch = true;
			}
			clear = inPageRequest.getRequestParameter(getSearchType() + "clearselection");
			if (Boolean.parseBoolean(clear))
			{
				tracker.deselectAll();
				tracker.setShowOnlySelected(false);
				runsearch = true;
			}
			else
			{
				String showonly = inPageRequest.getRequestParameter(getSearchType() + "showonlyselections");

				if (showonly != null)
				{
					tracker.setShowOnlySelected(Boolean.parseBoolean(showonly));
					runsearch = true;
				}
			}

			if (!runsearch && hasChanged(tracker))
			{
				runsearch = true;
			}
			if (!runsearch && !inQuery.equals(tracker.getSearchQuery()))
			{
				runsearch = true;
			}
			if (!runsearch && inQuery.getSortBy() != null)
			{
				String oldSort = tracker.getOrdering();
				String currentsort = inQuery.getSortBy();
				if (!currentsort.equals(oldSort))
				{
					runsearch = true;
				}
			}
			//Move this code down
			if (inQuery.getSortBy() == null)
			{
				String oldSort = tracker.getOrdering();
				inQuery.setSortBy(oldSort);
			}
		}
		HitTracker oldtracker = null;
		if (runsearch)
		{
			try
			{
				UserProfile usersettings = (UserProfile) inPageRequest.getUserProfile();
				if (usersettings != null && inQuery.getSortBy() == null)
				{
					String sort = usersettings.getSortForSearchType(inQuery.getResultType());
					inQuery.setSortBy(sort);
				}
				if (inQuery.getSortBy() == null)
				{
					String sort = inPageRequest.findValue("sortby");
					inQuery.setSortBy(sort);
				}
				oldtracker = tracker;
				if (oldtracker != null)
				{
					if (oldtracker.getSearchQuery().hasFilters())
					{
						String clearfilters = inPageRequest.getRequestParameter("clearfilters");
						if (!Boolean.parseBoolean(clearfilters))
						{
							inQuery.setFilters(oldtracker.getSearchQuery().getFilters());
						}
					}
				}
				String endusersearch = inPageRequest.findValue( getSearchType() + "endusersearch");
				if( endusersearch == null )
				{
					inQuery.setEndUserSearch(true);
				}
				else
				{
					inQuery.setEndUserSearch(Boolean.parseBoolean(endusersearch));
				}	

				String hitsperpage = inPageRequest.getRequestParameter("hitsperpage");
				if (hitsperpage == null)
				{
					hitsperpage = inPageRequest.getPageProperty("hitsperpage");
				}
				if (hitsperpage == null)
				{
					if (usersettings != null)
					{
						inQuery.setHitsPerPage(usersettings.getHitsPerPageForSearchType(inQuery.getResultType()));
					}
					else if (oldtracker != null)
					{
						inQuery.setHitsPerPage(oldtracker.getHitsPerPage());
					}
				}
				else
				{
					inQuery.setHitsPerPage(Integer.parseInt(hitsperpage));
				}
				inQuery = getSearchQueryFilter().attachFilter(inPageRequest, this, inQuery);
				tracker = search(inQuery); //search here <----
				tracker.setSearchQuery(inQuery);
				if (oldtracker != null && oldtracker.hasSelections())
				{
					tracker.loadPreviousSelections(oldtracker);
					tracker.setShowOnlySelected(oldtracker.isShowOnlySelected());
				}

				if (isFireEvents() && inQuery.isFireSearchEvent())
				{
					WebEvent event = new WebEvent();
					event.setSource(this);
					event.setOperation("search");
					event.setSearchType(getSearchType());
					event.setUser(inPageRequest.getUser());
					event.setCatalogId(getCatalogId());
					event.addDetail("query", inQuery.toFriendly());
					event.addDetail("detailed", inQuery.toQuery());
					event.addDetail("hits", String.valueOf(tracker.getTotal()));
					event.addDetail("sort", inQuery.getSortBy());
					fireSearchEvent(event);
				}
			}
			catch (Throwable ex)
			{
				String fullq = inQuery.toQuery();
				inPageRequest.putPageValue("error", "Invalid search input. " + URLUtilities.xmlEscape(fullq));
				log.error(ex + " on " + fullq);
				ex.printStackTrace();
				inQuery.setProperty("error", "Invalid search " + URLUtilities.xmlEscape(fullq));
				//					if( ex instanceof OpenEditException)
				//					{
				//						throw (OpenEditException)ex;
				//					}
				//					else
				//					{
				//						throw new OpenEditException(ex);
				//					}	
			}
		}
		if (tracker != null)
		{
			if (!runsearch)
			{
				String hitsperpage = inPageRequest.getRequestParameter("hitsperpage");
				if (hitsperpage != null)
				{
					tracker.setHitsPerPage(Integer.parseInt(hitsperpage));
				}
			}
			String pagenumber = inPageRequest.getRequestParameter("page");
			if (pagenumber != null)
			{
				if ("next".equals(pagenumber))
				{
					int page = tracker.getPage();
					page++;
					tracker.setPage(page);
				}
				else if ("previous".equals(pagenumber))
				{
					int page = tracker.getPage();
					page--;
					tracker.setPage(page);
				}
				else
				{
					tracker.setPage(Integer.parseInt(pagenumber));
				}
			}
			else if (oldtracker != null && oldtracker.getQuery().equals(inQuery))
			{
				if( tracker.size() > oldtracker.getPage() * tracker.getHitsPerPage() )
				{
					//Make sure it has not changed
					tracker.setPage(oldtracker.getPage());
				}	
			}
			
			if (tracker.getHitsName() == null)
			{
				String hitsname = inPageRequest.findValue("hitsname");
				if (hitsname != null)
				{
					tracker.setHitsName(hitsname);
				}
				else
				{
					tracker.setHitsName("hits");
				}
			}
			//We only want to reload this search if we are on the first page
			//This is because sometimes we are just clicking the next page link
			if (!runsearch && tracker.getPage() == 1)
			{
				tracker.refresh();
			}
			inPageRequest.putPageValue(tracker.getHitsName(), tracker);
			inPageRequest.putSessionValue(tracker.getSessionId(), tracker);
		}

		return tracker;
	}

	public boolean hasChanged(HitTracker inTracker)
	{
		String id1 = getIndexId();
		String id2 = inTracker.getIndexId();

		if (id1 == null || id1.equals(id2))
		{

			return false;
		}
		return true;
	}

	/**
	 * @deprecated Use loadHits which checks hitssessionid
	 * 
	 * @see org.openedit.data.Searcher#loadHits(org.openedit.WebPageRequest,
	 *      java.lang.String)
	 */
	public HitTracker loadHits(WebPageRequest inReq, String hitsname) throws OpenEditException
	{
		HitTracker otracker = (HitTracker) inReq.getSessionValue(hitsname + getCatalogId());

		if (otracker != null)
		{
			inReq.putPageValue(hitsname, otracker);
		}
		return otracker;
	}

	public HitTracker loadHits(WebPageRequest inReq)
	{
		String type = inReq.findValue("searchtype");
		String id = null;
		if( type != null )
		{
			id = inReq.findValue( type + "hitssessionid");
		}
		if( id == null)
		{
			id = inReq.findValue("hitssessionid");
		}	
		if (id != null)
		{
			HitTracker tracker = (HitTracker) inReq.getSessionValue(id);
			tracker = checkCurrent(inReq, tracker);
			if (tracker != null)
			{
				//TODO: Remove this code, is not a good policy
				String hitsname = inReq.findValue("hitsname");
				if (hitsname == null)
				{
					hitsname = tracker.getHitsName();
				}
				inReq.putPageValue(hitsname, tracker);
			}
			return tracker;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openedit.data.Searcher#getDefaultDateFormat()
	 */
	public DateFormat getDefaultDateFormat()
	{
		if (fieldDefaultDateFormat == null)
		{
			fieldDefaultDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		}
		return fieldDefaultDateFormat;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openedit.data.Searcher#setDefaultDateFormat(java.text.DateFormat)
	 */
	public void setDefaultDateFormat(DateFormat inDefaultDateFormat)
	{
		fieldDefaultDateFormat = inDefaultDateFormat;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openedit.data.Searcher#fieldSearch(org.openedit.WebPageRequest)
	 */
	public HitTracker fieldSearch(WebPageRequest inReq) throws OpenEditException
	{

		SearchQuery search = addStandardSearchTerms(inReq);

		if (search == null)
		{
			return null; // Noop
		}
		String sort = inReq.getRequestParameter("sortby");
		if (sort == null)
		{
			sort = inReq.findValue("sortby");
		}
		if (sort != null)
		{
			search.setSortBy(sort);
		}

		HitTracker hits = cachedSearch(inReq, search);
		return hits;
	}

	public HitTracker fieldSearch(String attr, String value, WebPageRequest inContext)
	{
		return fieldSearch(attr, value, null, inContext);
	}

	public HitTracker fieldSearch(String attr, String value, String orderby, WebPageRequest inContext)
	{
		SearchQuery query = createSearchQuery();

		if (attr != null && value != null)
		{
			PropertyDetail detail = new PropertyDetail();
			detail.setCatalogId(getCatalogId());
			detail.setId(attr);
			query.addExact(detail, value); //this is addMatches and not addExact so that we can handle wildcards
		}
		if (orderby != null)
		{
			query.setSortBy(orderby);
		}

		return cachedSearch(inContext, query);
	}

	public HitTracker fieldSearch(String attr, String value)
	{
		return fieldSearch(attr, value, (String) null);
	}

	public HitTracker fieldSearch(String attr, String value, String orderby)
	{
		SearchQuery query = createSearchQuery();

		query.addExact(attr, value); //this is addMatches and not addExact so that we can handle wildcards
		
		if (orderby != null)
		{
			query.setSortBy(orderby);
		}

		return search(query);
	}

	public HitTracker search(WebPageRequest inContext, String inQuery)
	{
		return search(inContext, parse(inQuery));
	}

	public SearchQuery parse(String inQuery)
	{
		String[] fields = inQuery.split(" ");
		SearchQuery query = createSearchQuery();
		for (int i = 0; i < fields.length; i++)
		{
			String[] parts = fields[i].split(":");
			if (parts.length != 2)
			{
				continue;
			}
			PropertyDetail detail = new PropertyDetail();
			detail.setCatalogId(getCatalogId());
			detail.setId(parts[0]);
			query.addMatches(detail, parts[1]);
		}
		return query;
	}

	public HitTracker search(WebPageRequest inPageRequest, SearchQuery search) throws OpenEditException
	{
		String defaultjoin = inPageRequest.getRequestParameter("defaultjoin");
		if (defaultjoin != null)
		{
			boolean andall = Boolean.parseBoolean(defaultjoin);
			search.setAndTogether(andall);
			search.setProperty("defaultjoin", defaultjoin);
		}
		String sort = inPageRequest.getRequestParameter("sortby");
		if (sort != null)
		{
			search.setSortBy(sort);
		}

		return cachedSearch(inPageRequest, search);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openedit.data.Searcher#addStandardSearchTerms(org.openedit.
	 * WebPageRequest )
	 */
	public SearchQuery addStandardSearchTerms(WebPageRequest inPageRequest) throws OpenEditException
	{
		String type = inPageRequest.getRequestParameter("searchtype");
		if( type != null && !type.equals(getSearchType()) )
		{
			return null;
		}
		
		SearchQuery search = addFields(inPageRequest);
		search = addOrGroups(search, inPageRequest);
		
		if (search == null)
		{
			return null;
		}
		addShowOnly(inPageRequest, search);
		
		if (search.getSortBy() == null)
		{
			String sort = inPageRequest.findValue(type + "sortby");
			search.setSortBy(sort);
		}

		
		String resultype = inPageRequest.getRequestParameter("resulttype");
		if (resultype == null)
		{
			resultype = getSearchType();
		}
		search.setResultType(resultype);

		String fireevent = inPageRequest.findValue("fireevent");
		search.setFireSearchEvent(Boolean.parseBoolean(fireevent));
		return search;
	}

	//	public void addFacets(WebPageRequest inReq, SearchQuery inSearch)
	//	{
	//
	//		//Grab from userprofile.
	//		UserProfile profile = inReq.getUserProfile();
	//		List details = getPropertyDetails().getDetailsByProperty("filter", "true");
	//		
	//		//assettype, color, 
	//		
	//		for (Iterator iterator = details.iterator(); iterator.hasNext();)
	//		{
	//			PropertyDetail detail = (PropertyDetail) iterator.next();
	//			HitTracker selected = profile.getFacetsForType(detail.getId());
	//			
	//			//Collection filters = inReq.getUserProfile().getValues(detail.getId() + "selectedfacets");
	//			
	//		}
	//		
	//
	//		
	//		
	//		
	//		// TODO Auto-generated method stub
	//		
	//	}

	protected SearchQuery addOrGroups(SearchQuery inSearch, WebPageRequest inPageRequest)
	{
		String[] orgroups = inPageRequest.getRequestParameters("orgroup");

		if (orgroups == null)
		{
			return inSearch;
		}
		if (inSearch == null)
		{
			inSearch = createSearchQuery();
		}

		for (int i = 0; i < orgroups.length; i++)
		{
			String[] vals = inPageRequest.getRequestParameters(orgroups[i] + ".value");
			if (vals != null)
			{
				StringBuffer buffer = new StringBuffer();
				for (int j = 0; j < vals.length; j++)
				{
					buffer.append(vals[j]);
					if (j < vals.length)
					{
						buffer.append(' ');
					}
				}
				inSearch.addOrsGroup(orgroups[i], buffer.toString());
			}
		}
		return inSearch;
	}

	protected SearchQuery addFields(WebPageRequest inPageRequest) throws OpenEditException
	{
		String[] fieldid = inPageRequest.getRequestParameters("field");

		if (fieldid == null)
		{
			return null;
		}
		SearchQuery search = createSearchQuery();
		String and = inPageRequest.getRequestParameter("querytype");
		if (and != null)
		{
			if ("or".equals(and))
			{
				search.setAndTogether(false);
			}
		}

		String[] operations = inPageRequest.getRequestParameters("operation");
		if (operations == null)
		{
			return null;
		}
		inPageRequest.removeSessionValue("crumb");

		DateFormat formater = null;
		String dateFormat = inPageRequest.getRequestParameter("dateformat");
		if (dateFormat != null)
		{
			formater = new SimpleDateFormat(dateFormat);
		}
		else
		{
			formater = getDefaultDateFormat(); // dateFormat = "MM/dd/yyyy";
		}

		Map valuecounter = new HashMap(fieldid.length);
		for (int i = 0; i < fieldid.length; i++)
		{
			//String id = termids[i];

			String field = fieldid[i];
			PropertyDetail detail = getDetail(field, inPageRequest);

			if (detail == null)
			{
				continue;
			}
			//the values are a counter. So each time we get a duplicate we inc the counter of values

			Integer count = (Integer) valuecounter.get(detail.getId());
			if (count == null)
			{
				count = new Integer(0);
			}
			else
			{
				count = new Integer(count.intValue() + 1);
			}
			valuecounter.put(detail.getId(), count);

			String[] vals = inPageRequest.getRequestParameters(detail.getId() + ".value");
			String val = null;
			if (vals != null && vals.length == 1 && vals[0].length() == 0)
			{
				vals = null;
			}

			if (vals != null && vals.length > count.intValue())
			{
				val = vals[count.intValue()]; //We should not get array out of bounds
			}
			if (val == null || val.equals(""))
			{
				val = inPageRequest.getRequestParameter(field + ".value");
			}
			if (val != null && val.contains("|"))
			{
				vals = MultiValued.VALUEDELMITER.split(val);
				val = null;
			}
			else if (val == null)
			{
				vals = inPageRequest.getRequestParameters(detail.getId() + ".values");
				if (vals != null && vals.length == 1 && vals[0].length() == 0)
				{
					vals = null;
				}
			}
			if ("".equals(val))
			{
				val = null;
			}

			if (operations.length <= i)
			{
				log.info("Cant search without operations");
				return null;
			}
			String op = operations[i];
			if (!detail.isMultiLanguage())
			{
				Term t = addTerm(search, detail, val, vals, op);
				if (t == null)
				{
					t = addDate(inPageRequest, search, formater, detail, val, op, count.intValue());
					if (t == null)
					{
						t = addNumber(inPageRequest, search, detail, val, op);
						if (t == null)
						{
							t = addPosition(inPageRequest, search, detail, val, op);
							if (t == null)
							{
								//This is for lobpicker and primaryproductpicker and maybe other ones
								addPicker(inPageRequest, search, detail, val, op, count.intValue());
							}
						}
					}
				}
			}
			else
			{
				loadLanguageValues(inPageRequest, search, detail, val, op);
			}
		}
		return search;
	}

	protected void loadLanguageValues(WebPageRequest inPageRequest, SearchQuery search, PropertyDetail detail, String val, String op)
	{
		//Could still be some more values we need to add....
		String[] language = inPageRequest.getRequestParameters(detail.getId() + ".language");
		if (language != null)
		{
			LanguageMap map = new LanguageMap();
			search.setValue(detail.getId(), map);
			
			for (int j = 0; j < language.length; j++)
			{
				String lang = language[j];
				String lid = detail.getId() + "_int." + lang;
				String fieldid = detail.getId() + "." + lang;
				String langval = inPageRequest.getRequestParameter(fieldid + ".value");
				if( langval == null)
				{
					langval = inPageRequest.getRequestParameter(detail.getId() + ".value");
				}
				if ("en".equals(lang) && langval == null)
				{
					langval = val;
				}
				if (langval != null)
				{
					if ("matches".equals(op) || "andgroup".equals(op))
					{
						search.addMatches(lid, langval);
					}
					else if ("exact".equals(op))
					{
						search.addExact(lid, langval);
					}
					else if ("startswith".equals(op))
					{
						search.addStartsWith(lid, langval);
					}
					else if ("contains".equals(op))
					{
						search.addContains(lid, langval);
					}
					else if ("not".equals(op))
					{
						search.addNot(lid, langval);
					}
					else if ("orsgroup".equals(op))
					{
						search.addOrsGroup(lid, langval);
					}
//					search.setProperty(detail.getId(), langval);
//					search.setProperty(detail.getId() + ".language", lang);
					map.setText(lang, langval);
				}
			}
		}
		else
		{	//Why do we have similar code here?
			if (val != null)
			{
				HitTracker languages = getSearcherManager().getList(getCatalogId(), "locale");
				SearchQuery child = createSearchQuery();
				child.setAndTogether(false);
				for (Iterator iterator = languages.iterator(); iterator.hasNext();)
				{
					Data locale = (Data) iterator.next();
					String lang = locale.getId();

					String lid = detail.getId() + "_int." + lang;
					if ("matches".equals(op) || "andgroup".equals(op))
					{
						child.addMatches(lid, val);
					}
					else if ("exact".equals(op))
					{
						child.addExact(lid, val);
					}
					else if ("startswith".equals(op))
					{
						child.addStartsWith(lid, val);
					}
					else if ("contains".equals(op))
					{
						child.addContains(lid, val);
					}
					else if ("not".equals(op))
					{
						child.addNot(lid, val);
					}
					else if ("orsgroup".equals(op))
					{
						child.addOrsGroup(lid, val);
					}
					child.setProperty(lid, val);
				}
				search.addChildQuery(child);
			}
		}
	}

	public void addShowOnly(WebPageRequest inPageRequest, SearchQuery search)
	{
		if (inPageRequest == null)
		{
			return;
		}
		String querystring = inPageRequest.findValue(getSearchType() + "showonly");
		if (querystring == null)
		{
			//Legacy check. Remove this line after Feb 15 2013
			querystring = inPageRequest.findValue("showonly");
		}
		if (querystring != null)
		{
			//Shoud we run a replace on this filter? So that user and groups roles can be put in here?
			//regionid:	${user.regionid}
			if (querystring.contains("$"))
			{
				//	String result = getReplacer().replace(format, tmp);
			}
			addShowOnlyFilter(inPageRequest, querystring, search);
		}
	}

	public void addShowOnlyFilter(WebPageRequest inPageRequest, String querystring, SearchQuery search)
	{

		if (querystring != null)
		{
			SearchQuery child = search.getChildQuery(querystring);
			if (child == null)
			{
				child = createSearchQuery(querystring, inPageRequest);
				child.setFilter(true);
				child.setId(querystring); //unique
				//make sure we dont have a conflict of fields? i.e. already searching by a certain term
				for (int i = 0; i < search.getTerms().size(); i++)
				{
					Term existingterm = (Term) search.getTerms().get(i);
					int c = 0;
					while (true)
					{
						Term childterm = child.getTermByDetailId(existingterm.getDetail().getId());
						if (childterm != null)
						{
							child.removeTerm(childterm);
						}
						else
						{
							break;
						}
						c++;
						if (c > 100)
						{
							log.error("infinite loop should never happen");
							break;
						}
					}
				}
				if (child.getTerms().size() > 0 || child.getChildren().size() > 0)
				{
					search.addChildQuery(child);
				}
			}
		}
	}

	public void addUserProfileSearchFilters(WebPageRequest inReq, SearchQuery search)
	{
		if (inReq.getUserProfile() == null)
		{
			return;
		}
		Collection filters = inReq.getUserProfile().getValues("profilesearchfilters");
		//hideassettype
		//String profileid = inReq.findValue("profilevalues");
		//String field = inReq.findValue("field");
		if (filters == null)
		{
			return;
		}
		for (Iterator iter = filters.iterator(); iter.hasNext();)
		{
			String filter = (String) iter.next();
			Collection values = inReq.getUserProfile().getValues(filter);
			if (values != null)
			{
				String[] terms = new String[values.size()];
				Iterator iterator = values.iterator();
				String term = null;
				String field = filter.substring(4);
				SearchQuery child = search.getChildQuery(filter);
				if (child != null)
				{
					search.getChildren().remove(child);
				}

				if (filter.startsWith("hide"))
				{
					term = "-" + field;
				}
				else if (filter.startsWith("show"))
				{
					term = "+" + field;
				}

				for (int i = 0; i < terms.length; i++)
				{
					terms[i] = term + ":" + iterator.next();
				}

				SearchQuery newchild = createSearchQuery(terms, inReq);
				newchild.setId(filter);
				newchild.setFilter(true);
				search.addChildQuery(newchild);
			}
			else
			{
				SearchQuery child = search.getChildQuery(filter);
				if (child != null)
				{
					search.getChildren().remove(child);
				}
			}
		}
	}

	protected PropertyDetail getDetail(String inTermId, WebPageRequest inReq)
	{

		PropertyDetail detail = null;
		String catalogid = null;
		String view = null;
		String[] splits = inTermId.split(BaseSearcher.delim);

		String propertyid = inTermId; //this is for backward compatability
		if (splits.length > 2)
		{
			catalogid = splits[0];
			view = splits[1];
			propertyid = splits[2];
		}

		if (propertyid == null || propertyid.length() == 0)
		{
			return null;
		}

		String searchtype = inReq.getRequestParameter(propertyid + ".searchtype");
		if (searchtype == null)
		{
			searchtype = getSearchType();
		}
		
		if (catalogid == null)
		{
			catalogid = getCatalogId();
		}
		//		Searcher remoteSearcher = getSearcherManager().getSearcher(catalogid, searchtype);
		//		if (remoteSearcher != null)
		//		{
		//			detail = remoteSearcher.getDetailForView(view, propertyid, inReq.getUser());
		//		}
		//		if (detail == null)
		//		{
		if( searchtype.equals(getSearchType()))
		{
			detail = getPropertyDetailsArchive().getDetail(searchtype, view, propertyid, inReq.getUserProfile());
		}	
		else
		{
			detail = getPropertyDetailsArchive().getPropertyDetailsCached(searchtype).getDetail(propertyid); 
		}
		//		}
		//		if (detail == null)
		//		{
		//			detail = getDetail(propertyid);
		//		}
		if (detail == null)
		{
			// continue;
			// create a virtual one?
			detail = new PropertyDetail();
			detail.setId(propertyid);
			detail.setView(view);
			detail.setCatalogId(catalogid);
		}
		//Needed?
//		if( detail.getSearchType() == null)
//		{
//			detail.setSearchType(searchtype);
//		}

		return detail;
	}

	protected Term addTerm(SearchQuery search, PropertyDetail detail, String val, String op)
	{
		return addTerm(search, detail, val, null, op);
	}

	protected Term addTerm(SearchQuery search, PropertyDetail detail, String val, String[] vals, String op)
	{
		Term t = null;
		if (detail.isDataType("number") || detail.isDataType("double") || detail.isDataType("float") || detail.isDataType("geo_point"))
		{
			//this is handled in else statement
			return null;
		}
		if ((val != null && val.length() > 0 && (vals == null || vals.length <2)))
		{
			if ("matches".equals(op) || "andgroup".equals(op))
			{
				t = search.addMatches(detail, val);
			}
			else if ("exact".equals(op))
			{
				t = search.addExact(detail, val);
			}
			else if ("startswith".equals(op))
			{
				t = search.addStartsWith(detail, val);
			}
			else if ("contains".equals(op))
			{
				t = search.addContains(detail, val);
			}
			else if ("not".equals(op))
			{
				t = search.addNot(detail, val);
			}
			else if ("orsgroup".equals(op))
			{
				t = search.addOrsGroup(detail, val);
			}
			else if ("freeform".equals(op))
			{
				t = search.addFreeFormQuery(detail, val);
			}

			if (t != null)
			{
				search.setProperty(t.getId(), val);
			}
		}
		else if (vals != null)
		{
			if ("andgroup".equals(op))
			{
				t = search.addAndGroup(detail, vals);
			}
			else
			{
				t = search.addOrsGroup(detail, vals);
			}
			search.setPropertyValues(t.getId(), vals);
		}
		if (t != null)
		{
			t.addParameter("op", op);
		}
		return t;
	}

	private String createOrValue(String[] ors)
	{
		String val = null;
		if (ors.length > 1)
		{
			StringBuffer orString = new StringBuffer();
			orString.append("(");
			for (int j = 0; j < ors.length; j++)
			{
				orString.append(ors[j]);
				if (j < ors.length - 1)
				{
					orString.append(" OR ");
				}
			}
			orString.append(")");
			val = orString.toString();
		}
		else if (ors.length == 1)
		{
			val = ors[0];
		}
		return val;
	}

	protected Term addPicker(WebPageRequest inReq, SearchQuery inQuery, PropertyDetail inDetail, String inVal, String inOp, int inIndex)
	{
		Term t = null;
		if ("is".equals(inOp))
		{
			t = inQuery.addMatches(inDetail, inVal);
			inQuery.setProperty(t.getId(), inVal);
			String tmp = inReq.getRequestParameters(inDetail.getId() + ".additionals")[inIndex];
			String[] additionals = tmp.split(",");
			for (int i = 0; i < additionals.length; i++)
			{
				String paramid = inDetail.getId() + "." + additionals[i];
				String inputid = t.getId() + "." + additionals[i];
				String val = inReq.getRequestParameters(paramid)[inIndex];
				inQuery.setProperty(inputid, val);
			}
		}
		return t;
	}

	protected Term addNumber(WebPageRequest inPageRequest, SearchQuery search, PropertyDetail field, String val, String op)
	{
		if (!(field.isDataType("number") || field.isDataType("double") || field.isDataType("float")))
		{
			return null;
		}

		Term t = null;
		if (val != null && val.length() > 0)
		{

			if ("greaterthannumber".equals(op))
			{
				t = search.addGreaterThan(field, Long.parseLong(val));
			}
			else if ("lessthannumber".equals(op))
			{
				t = search.addLessThan(field, Long.parseLong(val));
			}

			if ("equaltonumber".equals(op) || "matches".equals(op) || "startswith".equals(op))
			{
				if (field.isDataType("number") || field.isDataType("long"))
				{
					t = search.addExact(field, Long.parseLong(val));
				}
				if (field.isDataType("double"))
				{
					t = search.addExact(field, Double.parseDouble(val));
				}
			}
			else if ("betweennumbers".equals(op))
			{
				String highval = null;
				String lowval = null;
				//see if we have a range
				if (!val.contains("-"))
				{
					t = search.addExact(field, Long.parseLong(val));
				}
				else
				{
					String[] range = val.split("-");
					if (range[0].length() > 0)
					{
						lowval = range[0];
					}
					if (range.length > 1 && range[1].length() > 0)
					{
						highval = range[1];
					}
					t = addNumberRange(search, field, t, highval, lowval);
				}
			}
			if (t != null)
			{
				search.setProperty(t.getId(), val);
				t.addParameter("op", op);
			}
		}
		else if ("betweennumbers".equals(op))
		{
			String highval = inPageRequest.getRequestParameter(field.getId() + ".highval");
			String lowval = inPageRequest.getRequestParameter(field.getId() + ".lowval");
			t = addNumberRange(search, field, t, highval, lowval);
		}

		return t;
	}

	protected Term addNumberRange(SearchQuery search, PropertyDetail field, Term t, String highval, String lowval)
	{
		if (highval != null && lowval == null)
		{
			t = search.addLessThan(field, Long.parseLong(highval));
		}
		else if (highval == null && lowval != null)
		{
			t = search.addGreaterThan(field, Long.parseLong(lowval));
		}
		else if (highval != null && lowval != null)
		{
			t = search.addBetween(field, Long.parseLong(lowval), Long.parseLong(highval));
		}
		return t;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openedit.data.Searcher#deselect(java.lang.String,
	 * java.lang.String[])
	 */
	public List deselect(String inField, String[] toremove) throws OpenEditException
	{
		HitTracker options = getSearcherManager().getList(getPropertyDetailsArchive().getCatalogId(), inField);
		List list = options.keys();
		if (toremove != null)
		{
			for (int i = 0; i < toremove.length; i++)
			{
				String name = toremove[i];
				list.remove(name);
			}
		}
		return list;
	}

	private Term addSelect(WebPageRequest inPageRequest, SearchQuery search, PropertyDetail field, String op) throws OpenEditException
	{
		Term t = null;
		if ("multiselect".equals(op))
		{
			String param = field.getId() + ".value";

			String[] select = inPageRequest.getRequestParameters(param);
			if (select != null)
			{
				List remaining = deselect(field.getId(), select);
				for (int j = 0; j < remaining.size(); j++)
				{
					String val = (String) remaining.get(j);
					t = search.addNot(field, val);
					search.setProperty(t.getId(), val);
				}
				// search.addCategoryFilter(remaining, friendly);
			}
		}
		else if ("picker".equals(op))
		{
			String param = field.getId() + ".value";

			String[] select = inPageRequest.getRequestParameters(param);
			if (select != null)
			{
				SearchQuery or = createSearchQuery();
				or.setAndTogether(false);
				for (int j = 0; j < select.length; j++)
				{
					t = or.addMatches(field, select[j]);
					search.setProperty(t.getId(), select[j]);
				}
				search.addQuery(field, or.toQuery());
				search.setPropertyValues("picker." + param, select);
			}
		}
		return t;
	}

	protected Term addDate(WebPageRequest inPageRequest, SearchQuery search, DateFormat formater, PropertyDetail field, String val, String op, int count) throws OpenEditException
	{
		if (!field.isDate())
		{
			return null;
		}
		Term t = null;
		try
		{
			Date d = null;
			if (op.startsWith("before"))
			{
				if (op.length() > "before".length())
				{
					d = new Date();
					int len = Integer.parseInt(op.substring("before".length()));
					GregorianCalendar cal = new GregorianCalendar();
					cal.setTime(d);
					cal.add(GregorianCalendar.DAY_OF_MONTH, 0 - len); // subtract
					t = search.addBetween(field, cal.getTime(), d);
					t.setOperation("betweendates");
				}
				else if (val != null && !"".equals(val))
				{
					d = formater.parse(val);
					t = search.addBefore(field, d);
				}
				if (t != null)
				{
					search.setProperty(t.getId(), val);
					search.setProperty("datedirection" + field, op);
				}
			}
			else if (op.startsWith("after"))
			{
				if (op.length() > "after".length())
				{
					d = new Date();
					int len = Integer.parseInt(op.substring("after".length()));
					GregorianCalendar cal = new GregorianCalendar();
					cal.setTime(d);
					cal.add(GregorianCalendar.DAY_OF_MONTH, len); // subtract
					t = search.addBetween(field, d, cal.getTime());
					t.setOperation(op);
				}
				else if (val != null && !"".equals(val))
				{
					d = formater.parse(val);
					t = search.addAfter(field, d);
				}
				if (t != null)
				{
					search.setProperty("datedirection" + field, op);
					search.setProperty(t.getId(), val);
				}
			}
			else if ("equals".equals(op) && val != null && !"".equals(val))
			{
				d = formater.parse(val);

				Calendar c = new GregorianCalendar();

				c.setTime(d);
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.MILLISECOND, 0);
				c.set(Calendar.MINUTE, 0);

				Calendar c2 = new GregorianCalendar();
				c2.setTime(c.getTime());
				c2.add(Calendar.DAY_OF_YEAR, 1);

				t = search.addBetween(field, c.getTime(), c2.getTime());
				search.setProperty(t.getId(), val);
			}
			else if ("betweendates".equals(op))
			{
				String[] beforeStrings = inPageRequest.getRequestParameters(field.getId() + ".before");
				String[] afterStrings = inPageRequest.getRequestParameters(field.getId() + ".after");

				String beforeString = null, afterString = null;
				if (beforeStrings != null && beforeStrings.length > count)
				{
					beforeString = beforeStrings[count];
					afterString = afterStrings[count]; //We should not get array out of bounds
				}
				if (beforeString == null)
				{
					beforeString = inPageRequest.getRequestParameter(field.getId() + ".before");
					afterString = inPageRequest.getRequestParameter(field.getId() + ".after");
				}
				if (beforeString != null && beforeString.length() == 0)
				{
					beforeString = null;
				}
				if (afterString != null && afterString.length() == 0)
				{
					afterString = null;
				}
				if (beforeString == null && afterString == null)
				{
					//?
				}
				else if (beforeString == null)
				{
					Date after = formater.parse(afterString);
					t = search.addAfter(field, after);
					search.setProperty(t.getId() + ".after", beforeString);
				}
				else if (afterString == null)
				{
					Date before = formater.parse(beforeString);
					t = search.addBefore(field, before);
					search.setProperty(t.getId() + ".before", beforeString);
				}
				else
				{
					Date before = formater.parse(beforeString);
					Date after = formater.parse(afterString);
					
					Calendar c = new GregorianCalendar();

					c.setTime(before);
					 c.set(Calendar.HOUR_OF_DAY, 23);
					    c.set(Calendar.MINUTE, 59);
					    c.set(Calendar.SECOND, 59);
					    c.set(Calendar.MILLISECOND, 999);
					before = c.getTime();
					t = search.addBetween(field, after, before);
					search.setProperty(t.getId() + ".before", beforeString);
					search.setProperty(t.getId() + ".after", afterString);
				}
			}
			else if ("betweenages".equals(op))
			{
				String beforeString = inPageRequest.getRequestParameter(field.getId() + ".before");
				String afterString = inPageRequest.getRequestParameter(field.getId() + ".after");

				if (beforeString == null && afterString == null)
				{

				}
				else if (beforeString == null)
				{
					Calendar rightNow = Calendar.getInstance();
					int before = Integer.parseInt(afterString);
					rightNow.add(Calendar.YEAR, (-1 * before));
					Date after = rightNow.getTime();
					t = search.addBefore(field, after);
					search.setProperty(t.getId() + ".after", afterString);

				}
				else if (afterString == null)
				{
					Calendar rightNow = Calendar.getInstance();
					int after = Integer.parseInt(beforeString);
					rightNow.add(Calendar.YEAR, (-1 * after));
					Date before = rightNow.getTime();
					t = search.addAfter(field, before);
					search.setProperty(t.getId() + ".before", beforeString);
				}
				else
				{
					// before
					Calendar rightNow = Calendar.getInstance();
					int a = Integer.parseInt(afterString);
					rightNow.add(Calendar.YEAR, (-1 * a));
					Date before = rightNow.getTime();
					// after
					rightNow = Calendar.getInstance();
					int b = Integer.parseInt(beforeString);
					rightNow.add(Calendar.YEAR, (-1 * b));
					Date after = rightNow.getTime();
					// add search term
					t = search.addBetween(field, after, before);
					search.setProperty(t.getId() + ".after", afterString);
					search.setProperty(t.getId() + ".before", beforeString);
				}
			}
		}
		catch (ParseException ex)
		{
			throw new OpenEditException(ex);
		}
		if (t != null)
		{
			t.addParameter("op", op); //TODO make these match with standard operations?
		}
		return t;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openedit.data.Searcher#addActionFilters(org.openedit.WebPageRequest,
	 * org.openedit.hittracker.SearchQuery)
	 */

	public SearchQuery addActionFilters(WebPageRequest inReq, SearchQuery search)
	{
		Configuration config = inReq.getCurrentAction().getConfig();
		Configuration matchesConfig = config.getChild("matches");
		if (matchesConfig != null)
		{
			for (Iterator iterator = matchesConfig.getChildren().iterator(); iterator.hasNext();)
			{
				Configuration child = (Configuration) iterator.next();
				String fieldName = child.getName();
				String value = child.getValue();
				PropertyDetail detail = new PropertyDetail();
				detail.setCatalogId(getCatalogId());
				detail.setId(fieldName);
				search.addMatches(detail, value);
				search.setProperty(fieldName, value); // The last one wins, only
														// for matches
			}
		}
		Configuration notConfig = config.getChild("not");
		if (notConfig != null)
		{
			for (Iterator iterator = notConfig.getChildren().iterator(); iterator.hasNext();)
			{
				Configuration child = (Configuration) iterator.next();
				String fieldName = child.getName();
				String value = child.getValue();
				PropertyDetail detail = new PropertyDetail();
				detail.setCatalogId(getCatalogId());
				detail.setId(fieldName);

				search.addNot(detail, value);
			}
		}
		Configuration sortConfig = config.getChild("sortby");
		if (sortConfig != null)
		{
			search.setSortBy(sortConfig.getValue());
		}
		return search;
	}

	public HitTracker loadPageOfSearch(WebPageRequest inPageRequest) throws OpenEditException
	{
		HitTracker tracker = loadHits(inPageRequest);
		UserProfile usersettings = (UserProfile) inPageRequest.getUserProfile();

		if (tracker == null)
		{
			return null;
		}
		Searcher searcher = tracker.getSearcher();
		// This is where we handle changing the number of hits per page
		String hitsperpage = inPageRequest.getRequestParameter("hitsperpage");
		if (hitsperpage == null)
		{

			hitsperpage = inPageRequest.getPageProperty("hitsperpage");

		}
		if (tracker != null)
		{
			if (hitsperpage != null)
			{
				int numhitsperpage = Integer.parseInt(hitsperpage);
				tracker.setHitsPerPage(numhitsperpage);
			} /*
				 * else if (usersettings != null) {
				 * tracker.setHitsPerPage(usersettings.
				 * getHitsPerPageForSearchType(searcher.getSearchType()));
				 * 
				 * }
				 */
		}

		String page = inPageRequest.getRequestParameter("page");
		int totalPages = tracker.getTotalPages();
		if (page != null)
		{
			int jumpToPage = Integer.parseInt(page);
			if (jumpToPage <= totalPages && jumpToPage > 0)
			{
				tracker.setPage(jumpToPage);
			}
			else
			{
				tracker.setPage(1);
			}
			return tracker;
		}

		String nav = inPageRequest.getRequestParameter("nav");
		if (nav != null)
		{
			if ("next".equals(nav))
			{
				int jumpToPage = tracker.getPage();
				jumpToPage++;
				if (jumpToPage <= totalPages && jumpToPage > 0)
				{
					tracker.setPage(jumpToPage);
				}
			}
			else if ("previous".equals(nav))
			{
				int jumpToPage = tracker.getPage();
				jumpToPage--;
				if (jumpToPage <= totalPages && jumpToPage > 0)
				{
					tracker.setPage(jumpToPage);
				}
			}
		}
		return tracker;
	}

	/**
	 * @deprecated use changeSort which checks hitssessionid
	 * @param inReq
	 * @param sort
	 * @param hitsname
	 * @throws OpenEditException
	 */
	protected void changeSort(WebPageRequest inReq, String sort, String hitsname) throws OpenEditException
	{
		HitTracker hits = loadHits(inReq, hitsname);

		if (hits != null)
		{
			SearchQuery group = hits.getSearchQuery();
			group.setSortBy(sort);
			hits.setIndexId(hits.getIndexId() + sort); // Causes the hits to be
														// reloaded
														// inReq.removeSessionValue("hits");
			cachedSearch(inReq, group);
		}
	}

	public void changeSort(WebPageRequest inReq) throws OpenEditException
	{
		String sort = inReq.getRequestParameter("sortby");
		HitTracker hits = loadHits(inReq);

		if (hits != null && sort != null)
		{
			SearchQuery group = hits.getSearchQuery();
			String sortlanguage = inReq.getRequestParameter("sortlang");
			if (sortlanguage != null)
			{
				group.setSortLanguage(sortlanguage);
			}
			else
			{
				group.setSortLanguage(inReq.getLanguage());

			}

			if (!sort.equals(group.getSortBy()))
			{
				group.setSortBy(sort);
				hits.setIndexId(hits.getIndexId() + sort); // Causes the hits to be	rerun													// reloaded
				cachedSearch(inReq, group);
				UserProfile pref = (UserProfile) inReq.getUserProfile();
				if (pref != null)
				{
					pref.setSortForSearchType(hits.getSearchQuery().getResultType(), sort);
				}
			}
		}
	}

	public void updateFilters(WebPageRequest inReq) throws OpenEditException
	{
		HitTracker hits = loadHits(inReq);

		if (hits != null)
		{
			String toadd = inReq.getRequestParameter("filtertype");
			SearchQuery query = hits.getSearchQuery();
			if (toadd != null)
			{
				String toaddvalue = inReq.getRequestParameter("filtervalue");
				String toaddlabel = inReq.getRequestParameter("filterlabel");
				query.addFilter(toadd, toaddvalue, toaddlabel);
			}
			else
			{
				String toremove = inReq.getRequestParameter("removefilter");
				String asterisk = "*";
				if (toremove.equals(asterisk))
				{
					query.clearFilters();
				}
				else
				{
					query.removeFilter(toremove);
				}

			}
			hits.invalidate(); // Causes the hits to
			// be // reloaded
			cachedSearch(inReq, query);
		}

	}

	protected SearchQuery createSearchQuery(String inQueryString, WebPageRequest inPageRequest)
	{
		String[] array = inQueryString.split(";");
		return createSearchQuery(array, inPageRequest);
	}

	protected SearchQuery createSearchQuery(String[] inQueryStrings, WebPageRequest inReq)
	{
		SearchQuery query = createSearchQuery();

		for (int i = 0; i < inQueryStrings.length; i++)
		{
			String querystring = inQueryStrings[i];
			if (!"none".equals(querystring))
			{
				String[] parts = querystring.split(":");
				if (parts.length >= 2)
				{
					if (parts[0].startsWith("-"))
					{
						PropertyDetail detail = getDetail(parts[0].substring(1), inReq);
						addTerm(query, detail, parts[1], "not");
					}
					else if (parts[0].startsWith("~"))
					{
						PropertyDetail detail = getDetail(parts[0].substring(1), inReq);
						addTerm(query, detail, parts[1], "matches");
					}
					
					else if (parts[0].startsWith("+"))
					{
						PropertyDetail detail = getDetail(parts[0].substring(1), inReq);
						addTerm(query, detail, parts[1], "exact");
					}
					
					
					else
					{
						PropertyDetail detail = getDetail(parts[0], inReq);
						addTerm(query, detail, parts[1], "exact");
					}
				}
			}
		}

		return query;
	}

	/*
	 * Shows only the hits that have a certain property
	 */
	public void addChildQuery(WebPageRequest inReq) throws OpenEditException
	{
		HitTracker hits = loadHits(inReq);

		if (hits != null)
		{
			SearchQuery group = hits.getSearchQuery();

			String[] querystrings = inReq.getRequestParameters("childquery");
			if (querystrings != null)
			{
				group.getChildren().clear();

				for (int i = 0; i < querystrings.length; i++)
				{
					SearchQuery child = createSearchQuery(querystrings[i], inReq);
					if (child != null && child.getProperties().size() > 0)
					{
						group.addChildQuery(child);
					}
				}

				hits.setIndexId(hits.getIndexId() + "1"); // Causes the hits to be
				// reloaded
			}

			// inReq.removeSessionValue("hits");
			cachedSearch(inReq, group);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openedit.data.Searcher#getPropertyDetailsArchive()
	 */
	public PropertyDetailsArchive getPropertyDetailsArchive()
	{
		if (fieldPropertyDetailsArchive == null)
		{
			fieldPropertyDetailsArchive = (PropertyDetailsArchive) getSearcherManager().getModuleManager().getBean(getCatalogId(), "propertyDetailsArchive");
		}
		if (!fieldPropertyDetailsArchive.getCatalogId().equals(getCatalogId()) && !isAllowRemoteDetails())
		{
			fieldPropertyDetailsArchive = (PropertyDetailsArchive) getSearcherManager().getModuleManager().getBean(getCatalogId(), "propertyDetailsArchive");
			fieldPropertyDetailsArchive.setCatalogId(getCatalogId());
		}
		
		return fieldPropertyDetailsArchive;
	}

	public PropertyDetailsArchive getFieldArchive()
	{
		return getPropertyDetailsArchive();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openedit.data.Searcher#setPropertyDetailsArchive(org.openedit.data
	 * .PropertyDetailsArchive)
	 */
	public void setPropertyDetailsArchive(PropertyDetailsArchive inPropertyDetailsArchive)
	{
		fieldPropertyDetailsArchive = inPropertyDetailsArchive;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openedit.data.Searcher#getSearcherManager()
	 */
	public SearcherManager getSearcherManager()
	{
		return fieldSearcherManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.openedit.data.Searcher#setSearcherManager(org.openedit.data.
	 * SearcherManager)
	 */
	public void setSearcherManager(SearcherManager inSearcherManager)
	{
		fieldSearcherManager = inSearcherManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openedit.data.Searcher#getFieldName()
	 */
	/**
	 * @deprecated Use {@link #getSearchType()} instead
	 */
	public String getFieldName()
	{
		return getSearchType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openedit.data.Searcher#getFieldName()
	 */
	public String getSearchType()
	{
		return fieldSearchType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openedit.data.Searcher#setFieldName(java.lang.String)
	 */
	/**
	 * @deprecated Use {@link #setSearchType(String)} instead
	 */
	public void setFieldName(String inFieldName)
	{
		setSearchType(inFieldName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openedit.data.Searcher#setFieldName(java.lang.String)
	 */
	public void setSearchType(String inSearchType)
	{
		fieldSearchType = inSearchType;
	}

	protected void fireSearchEvent(WebEvent inEvent)
	{
		if (fieldEventManager != null)
		{
			fieldEventManager.fireEvent(inEvent);
		}
	}

	protected EventManager getEventManager()
	{
		return fieldEventManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openedit.data.Searcher#getCatalogId()
	 */
	public String getCatalogId()
	{
		return fieldCatalogId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openedit.data.Searcher#setCatalogId(java.lang.String)
	 */
	public void setCatalogId(String inCatalogId)
	{
		fieldCatalogId = inCatalogId;
	}

	public void setEventManager(EventManager inEventManager)
	{
		fieldEventManager = inEventManager;
	}

	public List getProperties()
	{
		PropertyDetails details = getPropertyDetailsArchive().getPropertyDetailsCached(getSearchType());
		if (details == null)
		{
			return null;
		}
		return details.getDetails();
	}

	public List getIndexProperties()
	{
		PropertyDetails details = getPropertyDetailsArchive().getPropertyDetailsCached(getSearchType());
		if (details == null)
		{
			return null;
		}
		return details.findIndexProperties();
	}

	public List getSearchProperties(User inUser)
	{
		List details = getDetailsForView(getSearchType() + "/" + getSearchType() + "search", inUser);
		if (details == null)
		{
			PropertyDetail det = getDetail("name");
			details = new ArrayList();
			if (det != null)
			{
				details.add(det);
			}
			det = getDetail("id");
			if (det != null)
			{
				details.add(det);
			}
		}
		return details;
	}

	public List getStoredProperties()
	{
		PropertyDetails details = getPropertyDetailsArchive().getPropertyDetailsCached(getSearchType());
		if (details == null)
		{
			return null;
		}
		return details.findStoredProperties();
	}

	public List getKeywordProperties()
	{
		PropertyDetails details = getPropertyDetailsArchive().getPropertyDetailsCached(getSearchType());
		if (details == null)
		{
			return null;
		}
		return details.findKeywordProperties();
	}

	public Data searchByQuery(SearchQuery inQuery)
	{
		inQuery.setHitsPerPage(1);
		HitTracker hits = search(inQuery);
		
		Data data = loadData((Data) hits.first());
		return data;
	}

	public Object searchByField(String inField, String inValue)
	{
		SearchQuery query = createSearchQuery();
		query.setHitsPerPage(1);
		query.addMatches(inField, inValue);
		HitTracker hits = search(query);
		return loadData((Data)hits.first());
	}

	public HitTracker searchByIds(Collection<String> inIds)
	{
		SearchQuery query = createSearchQuery();
		String[] args = inIds.toArray(new String[inIds.size()]);
		PropertyDetail detail = getDetail("id");
		query.addOrsGroup(detail, args);
		HitTracker hits = search(query);
		return hits;
	}

	public Object searchById(String inId)
	{
		if (inId == null || inId.length() == 0)
		{
			return null;
		}
		return searchByField("id", inId);
	}

	public Data createNewData()
	{
		String classname = getNewDataName();
		if (classname == null)
		{
			BaseData data = new BaseData();
			return data;
		}
		return (Data) getModuleManager().getBean(getCatalogId(),getNewDataName(),false);
	}

	/**
	 * @deprecated use getDetailsForView
	 * @param inView
	 * @param inUser
	 * @return
	 * @throws Exception
	 */
	public List getDataProperties(String inView, User inUser) throws Exception
	{
		return getPropertyDetailsArchive().getDataProperties(getSearchType(), inView, inUser);
	}

	public List getDetailsForView(String inView)
	{
		return getDetailsForView(inView, (UserProfile) null);
	}

	//Some problem with Velocity, if the user is null then it does not resolve this method
	/**
	 * @deprecated remove this by April 1 2013
	 */
	public List getDetailsForView(String inView, User inUser)
	{
		List results = getPropertyDetailsArchive().getDataProperties(getSearchType(), inView, inUser);
		return results;
	}

	public List getDetailsForView(String inView, UserProfile inProfile)
	{
		//		List results = getPropertyDetailsArchive().getDataProperties(getSearchType(), inView, inProfile);
		//		return results;
		View view = getPropertyDetailsArchive().getView(getSearchType(), inView, inProfile);
		return view;
	}

	/**
	 * This is old code and should not be used any more
	 */
	public PropertyDetail getDetailForView(String inView, String inFieldName, User inUser)
	{
		return getDetail(inFieldName);
		//		PropertyDetails details = getPropertyDetailsArchive().getPropertyDetailsCached(getSearchType());
		//		if (details == null)
		//		{
		//			return null;
		//		}

		//		getPropertyDetailsArchive().getDetails(details,inView,inUser);
		//
		//		
		//		PropertyDetail detail = null;
		//		if( inView != null )
		//		{
		//			detail = getPropertyDetailsArchive().getDetail(details, inView, inFieldName, inUser);
		//		}
		//		if (detail == null)
		//		{
		//			detail = details.getDetail(inFieldName);
		//		}
		//		return detail;
	}

	public List getDetailsForFields(String[] headers)
	{
		PropertyDetails details = getPropertyDetails();
		List results = new ArrayList(headers.length);
		for (int i = 0; i < headers.length; i++)
		{
			String field = headers[i];
			PropertyDetail detail = details.getDetail(field);
			if (detail != null)
			{
				results.add(detail);
			}

		}
		return results;
	}

	public PropertyDetails getPropertyDetails()
	{
		PropertyDetails details = getPropertyDetailsArchive().getPropertyDetailsCached(getSearchType());
		return details;
	}

	public Collection<PropertyDetail> getUserPropertyDetails()
	{
		PropertyDetails details = getPropertyDetails();
		List<PropertyDetail> sublist = new ArrayList<PropertyDetail>(details.size());
		for (Iterator iterator = details.iterator(); iterator.hasNext();)
		{
			PropertyDetail detail = (PropertyDetail) iterator.next();
			String val = detail.get("internalfield");
			if (val == null || val.equals("false"))
			{
				sublist.add(detail);
			}
		}
		Collections.sort(sublist);
		return sublist;
	}

	public HitTracker getAllHits()
	{
		return getAllHits(null);
	}

	public boolean isFireEvents()
	{
		return fieldFireEvents;
	}

	public void setFireEvents(boolean inFireEvents)
	{
		fieldFireEvents = inFireEvents;
	}

	public PropertyDetail getDetail(String inId)
	{
		PropertyDetails details = getPropertyDetailsArchive().getPropertyDetailsCached(getSearchType());
		if (details == null)
		{
			return null;
		}
		PropertyDetail detail = details.getDetail(inId);
		return detail;
	}

	public HitTracker getAllHits(WebPageRequest inReq)
	{
		SearchQuery q = createSearchQuery();
		q.addMatches("id", "*");
		
		if (inReq != null)
		{
			String sort = inReq.getRequestParameter("sortby");
			if (sort == null)
			{
				sort = inReq.findValue("sortby");
			}
			if (sort != null)
			{
				q.setSortBy(sort);
			}
			return cachedSearch(inReq, q);
		}
		else
		{
			if(getDetail("name") != null )
			{
				q.addSortBy("name");
			}	
			return search(q);
		}

	}

	public void saveCompositeData(CompositeData inData, User inUser)
	{
		for (Iterator iterator = inData.iterator(); iterator.hasNext();)
		{
			Data data = (Data) iterator.next();
			saveData(data, inUser);
		}
	}

	public String lookupValue(String val, WebPageRequest inReq, Data data)
	{
		String value = inReq.getRequestParameter(val);
		if (value != null)
		{
			return value;

		}
		if (data != null)
		{
			value = data.get(val);
		}
		return value;
	}

	/*
	 * Check for a more recent index
	 * 
	 * @see org.openedit.data.Searcher#checkCurrent(org.openedit.hittracker.
	 * HitTracker )
	 */
	public HitTracker checkCurrent(WebPageRequest inReq, HitTracker tracker) throws OpenEditException
	{
		if (tracker != null && tracker.getQuery() != null)
		{
			String forcerun = inReq.getRequestParameter("cache");
			boolean runsearch = false;
			if (forcerun != null && !Boolean.parseBoolean(forcerun))
			{
				tracker.setIndexId(tracker.getIndexId() + "1"); // Causes the hits to be
				runsearch = true;
			}

			String clear = inReq.getRequestParameter(getSearchType() + "clearselection");
			if (clear != null)
			{
				runsearch = true;
			}
			else
			{
				String showonly = inReq.getRequestParameter(getSearchType() + "showonlyselections");
				if (showonly != null)
				{
					runsearch = true;
				}
			}
			//TODO: Check for new sorting

			if (runsearch || hasChanged(tracker))
			{
				int oldNum = tracker.getPage();
				SearchQuery newQuery = tracker.getSearchQuery().copy();
				HitTracker tracker2 = cachedSearch(inReq, newQuery);
				if (tracker2 != null)
				{
					tracker2.setPage(oldNum);
					tracker2.setHitsPerPage(tracker.getHitsPerPage());
				}
				return tracker2;
			}
		}
		return tracker;
	}

	protected void fireDataEditEvent(WebPageRequest inReq, Data object)
	{

		if (fieldEventManager == null)
		{
			return;
		}
		StringBuffer changes = new StringBuffer();
		String[] fields = inReq.getRequestParameters("field");
		if (fields == null)
		{
			return;
		}
		//		if (composite != null)
		//		{
		//			changes.append("MutliEdit->");
		//		}
		for (int i = 0; i < fields.length; i++)
		{
			String field = fields[i];
			String value = inReq.getRequestParameter(field + ".value");
			//			if (composite != null)
			//			{
			//				String compositeVal = composite.get(field);
			//				if (compositeVal == value)
			//				{
			//					continue;
			//				}
			//			}
			String oldval = object.get(field);

			if (oldval == null)
			{
				oldval = "";
			}
			if (value == null)
			{
				value = "";
			}

			if (!value.equals(oldval))
			{
				PropertyDetail detail = getDetail(field);
				if (detail != null && detail.isList())
				{
					Searcher listSearcher = getSearcherManager().getListSearcher(detail);
					Data data = (Data) listSearcher.searchById(oldval);
					if (data != null)
					{
						oldval = data.getName();
					}
					data = (Data) listSearcher.searchById(value);
					if (data != null)
					{
						value = data.getName();
					}

				}
				if (changes.length() > 0)
				{
					changes.append(", ");
				}
				changes.append(field + ": " + oldval + " -> " + value);
			}
		}

		if (changes.length() > 0)
		{
			WebEvent event = new WebEvent();
			event.setCatalogId(getCatalogId());
			String type = getSearchType();
			event.setSearchType(type);
			event.setSource(this);
			//assetedit

			//			event.setSearchType("assetedit");
			//			event.setSource(this);
			//			event.setOperation("assetedit");
			//			event.setSourcePath(asset.getSourcePath());

			//			event.setProperty("assetid", asset.getId());
			//			event.setProperty("assetname", asset.getName());
			//			event.setProperty("changes", changes.toString());

			event.setOperation("edit");
			event.setSourcePath(object.getSourcePath());
			event.setProperty("id", object.getId());
			//aka "changes"
			event.setProperty("details", changes.toString());
			event.setUser(inReq.getUser());
			getEventManager().fireEvent(event);
		}
	}

	/*
	 * public EventManager getEventManager() { return
	 * fieldEventManager; }
	 * 
	 * public void setEventManager(EventManager inEventManager) {
	 * fieldEventManager = inEventManager; }
	 */
	public void saveDetails(WebPageRequest inReq, String[] fields, Data data, String id)
	{
		//		// This might be a productid of multiple products. We need to create
		//		// lots of data objects
		//		if (id != null && id.startsWith("multiedit:"))
		//		{
		//			data = (Data) inReq.getSessionValue(id);
		//		}
		//
		if (data instanceof CompositeData)
		{
			CompositeData target = (CompositeData) data;
			for (Iterator iterator = target.iterator(); iterator.hasNext();)
			{
				Data real = (Data) iterator.next();
				fireDataEditEvent(inReq, real);
				data = updateData(inReq, fields, real);
				saveData(real, inReq.getUser());
			}
		}
		else
		{
			fireDataEditEvent(inReq, data);
			data = updateData(inReq, fields, data);
			saveData(data, inReq.getUser());
		}
		inReq.putPageValue("message", data.getId() + " is saved");
		inReq.putPageValue("data", data);
	}

	public Data updateData(WebPageRequest inReq, String[] fields, Data data)
	{
		if (fields == null)
		{
			log.error("No fields " + data);
			return null;
		}
		
		for (int i = 0; i < fields.length; i++)
		{
			PropertyDetail detail = getDetail(fields[i]);
			String field = null;
			if(detail != null){
				 field = detail.getId();
			} else{
				field = fields[i];
			}
			
			String[] values = inReq.getRequestParameters(field + ".values");
			if (values == null)
			{
				values = inReq.getRequestParameters(fields[i] + ".value");
			}
			if (values == null)
			{
				values = inReq.getRequestParameters(field + ".value");
			}
			
			
			if( detail == null )
			{
				log.error("No detail " + fields[i]);
				detail = new PropertyDetail();
				detail.setId(field);				
				//This way code relying on this setting values will still work
				
			}
			
			
			
			
			Object result = null;
			
			if ( detail.isMultiLanguage())
			{
				LanguageMap map = null;
				Object oldval = data.getValue(detail.getId());
				if(oldval != null){
					if(oldval instanceof LanguageMap){
						map = (LanguageMap) oldval;										
					} else{
						map = new LanguageMap();
						map.setText("en",(String) oldval);
					}
				}
				if (map == null)
				{
					map = new LanguageMap();
				}
				String term = fields[i];
				if( values != null && values.length > 0 && term.contains("."))
				{
					String lang = term.substring(term.indexOf( ".") + 1);
					map.setText(lang,values[0]);
				}
				else
				{
					String[] language = inReq.getRequestParameters(field + ".language");
					if( language != null)
					{	
						//Load new values
						for (int j = 0; j < language.length; j++)
						{
							String lang = language[j];
							String langval = inReq.getRequestParameter(field + "." + lang + ".value"); 
							if( langval == null)
							{
								langval = inReq.getRequestParameter(field + ".language." + (j + 1)); //legacy
							}
							map.setText(lang,langval);
						}
					}	
					else if( values != null && values.length > 0)
					{
						String val = values[0];
						if ("multilanguage".equals(language))  //Well, this will never work language is an array
						{
							JsonSlurper parser = new JsonSlurper();
							Map vals = (Map) parser.parseText(val);
							map.putAll(vals);
						}
						else
						{
							map.setText("en",String.valueOf( val ) );
						}
					}
				}	
				result = map;
			}
			else if(values != null && detail.isMultiValue())
			{
				if( values.length == 1 && values[0].contains("|"))
				{
					values = MultiValued.VALUEDELMITER.split(values[0]);
				}
				result = Arrays.asList(values);
			}
			else if (values != null && values.length > 0)
			{
				String val = values[0];

				if (detail != null && detail.isDate())
				{
					Date date = null;
					String hour = inReq.getRequestParameter(field + ".hour");
					String minute = inReq.getRequestParameter(field + ".minute");
					if (hour != null && minute != null)
					{
						val = val + " " + hour + ":" + minute;
						result = DateStorageUtil.getStorageUtil().parse(val, "yyyy-MM-dd HH:mm");

					}
					else if (val.length() == 10) //We assume US format or Storage Format
					{
						String format = "yyyy-MM-dd";
						if (val.matches("[0-9]{2}/[0-9]{2}/[0-9]{4}"))
						{
							format = "MM/dd/yyyy";
						}
						result = DateStorageUtil.getStorageUtil().parse(val, format);
					}
					else
					{
						result = DateStorageUtil.getStorageUtil().parseFromStorage(val);
					}
				}
				else if(detail != null && detail.isBoolean()){
					result = Boolean.parseBoolean(val);
				}
				else
				{
					result = val;
				}
			}
			
			if( result == null && detail.isBoolean())
			{
				data.setValue(field, false);
			}
			else
			{
				data.setValue(field, result);
			}
		}

		return data;

	}

	public String nextId()
	{
		throw new IllegalAccessError("nextId Not implemented");
	}

	public String getViewLabel(String inView)
	{
		return getPropertyDetailsArchive().getViewLabel(inView);
	}

	public Data uniqueResult(SearchQuery inQ)
	{
		HitTracker tracker = search(inQ);
		Iterator iter = tracker.iterator();
		if (iter.hasNext())
		{
			return (Data) iter.next();
		}
		return null;
	}

	/**
	 * @deprecated
	 * @param inData
	 * @param inUser
	 */
	public void saveData(Object inData, User inUser)
	{
		saveData((Data) inData, inUser);
	}

	public void saveData(Data inData)
	{
		saveData(inData, null);
	}

	public void saveData(Data inData, User inUser)
	{
		throw new OpenEditException("Save not implemented for " + getSearchType());
	}
//	public Data loadData(String inId)
//	{
//		
//	}

	@Override
	public Data loadData(Data inHit)
	{
		if( inHit == null)
		{
			return null;
		}
		if (getNewDataName() == null && inHit instanceof SaveableData)
		{
			return inHit;
		}
		else
		{
			Data data = (Data)createNewData();
			data.setProperties(inHit.getProperties());
			data.setId(inHit.getId());
			return data;
		}
	}

	public LockManager getLockManager()
	{
		return getSearcherManager().getLockManager(getCatalogId());
	}

	@Override
	public void restoreSettings()
	{
		getPropertyDetailsArchive().clearCustomSettings(getSearchType());

	}

	@Override
	public void reloadSettings()
	{
		getPropertyDetailsArchive().reloadSettings(getSearchType());

	}

	@Override
	public boolean putMappings()
	{
		// TODO Auto-generated method stub
		return false;
	}
	public void reindexInternal() throws OpenEditException
	{
		//do nothing?
		throw new OpenEditException("Not implemented");
		//reIndexAll();
	}
	
	public Term addPosition(WebPageRequest inReq, SearchQuery search, PropertyDetail field, String val, String op)
	{
		if (!field.isDataType("geo_point") || val == null || val.isEmpty() )
		{
			return null;
		}
		
		String rangeString = inReq.getRequestParameter("maprange" + field.getId()); //distance in meters
		if (rangeString == null)
		{
			rangeString = "5000000"; //default.  
		}

		//rangeString = rangeString + "000";
		double range = Double.parseDouble(rangeString); //meters
		range = range / 157253.2964;//convert to decimal degrees (FROM Meters)
		Position p = (Position)getGeoCoder().findFirstPosition(val);
		GeoFilter filter = new GeoFilter();

		if( p != null)
		{
			Double latitude = p.getLatitude();
			Double longitude = p.getLongitude();
			filter.setLatitude(latitude);
			filter.setLongitude(longitude);
			filter.addParameter("formatted_address", p.getFormatedAddress());
			filter.setCenter(p);
		}
		else
		{
			log.error("No location found " + search.hashCode() );
			filter.addParameter("maperror","No results");
			//filter.setCenter(p);
		}

		filter.setDistance(Long.parseLong( rangeString));
		filter.setType("distance");
		filter.setOperation("geofilter");
		filter.setDetail(field);
		filter.setValue(val);
		Term term = search.addGeoFilter(field, filter);

		return term;
	}

	public GeoCoder getGeoCoder()
	{
		GeoCoder coder = (GeoCoder)getModuleManager().getBean(getCatalogId(),"geoCoder");
		coder.setGoogleKey(getConfigValue("google-maps-key"));
		if( coder.getGoogleKey() == null)
		{
			log.error("No key set");
		}
		return coder;
	}

	public String getConfigValue(String inKey)
	{
		//look up values in db
		return null;
	}
	
	
	
}