package org.openedit.locks;

import java.util.Date;

import org.openedit.data.BaseData;
import org.openedit.data.SaveableData;
import org.openedit.util.DateStorageUtil;

public class Lock extends BaseData implements SaveableData
{
	public String getOwnerId()
	{
		return get("ownerid");
	}
	public void setOwnerId(String inOwnerId)
	{
		setProperty("ownerid", inOwnerId);
	}
	public Date getDate()
	{
		String date = get("date");
		Date thedate = DateStorageUtil.getStorageUtil().parseFromStorage(date);
		return thedate;
	}
	public void setDate(Date inDate)
	{
		setValue("date", inDate);
	}
	public boolean isLockedBy(String inNodeId, String inOwnerId)
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
		return getSourcePath();
	}
	public void setNodeId(String inId)
	{
		setValue("nodeid", inId);
	}
	public String getNodeId()
	{
		return get("nodeid");
	}
	
	public String getVersion()
	{
		return get("version");
	}

	public boolean isLocked()
	{
		return getBoolean("locked");
	}

	public void setLocked(boolean isLocked)
	{
		setProperty("locked", String.valueOf(isLocked));
	}
	
}
