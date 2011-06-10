/*
 * Created on Jun 7, 2005
 */
package com.openedit.users;

import java.util.Comparator;

/**
 * @author cburkey
 *
 */
public class GroupComparator implements Comparator
{

	/* (non-javadoc)
	 * @see java.util.Comparator#compare(T, T)
	 */
	public int compare(Object inO1, Object inO2)
	{
		return String.valueOf( inO1 ).toLowerCase().compareTo(String.valueOf(inO2).toLowerCase());
	}

}
