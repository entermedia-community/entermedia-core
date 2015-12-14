package org.openedit.util.strainer;

import org.openedit.util.PathUtilities;


public class PathMatchesFilter extends BaseFilter
{
	
	public PathMatchesFilter()
	{
		// TODO Auto-generated constructor stub
	}
	public PathMatchesFilter(String inValue)
	{
		setValue(inValue);
	}
	
	

	public boolean passes(Object inObj) throws FilterException, ClassCastException
	{
		boolean matches = PathUtilities.match(inObj.toString(), getValue());
		return matches;
	}
	
	public String toString() 
	{
		return "Path=" + getValue();
	}

}
