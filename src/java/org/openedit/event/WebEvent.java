package org.openedit.event;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openedit.Data;
import org.openedit.MultiValued;
import org.openedit.users.User;

public class WebEvent implements Data, MultiValued
{ 
	protected String fieldId; //optional
	protected String fieldSearchType;  //user search
	protected String fieldOperation;  //login logout
	protected Date fieldDate = new Date(); //defaults to "Right Now"
	protected Map fieldProperties;
	protected Object fieldSource;
	protected User fieldUser;
	/**
	 * Warning this may not be set. use getUserName to be sure
	 * @return
	 */
	public User getUser()
	{
		return fieldUser;
	}
	public void setUser(User inUser)
	{
		fieldUser = inUser;
		if( inUser != null)
		{
			setUsername(inUser.getUserName());
		}
	}
	protected String fieldUsername;
	
	public WebEvent() {
	}
	public String getSearchType()
	{
		return fieldSearchType;
	}
	public void setSearchType(String inType)
	{
		fieldSearchType = inType;
	}

	/**
	 * @deprecated use setProperty
	 * @param inKey
	 * @param inValue
	 */
	
	public void addDetail( String inKey, String inValue)
	{
		setProperty(inKey, inValue);
	}
	public String get( String inKey)
	{
		return (String)getProperties().get(inKey);
	}

	public void setProperties(Map inDetails)
	{
		fieldProperties = inDetails;
	}
	public Object getSource()
	{
		return fieldSource;
	}
	public void setSource(Object inSource)
	{
		fieldSource = inSource;
	}
	public String getOperation()
	{
		return fieldOperation;
	}
	public void setOperation(String inOperation)
	{
		fieldOperation = inOperation;
	}
	public String getUsername()
	{
		return fieldUsername;
	}
	public void setUsername(String inUser)
	{
		fieldUsername = inUser;
	}
	public String getCatalogId()
	{
		return get("catalogid");
	}
	public void setCatalogId(String inCatalogId)
	{
		setProperty("catalogid", inCatalogId);
	}
	public Date getDate()
	{
		return fieldDate;
	}
	public void setDate(Date inDate)
	{
		fieldDate = inDate;
	}
	public String getName()
	{
		return getSearchType();
	}
	public void setName(String inName)
	{
		setSearchType(inName);
	}
	public void setProperty(String inId, String inValue)
	{
		getProperties().put( inId, inValue);
	}
	public String getId()
	{
		return fieldId;
	}
	public void setId(String inId)
	{
		fieldId = inId;
	}
	//part of the API
	public String getSourcePath()
	{
		return get("sourcepath");
	}
	/** This is more like a path */
	public void setSourcePath(String inSourcepath)
	{
		setProperty("sourcepath", inSourcepath);		
	}
	
	public Map getProperties() 
	{
		if (fieldProperties == null)
		{
			fieldProperties = new HashMap();
		}
		return fieldProperties;
	}
	public Collection getValues(String inPreference)
	{
		String val = get(inPreference);
		
		if (val == null)
		{
			return null;
		}
		String[] vals = null;
		if( val.contains("|") )
		{
			vals = VALUEDELMITER.split(val);
		}
		else
		{
			vals = val.split("\\s+"); //legacy
		}

		Collection collection = Arrays.asList(vals);
		//if null check parent
		return collection;
	}
	
	public void setValues(String inKey, Collection<String> inValues)
	{
		StringBuffer values = new StringBuffer();
		for (Iterator iterator = inValues.iterator(); iterator.hasNext();)
		{
			String detail = (String) iterator.next();
			values.append(detail);
			if( iterator.hasNext())
			{
				values.append(" | ");
			}
		}
		setProperty(inKey,values.toString());
	}
}
