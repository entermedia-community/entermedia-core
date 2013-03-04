package org.entermedia.cache;

import java.util.HashMap;
import java.util.Map;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class CacheManager
{
	protected Map<String,Map> fieldCaches;
	
	public Map<String,Map> getCaches()
	{
		if (fieldCaches == null)
		{
			fieldCaches = new HashMap<String,Map>();
		}
		return fieldCaches;
	}

	public Map createCache(int inTargetSize)
	{
		Cache<String, Object> map = CacheBuilder.newBuilder()
			       .maximumSize(inTargetSize)
			       .build();
		return map.asMap();
	}

	public Object get(String inType, String inId)
	{
		Map cache = getCaches().get(inType);
		if( cache == null )
		{
			synchronized (getCaches())
			{
				cache = getCaches().get(inType);
				if( cache == null )
				{
					cache = createCache(1000);
					getCaches().put(inType,cache);
				}
			}
		}
		return cache.get(inId);
	}

	public void put(String inType, String inKey, Object inValue)
	{
		Map cache = getCaches().get(inType);
		cache.put(inKey, inValue);
	}

	public void remove(String inType, String inKey)
	{
		Map cache = getCaches().get(inType);
		if( cache != null )
		{
			cache.remove(inKey);
		}
	}

	public void clear(String inType)
	{
		Map cache = getCaches().get(inType);
		cache.clear();
	}
}
