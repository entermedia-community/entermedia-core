/*
 * Created on Jul 19, 2006
 */
package com.openedit.hittracker;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.openedit.Data;
import org.openedit.data.BaseData;
import org.openedit.data.PropertyDetail;
import org.openedit.data.PropertyDetails;
import org.openedit.data.Searcher;
import org.openedit.data.SearcherManager;
import org.openedit.util.GenericsUtil;

import com.openedit.OpenEditException;
import com.openedit.OpenEditRuntimeException;

public class SearchQuery extends BaseData implements Cloneable, Serializable, Comparable
{
	protected transient List fieldTerms = new ArrayList();

	protected boolean fieldAndTogether = true;
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
		return getHitsName() + getCatalogId();
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
		getTerms().add(term);
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
		getTerms().add(term);
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
		getTerms().add(term);
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
		getTerms().add(term);
		return term;
	}
	
	public Term addNots(String inId, String inNots)
	{
		PropertyDetail detail = new PropertyDetail();
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
		for (Iterator iterator = getChildren().iterator(); iterator.hasNext();)
		{
			SearchQuery query = (SearchQuery) iterator.next();
			if( done.length() > 0 )
			{
				done.append(op);
			}
			done.append(query.toFriendly());
		}

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
		if (!inDetail.isViewType("list"))
		{
			return inRawValue;
		}

		Searcher searcher = getSearcherManager().getSearcher(inDetail.getCatalogId(getCatalogId()), inDetail.getId());
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
		getTerms().add(term);
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
		term.setDetail(inDetail);
		term.setValue(inVal);
		addTerm(term);
		return term;
	}

	public boolean isEmpty()
	{
		return fieldTerms.isEmpty();
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
		term.setDetail(inField);
		term.setValue(inVal);
		getTerms().add(term);
		return term;
	}

	public Term addStartsWith(PropertyDetail inField, String inVal)
	{
		if (!inVal.endsWith("*"))
		{
			inVal = inVal + "*";
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
		term.setDetail(inField);
		term.setValue(inVal);
		addTerm(term);
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

	public String[] getInputs(String inKey)
	{
		Object input = get(inKey);
		if (input != null)
		{
			if (input instanceof String)
			{
				String[] results = { (String) input };
				return results;
			}
			else
			{
				String[] vals = (String[]) input;
				return vals;
			}
		}

		return null;
	}

	public String getSortBy()
	{
		if (getSorts().size() > 0)
		{
			return (String) getSorts().get(0);
		}
		return null;
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
			getTerms().add(term);
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
		getTerms().add(term);
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
	 * @deprecated use getTermByTermId(String) instead
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
		PropertyDetail detail = new PropertyDetail();
		detail.setId(inString);
		return addMatches(detail, value);
	}

	public Term addAfter(String inString, Date inSearchDate)
	{
		PropertyDetail detail = new PropertyDetail();
		detail.setId(inString);
		return addAfter(detail, inSearchDate);
	}

	public Term addExact(String inKey, String inValue)
	{
		PropertyDetail detail = getPropertyDetails().getDetail(inKey);
		if(detail == null)
		{
			detail = new PropertyDetail();
			detail.setId(inKey);
		}
		return addExact(detail, inValue);
	}

	public Term addExact(PropertyDetail inField, long inParseInt)
	{
		String inString = String.valueOf(inParseInt);
		return addExact(inField, inString);
	}

	public Term addStartsWith(String inString, String inQuery)
	{
		PropertyDetail detail = new PropertyDetail();
		detail.setId(inString);
		return addStartsWith(detail, inQuery);
	}

	public Term addMatches(String inInQuery)
	{
		PropertyDetail detail = new PropertyDetail();
		return addMatches(detail, inInQuery);
	}

	public Term addNot(String inId, String inQuery)
	{
		PropertyDetail detail = new PropertyDetail();
		detail.setId(inId);
		return addNot(detail, inQuery);
	}

	public Term addOrsGroup(String inId, String inQuery)
	{
		PropertyDetail detail = new PropertyDetail();
		detail.setId(inId);
		return addOrsGroup(detail, inQuery);
	}

	public Term addQuery(String inString, String inValue)
	{
		PropertyDetail detail = new PropertyDetail();
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
		for (Iterator iterator = getChildren().iterator(); iterator.hasNext();)
		{
			SearchQuery child = (SearchQuery) iterator.next();
			Term term = child.getTermByTermId(inTermId);
			if( term != null)
			{
				return term;
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
			if( term.getId().equals(inTermId))
			{
				return term;
			}
		}
		return null;
	}

	public void addTerm(Term inTerm)
	{
		if( !isAndTogether())
		{
			//You can OR anything together
			getTerms().add(inTerm);
			return;
		}
		
		SearchQuery child = getChildQuery(inTerm.getDetail().getId());
		List existing = getTerms(inTerm.getDetail().getId());
		if( existing.size() == 0 && child == null )
		{
			inTerm.setId(inTerm.getDetail().getId() + "_0");
			getTerms().add(inTerm);
		}
		else
		{
			if( child == null)
			{
				try
				{
					child = (SearchQuery)getClass().newInstance();
					child.setSearcherManager(getSearcherManager());
					child.setId(inTerm.getDetail().getId());
					child.setAndTogether(false);
					addChildQuery(child);
				}
				catch (Exception e)
				{
					//should never happen
				}
			}
			
			//add the old and new one to the child
			getTerms().removeAll(existing);
			child.getTerms().addAll(existing);
			inTerm.setId(inTerm.getDetail().getId() + "_" + child.getTerms().size());
			child.getTerms().add(inTerm);
		}
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
			if( inId.equals( query.getId()) )
			{
				return query;
			}
		}
		return null;
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
		try
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
		} catch( CloneNotSupportedException ex)
		{
			throw new OpenEditException(ex);
		}
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
		PropertyDetail d = new PropertyDetail();
		d.setId(inString);
		addBetween(d, inNow, inNext);
		
	}

	public void addBetween(String string, long longValue, long longValue2) {
		PropertyDetail d = new PropertyDetail();
		d.setId(string);
		addBetween(d, longValue, longValue2);
	}
	public void addBetween(String string, double longValue, double longValue2) {
		PropertyDetail d = new PropertyDetail();
		d.setId(string);
		addBetween(d, longValue, longValue2);
	}

	public Term addBetween(PropertyDetail d, double longValue,
			double longValue2) {
		return null;
		
	}

	public void addBefore(String inString, Date inDate) {
		PropertyDetail detail = new PropertyDetail();
		detail.setId(inString);
		addBefore(detail, inDate);		
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
			name = getFriendlyValue(term.getValue(),term.getDetail());
		}
		return name;
	}
}
