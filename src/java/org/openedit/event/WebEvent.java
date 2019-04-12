package org.openedit.event;

import java.util.Date;

import org.openedit.data.BaseData;
import org.openedit.users.User;

public class WebEvent extends BaseData
{ 
	protected String fieldId; //optional
	protected String fieldSearchType;  //user search
	protected String fieldOperation;  //login logout
	protected Date fieldDate = new Date(); //defaults to "Right Now"
	protected Object fieldSource;
	protected User fieldUser;
	protected boolean fieldCancelEvent;
	
	public boolean isCancelEvent()
	{
		return fieldCancelEvent;
	}
	public void setCancelEvent(boolean inCancelEvent)
	{
		fieldCancelEvent = inCancelEvent;
	}
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
	
}
