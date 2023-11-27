package org.openedit.hittracker;

import java.util.List;

public class Links
{
	List fieldBefore;
	public List getBefore()
	{
		return fieldBefore;
	}
	public void setBefore(List inBefore)
	{
		fieldBefore = inBefore;
	}
	public int getCurrentPosition()
	{
		return fieldCurrentPosition;
	}
	public void setCurrentPosition(int inCurrentPosition)
	{
		fieldCurrentPosition = inCurrentPosition;
	}
	List fieldAfter;
	public List getAfter()
	{
		return fieldAfter;
	}
	public void setAfter(List inAfter)
	{
		fieldAfter = inAfter;
	}
	int fieldCurrentPosition;
	
}
