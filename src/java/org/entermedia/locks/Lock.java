package org.entermedia.locks;

import java.util.Date;

import org.openedit.data.BaseData;
import org.openedit.util.DateStorageUtil;

public class Lock extends BaseData
{
	public String getOwnerId()
	{
		return get("ownerid");
	}
	public void setOwnerId(String inOwnerId)
	{
		setProperty("ownerid", inOwnerId);
	}
	public String getPath()
	{
		return get("path");
	}
	public void setPath(String inPath)
	{
		setProperty("path", inPath);
	}
	public Date getDate()
	{
		String date = get("date");
		Date thedate = DateStorageUtil.getStorageUtil().parseFromStorage(date);
		return thedate;
	}
	public void setDate(Date inDate)
	{
		String date = DateStorageUtil.getStorageUtil().formatForStorage(inDate);
		setProperty("date", date);
	}
	public boolean isOwner(String inNodeId, String inOwnerId)
	{
		boolean owner = inOwnerId.equals(getOwnerId());
		if(owner)
		{
			if( inNodeId.equals(getNodeId()) )
			{
				return true;
			}
		}
		return false;
	}
	public String getName()
	{
		return getPath();
	}
	public void setNodeId(String inId)
	{
		setProperty("nodeid", inId);
	}
	public String getNodeId()
	{
		return get("nodeid");
	}
}
