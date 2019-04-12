/*
 * Created on Jan 13, 2005
 */
package org.openedit.util.strainer;


/**
 * @author cburkey
 *
 */
public class BlankFilter extends BaseFilter
{

	/* (non-javadoc)
	 * @see org.openedit.util.strainer.Filter#passes(java.lang.Object)
	 */
	public boolean passes(Object inObj) throws FilterException, ClassCastException
	{
		return true;
	}

	/* (non-javadoc)
	 * @see org.openedit.util.strainer.Filter#accept(org.openedit.util.strainer.FilterVisitor)
	 */
	public String toString() {
		return "true";
	}

}
