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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.openedit.Data;
import org.openedit.OpenEditException;
import org.openedit.OpenEditRuntimeException;
import org.openedit.data.BaseData;
import org.openedit.data.PropertyDetail;
import org.openedit.data.PropertyDetails;
import org.openedit.data.Searcher;
import org.openedit.data.SearcherManager;
import org.openedit.util.GenericsUtil;

public class SearchQuery extends BaseData implements Cloneable, Serializable, Comparable
{
	protected transient List fieldTerms = new ArrayList();

	protected boolean fieldAndTogether = true;
	protected boolean fieldEndUserSearch = false;
	protected String fieldSortLanguage = "en";
	
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
	
	public String getResultType()
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

	public List getTerms()
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

	public Term addAfter(PropertyDetail inFieldId, Date inDate)
	{
		String date = getDateFormat().format(inDate);
		final Date after = inDate;
		Term term = new Term()
		{
			public String toQuery()
			{
				String fin = getDetail().getId() + ":[" + getValue() + " TO 99999999]";
				return fin;
			}
		};
		term.setDetail(inFieldId);
		term.addParameter("afterDate", date);
		term.setOperation("afterdate");
		term.setValue(date);
		addTermByDataType(term);
		return term;
	}

	public Term addBefore(PropertyDetail inFieldId, Date inDate)
	{
		String date = getDateFormat().format(inDate);
		final Date targetDate = inDate;
		Term term = new Term()
		{
			public String toQuery()
			{
				String fin = getDetail().getId() + ":[00000000 TO " + getValue() + "]";
				return fin;
			}
		};
		term.setDetail(inFieldId);
		term.addParameter("beforeDate", date);
		term.setValue(date);
		term.setOperation("beforedate");
		addTermByDataType(term);
		return term;
	}
	public Term addOrsGroup(PropertyDetail inDetail, final String[] inValues)
	{
		Term term = new Term()
		{
			public String toQuery()
			{
				StringBuffer orString = new StringBuffer();
				if (inValues.length > 0)
				{
					orString.append("(");
					for (int i = 0; i < inValues.length - 1; i++)
					{
						if(inValues[i].length() > 0)
						{
							orString.append(inValues[i]);
							orString.append(" OR ");
						}
					}
					orString.append(inValues[inValues.length - 1]);
					orString.append(")");
				}
				return getDetail().getId() + ":" + orString.toString();
			}
		};
		term.setDetail(inDetail);
		term.setValues(inValues);
		term.setOperation("orgroup");
		addTermByDataType(term);
		return term;
	}
	public Term addOrsGroup(PropertyDetail inDetail, String inValue)
	{
		Term term = new Term()
		{
			public String toQuery()
			{
				StringBuffer orString = new StringBuffer();
				String[] orwords = getValue().split("\\s");
				if (orwords.length > 0)
				{
					orString.append("(");
					for (int i = 0; i < orwords.length - 1; i++)
					{
						if(orwords[i].length() > 0)
						{
							orString.append(orwords[i]);
							orString.append(" OR ");
						}
					}
					orString.append(orwords[orwords.length - 1]);
					orString.append(")");
				}
				return getDetail().getId() + ":" + orString.toString();
			}
		};
		term.setDetail(inDetail);
		term.setValue(inValue);
		term.setOperation("orgroup");
		addTermByDataType(term);
		return term;
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

	public Term addNots(PropertyDetail inDetail, String inNots)
	{
		Term term = new Term()
		{
			public String toQuery()
			{
				StringBuffer orString = new StringBuffer();
				String[] notwords = getValue().split("\\s");
				if (notwords.length > 0)
				{
					orString.append("(");
					for (int i = 0; i < notwords.length - 1; i++)
					{
						if(notwords[i].length() > 0)
						{
							orString.append(" NOT " + notwords[i]);
						}
					}
					orString.append(notwords[notwords.length - 1]);
					orString.append(")");
				}
				return orString.toString();
			}
		};
		term.setDetail(inDetail);
		term.setValue(inNots);
		term.setOperation("notgroup");
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
					String q = detail.getText();
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
					done.append(getFriendlyValue(term.getValue(), detail));
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
//		for (Iterator iterator = getChildren().iterator(); iterator.hasNext();)
//		{
//			SearchQuery query = (SearchQuery) iterator.next();
//			if( done.length() > 0 )
//			{
//				done.append(op);
//			}
//			done.append(query.toFriendly());
//		}

		return done.toString();
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

	public Term addExact(PropertyDetail inDetail, String inValue)
	{
		Term term = new Term()
		{
			public String toQuery()
			{
				return getDetail().getId() + ":\"" + getValue() + "\"";
			}
		};
		term.setOperation("exact");
		term.setDetail(inDetail);
		term.setValue(inValue);
		addTermByDataType(term);
		return term;
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

	public Term addMatches(PropertyDetail inDetail, String inVal)
	{
		Term term = new Term()
		{
			public String toQuery()
			{
				if (getDetail().getId() != null)
				{
					return getDetail().getId() + ":" + getValue();
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
				term.addAttribute("op", "matches");
				if (getParameter("op") != null)
					term.addAttribute("realop", getParameter("op"));

				return term;
			}
		};
		term.setOperation("matches");
		term.setDetail(inDetail);
		term.setValue(inVal);
		addTermByDataType(term);
		return term;
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
				if (getParameter("op") != null)
					term.addAttribute("realop", getParameter("op"));

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
	public Term addNot(PropertyDetail inField, String inVal)
	{
		Term term = new Term()
		{
			public String toQuery()
			{
				return "-" + getDetail().getId() + ":" + getValue();
			}

			public Element toXml()
			{
				Element term = DocumentHelper.createElement("term");
				term.addAttribute("id", getDetail().getId());
				term.addAttribute("val", getValue());
				term.addAttribute("op", "not");
				if (getParameter("op") != null)
					term.addAttribute("realop", getParameter("op"));

				return term;
			}
		};
		term.setOperation("not");
		term.setDetail(inField);
		term.setValue(inVal);
		addTermByDataType(term);
		return term;
	}

	public Term addStartsWith(PropertyDetail inField, String inVal)
	{
		if (!inVal.endsWith("*"))
		{
			inVal = inVal + "*";  //TODO: Remove this
		}

		Term term = new Term()
		{
			public String toQuery()
			{
				String val = getValue();
				return val;

			}

			public Element toXml()
			{
				Element term = DocumentHelper.createElement("term");
				term.addAttribute("id", getDetail().getId());
				term.addAttribute("val", getValue());
				term.addAttribute("op", "startswith");
				if (getParameter("op") != null)
					term.addAttribute("realop", getParameter("op"));
				
				return term;
			}
		};
		term.setOperation("startswith");
		term.setDetail(inField);
		term.setValue(inVal);
		addTermByDataType(term);
		return term;
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
		if (getSorts().size() == 0)
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
			getSorts().add(inSortBy);
		}
	}

	public List getSorts()
	{
		if (fieldSorts == null)
		{
			fieldSorts = new ArrayList(2);
		}
		return fieldSorts;
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
					if (getParameter("op") != null)
						term.addAttribute("realop", getParameter("op"));

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
		getTerms().remove(inTerm);
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

	public Term addGreaterThan(PropertyDetail inField, long inVal)
	{
		//Not supported
		return null;
	}

	public Term addLessThan(PropertyDetail inField, long inVal)
	{
		// not supported
		return null;
	}

	public Term addBetween(PropertyDetail inField, Date inAfter, Date inBefore)
	{
		Term term = new Term()
		{
			public String toQuery()
			{
				String fin = getDetail().getId() + ":[" + getParameter("afterDate") + " TO " + getParameter("beforeDate") + "]";
				return fin;
			}
		};
		
		term.setValue(getDateFormat().format(inAfter) + " - " + getDateFormat().format(inBefore));
		term.setDetail(inField);
		term.addParameter("afterDate", getDateFormat().format(inAfter));
		term.addParameter("beforeDate", getDateFormat().format(inBefore));
		term.setOperation("betweendates");
		addTermByDataType(term);
		return term;
	}

	public Term addBetween(PropertyDetail inFieldId, long lowval, long highval)
	{
		// default
		return null;
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

	public Term addMatches(String inString, String value)
	{
		PropertyDetail detail = createDetail(inString);
		return addMatches(detail, value);
	}

	public Term addOrsGroup(String inString, Collection<String> values)
	{
		PropertyDetail detail = createDetail(inString);
		String[] array = values.toArray(new String[values.size()]);
		return addOrsGroup(detail, array);
	}
	
	public Term addContains(String inString, String value)
	{
		PropertyDetail detail = createDetail(inString);
		return addContains(detail, value);
	}

	
	
	
	public Term addAfter(String inString, Date inSearchDate)
	{
		PropertyDetail detail = createDetail(inString);
		return addAfter(detail, inSearchDate);
	}

	public Term addExact(String inKey, String inValue)
	{
		PropertyDetail detail = createDetail(inKey);
		return addExact(detail, inValue);
	}

	public Term addExact(PropertyDetail inField, long inParseInt)
	{
		String inString = String.valueOf(inParseInt);
		return addExact(inField, inString);
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
		PropertyDetail detail = getDetail(inId);
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
				if (inCatalogid.equals(detail.getCatalogId()) && inView.equals(detail.getView()) && inField.equals(detail.getId()))
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
				if (inCatalogid.equals(detail.getCatalogId()) && inView.equals(detail.getView()) && inField.equals(detail.getId()))
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
				String view = detail.getView();
				termelem.addAttribute("view", view);
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

	public String toQuery()
	{
		String op = " OR ";
		if (isAndTogether())
		{
			op = "+";
		}
		StringBuffer done = new StringBuffer();
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
		
		if (getChildren().size() > 1)
		{
			for (Iterator iterator = getChildren().iterator(); iterator.hasNext();)
			{
				SearchQuery child = (SearchQuery) iterator.next();
				String query = child.toQuery();
				if (query.length() > 0)
				{
					done.append(" (");
					done.append(query);
					done.append(" )");	
				}
			}
		}
		else if (getChildren().size() == 1)
		{
			SearchQuery child = (SearchQuery)getChildren().get(0);
			done.append(child.toQuery());
		}
		return done.toString();
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

	public Term addBetween(PropertyDetail d, double longValue,
			double longValue2) {
		return null;
		
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

	public Term addJoinFilter(PropertyDetail inField, String inValue)
	{
		return addJoinFilter( inField.getCatalogId(), inField.getId(), inField.getListId(), inValue, inField.getForeignKeyId());
	}
	
	public Term addJoinFilter(String inCatalogId, String inType, String inFilterColumn, String inFilterValue, String inDataPath)
	{
		// TODO Auto-generated method stub
		JoinFilter filter = new JoinFilter();
		filter.addParameter("catalog", inCatalogId);
		filter.addParameter("type", inType);
		filter.addParameter("column", inFilterColumn);
		filter.setValue(inFilterValue);
		filter.addParameter("datapath", inDataPath);
		filter.setOperation("searchjoin");
		addTerm(filter);
		return filter;
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
	public boolean hasFilters()
	{
		return fieldFilters != null && !fieldFilters.isEmpty();
	}
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


	
	
	
	
	
}
