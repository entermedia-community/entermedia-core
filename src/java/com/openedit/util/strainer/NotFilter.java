/*
 * $Id: NotFilter.java,v 1.6 2009/04/16 19:26:50 axis Exp $
 */
package com.openedit.util.strainer;

/**
 * This class implements the inverse of its sub-filter.
 * 
 * @author Eric Galluzzo
 */
public class NotFilter extends BaseFilter implements DecoratorFilter 
{
	protected Filter fieldFilter = null;

	/**
	 * This constructor should only be used for JavaBean-style creation.
	 */
	public NotFilter()
	{
	}

	/**
	 * Create a filter that inverts the given sub-filter.
	 * 
	 * @param inFilter  The filter to invert
	 */
	public NotFilter(Filter inFilter)
	{
		fieldFilter = inFilter;
	}

	/**
	 * Retrieve this filter's sub-filter.
	 * 
	 * @return  This filter's sub-filter
	 */
	public Filter getFilter()
	{
		return fieldFilter;
	}

	/* (non-Javadoc)
	 * @see com.openedit.util.strainer.Filter#getFilters()
	 */
	public Filter[] getFilters()
	{
		Filter[] array = new Filter[1];
		array[0] = fieldFilter;
		return array;
	}

	/**
	 * Set this filter's sub-filter.
	 * 
	 * @param newFilter  The new filter
	 */
	public void setFilter(Filter newFilter)
	{
		fieldFilter = newFilter;
	}
	
	/**
	 * Set this filter's sub-filter.
	 * 
	 * @param newFilter  The new filter
	 */
	public void addFilter(Filter newFilter)
	{
		fieldFilter = newFilter;
	}

	/**
	 * Determine whether the given object passes this filter by returning the
	 * opposite of its sub-filter.
	 *
	 * @param inObj  The object to check
	 *
	 * @return <code>true</code> if the object passes, <code>false</code>
	 * otherwise.
	 *
	 * @exception FilterException
	 * If some error occurs while filtering
	 * @exception ClassCastException
	 * If the given object is not of the expected type
	 */
	public boolean passes(Object inObj) throws FilterException, ClassCastException
	{
		if ( fieldFilter == null)
		{
			return false;
		}
		return !fieldFilter.passes(inObj);
	}

	/* (non-Javadoc)
	 * @see com.openedit.util.strainer.Filter#accept(com.openedit.util.strainer.FilterVisitor)
	 */
	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		
		buf.append("Not (");
		if (getFilter() != null)
		{
			buf.append(getFilter().toString());
		}
		buf.append(")");
		return buf.toString();
	}
}
