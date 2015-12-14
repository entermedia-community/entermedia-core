package org.openedit.util.strainer;

/**
 * This interface represents a filter which delegates to multiple other
 * filters.
 *
 * @author Eric Galluzzo
 */
public interface CompositeFilter extends Filter
{
	/**
	 * Retrieve this filter's sub-filters.
	 *
	 * @return  This filter's sub-filters
	 */
	public Filter[] getFilters();
	
	/**
	 * Set this filter's sub-filters.
	 * 
	 * @param newFilters  The new sub-filters
	 * 
	 * @throws FilterException
	 *     If the new sub-filters are not compatible with this filter
	 */
	public void setFilters( Filter[] newFilters ) throws FilterException;
}
