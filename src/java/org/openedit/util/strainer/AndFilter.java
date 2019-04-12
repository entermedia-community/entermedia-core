/*
 * $Id: AndFilter.java,v 1.7 2010/02/17 20:48:21 jvalencia Exp $
 */
package org.openedit.util.strainer;

/**
 * This is a combinatorial filter which ANDs together all of its sub-filters.
 * 
 * @author Eric Galluzzo
 */
public class AndFilter extends BaseFilter implements CompositeFilter
{

	/**
	 * This constructor should only be used for JavaBean-style creation.
	 */
	public AndFilter()
	{
	}

	/**
	 * Create a filter that ANDs together all the given sub-filters.
	 * 
	 * @param inFilters  The sub-filters to AND together
	 */
	public AndFilter(Filter[] inFilters)
	{
		fieldFilters = inFilters;
	}

	/**
	 * Create a filter that ANDs together both of the given sub-filters.
	 * 
	 * @param inFilter1 The first sub-filter
	 * @param inFilter2 The second sub-filter
	 */
	public AndFilter(Filter inFilter1, Filter inFilter2)
	{
		fieldFilters = new Filter[] { inFilter1, inFilter2 };
	}

	public AndFilter(Filter inFilter1)
	{
		fieldFilters = new Filter[] { inFilter1 };
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
	 * Determine whether the given object passes this filter by ANDing together
	 * all the sub-filters.
	 *
	 * @param inObj  The object to check
	 *
	 * @return <code>true</code> if the object passes all of the sub-filters,
	 * <code>false</code> otherwise.
	 */
	public boolean passes(Object inObj) throws FilterException
	{
		if( fieldFilters == null)
		{
			return true;
		}
		for (int i = 0; i < fieldFilters.length; i++)
		{
			if (!fieldFilters[i].passes(inObj))
			{
				return false;
			}
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see org.openedit.util.strainer.Filter#accept(org.openedit.util.strainer.FilterVisitor)
	 */
//	public void accept(FilterVisitor inFilterVisitor) throws FilterException
//	{
//		if (inFilterVisitor instanceof AndFilterVisitor)
//		{
//			((AndFilterVisitor) inFilterVisitor).visitAndFilter(this);
//		}
//	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		Filter[] filters = getFilters();
		for (int i = 0; i < filters.length; i++)
		{
			if (i > 0)
			{
				buffer.append(" and ");
			}
			buffer.append("(");
			buffer.append(filters[i].toString());
			buffer.append(")");
		}
		return buffer.toString();
	}
}
