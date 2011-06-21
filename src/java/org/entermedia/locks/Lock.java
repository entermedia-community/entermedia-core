package org.entermedia.locks;

import java.util.Date;

public class Lock
{
	protected String fieldOwnerId;
	protected String fieldPath;
	protected Date fieldDate;

	public String getOwnerId()
	{
		return fieldOwnerId;
	}
	public void setOwnerId(String inOwnerId)
	{
		fieldOwnerId = inOwnerId;
	}
	public String getPath()
	{
		return fieldPath;
	}
	public void setPath(String inPath)
	{
		fieldPath = inPath;
	}
	public Date getDate()
	{
		return fieldDate;
	}
	public void setDate(Date inDate)
	{
		fieldDate = inDate;
	}
	public boolean isOwner(String inOwnerId)
	{
		return inOwnerId.equals(getOwnerId());
	}
}
