package org.openedit.util.strainer;

/**
 * This interface represents a filter which delegates to exactly one other
 * filter.
 *
 * @author Eric Galluzzo
 */
public interface DecoratorFilter extends Filter
{
	/**
	 * Retrieve this filter's sub-filter.
	 * 
	 * @return  This filter's sub-filter
	 */
	public Filter getFilter();
	
	/**
	 * Set this filter's sub-filter.
	 * 
	 * @param newFilter  The new filter
	 */
	public void setFilter( Filter newFilter );
}
