package org.openedit.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class CacheManager
{
	public static final String NULLVALUE = new String();
	public static final Object NULLOBJECT = new Object();
	
	protected Map<String,Cache> fieldCaches;
	protected int fieldExpiryTime = -1;//minutes
	
	public int getExpiryTime()
	{
		return fieldExpiryTime;
	}

	public void setExpiryTime(int inExpiryTime)
	{
		fieldExpiryTime = inExpiryTime;
	}

	public Map<String,Cache> getCaches()
	{
		if (fieldCaches == null)
		{
			fieldCaches = new HashMap<String,Cache>();
		}
		return fieldCaches;
	}

	protected Cache createCache(int inTargetSize)
	{
		Cache<String, Object> map = null;
		
		if(fieldExpiryTime == -1)
		{
			map = CacheBuilder.newBuilder()
					       .maximumSize(inTargetSize)
	//				       .expireAfterWrite(15, TimeUnit.MINUTES)
					       .build();
		} 
		else
		{
			map = CacheBuilder.newBuilder()
				       .maximumSize(inTargetSize)
				       .expireAfterWrite(fieldExpiryTime, TimeUnit.MINUTES)
				       .build();
		}
		
		return map;
	}
	
	
	
	

	public Object get(String inType, String inId)
	{
		Cache cache = getCaches().get(inType);
		if( cache == null )
		{
			return null;
		}
		return cache.getIfPresent(inId);
	}

	public void put(String inType, String inKey, Object inValue)
	{
		Cache cache = getCaches().get(inType);
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
		if(inKey != null && inValue != null){
		cache.put(inKey, inValue);
		}
	}

	
	public void put(String inType, String inKey, Object inValue, int size)
	{
		Cache cache = getCaches().get(inType);
		if( cache == null )
		{
			synchronized (getCaches())
			{
				cache = getCaches().get(inType);
				if( cache == null )
				{
					cache = createCache(size);
					getCaches().put(inType,cache);
				}
			}
		}
		cache.put(inKey, inValue);
	}
	
	
	public void remove(String inType, String inKey )
	{
		Cache cache = getCaches().get(inType);
		if( cache != null )
		{
			cache.invalidate(inKey);
		}
	}
	public void removeAll(String inType, Collection inAll)
	{
		Cache cache = getCaches().get(inType);
		if( cache != null )
		{
			cache.invalidateAll(inAll);
		}
	}

	public void clear(String inType)
	{
		Cache cache = getCaches().get(inType);
		if(cache == null){
			return;
		}
		cache.invalidateAll();
	}
	
	public void clearAll(){
		for (Iterator iterator = getCaches().keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			Cache cache = (Cache) getCaches().get(key); 
			cache.invalidateAll();
		}
	}
	
}
