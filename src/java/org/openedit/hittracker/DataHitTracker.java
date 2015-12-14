package org.openedit.hittracker;

import java.io.IOException;
import java.util.Iterator;

import org.openedit.Data;

public class DataHitTracker<T> extends ListHitTracker 
{
	//Use getByID.
	public Object get(String inId) throws IOException
	{
		return getById(inId);
	}

	
	public Data get(int count)
	{
		return (Data)getList().get(count);
	}
	public Data getById(String inId)
	{
		if (inId == null)
		{
			return null;
		}
		for (Iterator iterator = getList().iterator(); iterator.hasNext();) {
			Object obj = (Object) iterator.next();
			if (obj instanceof Data)
			{
				String id = ((Data)obj).getId();
				if (inId.equals(id))
				{
					return (Data)obj;
				}
			}
		}
		return null;
	}
	
	public String getValue(Object inHit, String inString)
	{
		Data target = (Data)inHit;
		return target.get(inString);
		

	}
	
	

}
