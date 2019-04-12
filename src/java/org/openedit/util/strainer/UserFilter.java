/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

package org.openedit.util.strainer;

import org.openedit.WebPageRequest;
import org.openedit.users.User;
import org.openedit.util.Replacer;


/**
 * This filter only passes users whose names match a given name.
 *
 * @author Eric Galluzzo
 */
public class UserFilter extends BaseFilter
{

	/**
	 * Construct a filter that passes all users.
	 *
	 * @see #setUsername(String)
	 */
	public UserFilter()
	{
		super();
	}

	/**
	 * Construct a filter that only passes users with the given username.
	 *
	 * @param inUsername The username to pass
	 */
	public UserFilter(String inUsername)
	{
		setUsername(inUsername);
	}

	/**
	 * Sets the username.
	 *
	 * @param username The username to set
	 */
	public void setUsername(String username)
	{
		setValue(username);
	}

	/**
	 * Returns the username.
	 *
	 * @return String
	 */
	public String getUsername()
	{
		return fieldValue;
	}

	/**
	 * @see org.openedit.util.strainer.Filter#passes(java.lang.Object)
	 */
	public boolean passes(Object inObj) throws FilterException, ClassCastException
	{
		WebPageRequest req = (WebPageRequest) inObj;

		User user = req.getUser();

		if (user != null)
		{
			if ((getUsername() == null) || user.getUserName().equalsIgnoreCase(getUsername()))
			{
				return true;
			}
			if( getUsername().startsWith("${"))
			{
				String username = new Replacer().replace(getUsername(), req.getPageMap());
				 if( user.getUserName().equalsIgnoreCase(username) )
				 {
					 return true;
				 }
			}
		}
		return false;
	}
	public String toString() 
	{
		if( getUsername() == null)
		{
			return "User";
		}
		return "User=" + getUsername();
	}
	public boolean equals(Object inObj)
	{
		if (inObj instanceof UserFilter)
		{
			UserFilter toCompare = (UserFilter)inObj;
			return getUsername().equals(toCompare.getUsername());
		}
		return false;
	}
}
