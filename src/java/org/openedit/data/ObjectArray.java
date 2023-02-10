package org.openedit.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;



public class ObjectArray
{
	protected Collection<ValuesMap> fieldValueMaps;
	
	public ObjectArray(Collection inGenericMaps)
	{
		for (Iterator iterator = inGenericMaps.iterator(); iterator.hasNext();)
		{
			Map object = (Map) iterator.next();
			getMaps().add(new ValuesMap(object));
		}
	}

	public Collection<ValuesMap> getMaps()
	{
		if (fieldValueMaps == null)
		{
			fieldValueMaps = new ArrayList<>();
		}
		return fieldValueMaps;
	}

	public void setMaps(Collection<ValuesMap> inMaps)
	{
		fieldValueMaps = inMaps;
		
	}
	
	
}
