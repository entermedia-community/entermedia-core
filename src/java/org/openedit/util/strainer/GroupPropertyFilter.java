package org.openedit.util.strainer;

import java.util.Iterator;

import org.openedit.WebPageRequest;
import org.openedit.users.Group;
import org.openedit.users.User;

/**
 * Checks for a particular property in all of a user's groups.
 * @author axis
 *
 */
public class GroupPropertyFilter extends BaseFilter
{
	protected String fieldPropertyName;
	
	public GroupPropertyFilter(String inPropertyName, String inValue)
	{
		setPropertyName(inPropertyName);
		setValue(inValue);
	}
	
	public String getPropertyName()
	{
		return fieldPropertyName;
	}
	
	public void setPropertyName(String inPropertyName)
	{
		fieldPropertyName = inPropertyName;
	}
	
	/**
	 * @see org.openedit.util.strainer.Filter#passes(java.lang.Object)
	 */
	public boolean passes(Object inObj) throws FilterException, ClassCastException
	{
		WebPageRequest req = (WebPageRequest) inObj;

		User data = req.getUser();

		if (data == null)
		{
			return false;
		}
		
		if (getValue() == null)
		{
			return true;
		}		
		for (Iterator iterator = data.getGroups().iterator(); iterator.hasNext();)
		{
			Group group = (Group) iterator.next();
			String value = group.get(getPropertyName());
			if (value != null && value.equals(getValue()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public String toString()
	{
		return "GroupProperty" + getPropertyName() + "="+ getValue();
	}
	
	public boolean equals(Object inObj)
	{
		if (inObj instanceof GroupPropertyFilter)
		{
			GroupPropertyFilter toCompare = (GroupPropertyFilter)inObj;
			return (getPropertyName().equals(toCompare.getPropertyName())) && (getValue().equals(toCompare.getValue()));
		}
		return false;
	}
}
