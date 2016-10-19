package org.openedit.users.authenticate;

import java.util.HashMap;
import java.util.Map;

import org.openedit.CatalogEnabled;
import org.openedit.users.User;

public class AuthenticationRequest implements CatalogEnabled
{
	protected User fieldUser;
	protected String fieldPassword;
	protected Map fieldProperties;
	protected String fieldCatalogId;
	
	public String getCatalogId()
	{
		return fieldCatalogId;
	}
	public void setCatalogId(String inCatalogId)
	{
		fieldCatalogId = inCatalogId;
	}
	public User getUser()
	{
		return fieldUser;
	}
	public void setUser(User inUser)
	{
		fieldUser = inUser;
	}
	public String getPassword()
	{
		return fieldPassword;
	}
	public void setPassword(String inPassword)
	{
		fieldPassword = inPassword;
	}
	public Map getProperties()
	{
		if (fieldProperties == null)
		{
			fieldProperties = new HashMap();
		}
		return fieldProperties;
	}
	public void setProperties(Map inProperties)
	{
		fieldProperties = inProperties;
	}
	public String get(String inKey)
	{
		if( fieldProperties != null)
		{
			return (String)fieldProperties.get(inKey);
		}
		return null;
	}
	public String getUserName()
	{
		return getUser().getUserName();
	}
	public void putProperty(String inKey, String inValue)
	{
		getProperties().put( inKey, inValue);
	}
	
}
