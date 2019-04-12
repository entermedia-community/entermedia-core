package org.openedit.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openedit.Data;
import org.openedit.MultiValued;
import org.openedit.util.DateStorageUtil;

public class ValuesMap extends HashMap
{
	public static final Object NULLVALUE = new Object(); 
	public ValuesMap()
	{
		super(5);
	}
	public ValuesMap(Map inMap)
	{
		putAll(inMap);
	}
	/**
	 * Thinking to not override the nullvalue check here
	 * @param inKey
	 * @return
	 */
	public Object getObject(String inKey)
	{
		Object obj = super.get(inKey);
//TODO: Should we just do this here?
//		if( obj == null || obj == NULLVALUE)
//		{
//			return null;
//		}
		return obj;
	}
	
	public Object get(String inKey)
	{
		return getString(inKey);
	}
	
	public void removeValue(String inKey, Object inOldValue)
	{
		Object val = getObject(inKey);
		if( val == null || val == NULLVALUE)
		{
			return;
		}
		if( val instanceof Collection)
		{
			Collection vals = (Collection)val;
			if( vals.contains(inOldValue))
			{
				vals = new ArrayList(vals);
				vals.remove(inOldValue);
				put(inKey, vals);
			}
		}
		else
		{
			//remove(inKey);
			put( inKey, NULLVALUE);
		}
	}
	public void addValue(String inKey, Object inNewValue)
	{
		Object values = getObject(inKey);
		if(values == null )
		{
			ArrayList valuesa = new ArrayList();
			valuesa.add(inNewValue);
			values = valuesa;
			put(inKey, values);
		}
		else if( !(values instanceof Collection ) )
		{
			put(inKey, inNewValue);
		}
		else
		{
			Collection valuesa = new ArrayList((Collection)values);
			if( !valuesa.contains(inNewValue))
			{
				valuesa.add(inNewValue);
			}
			put(inKey, valuesa);
		}
	}
//	@Override
//	public Object get(Object inKey)
//	{
//		Object val =  super.get(inKey);
//		if( val == NULLVALUE)
//		{
//			val = null;
//		}
//		return val;
//	}
	@Override
	public Object put(Object inArg0, Object inArg1)
	{
		if( inArg1 == null)
		{
			inArg1 = NULLVALUE;
		}
		return super.put(inArg0, inArg1);
	}
	
	public Collection<String> getValues(String inPreference)
	{
		Object object = getObject(inPreference);
		if( object == null || object == NULLVALUE)
		{
			return null;
		}
		if( object instanceof Collection)
		{
			return (Collection<String>)object;
		}
		String val = getString(inPreference);
		
		if (val == null)
		{
			return null;
		}
		String[] vals = null;
		if( val.contains("|") )
		{
			vals = MultiValued.VALUEDELMITER.split(val);
		}
		else
		{
			vals = new String[]{val};
		}
		Collection collection = Arrays.asList(vals);
		return collection;
	}
	public Collection getObjects(String inPreference)
	{
		Object object = getObject(inPreference);
		if( object == null || object == NULLVALUE)
		{
			return null;
		}
		if( object instanceof Collection)
		{
			return (Collection)object;
		}
		if( object instanceof String)
		{
			String val = String.valueOf( object );
			String[] vals = null;
			if( val.contains("|") )
			{
				vals = MultiValued.VALUEDELMITER.split(val);
			}
			else
			{
				vals = new String[]{val};
			}
			Collection collection = Arrays.asList(vals);
			return collection;
		}
		else
		{
			Collection one = new ArrayList(1);
			one.add(object);
			return one;
		}
	}
	public String getString(String inKey)
	{
		Object object = getObject(inKey);
		return toString(object);
	}
	public boolean getBoolean(String inId)
	{
		Object val = getObject(inId);
		if( val == null || val == NULLVALUE)
		{
			return false;
		}
		if( val instanceof Boolean)
		{
			return (boolean)val;
		}
		return Boolean.valueOf(val.toString());
		
	}
	
	
	public Object getValue(String inId)
	{
		Object val = getObject(inId);
		if( val == null || val == NULLVALUE)
		{
			return null;
		}
		return val;
		
	}
	
	
	
	public float getFloat(String inId)
	{
		String val = getString(inId);
		if( val != null)
		{
			return Float.parseFloat(val);
		}
		return 0;
	}

	
	public Double getDouble(String inId)
	{
		Object val = getObject(inId);
		if( val != null)
		{
			if( val instanceof Double)
			{
				return (Double)val;
			}
			return Double.parseDouble(getString(inId));
		}
		return null;
	}

	public BigDecimal getBigDecimal(String inKey)
	{
		String val = getString(inKey);
		if( val == null || val.contains(".") )
		{
			return new BigDecimal(0);
		}
		return new BigDecimal(val);
	}
	public Date getDate(String inField)
	{
		Object val = getObject(inField);
		if( val == null || val == NULLVALUE)
		{
			return null;
		}
		if( val instanceof Date)
		{
			return (Date)val;
		}
		//??
		Date date = DateStorageUtil.getStorageUtil().parseFromStorage((String)val);
		return date;
	}
	
	public Date getDate(String inField, String inDateFormat)
	{
		Object val = getObject(inField);
		if( val == null || val == NULLVALUE)
		{
			return null;
		}
		if( val instanceof Date)
		{
			return (Date)val;
		}
		String date = (String)val;
		if (date != null)
		{
			Date dateval = DateStorageUtil.getStorageUtil().parse((String)val, inDateFormat);
			return dateval;
		}
		return null;
	}
	public String toString(Object object)
	{
		if (object == null || object == NULLVALUE) {
			return null;
		}
		if (object instanceof String) {
			return (String) object;
		}
		if (object instanceof Date) {
			return DateStorageUtil.getStorageUtil().formatForStorage(((Date)object));
		}
		if (object instanceof Boolean) {
			return String.valueOf((Boolean) object);
		}
		if (object instanceof Integer) {
			return String.valueOf((Integer) object);
		}
		if (object instanceof Float) {
			return String.valueOf((Float) object);
		}
		if( object instanceof Collection)
		{
			StringBuffer values = new StringBuffer();
			Collection existingvalues = (Collection)object;
			for (Iterator iterator = existingvalues.iterator(); iterator.hasNext();)
			{
				Object detail = (Object) iterator.next();
				if( detail instanceof Data)
				{
					Data data = (Data)detail;
					values.append(data.getId());
				}
				else
				{
					values.append(String.valueOf( detail ) );
				}
				if( iterator.hasNext())
				{
					values.append("|");
				}
			}
			return values.toString();
		}
		return String.valueOf(object);
	}

	@Override
	public void putAll(Map inArg0)
	{
		if( inArg0 instanceof ValuesMap)
		{
			ValuesMap map = (ValuesMap)inArg0;
			for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();)
			{
				String key = (String) iterator.next();
				put( key, map.getObject(key));
			}
		}
		else
		{
			super.putAll(inArg0);
		}
	}
	public Long getLong(String inField)
	{
		Object val = getObject(inField);
		if( val == null || val == NULLVALUE)
		{
			return null;
		}
		if( val instanceof Long)
		{
			return (Long)val;
		}
		if( val instanceof Integer)
		{
			return ((Integer)val).longValue();
		}
		if( val instanceof Double && inField.contains("timecode"))
		{
			Double d = (Double)val;
			Long newval = Math.round( d * 1000d);
			return newval;
		}
		long l = Long.parseLong(String.valueOf( val ));
		
		return l;
	}
	
}
