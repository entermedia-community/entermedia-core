package org.openedit.data;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openedit.Data;
import org.openedit.hittracker.ListHitTracker;

public class BaseDataHitTracker extends ListHitTracker
{
	
	public BaseDataHitTracker()
	{
		// TODO Auto-generated constructor stub
	}
	public BaseDataHitTracker(List inList)
	{
		super(inList);
	}
	public Iterator iterator()
	{
		return new BaseDataIterator(getList());
	}
	public Data get(int inCount)
	{
		Map element = (Map)getList().get(inCount);
		if( element == null)
		{
			return null;
		}
		return new BaseData(element);
	}

	public Data toData(Object inHit)
	{
		if( inHit instanceof Data)
		{
			return (Data)inHit;
		}
		return new BaseData((Map)inHit);
	}
	
	
}
