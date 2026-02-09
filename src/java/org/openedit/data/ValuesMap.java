package org.openedit.data;

import java.math.BigDecimal;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import org.json.simple.JSONObject;
import org.openedit.Data;
import org.openedit.MultiValued;
import org.openedit.modules.translations.LanguageMap;
import org.openedit.util.DateStorageUtil;



public class ValuesMap extends HashMap
{
	public static final Object NULLVALUE = new NullObject(); 
	public static final String NULLSTRING= "NULLSTRING";
	public static final Data NULLDATA = new NullData(); 
	
	public ValuesMap()
	{
		super(5);
	}
	public ValuesMap(Map inMap)
	{
		putAll(inMap);
	}
	/**
	 * May return null object
	 * @param inKey
	 * @return
	 */
	public Object get(Object inKey)
	{
		Object obj = getValue((String)inKey);
		return obj; 
	}
	
	public void removeValue(String inKey, Object inOldValue)
	{
		Object val = getValue(inKey);
		if( val == null)
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
	public Collection addValue(String inKey, Object inNewValue)
	{
		Collection values = getValues(inKey);
		if(values == null )
		{
			ArrayList valuesa = new ArrayList();
			valuesa.add(inNewValue);
			put(inKey, valuesa);
			values = valuesa;
		}
		else
		{
			if( !values.contains(inNewValue))
			{
				values.add(inNewValue);
			}
			put(inKey, values);
		}
		return (Collection)values;
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
		Object object = getValue(inPreference);
		if( object == null)
		{
			return null;
		}
		if( object instanceof Collection)
		{
			return (Collection<String>)object;
		}
		if( object instanceof String[])
		{
			Collection<String> values = Arrays.asList((String[])object);
			return values;
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
		Collection collection = new ArrayList(Arrays.asList(vals)); //To make it editable
		return collection;
	}
	
	public Collection gets(String inPreference)
	{
		Object object = get(inPreference);
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
			Collection collection = new ArrayList(Arrays.asList(vals));
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
		Object object = getValue(inKey);
		return toString(object);
	}
	public boolean getBoolean(String inId)
	{
		Object val = getValue(inId);
		if( val == null)
		{
			return false;
		}
		if( val instanceof Boolean)
		{
			return (boolean)val;
		}
		return Boolean.valueOf(val.toString());
		
	}
	
	public Object getObject(String inId)
	{
		Object val = super.get(inId);
		return val;
		
	}
	
	
	public Object getValue(String inId)
	{
		Object val = super.get(inId);
		if( val == null || val == NULLVALUE || val == NULLSTRING)
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
		Object val = getValue(inId);
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
		Object val = getValue(inField);
		if( val == null)
		{
			return null;
		}
		if( val instanceof Date)
		{
			return (Date)val;
		}
		Date date = DateStorageUtil.getStorageUtil().parseFromStorage((String)val);
		return date;
	}
	
	public Date getDate(String inField, String inDateFormat)
	{
		Object val = getValue(inField);
		if( val == null)
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
		if( object instanceof String[])
		{
			String[] vals = (String[])object;
			if( vals.length == 1)
			{
				return vals[0];
			}
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < vals.length; i++)
			{
				buf.append(vals[i]);
				if( i + 1 < vals.length)
				{
					buf.append("|");
				}
			}
			return buf.toString();
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
			Collection existingvalues = (Collection)object;
			if(existingvalues.isEmpty())
			{
				return null;
			}
			StringBuffer values = new StringBuffer();
			for (Iterator iterator = existingvalues.iterator(); iterator.hasNext();)
			{
				Object detail = (Object) iterator.next();
				if( detail instanceof Data)
				{
					Data data = (Data)detail;
					values.append(data.getId());
				}
				else if( detail instanceof Map)
				{
					JSONObject json = new JSONObject((Map) detail);
					values.append( json.toJSONString() );
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
		if( object instanceof LanguageMap)
		{
			LanguageMap lan = (LanguageMap)object;
			return lan.getText(null);
		}		
		if( object instanceof Map)
		{
			JSONObject values = new JSONObject((Map) object);
			return values.toJSONString();
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
				Object value = map.getValue(key);
				if( value != null && value != NULLVALUE)
				{
					put( key, value);
				}
			}
		}
		else
		{
			super.putAll(inArg0);
		}
	}
	public Long getLong(String inField)
	{
		Object val = getValue(inField);
		if( val == null)
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
	public boolean containsInValues(String inField, String inValue)
	{
		Collection values = getValues(inField);
		if( values == null)
		{
			return false;
		}
		return values.contains(inValue);
	}
	public Integer getInteger(String inField)
	{
		Object val = getValue(inField);
		if( val == null)
		{
			return 0;
		}
		if( val instanceof Long)
		{
			return Math.round((Long)val);
		}
		if( val instanceof Integer)
		{
			return ((Integer)val);
		}
		return Integer.parseInt((String)val);
	}
	
	public Map toMap()
	{
		ValuesMap newmap = new ValuesMap();
		for (Iterator iterator = keySet().iterator(); iterator.hasNext();)
		{
			Object key = (Object) iterator.next();
			Object value = get(key);
			if( value != null && value != NULLVALUE && value != NULLSTRING)
			{
				newmap.put(key, value);
			}
		}
		return newmap;
	}
	
	
	public Set keySet() 
	{
		Set set = new HashSet();
		Set superset =super.keySet();
		for (Iterator iterator = superset.iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			/*
			if( key.endsWith("_int"))
			{
				key = key.substring(0, key.length() - 4);
			}
			*/
			if  ( getValue(key) != null) 
			{
				set.add(key);
			}
		}
		return set;
	}
	
	@Override
	public Set<Map.Entry<Object, Object>> entrySet() 
	 {
		 List<Object> keys = new ArrayList<>(keySet());
		 
        return new AbstractSet<Map.Entry<Object, Object>>() {

            @Override
            public Iterator<Map.Entry<Object, Object>> iterator() {
                return new Iterator<Map.Entry<Object, Object>>() {
                    private int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index < keys.size();
                    }

                    @Override
                    public Map.Entry<Object, Object> next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        return new ValueEntry(keys, index++);
                    }
                };
            }

            @Override
            public int size() {
                return keys.size();
            }
        };
    }

    /* ---------------- Entry Implementation ---------------- */

    private class ValueEntry implements Map.Entry<Object, Object> {
        private final int index;
        List<Object> keys = null;
        ValueEntry(List<Object> inKeys, int index) {
            this.index = index;
            this.keys = inKeys;
        }

        @Override
       public Object getKey() {
           return this.keys.get(index);
       }

       @Override
       public Object getValue() {
           return ValuesMap.this.getValue((String) keys.get(index));
       }

       @Override
       public Object setValue(Object value) {
           Object oldValue = getValue();
           ValuesMap.this.put(keys.get(index), value);
           return oldValue;
       }

       @Override
       public int hashCode() {
           return Objects.hash(getKey(), getValue());
       }

   }
}
