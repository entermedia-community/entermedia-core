/*
 * $Id: OrFilter.java,v 1.7 2010/06/28 20:15:48 oeian Exp $
 */
package com.openedit.util.strainer;

import com.openedit.WebPageRequest;

/**
 * This is a combinatorial filter which ORs together all of its sub-filters.
 * 
 * @author Eric Galluzzo
 */
public class OrFilter extends BaseFilter implements CompositeFilter
{

	/**
	 * This constructor should only be used for JavaBean-style creation.
	 */
	public OrFilter()
	{
	}

	/**
	 * Create a filter that ORs together all the given sub-filters.
	 * 
	 * @param inFilters  The sub-filters
	 */
	public OrFilter(Filter[] inFilters)
	{
		fieldFilters = inFilters;
	}

	/**
	 * Create a filter that ORs together both of the given sub-filters.
	 * 
	 * @param inFilter1  The first sub-filter
	 * @param inFilter2  The second sub-filter
	 */
	public OrFilter(Filter inFilter1, Filter inFilter2)
	{
		fieldFilters = new Filter[] { inFilter1, inFilter2 };
	}

	/**
	 * Retrieve this filter's sub-filters.
	 * 
	 * @return  This filter's sub-filters
	 */
	public Filter[] getFilters()
	{
		return fieldFilters;
	}

	/**
	 * Set this filter's sub-filters.
	 * 
	 * @param newFilters  The new sub-filters
	 */
	public void setFilters(Filter[] newFilters)
	{
		fieldFilters = newFilters;
	}

	/**
	 * Determine whether the given object passes this filter by ORing together
	 * all the sub-filters.
	 *
	 * @param inObj  The object to check
	 *
	 * @return <code>true</code> if the object passes at least one of the
	 * sub-filters, <code>false</code> otherwise.
	 */
	public boolean passes(Object inObj) throws FilterException
	{
		String method = null;
		if( inObj instanceof WebPageRequest)
		{
			WebPageRequest inParent = (WebPageRequest)inObj;
			method = inParent.getMethod();
		}
		
		for (int i = 0; i < fieldFilters.length; i++)
		{
			Filter filter = fieldFilters[i];
			if( method == null)
			{
				if (filter.passes(inObj))
				{
					return true;
				}
			}
			else
			{
				String onlymethod =  filter.get("method");
				if(onlymethod == null || method.equalsIgnoreCase(onlymethod))
				{
					if (filter.passes(inObj))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		Filter[] filters = getFilters();
		if( filters == null || filters.length == 0)
		{
			return "false";
		}
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < filters.length; i++)
		{
			if (i > 0)
			{
				buffer.append(" Or ");
			}
			buffer.append("(");
			buffer.append(filters[i].toString());
			buffer.append(")");
		}
		return buffer.toString();
	}
	
	public boolean equals(Object inObj)
	{
		if (inObj instanceof OrFilter)
		{
			OrFilter toCompare = (OrFilter)inObj;
			for (int i = 0; i < fieldFilters.length; i++){
				boolean matchFound = false;
				for(int j = 0; j < toCompare.fieldFilters.length; j++)
				{
					if(fieldFilters[i].equals(toCompare.fieldFilters[j]))
					{
						matchFound = true;
						break;
					}
				}
				if(!matchFound)
				{
					return false;
				}
			}
		}
		return true;
	}
}