/*
 * Created on May 2, 2004
 */
package com.openedit.hittracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openedit.Data;


/**
 * @author cburkey
 *
 */
public class ListHitTracker extends HitTracker
{
	protected List fieldList;
	protected String fieldSessionId;
	public String getSessionId()
	{
		if( fieldSessionId == null)
		{
			return super.getSessionId();
		}
		return fieldSessionId;
	}

	public void setSessionId(String inSessionId)
	{
		fieldSessionId = inSessionId;
	}

	public ListHitTracker()
	{
		
	}
	
	public ListHitTracker(List inHits)
	{
		setList(inHits);
	}
	
	public List getList()
	{
		if (fieldList == null)
		{
			fieldList = new ArrayList();
		}
		return fieldList;
	}
	
	public void setList(List inObjects)
	{
		fieldList = inObjects;
		setPage(1);
	}
	
	public boolean add(Object inObject)
	{
		return getList().add(inObject);
	}
	
	public boolean addAll(Collection inCollection)
	{
		return getList().addAll(inCollection);
	}
	
	public int size()
	{
		if ( getList() == null )
		{
			return 0;
		}
		else
		{
			return getList().size();
		}
	}

//	public String get(String inId)
//	{
//		for (Iterator iterator = iterator(); iterator.hasNext();)
//		{
//			Object obj = iterator.next();
//			if( obj instanceof Data)
//			{
//				Data data = (Data)obj;
//				if( data.getId().equals(inId))
//				{
//					return data.getN;
//				}
//			}
//			else
//			{
//				String key = getKey(obj);
//				if( key != null && key.equals(inId))
//				{
//					if( obj instanceof Map)
//					{
//						Map map = (Map)obj;
//						return (String)map.get("name");
//					}
//				}
//			}
//		}
//		return null;
//	}
	
	public Data get(int count)
	{
		return (Data)getList().get(count);
	}
	
	public Object get(String inId) throws IOException
	{
		return getById(inId);
	}

	public Iterator iterator()
	{
		return getList().iterator();
	}

	public boolean contains(Object inHit)
	{
		return getList().contains(inHit);
	}


	public void clear()
	{
		// TODO Auto-generated method stub
		
	}

	//Remaining API are not implemented
	public List keys()
	{
		List keys = new ArrayList();
		for (Iterator iterator = iterator(); iterator.hasNext();)
		{
			Object obj =  iterator.next();
			String key = getKey(obj);
			keys.add(key);
		}
		return keys;
	}

	protected String getKey(Object obj)
	{
		if( obj instanceof Map)
		{
			Map map = (Map)obj;
			return (String)map.get("id");
		}
		else if( obj instanceof Data)
		{
			Data data = (Data)obj;
			return data.getId();
		}
		return null;
	}

	
	public boolean containsAll(Collection arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEmpty()
	{
		return size() == 0;
	}

	public boolean remove(Object inO)
	{
		return getList().remove(inO);
	}

	public boolean removeAll(Collection arg0)
	{
		return getList().removeAll(arg0);
	}

	public boolean retainAll(Collection arg0)
	{
		return getList().retainAll(arg0);
	}

	public Object[] toArray()
	{
		return getList().toArray();
	}

	public Object[] toArray(Object[] arg0)
	{
		return getList().toArray(arg0);
	}
	public Data toData(Object inHit)
	{
		if( inHit instanceof Data)
		{
			return (Data)inHit;
		}
		throw new IllegalArgumentException("Not implemented");
	}

	public String getValue(Object inHit, String inKey)
	{
		if( inHit instanceof Data)
		{
			Data data = (Data)inHit;
			return data.get(inKey);
		}
		return super.getValue(inHit, inKey);
	}
	
	public String getIndexId()
	{
		return null; //ListHitTrackers are always current
	}

}
