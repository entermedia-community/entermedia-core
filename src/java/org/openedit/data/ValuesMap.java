package org.openedit.data;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openedit.Data;
import org.openedit.MultiValued;
import org.openedit.OpenEditRuntimeException;
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
	public void removeValue(String inKey, Object inOldValue)
	{
		Object val = get(inKey);
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
		Object values = get(inKey);
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
		Object object = get(inPreference);
		if( object == null || object == NULLVALUE)
		{
			return null;
		}
		if( object instanceof Collection)
		{
			return (Collection<String>)object;
		}
		String val = (String)get(inPreference);
		
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

	public String getString(String inKey)
	{
		Object object = get(inKey);
		return toString(object);
	}
	public boolean getBoolean(String inId)
	{
		Object val = get(inId);
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
	
	public float getFloat(String inId)
	{
		String val = getString(inId);
		if( val != null)
		{
			return Float.parseFloat(val);
		}
		return 0;
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
		Object val = get(inField);
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
		Object val = get(inField);
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

	
}
