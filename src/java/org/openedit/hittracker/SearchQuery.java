/*
 * Created on Jul 19, 2006
 */
package org.openedit.hittracker;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.openedit.CatalogEnabled;
import org.openedit.Data;
import org.openedit.OpenEditException;
import org.openedit.OpenEditRuntimeException;
import org.openedit.data.BaseData;
import org.openedit.data.PropertyDetail;
import org.openedit.data.PropertyDetails;
import org.openedit.data.Searcher;
import org.openedit.data.SearcherManager;
import org.openedit.util.DateStorageUtil;
import org.openedit.util.GenericsUtil;

public class SearchQuery extends BaseData implements Cloneable, Serializable, Comparable, CatalogEnabled
{
	private static final Log log = LogFactory.getLog(SearchQuery.class);

	protected transient List fieldTerms = new ArrayList();

	protected boolean fieldAndTogether = true;
	protected boolean fieldShowAll = false;
	protected boolean fieldEndUserSearch = false;
	protected String fieldSortLanguage = "en";
	//protected boolean fieldIncludeFacets = false;
	protected Collection<PropertyDetail> fieldUserFacets;
	protected List fieldSorts;
	protected Map fieldSuggestedSearches;

	protected transient PropertyDetails fieldPropertyDetails;
	protected transient SearcherManager fieldSearcherManager;
	protected String fieldCatalogId;
	protected List<String> fieldCatalogs;
	protected transient DateFormat fieldDateFormat = new SimpleDateFormat("M/d/yyyy");
	protected String fieldDescription;
	protected List fieldChildren;
	protected String fieldHitsName;
	protected String fieldResultType; //This might be things like assets, albums, selection or search. Used by resulttype
	protected boolean fieldFireSearchEvent = false;
	protected boolean fieldFilter = false;
	//protected List<ChildFilter> fieldChildrenFilters;
	protected List<FilterNode> fieldFilters; 
	protected Collection<String> fieldSecurityIds;
	protected boolean fieldIncludeDescription = false;
	protected boolean fieldIncludeDeleted = false;
	protected String fieldTimeZone;
	protected int fieldDefaultAggregationCount = 50;
	
	public int getDefaultAggregationCount()
	{
		return fieldDefaultAggregationCount;
	}


	public void setDefaultAggregationCount(int inDefaultAggregationCount)
	{
		fieldDefaultAggregationCount = inDefaultAggregationCount;
	}

	protected boolean fieldForceEmpty = false;

	public boolean isForceEmpty() {
		return fieldForceEmpty;
	}


	public void setForceEmpty(boolean inForceEmpty) {
		fieldForceEmpty = inForceEmpty;
	}


	public String getTimeZone() {
		return fieldTimeZone;
	}


	public void setTimeZone(String inTimeZone) {
		fieldTimeZone = inTimeZone;
	}


	public boolean isIncludeDeleted()
	{
		return fieldIncludeDeleted;
	}


	public void setIncludeDeleted(boolean inIncludeDeleted)
	{
		fieldIncludeDeleted = inIncludeDeleted;
	}


	public void setIncludeDescription(boolean inIncludeDescription)
	{
		fieldIncludeDescription = inIncludeDescription;
	}

	protected int fieldHitsPerPage = 15;	
	
	public SearchQuery()
	{
		// TODO Auto-generated constructor stub
	}
	
	
	public boolean isSortUp() {
		if(getSortBy() == null) {
			return false;
			
		}
		if(getSortBy().endsWith("Up"))
		{
			return true;
			
		} 
		else {
			return false;
		}
		
	}
	
	
	public Collection<PropertyDetail> getFacets()
	{
	if (fieldUserFacets == null)
	{
		fieldUserFacets = new HashSet<PropertyDetail>();
	}

	return fieldUserFacets;
	}

	public void setFacets(Collection<PropertyDetail> facets)
	{
		fieldUserFacets = facets;
//		//UserProfile profile = inPageRequest.getUserProfile();
//		if (facets != null && facets.size() > 0)
//		{
//			for (Iterator iterator = facets.iterator(); iterator.hasNext();)
//			{
//				PropertyDetail detail = (PropertyDetail) iterator.next();
//				
//				getFacets().add(detail);
//
//			}
//		}

	}
	
	public String getSortLanguage()
	{
		return fieldSortLanguage;
	}

	public void setSortLanguage(String inSortLanguage)
	{
		fieldSortLanguage = inSortLanguage;
	}

	public boolean isEndUserSearch()
	{
		return fieldEndUserSearch;
	}

	public void setEndUserSearch(boolean inEndUserSearch)
	{
		fieldEndUserSearch = inEndUserSearch;
	}


	
	
	public int getHitsPerPage()
	{
		return fieldHitsPerPage;
	}

	public void setHitsPerPage(int inHitsPerPage)
	{
		fieldHitsPerPage = inHitsPerPage;
	}

	public String getResultType()  //this is searchtype
	{
		return fieldResultType;
	}

	public void setResultType(String inSearchType)
	{
		fieldResultType = inSearchType;
	}

	protected boolean fieldSecurityAttached = false;
	
	
	public String getHitsName()
	{
		return fieldHitsName;
	}

	public void setHitsName(String inHitsName)
	{
		fieldHitsName = inHitsName;
	}
	
	public String getSessionId()
	{
		return getHitsName() + getResultType() +  getCatalogId();
	}

	public SearcherManager getSearcherManager()
	{
		return fieldSearcherManager;
	}

	public void setSearcherManager(SearcherManager inSearcherManager)
	{
		fieldSearcherManager = inSearcherManager;
	}

	public String getCatalogId()
	{
		return fieldCatalogId;
	}

	public void setCatalogId(String inCatalogId)
	{
		fieldCatalogId = inCatalogId;
	}

	public PropertyDetails getPropertyDetails()
	{
		return fieldPropertyDetails;
	}

	public void setPropertyDetails(PropertyDetails propertDetails)
	{
		fieldPropertyDetails = propertDetails;
	}

	public String getDescription()
	{
		return fieldDescription;
	}

	public void setDescription(String inDescription)
	{
		fieldDescription = inDescription;
	}

	public DateFormat getDateFormat()
	{
		return fieldDateFormat;
	}

	public void setDateFormat(DateFormat inDateFormat)
	{
		fieldDateFormat = inDateFormat;
	}

	public Map getSuggestedSearches()
	{
		return fieldSuggestedSearches;
	}

	public void setSuggestedSearches(Map suggestedSearches)
	{
		fieldSuggestedSearches = suggestedSearches;
	}

	public void addGroup(SearchQuery inGroup)
	{
		for(Iterator iterator = inGroup.getTerms().iterator(); iterator.hasNext();)
		{
			Term term = (Term) iterator.next();
			fieldTerms.add(term);
		}
	}

	public List<Term> getTerms()
	{
		return fieldTerms;
	}

	public boolean contains(String inField)
	{
		for (Iterator iterator = getTerms().iterator(); iterator.hasNext();)
		{
			Term term = (Term) iterator.next();
			if (term.getDetail().getId().equals(inField))
			{
				return true;
			}
		}
		return false;
	}
	public Term addOrsGroup(PropertyDetail inField, Collection inValues)
	{
		String[] ids = extractIds(inValues);
		return addOrsGroup( inField,ids);
	}
	public Term addOrsGroup(PropertyDetail inField, String[] inValues)
	{
		Term term = new Term()
		{
			public String toQuery()
			{
				StringBuffer orString = new StringBuffer();
				Object[] values = getValues();
				if (values.length > 0)
				{
					orString.append("(");
					for (int i = 0; i < values.length - 1; i++)
					{
						Object val = values[i];
						if(val != null && val.toString().length() > 0)
						{
							orString.append(values[i]);
							orString.append(" OR ");
						}
					}
					orString.append(values[values.length - 1]);
					orString.append(")");
				}
				return getDetail().getId() + ":" + orString.toString();
			}
		};
		term.setDetail(inField);
		//term.setValue(inValue);
		//String[] orwords = inValue.split("\\s+");
		term.setValues(inValues);
		term.setOperation("orgroup");
		getTerms().add(term);
		return term;
	}
	
	public Term addOrsGroup(PropertyDetail inField, String inValue)
	{
		Term term = new Term()
		{
			public String toQuery()
			{
				StringBuffer orString = new StringBuffer();
				Object[] values = getValues();
				if (values.length > 0)
				{
					orString.append("(");
					for (int i = 0; i < values.length - 1; i++)
					{
						if(values[i].toString().length() > 0)
						{
							orString.append(values[i]);
							orString.append(" OR ");
						}
					}
					orString.append(values[values.length - 1]);
					orString.append(")");
				}
				return getDetail().getId() + ":" + orString.toString();
			}
		};
		term.setDetail(inField);
		inValue = inValue.replace(",", " ").replace("|", " ");
		term.setValue(inValue);
		String[] orwords = inValue.split("\\s+");
		term.setValues(orwords);
		term.setOperation("orgroup");
		getTerms().add(term);
		return term;
	}
	public Term addAndGroup(String inDetailId, final String[] inValues)
	{
		PropertyDetail detail = createDetail(inDetailId);
		detail.setId(inDetailId);
		return addAndGroup(detail,inValues);
	}
	
	public Term addAndGroup(PropertyDetail inDetail, final String[] inValues)
	{
		Term term = new Term()
		{
			public String toQuery()
			{
				StringBuffer orString = new StringBuffer();
				if (inValues.length > 0)
				{
					orString.append("(");
					for (int i = 0; i < inValues.length; i++)
					{
						if(inValues[i].length() > 0)
						{
							orString.append("+");
							orString.append(inValues[i]);
							orString.append(" ");
						}
					}
					orString.append(")");
				}
				return getDetail().getId() + ":" + orString.toString();
			}
		};
		term.setDetail(inDetail);
		term.setValues(inValues);
		term.setOperation("andgroup");
		addTermByDataType(term);
		return term;
	}

	
	public Term addNots(String inId, String inNots)
	{
		PropertyDetail detail = createDetail(inId);
		detail.setId(inId);
		return addNots(detail, inNots);
	}

	public Term addNot(String inNot)
	{
		return addNots((PropertyDetail)null, inNot);
	}

	public String toString()
	{
		return toFriendly();
	}

	public String toFriendly()
	{
		String op = " or ";
		if (isAndTogether())
		{
			op = " and ";
		}
		StringBuffer done = new StringBuffer();
		for (int i = 0; i < fieldTerms.size(); i++)
		{
			Term term = (Term) fieldTerms.get(i);
			PropertyDetail detail = term.getDetail();
			if (detail != null)
			{
				if (!detail.isFilter())
				{
					if (i > 0)
					{
						done.append(op);
					}
					String q = detail.getName();
					if(q == null){
						q = detail.getName();
						
					}
					if(q == null){
						q = detail.getId();
						
					}
					
					done.append(q);
					if( "not".equals(term.getOperation() ) ) 
					{
						done.append(" not ");
					}
					else
					{
						done.append(":");
					}
					done.append(getFriendlyValue(term, detail));
				}
			}
			else
			{
				if (i > 0)
				{
					done.append(op);
				}
				String q = term.getId();
				done.append(q);
				done.append(":");
				done.append(term.getValue());
			}
		}
		if( getSorts().size() > 0)
		{
			done.append(" sort ");
			for (Iterator iterator = getSorts().iterator(); iterator.hasNext();)
			{
				String sort= (String) iterator.next();
				done.append(sort);
				done.append(" ");
			}
		}

		return done.toString();
	}
	public String getFriendlyValue(Term inTerm, PropertyDetail inDetail)
	{
		String value = inTerm.getValue();
		if( value != null)
		{
			return getFriendlyValue(value, inDetail);
		}
		StringBuffer out = new StringBuffer();
		Object[] values  = inTerm.getValues();
		if(values != null)
		{
			for (int i = 0; i < values.length; i++)
			{
				Object val = values[i];
				if( val instanceof String)
				{
					out.append( getFriendlyValue((String)val, inDetail) );
					out.append(", " );
				}
			}
		}
		return out.toString();
	}

	/**
	 * Transforms a list ID value into a human-readable value.
	 * 
	 * @param inRawValue
	 *            could be a list value ID.
	 * @param inDetail
	 *            details for the property.
	 * @param inSearcherManager
	 *            to get a searcher for the property (if it is a list).
	 * @return the friendly value if it can get one. inRawValue otherwise.
	 */
	public String getFriendlyValue(String inRawValue, PropertyDetail inDetail)
	{
		if (!inDetail.isViewType("list") && !inDetail.isViewType("category"))
		{

//			if(inDetail.isDate())
//			{
//				Date found =  DateStorageUtil.getStorageUtil().parseFromStorage(inRawValue);
//				if( found != null)
//				{
//					return found.getTime();
//				}
//			}
			

			return inRawValue;
		}

		Searcher searcher = getSearcherManager().getSearcher(inDetail.getCatalogId(getCatalogId()), inDetail.getListId());
		if (searcher == null)
		{
			return inRawValue;
		}
		
		Data data = (Data) searcher.searchById(inRawValue);
		if (data == null)
		{
			return inRawValue;
		}
		return data.getName();
	}

	public boolean isAndTogether()
	{
		return fieldAndTogether;
	}

	public void setAndTogether(boolean inAndTogether)
	{
		fieldAndTogether = inAndTogether;
	}
	public SearchQuery append(String inKey, String inVal)
	{
		addMatches(inKey,inVal);
		return this;
	}
	
	public SearchQuery appendNot(String inKey, String inVal)
	{
		addNot(inKey,inVal);
		return this;
	}

	public Term addContains(PropertyDetail inDetail, String inVal)
	{
		Term term = new Term()
		{
			public String toQuery()
			{
				if (getDetail().getId() != null)
				{
					return getDetail().getId() + ":*" + getValue() + "*";
				}
				else
				{
					return getValue();
				}
			}

			public Element toXml()
			{
				Element term = DocumentHelper.createElement("term");
				term.addAttribute("id", getDetail().getId());
				term.addAttribute("val", getValue());
				term.addAttribute("op", "contains");
				if (getValue("op") != null)
					term.addAttribute("realop", (String) getValue("op"));

				return term;
			}
		};
		term.setOperation("contains");
		term.setDetail(inDetail);
		term.setValue(inVal);
		addTermByDataType(term);
		return term;
	}
	
	
	public boolean isEmpty()
	{
		if(fieldTerms.isEmpty())
		{
//			String fullq = toQuery();
//			if( fullq == null || fullq.length() == 0 )
//			{
			if( hasChildren() )
			{
				for (Iterator iterator = getChildren().iterator(); iterator.hasNext();)
				{
					SearchQuery q = (SearchQuery) iterator.next();
					if( !q.isEmpty() )
					{
						return false;
					}
				}
			}
			return true;
//			}
		}
		return false;
	}

	@Override
	public boolean equals(Object inObj)
	{
		if( inObj == this )
		{
			return true;
		}
		if( inObj instanceof SearchQuery)
		{
			SearchQuery q = (SearchQuery)inObj;
			Collection searchmodules = getValues("searchtypes");
			if( searchmodules != null)
			{
				Collection searchmodules2 = q.getValues("searchtypes");
				if( !searchmodules.equals(searchmodules2))
				{
					return false;
				}
			}
			String one = q.toQuery();
			if( one != null)
			{
				if( !one.equals(toQuery() ) )
				{
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * This is the user input (!= term.value)
	 */
	public String getInput(String inKey)
	{
		Object input = get(inKey);
		if (input != null)
		{
			if (input instanceof String)
			{
				return (String) input;
			}
			else
			{
				String[] vals = (String[]) input;
				return vals[0];
			}
		}
		Term term = getTermByDetailId(inKey);
		if( term == null)
		{
			term = getTerm(inKey);
		}
		if( term != null)
		{
			return term.getValue();
		}
		if( hasChildren())
		{
			for (Iterator iterator = getChildren().iterator(); iterator.hasNext();)
			{
				SearchQuery child = (SearchQuery) iterator.next();
				input = child.getInput(inKey);
				if( input != null)
				{
					if (input instanceof String)
					{
						return (String) input;
					}
					else
					{
						String[] vals = (String[]) input;
						return vals[0];
					}
				}
			}
		}	
		
		return null;
	}

	public Collection getInputs(String inKey)
	{
		Collection input = getValues(inKey);
		if( input == null)
		{
			Term term = getTermByDetailId(inKey);
			if( term == null)
			{
				term = getTerm(inKey);
			}
			if( term != null && term.getValues() != null)
			{
				return Arrays.asList( term.getValues() );
			}
			return null;
		}
		return input;
	}

	public String getSortBy()
	{
		if (getSorts().size() == 0)
		{
			return null;
		}
		if (getSorts().size() == 1)
		{
			return (String)getSorts().get(0);
		}
		StringBuffer sorts = new StringBuffer();
		for (Iterator iterator = getSorts()	.iterator(); iterator.hasNext();)
		{
			String sort = (String) iterator.next();
			sorts.append(sort);
			if(iterator.hasNext() )
			{
				sorts.append(",");				
			}
		}
		return sorts.toString();
	}
	public PropertyDetail getSortByDetail()
	{
		String id = null;
		for (Iterator iterator = getSorts().iterator(); iterator.hasNext();)
		{
			String sort = (String) iterator.next();
			if( sort.endsWith("Up"))
			{
				id = sort.substring(0,sort.length() - 2 );
				break;
			}
			if( sort.endsWith("Down"))
			{
				id = sort.substring(0,sort.length() - 4 );
				break;
			}
			id = sort;
		}
		PropertyDetail detail = getPropertyDetails().getDetail(id);
		return detail;
	}

	public void setSortBy(String inSortBy)
	{
		if (inSortBy != null)
		{
			getSorts().clear();
			String[] sorts = inSortBy.split(",");
			for (int i = 0; i < sorts.length; i++)
			{
				addSortBy(sorts[i]);
			}
		}

	}

	public void addSortBy(String inSortBy)
	{
		if (inSortBy != null)
		{
			
			for (Iterator iterator = getSorts().iterator(); iterator.hasNext();)
			{
				String sort = (String) iterator.next();
				if( sort.contains( inSortBy))
				{
					return;
				}
			}

			getSorts().add(inSortBy);
		}
	}

	public List<String> getSorts()
	{
		if (fieldSorts == null)
		{
			fieldSorts = new ArrayList(2);
		}
		return fieldSorts;
	}

	public boolean isSortedBy(String inKey)
	{
		for (Iterator iterator = getSorts().iterator(); iterator.hasNext();)
		{
			String sort = (String) iterator.next();
			if( sort.contains( inKey))
			{
				return true;
			}
		}
		return false;
	}
	public Term addQuery(PropertyDetail inId, String inFilter)
	{
		if (inFilter != null && inFilter.length() > 1)
		{
			Term term = new Term()
			{
				public String toQuery()
				{
					return getValue();
				}

				public Element toXml()
				{
					Element term = DocumentHelper.createElement("term");
					term.addAttribute("id", getDetail().getId());
					term.addAttribute("val", getValue());
					term.addAttribute("op", "exact");
					if (getValue("op") != null)
						term.addAttribute("realop", (String) getValue("op"));

					return term;
				}
			};
			term.setDetail(inId);
			term.setValue(inFilter);
			addTermByDataType(term);
			return term;
		}
		return null;
	}

	public void addCategoryFilter(List inRemaining)
	{
		throw new OpenEditRuntimeException("Not implemented");
	}

	public void removeTerm(Term inTerm)
	{
		if( inTerm == null)
		{
			log.error("inTerm was null");
			return;
		}
		getTerms().remove(inTerm);
		
		setProperty(inTerm.getDetail().getId(), null);
		for (Iterator iterator = getChildren().iterator(); iterator.hasNext();)
		{
			SearchQuery query = (SearchQuery) iterator.next();
			query.removeTerm(inTerm);
			if( query.getTerms().size() == 0)
			{
				getChildren().remove(query);
				return;
			}
		}
	}
	public void removeTerm(String inTermid)
	{
		Term term = getTermByTermId(inTermid);
		removeTerm(term);
	}
 
	public void removeTerms(String inFieldId)
	{
		List terms = new ArrayList(getTerms());
		for (Iterator iter = terms.iterator(); iter.hasNext();)
		{
			Term term = (Term) iter.next();
			if (term.getDetail().getId().equals(inFieldId))
			{
				removeTerm(term);
			}
		}
	}
	
	public PropertyDetail getDetail(String inId)
	{
		if (getPropertyDetails() == null)
		{
			return null;
		}
		return getPropertyDetails().getDetail(inId);
	}

	public List<String> getCatalogs()
	{
		if (fieldCatalogs == null)
		{
			fieldCatalogs = GenericsUtil.createList();

		}

		return fieldCatalogs;
	}

	public void addCatalog(String inCat)
	{
		if (!getCatalogs().contains(inCat))
		{
			getCatalogs().add(inCat);
		}
	}

	public void removeCatalog(String inCat)
	{
		getCatalogs().remove(inCat);
		if(getCatalogs().size() == 0){
			setTerms(new ArrayList());
		}

	}

	public void setCatalogs(List<String> inCatalogs)
	{
		fieldCatalogs = inCatalogs;
	}

	public Term addMatches(PropertyDetail inDetail)
	{
		// this is a generic add
		return addMatches(inDetail, "");
	}

	/**
	 * @deprecated use getTermByDetailId(String) instead
	 */
	public Term getTerm(String inFieldId)
	{
		if(inFieldId == null){
			return null;
		}
		if(getTerms() == null){
			return null;
		}
		for (Iterator iterator = getTerms().iterator(); iterator.hasNext();)
		{
			Term term = (Term) iterator.next();
			PropertyDetail detail = term.getDetail();
			if (detail != null && detail.getId() != null && detail.getId().equals(inFieldId))
			{
				return term;
			}
		}
		return null;
	}

	/**
	 * Returns all the terms related to a field Id.
	 * @param inFieldId the field's Id.
	 * @return a list with all the found terms. An empty list if none was found.
	 */
	public List getTerms(String inFieldId)
	{
		List terms = new ArrayList();
		for (Iterator iterator = getTerms().iterator(); iterator.hasNext();)
		{
			Term term = (Term) iterator.next();
			if (term.getDetail().getId().equals(inFieldId))
			{
				terms.add(term);
			}
		}
		return terms;
	}
	
	public Collection getExtraTerms(Collection<PropertyDetail> inDetails)
	{
		if( getTerms() == null || getTerms().isEmpty() || inDetails == null )
		{
			return Collections.EMPTY_LIST;
		}
			
		Set detailids = new HashSet();
		for (Iterator iterator = inDetails.iterator(); iterator.hasNext();)
		{
			PropertyDetail detail = (PropertyDetail) iterator.next();
			detailids.add(detail.getId() );
		}
		
		List terms = new ArrayList();
		for (Iterator iterator = getTerms().iterator(); iterator.hasNext();)
		{
			Term term = (Term) iterator.next();
			if( !detailids.contains(term.getDetail().getId()) )
			{
				terms.add(term);
			}
		}
		return terms;
	}
	

	public Term addMatches(String inString, String value)
	{
		PropertyDetail detail = createDetail(inString);
		return addMatches(detail, value);
	}

	public Term addOrsGroup(String inString, Collection values)
	{
		PropertyDetail detail = createDetail(inString);
		String[] array = extractIds(values);
		return addOrsGroup(detail, array);
	}
	
	public Term addContains(String inString, String value)
	{
		PropertyDetail detail = createDetail(inString);
		return addContains(detail, value);
	}

	
	
	public Term addExact(String inKey, String inValue)
	{
	
		PropertyDetail detail = createDetail(inKey);
		return addExact(detail, inValue);
	}

	
	public Term addExact(PropertyDetail inField, double inParseInt)
	{
		String inString = String.valueOf(inParseInt);
		return addExact(inField, inString);
	}

	public Term addStartsWith(String inString, String inQuery)
	{
		PropertyDetail detail = createDetail(inString);
		detail.setId(inString);
		return addStartsWith(detail, inQuery);
	}

	public Term addMatches(String inInQuery)
	{
		PropertyDetail detail = createDetail("description");
		return addMatches(detail, inInQuery);
	}

	public Term addNot(String inId, String inQuery)
	{
		PropertyDetail detail = createDetail(inId);
		detail.setId(inId);
		return addNot(detail, inQuery);
	}

	public Term addOrsGroup(String inId, String inQuery)
	{
		PropertyDetail detail = createDetail(inId);
		return addOrsGroup(detail, inQuery);
	}

	protected PropertyDetail createDetail(String inId)
	{
		PropertyDetail detail = null;
		//If this contains a . we actually want to search on that.  Not the parent ID of an object.		
		//if(!inId.contains(".")) 
		{
			detail = getDetail(inId);
		}
		if( detail == null )
		{
			detail = new PropertyDetail();
			detail.setId(inId);
		}
		return detail;
	}

	public Term addQuery(String inString, String inValue)
	{
		PropertyDetail detail = createDetail(inString);
		detail.setId(inString);
		return addQuery(detail, inValue);

	}

	
	public List getTerms(String inCatalogid, String inView, String inField)
	{
		// returns matching terms.
		List termList = new ArrayList();
		for (Iterator iterator = getTerms().iterator(); iterator.hasNext();)
		{
			Term term = (Term) iterator.next();
			PropertyDetail detail = term.getDetail();
			if (detail != null)
			{
				if (inCatalogid.equals(detail.getCatalogId()) && inField.equals(detail.getId()))
				{
					termList.add(term);
				}
			}
		}
		return termList;
	}
	

	public Term getTerm(String inCatalogid, String inView, String inField)
	{
		for (Iterator iterator = getTerms().iterator(); iterator.hasNext();)
		{
			Term term = (Term) iterator.next();
			PropertyDetail detail = term.getDetail();
			if (detail != null)
			{
				if (inCatalogid.equals(detail.getCatalogId()) && inField.equals(detail.getId()))
				{
					return term;
				}
			}
		}
		return null;
	}
	
	public Term getTermByTermId(String inTermId)
	{
		for (Iterator iterator = getTerms().iterator(); iterator.hasNext();)
		{
			Term term = (Term) iterator.next();
			if (term.getId().equals(inTermId))
			{
				return term;
			}
		}
		if( hasChildren())
		{
			for (Iterator iterator = getChildren().iterator(); iterator.hasNext();)
			{
				SearchQuery child = (SearchQuery) iterator.next();
				Term term = child.getTermByTermId(inTermId);
				if( term != null)
				{
					return term;
				}
			}
		}
		return null;
	}

	public Term getTermByDetailId(String inTermId)
	{
		for (Iterator iterator = getTerms().iterator(); iterator.hasNext();)
		{
			Term term = (Term) iterator.next();
			
			PropertyDetail detail = term.getDetail();
			if (detail != null && detail.getId() != null && detail.getId().equals(inTermId))
			{
				return term;
			}
//			if( term.getId().equals(inTermId))
//			{
//				return term;
//			}
		}
		return null;
	}
	protected void addTermByDataType(Term inTerm)
	{
		addTerm(inTerm);
	}
	
	/**
	 * A better way to do this is to have "or" composite terms
	 * Then flag the term as a composite for saving to the database
	 * @param inTerm
	 */
	public void addTerm(Term inTerm)
	{
		if( !isAndTogether())
		{
			//You can OR anything together
			getTerms().add(inTerm);
			return;
		}
		
		List existing = getTerms(inTerm.getDetail().getId());
		SearchQuery child = getChildQueryWithDetail(inTerm.getDetail().getId());
		if( existing.size() == 0 && child == null )
		{
			//inTerm.setId(inTerm.getDetail().getId() + "_0");
			getTerms().add(inTerm);
		}
		else
		{
			if( child == null)
			{
				try
				{
					child = (SearchQuery)getClass().newInstance();
				}
				catch (Exception e)
				{
					//should never happen
					throw new OpenEditException(e);
				}
					child.setSearcherManager(getSearcherManager());
					//child.setId(inTerm.getDetail().getId());
					child.setAndTogether(false);
					addChildQuery(child);
			}
			
			//add the old and new one to the child
			getTerms().removeAll(existing);
			child.getTerms().addAll(existing);
			//inTerm.setId(inTerm.getDetail().getId() + "_" + child.getTerms().size());
			child.getTerms().add(inTerm);
		}
	}

	protected SearchQuery getChildQueryWithDetail(String inId)
	{
		if( hasChildren() )
		{
			for (Iterator iterator = getChildren().iterator(); iterator.hasNext();)
			{
				SearchQuery query = (SearchQuery) iterator.next();
				if( query.getTermByDetailId(inId) != null)
				{
					return query;
				}
			}
		}
		return null;
	}

	public void addChildQuery(SearchQuery inQuery)
	{
		getChildren().add(inQuery);
	}
	
	public SearchQuery getChildQuery(String inId)
	{
		for (Iterator iterator = getChildren().iterator(); iterator.hasNext();)
		{
			SearchQuery query = (SearchQuery) iterator.next();
			if( inId.equals( query.getId() ) )
			{
				return query;
			}
		}
		return null;
	}
	public boolean hasChildren()
	{
		if( fieldChildren == null || fieldChildren.size() == 0)
		{
			return false;
		}
		return true;
	}
	public List getChildren()
	{
		if (fieldChildren == null)
		{
			fieldChildren = new ArrayList();
		}

		return fieldChildren;
	}

	public void setChildren(List inChildren)
	{
		fieldChildren = inChildren;
	}

	public Element toXml()
	{
		Element query = DocumentHelper.createElement("query");
		if( fieldCatalogs != null && fieldCatalogs.size() > 0)
		{
			Element cats = query.addElement("catalogs");
			for (Iterator iterator = getCatalogs().iterator(); iterator.hasNext();) {
				String catid = (String) iterator.next();
				cats.addElement("catalog").addAttribute("id", catid);
			}
		}
		query.addAttribute("description", getDescription());
		query.addAttribute("id", getId());
		query.addAttribute("name", getName());
		if( !isAndTogether())
		{
			query.addAttribute("ortogether","true");
		}
		
		for (Iterator iterator = getTerms().iterator(); iterator.hasNext();)
		{
			Term term = (Term) iterator.next();
			Element termelem = term.toXml();
			PropertyDetail detail = term.getDetail();
			if (detail != null)
			{
				String catalogid = detail.getCatalogId();
				termelem.addAttribute("catalogid", catalogid);
				termelem.addAttribute("searchtype", detail.getSearchType());
			}
			query.add(termelem);
			// add the property detail
		}
		for (Iterator iterator = getChildren().iterator(); iterator.hasNext();)
		{
			SearchQuery child = (SearchQuery) iterator.next();
			query.add(child.toXml());
		}
		
		return query;
	}


	public void setTerms(List inTerms)
	{
		fieldTerms = inTerms;
	}
	
	public SearchQuery copy() 
	{
			SearchQuery query = (SearchQuery)clone();
	//		query.setHitsName(getHitsName());
	//		
	//		query.setCatalogId(getCatalogId());
	//		query.setAndTogether(isAndTogether());
	//		query.setChildren(getChildren());
	//		query.setDescription(getDescription());
			//it was doing a shallow copy for the terms which was causing a problem
			//copy the search terms if this was java 1.5, I would use list's static copy method
			query.setTerms(new ArrayList());
			for (int i = 0; i < fieldTerms.size(); i++)
			{
				query.addTerm((Term)fieldTerms.get(i));
			}
/*			query.setCatalogs(getCatalogs());
			query.setSuggestedSearches(getSuggestedSearches());
	//		query.setSortBy(getSortBy());
			query.setInputs(getInputs());
			query.setDateFormat(getDateFormat());
			query.setPropertyDetails(fieldPropertyDetails);
			query.setSearcherManager(fieldSearcherManager);
			query.setSecurityAttached(isSecurityAttached());
*/
			return query;
	}

	public boolean isSecurityAttached()
	{
		return fieldSecurityAttached;
	}

	public void setSecurityAttached(boolean inSecurityAttached)
	{
		fieldSecurityAttached = inSecurityAttached;
	}

	public boolean isFireSearchEvent()
	{
		return fieldFireSearchEvent;
	}

	public void setFireSearchEvent(boolean inFireSearchEvent)
	{
		fieldFireSearchEvent = inFireSearchEvent;
	}

	public void addBetween(String inString, Date inNow, Date inNext)
	{
		PropertyDetail d = createDetail(inString);
		d.setId(inString);
		addBetween(d, inNow, inNext);
		
	}
	
	
	public void addGeoFilter(String inString, GeoFilter inLocation){
		PropertyDetail d = createDetail(inString);
		d.setId(inString);
		addGeoFilter(d, inLocation);
	}
	
	

	public Term addGeoFilter(PropertyDetail inD, GeoFilter inLocation)
	{
//		Term term = new Term()
//		{
//			public String toQuery()
//			{
//				String fin = getDetail().getId() + "location";
//				return fin;
//			}
//		};
//		term.setOperation("geofilter");
//		term.setDetail(inD);
//		term.setData(inLocation);
		getTerms().add(inLocation);

		return inLocation;
		
		
	}

	public void addBetween(String string, long longValue, long longValue2) {
		PropertyDetail d = createDetail(string);
		d.setId(string);
		addBetween(d, longValue, longValue2);
	}
	public void addBetween(String string, double longValue, double longValue2) {
		PropertyDetail d = createDetail(string);
		d.setId(string);
		addBetween(d, longValue, longValue2);
	}

	public void addBefore(String inString, Date inDate) {
		PropertyDetail detail = createDetail(inString);
		detail.setId(inString);
		addBefore(detail, inDate);		
	}	

	public Term addFreeFormQuery(PropertyDetail inField, String inValue)
	{
		Term term = new Term()
		{
			public String toQuery()
			{
				String inVal = getValue();
				return inVal;
			}
		};
		term.setOperation("freeform");
		term.setDetail(inField);
		term.setValue(inValue);
		addTermByDataType(term);
		return term;
	}

	

	
	public int compareTo(Object inO)
	{
		SearchQuery q1 = (SearchQuery)inO;
		
		return super.getId().compareTo(q1.getId());
	}

	@Override
	public String getName()
	{
		String name = super.getName();
		if( name == null)
		{
			name = getInput("description");
		}
		if( name == null && getTerms().size() > 0)
		{
			Term term = (Term)getTerms().get(0);
			if (!term.getDetail().isFilter())
			{
				name = getFriendlyValue(term.getValue(),term.getDetail());
			}
		}
		if( name == null)
		{
			name = "All";
		}
		return name;
	}

	public boolean isFilter() 
	{
		return fieldFilter;
	}
	public void setFilter(boolean inVal)
	{
		fieldFilter = inVal;
	}

	/**
	 * Searching the parent table
	 * A child depends on a parent but a parent does not depend on a child
	 * The asset table is the parent table of many things
	 * @param childtable
	 * @param fieldname
	 * @param inValue
	 */
	public void addChildFilter(String childtable, String fieldname, String inValue)
	{
		ChildFilter join = new ChildFilter();
		join.setChildTable(childtable);
		join.setChildColumn(fieldname);
		join.setValue(inValue);
		join.setOperation("childfilter");
		join.setDetail(createDetail("id"));
		addTerm(join);
	}
	
	public void addFilter(String inToaddType, String inToaddvalue, String toAddLabel)
	{
		if (hasFilters()){
			for (FilterNode node:getFilters()){
				if (node.getId()!=null && node.getId().equals(inToaddType) && node.get("value") != null && node.get("value").equals(inToaddvalue)){
					return;
				}
			}
		}
		FilterNode node = new FilterNode();
		node.setId(inToaddType);
		node.setProperty("value", inToaddvalue);
		node.setName(toAddLabel);
		getFilters().add(node);
	}
	

	public Collection getUserFilters()
	{
		Collection filters = new  ArrayList();
		for (Iterator iterator = getTerms().iterator(); iterator.hasNext();)
		{
			Term term = (Term) iterator.next();
			if( isUserFilter(term) )
			{
				filters.add(term);
			}
		}
		return filters;
	}


	protected boolean isUserFilter(Term term)
	{
		if( term.isUserFilter() )
		{
			return true;
		}
		if( "asset".equals(getResultType()) && term.getDetail().getId().equals("description") )
		{
			return true;
		}
		return false;
	}	
	
	public boolean hasFilters()
	{
		for (Iterator iterator = getTerms().iterator(); iterator.hasNext();)
		{
			Term term = (Term) iterator.next();
			if( isUserFilter(term) )
			{
				return true;
			}
		}
		
		return fieldFilters != null && !fieldFilters.isEmpty();
	}
	/**
	 * These are filters that get added as booleans
	 * @deprecated dont use filters anymore, just flag a field as a user filter
	 * @return
	 */
	public List<FilterNode> getFilters()
	{
		if( fieldFilters == null)
		{
			fieldFilters = new ArrayList<FilterNode>();
		}
		return fieldFilters;
	}

	public void setFilters(List<FilterNode> inFilters)
	{
		fieldFilters = inFilters;
	}

	public void removeFilter(String inToremove)
	{
		if( hasFilters() )
		{
			for (FilterNode node: getFilters())
			{
				if(inToremove.equals( node.getId() ) )
				{
					getFilters().remove(node);
					break;
				}
			}
		}
	}
	
	public void clearFilters()
	{
		if( !hasFilters() )
		{
			return;
		}
		
		List<FilterNode> nodes = getFilters();
		
		if(nodes == null)
		{
			return;
		}
		
		int amtOfNodes = nodes.size();
		
		for (int i=amtOfNodes - 1;i >= 0; --i)
		{
			if(nodes.get(i) != null)
			{
				nodes.remove(nodes.get(i));
			}
		}
	}
	
	public boolean hasFilter(String inId)
	{
		if( hasFilters() )
		{
			for (FilterNode node: getFilters())
			{
				if(inId.equals( node.getId() ) )
				{
					return true;
				}
			}
		}
		return false;
	}

	public Collection<String> getSecurityIds()
	{
		return fieldSecurityIds;
	}

	public void setSecurityIds(Collection<String> inSecurityIds)
	{
		fieldSecurityIds = inSecurityIds;
	}

	public Object getAggregation()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setAggregation(Object inObject)
	{
		// TODO Auto-generated method stub
	}

	public Term addAfter(String inString, Date inSearchDate)
	{
		PropertyDetail detail  = null;
		if( getPropertyDetails() != null)
		{
			detail = getPropertyDetails().getDetail(inString);
		}
		if(detail == null)
		{
			detail = new PropertyDetail();
			detail.setId(inString);
			detail.setDataType("date");
		}
		return addAfter(detail, inSearchDate);
	}
	//public void addJoinFilter(SearchQuery filterQuery, String inFilterColumn, boolean inFilterHasMultiValues, String filterSearchType, String inResultsColumn)
		//https://www.elastic.co/guide/en/elasticsearch/guide/current/parent-child-mapping.html
		//https://www.elastic.co/guide/en/elasticsearch/guide/master/indexing-parent-child.html
		//https://www.elastic.co/guide/en/elasticsearch/guide/master/has-child.html

	public Term addAfter(PropertyDetail inFieldId, final Date inDate)
	{
		Term term = new Term()
		{
			public String toQuery()
			{
				//String date = DateTools.dateToString(inDate, Resolution.SECOND);
				String fin = getDetail().getId() + ":[" + inDate.getTime() + " TO 99999999999999]";
				return fin;
			}
		};
		term.setDetail(inFieldId);
		String valueof= DateStorageUtil.getStorageUtil().formatForStorage(inDate);
	
		term.setValue(valueof);
		term.setOperation("afterdate");
		getTerms().add(term);
		return term;
	}

	public Term addBetween(PropertyDetail inFieldId, final Date inAfter, final Date inBefore)
	{
		Term term = new Term()
		{
			public String toQuery()
			{
				String fin = getDetail().getId() + ":[" + inAfter + " TO " + inBefore + "]";
				return fin;
			}
		};
		String lowDate = getDateFormat().format(inAfter);
		String highDate = getDateFormat().format(inBefore);
		term.setValue(lowDate + " - " + highDate);
		term.setDetail(inFieldId);
		term.addValue("afterDate", inAfter);
		term.addValue("beforeDate", inBefore);
		term.setOperation("betweendates");
		getTerms().add(term);
		return term;
	}

	public Term addBefore(PropertyDetail inField, final  Date inDate)
	{
		final String valueof= DateStorageUtil.getStorageUtil().formatForStorage(inDate);

		Term term = new Term()
		{
			public String toQuery()
			{
				String fin = getDetail().getId() + ":[00000000000000 TO " +valueof + "]";
				return fin;
			}
		};
		term.setOperation("beforedate");
		term.setDetail(inField);
	
		term.setValue(valueof);
	
		getTerms().add(term);
		return term;
	}
	public Term addOn(String inField, final  Date inDate)
	{
		PropertyDetail detail = createDetail(inField);
		return addOn(detail,inDate);
	}
	public Term addOn(PropertyDetail inField, final  Date inDate)
	{
		final String valueof= DateStorageUtil.getStorageUtil().formatForStorage(inDate);
		Term term = new Term()
		{
			public String toQuery()
			{
				String fin = getDetail().getId() + ":[00000000000000 TO " +valueof + "]";
				return fin;
			}
		};
		term.setOperation("ondate");
		term.setDetail(inField);
	
		term.setValue(valueof);
	
		getTerms().add(term);
		return term;
	}
	
	public Term addMatches(PropertyDetail inField, String inValue)
	{
		Term term = new Term()
		{
			public String toQuery()
			{
				String inVal = getValue();
				if( inVal != null && inVal.startsWith("'") && inVal.endsWith("'"))
				{
					inVal = inVal.replace('\'', '\"');
				}
	
				if (getDetail().getId() != null)
				{
					return getDetail().getId() + ":(" + inVal + ")";
				}
				else
				{
					return inVal;
				}
			}
		};
		term.setOperation("matches");
		term.setDetail(inField);
		term.setValue(inValue);
		addTerm(term);
		return term;
	}

	public Term addStartsWith(PropertyDetail inField, String inVal)
	{
		Term term = new Term()
		{
			public String toQuery()
			{
				StringBuffer q = new StringBuffer();
				q.append(getDetail().getId());
				q.append(":(");
	
				if (getValue().startsWith("\""))
				{
					q.append(getValue());
				}
				else
				{
					String[] spaces = getValue().split("\\s+");
					for (int i = 0; i < spaces.length; i++)
					{
						String chunk = spaces[i];
						q.append(chunk);
						if (chunk.indexOf('*') == -1)
						{
							q.append('*');
						}
						if (i + 1 < spaces.length)
						{
							q.append(' ');
						}
					}
				}
				q.append(")");
				return q.toString();
			}
		};
		term.setOperation("startswith");
		term.setDetail(inField);
		term.setValue(inVal);
		addTerm(term);
		return term;
	}

	public Term addNots(PropertyDetail inField, String inNots)
	{
		Term term = new Term()
		{
			public String toQuery()
			{
				StringBuffer orString = new StringBuffer();
	
				String[] notwords = getValue().split("\\s");
				if (notwords.length > 0)
				{
					for (int i = 0; i < notwords.length; i++)
					{
						orString.append(" NOT " + notwords[i]);
					}
				}
				return orString.toString();
			}
		};
		
		term.setOperation("notgroup");
		term.setDetail(inField);
		term.setValue(inNots);
		getTerms().add(term);
		return term;
	}

	public Term addExact(PropertyDetail inField, String inValue)
	{
		if( inValue == null)
		{
			return null;
		}
		Term term = new Term()
		{
			public String toQuery()
			{
				String val = getValue();
				if(val.startsWith("\""))
				{
					val = val.substring(1);
				}
				if(val.endsWith("\""))
				{
					val = val.substring(0,val.length()-2);
				}
				val = val.replace("\"", "\\\"");
				return getDetail().getId() + ":\"" + val + "\"";
			}
		};
		term.setOperation("exact");
		term.setDetail(inField);
		term.setValue(inValue);
		addTerm(term);
		return term;
	}

	public void addExact(String inValue)
	{
		Term term = new Term()
		{
			public String toQuery()
			{
				return "\"" + getValue() + "\"";
			}
		};
	
		term.setOperation("exact");
		term.setValue(inValue);
		addTerm(term);
	
	}

	public Term addNot(PropertyDetail inField, String inVal)
	{
			Term term = new Term()
			{
				public String toQuery()
				{
					return "-" + getDetail().getId() + ":" + getValue();
				}
			};
			term.setDetail(inField);
			term.setValue(inVal);
			term.setOperation("not");
			getTerms().add(term);
			return term;
		}
	/* is this used anyplace?
		public void addCategoryFilter(List inRemaining, String inFriendly)
		{
			final List categories = inRemaining;
			Term term = new Term()
			{
				public String toQuery()
				{
					return "-" + getId() + ":" + getValue() + "";
				}
	
				public Element toXml()
				{
					Element term = DocumentHelper.createElement("term");
					term.addAttribute("id", getId());
					term.addAttribute("val", getValue());
					term.addAttribute("op", "categoryfilter");
	
					for (Iterator iterator = categories.iterator(); iterator.hasNext();)
					{
						String category = (String) iterator.next();
						Element cat = term.addElement("category");
						cat.addAttribute("categoryid", category);
					}
	
					return term;
				}
			};
			term.setId("category");
			StringBuffer all = new StringBuffer();
			all.append("(");
			for (Iterator iter = inRemaining.iterator(); iter.hasNext();)
			{
				String cat = (String) iter.next();
				all.append(cat);
				all.append(" ");
			}
			all.append(")");
			term.setValue(all.toString());
			addTerm(term);
		}
	*/

	public Term addLessThan(PropertyDetail inFieldId, long val)
	{
		Term term = new Term()
		{
			public String toQuery()
			{
				return getValue();
			}
		};
		term.setOperation("lessthannumber");
		term.setDetail(inFieldId);
		term.setValue(String.valueOf( val ) );
		addTerm(term);
		return term;
	}

	public Term addGreaterThan(PropertyDetail inFieldId, final long high)
	{
		Term term = new Term()
		{
			public String toQuery()
			{
				return getValue();
			}
	
		};
		term.setOperation("greaterthannumber");
		term.setDetail(inFieldId);
		term.setValue(String.valueOf( high) );
		addTerm(term);
		return term;
	}

	public Term addExact(PropertyDetail inField, long inParseInt)
	{
	
		Term term = new Term()
		{
			public String toQuery()
			{
				return getValue();
			}
	
		};
		term.setOperation("exactnumber");
		term.setDetail(inField);
		term.setValue(String.valueOf(inParseInt));
		addTerm(term);
		return term;
	
	}

	public Term addBetween(PropertyDetail inField, long lowval, long highval)
	{
		// lowval = pad(lowval);
		// highval = pad(highval);
		Term term = new Term()
	
		{
			public String toQuery()
			{
				return getValue();
			}
	
		};
		term.setDetail(inField);
		term.setOperation("betweennumbers");
		term.addValue("lowval",  lowval  );
		term.addValue("highval", highval);
		term.setValue(lowval  + " to "  + highval);
		addTerm(term);
		return term;
	}

	public Term addBetween(PropertyDetail inField, double lowval, double highval)
	{
		// lowval = pad(lowval);
		// highval = pad(highval);
		Term term = new Term()
	
		{
			public String toQuery()
			{
				return getValue();
			}
	
		};
		term.setDetail(inField);
		term.setOperation("betweennumbers");
		term.addValue("lowval",   lowval);
		term.addValue("highval",highval);
		term.setValue(lowval  + " to "  + highval);
		addTerm(term);
		return term;
	}

	public String toQuery()
	{
			StringBuffer done = new StringBuffer();
			String op = null;
			if (isAndTogether())
			{
				op = "+";
			}
			else
			{
				op = " OR ";
			}
			if( getTerms().size() > 0)
			{
				
				for (int i = 0; i < fieldTerms.size(); i++)
				{
					Term field = (Term) fieldTerms.get(i);
					String q = field.toQuery();
					if (i > 0 && !q.startsWith("+") && !q.startsWith("-"))
					{
						done.append(op);
					}
					done.append(q);
					if (i + 1 < fieldTerms.size())
					{
						done.append(" ");
					}
				}
	
	//			if (!isAndTogether())
	//			{
	//				done.append(")");
	//			}
			}
			if( fieldChildren != null && fieldChildren.size() > 0)
			{
				for (int j = 0; j < getChildren().size(); j++)
				{
					SearchQuery child = (SearchQuery) getChildren().get(j);
					String query = child.toQuery();
					//&& !query.startsWith("+") && !query.startsWith("-")
					done.append(" ");
					if( isAndTogether())
					{
						done.append("AND( ");	
					}
					else
					{
						done.append("OR( ");
					}
					done.append(query);
					done.append(" )");	
				}
			}
			return done.toString();
		}


	public void addAggregation(String inFacet){
		
		PropertyDetail detail = getDetail(inFacet);
		if( detail == null)
		{
			throw new OpenEditException("No such field " + inFacet);
		}
		getFacets().add(detail);
	}

	public void addAggregation(PropertyDetail inDetail){
		
		if( inDetail == null)
		{
			throw new OpenEditException("No such field " + inDetail);
		}
		Collection existing = getFacets();
		existing.add(inDetail);
		setFacets(existing);
	}

	/**
	 * Take a list of things and only OR the common ones
	 * This can be called more than once
	 * @param inKey
	 * @param inIds
	 */
	public void appendOrGroup(PropertyDetail inKey, Collection<String> inIds)
	{
		Term existing = getTermByDetailId(inKey.getId());
		if( existing == null)
		{
			addOrsGroup(inKey, (String[])inIds.toArray(new String[inIds.size()]));			
		}
		else
		{
			Set existingvalues = new HashSet( Arrays.asList( existing.getValues() ) );
			Set goodvalues = new HashSet();
			
			for (String id : inIds)
			{
				if( existingvalues.contains( id) )
				{
					goodvalues.add(id);
				}
			}
			addOrsGroup(inKey, (String[])goodvalues.toArray(new String[goodvalues.size()]));			
		}
	}
	public Term addNots(String inField, Collection inNotshown)
	{
		PropertyDetail detail = createDetail(inField);
		return addNots(detail,inNotshown);
	}
	public Term addNots(PropertyDetail inField, Collection inNotshown)
	{
		
		String[] values =  new String[inNotshown.size()];
		int i = 0;
		for (Iterator iterator = inNotshown.iterator(); iterator.hasNext();)
		{
			Object object = (Object) iterator.next();
			if( object instanceof Data)
			{
				values[i] = ((Data)object).getId();
			}
			else
			{
				values[i] = String.valueOf(object);
			}
			i++;
		}
		
		Term term = new Term()
		{
			public String toQuery()
			{
				StringBuffer orString = new StringBuffer();
				orString.append(getDetail().getId() + " NOT = " );
				for (int i = 0; i < getValues().length; i++)
				{
					if( i < 0)
					{
						orString.append("|");
					}
					orString.append(getValues()[i] );
				}
				return orString.toString();
			}
		};
		term.setValues(values);
		term.setOperation("notgroup");
		term.setDetail(inField);
		getTerms().add(term);
		return term;
	}


	public Term addLessThan(PropertyDetail inField, double low) {
		Term term = new Term()
		{
			public String toQuery()
			{
				return getValue();
			}
		};
		term.setOperation("lessthannumber");
		term.setDetail(inField);
		term.setValue(String.valueOf( low ) );
		addTerm(term);
		return term;
	}


	public Term addGreaterThan(PropertyDetail inField, double high) {
		Term term = new Term()
		{
			public String toQuery()
			{
				return getValue();
			}
	
		};
		term.setOperation("greaterthannumber");
		term.setDetail(inField);
		term.setValue(String.valueOf( high) );
		addTerm(term);
		return term;
	}


	public void addFreeFormQuery(String inKey, String inValue) {
		PropertyDetail detail = createDetail(inKey);
		addFreeFormQuery(detail, inValue);
		
	}
	public boolean hasMainInput()
	{
		String input = getInput("description");
		return input != null;
	}

	public boolean hasTermValue(String inDetailId, String inValue)
	{
		List terms = getTerms(inDetailId);
		if( terms != null && !terms.isEmpty() )
		{
			for (Iterator iterator = terms.iterator(); iterator.hasNext();)
			{
				Term term = (Term) iterator.next();
				if( term.containsValue(inValue) )
				{
					return true;
				}
			}
		}
		return false;
	}
	public String getMainInput()
	{
		String input = getInput("description");
		return input;
	}


public boolean isFilterSelected(String type, String value) {
		
		for (FilterNode node: getFilters())
		{
			if(type.equals( node.getId() ) && value.equals(node.get("value")) )
			{
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<FilterNode> getNodesForType(String inType)
	{
		ArrayList nodes = new ArrayList();
		for (Iterator iterator = getFilters().iterator(); iterator.hasNext();)
		{
			FilterNode node = (FilterNode) iterator.next();
			if(node.getId().equals(inType)) {
				nodes.add(node);
			}
		}
		return nodes;
	}


	public boolean isIncludeDescription()
	{
		return fieldIncludeDescription;
	}


	public void addMissing(String inKey)
	{
		PropertyDetail detail = createDetail(inKey);

		Term term = new Term()
		{
			public String toQuery()
			{
				return getDetail().getId();
			}
		};
		term.setOperation("missing");
		term.setDetail(detail);
		addTerm(term);
		
	}	
	public void addExists(String inKey)
	{
		PropertyDetail detail = createDetail(inKey);

		Term term = new Term()
		{
			public String toQuery()
			{
				return getDetail().getId();
			}
		};
		term.setOperation("exists");
		term.setDetail(detail);
		addTerm(term);
		
	}	
	
	

	public String[] extractIds(Collection inDataCollection)
	{
		if( inDataCollection ==  null || inDataCollection.isEmpty())
		{
			return null;
		}
		Collection ids  = null;
		Iterator iter = inDataCollection.iterator();
		if( iter.hasNext())  //TODO: This code is terrible. Just loop over the list
		{
			Object value = iter.next();
			if( value instanceof Data)
			{
				ids = new ArrayList(inDataCollection.size());
				Data data = (Data) value;
				String id = data.getId();
				if( id != null)
				{
					ids.add(id);
					for (; iter.hasNext();)
					{
						data = (Data) iter.next();
						id = data.getId();
						if( id != null)
						{
							ids.add(id);
						}	
					}
				}	
			}
			else
			{
				ids =  inDataCollection;
			}
		}
		return (String[])ids.toArray(new String[ids.size()]);
	}

	public void setSearchTypes(Collection<String> inTypes)
	{
		setValue("searchtypes",inTypes);

	}


	public boolean equalTerms(SearchQuery inSearchQuery)
	{
		if( inSearchQuery == this )
		{
			return true;
		}
		SearchQuery q = (SearchQuery)inSearchQuery;
		Collection searchmodules = getValues("searchtypes");
		if( searchmodules != null)
		{
			Collection searchmodules2 = q.getValues("searchtypes");
			if( !searchmodules.equals(searchmodules2))
			{
				return false;
			}
		}
		String one = q.toQuery();
		if( one != null)
		{
			if( !one.equals(toQuery() ) )
			{
				return false;
			}
		}
		return true;
	}


	public boolean isShowAll()
	{
		return fieldShowAll;
	}
	public void setShowAll(boolean inAll)
	{
		fieldShowAll = inAll;
	}
	
}
