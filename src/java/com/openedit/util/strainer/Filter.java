package com.openedit.util.strainer;

/**
 * This interface supplies a contract for determining whether an object passes
 * certain criteria.  It should be implemented by multiple subclasses, each of
 * which implements a particular search criterion.  Filters can be combined via
 * combinatorial filters ({@link AndFilter}, {@link OrFilter}, etc.).
 * 
 * @author Eric Galluzzo
 */
public interface Filter
{
	/**
	 * Determine whether the given object passes this filter.
	 *
	 * @param inObj  The object to check
	 *
	 * @return <code>true</code> if the object passes, <code>false</code>
	 *         otherwise.
	 *
	 * @exception FilterException     If some error occurs while filtering
	 * @exception ClassCastException  If the given object is not of the
	 *                                expected type
	 */
	boolean passes(Object inObj) throws FilterException;

	/**
	 * Accept the given filter visitor as per the Acyclic Visitor pattern, which
	 * is based on the standard Gang of Four Visitor pattern.
	 * 
	 * @param inFilterVisitor  The visitor to accept
	 * 
	 * @throws FilterException
	 * If the visitor threw an exception
	 */
//	void accept(FilterVisitor inFilterVisitor) throws FilterException;

	/**
	 * List any child filters
	 * @author sgonsa
	 * @return may return null if there are not children
	 *
	 */
	Filter[] getFilters();
	
	
	String getType();

	void removeFilter(Filter inNode);
	void addFilter(Filter inNode);

	void setValue(String inValue);

	Filter copy(String inName);

	void setProperty(String inKey, String inValue);

}
