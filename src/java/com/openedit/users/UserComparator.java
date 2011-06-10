/*
 * Created on Jun 7, 2005
 */
package com.openedit.users;

import java.util.Comparator;

/**
 * @author cburkey
 *
 */
public class UserComparator implements Comparator
{

	/* (non-javadoc)
	 * @see java.util.Comparator#compare(T, T)
	 */
	public int compare(Object inO1, Object inO2)
	{
		User low = (User)inO1;
		User high = (User)inO2;
		
		String lowlast = low.getLastName();
		String highlast = high.getLastName();
		
		if ( lowlast == null)
		{
			lowlast = "";
		}
		if ( highlast == null)
		{
			highlast = "";
		}
		int i = lowlast.compareTo(highlast);
		if ( i == 0)
		{
			return checkFirst(low.getFirstName(),high.getFirstName());
		}
		else
		{
			return i;
		}
	}

	/**
	 * @param inFirstName
	 * @param inFirstName2
	 * @return
	 */
	private int checkFirst(String inFirstName, String inFirstName2)
	{
		if ( inFirstName ==null)
		{
			inFirstName = "";
		}
		if ( inFirstName2 == null)
		{
			inFirstName2 = "";
		}
		return inFirstName.compareTo(inFirstName2);
	}

}
