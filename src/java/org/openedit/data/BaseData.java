package org.openedit.data;

import java.util.Collection;
import java.util.Map;

import org.openedit.Data;
import org.openedit.MultiValued;
import org.openedit.OpenEditException;


public class BaseData implements MultiValued, Comparable, Cloneable
{
	public static final Data NULL = new BaseData(); 
	
	protected ValuesMap fieldMap;

	public BaseData() {
	}

	public BaseData(Map inMap) 
	{
		fieldMap = new ValuesMap(inMap);
	}

	public String get(String inId) 
	{
		if( fieldMap == null)
		{
			return null;
		}
		Object value = getValue(inId);
		return getMap().toString(value);
	}
	public boolean getBoolean(String inId)
	{
		return getMap().getBoolean(inId);
	}
	
	public float getFloat(String inId)
	{
		return getMap().getFloat(inId);
	}
	public String getId() {
		String name = get("id");
		return name;
	}

	public String getName() {
		String name = get("name");
		return name;
	}

	public void setName(String inName) {
		setProperty("name", inName);
	}

	public String toString() {
		String name = getName();
		if (name == null) {
			return super.toString();
		}
		return name;
	}

	public void setProperty(String inId, String inValue) 
	{
		if (inValue == null) 
		{
			getMap().remove(inId);
		} else {
			setValue(inId, inValue);
		}
	}
	/**
	 * @deprecated This API should be String based for now
	 * @param inId
	 * @param inValue
	 */
	public void setPropertyValues(String inId, String[] inValues) 
	{
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < inValues.length; i++)
		{
			if( i > 0)
			{
				buffer.append(" | ");
			}
			buffer.append(inValues[i]);
		}
		setProperty(inId, buffer.toString());
	}

	public Object clone()
	{
		try
		{
			return super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			throw new OpenEditException(e);
		}
	}
	protected ValuesMap getMap() 
	{
		if (fieldMap == null) {
			fieldMap = new ValuesMap();
		}
		return fieldMap;
	}

	public void setId(int inNewid) {
		setProperty("id", String.valueOf( inNewid) );
	}

	public void setId(String inNewid) {
		setProperty("id", inNewid);
	}

	public String getSourcePath() {
		return get("sourcepath");
	}

	public void setSourcePath(String inSourcepath) {
		setProperty("sourcepath", inSourcepath);
	}

	public Map getProperties() 
	{
		return getMap();
	}
	public void setProperties(Map inProperties)
	{
		getMap().putAll(inProperties);
	}
	public int compareTo(Object inO) {
		BaseData inData = (BaseData) inO;
		if (getName() != null && inData.getName() != null) {
			return getName().compareTo(inData.getName());
		}
		return 0;
	}
	
	public Collection<String> getValues(String inPreference)
	{
		return getMap().getValues(inPreference);
//		Object object = getValue(inPreference);
//		if( object instanceof Collection)
//		{
//			return (Collection<String>)object;
//		}
//		String val = get(inPreference);
//		
//		if (val == null)
//		{
//			return null;
//		}
//		String[] vals = null;
//		if( val.contains("|") )
//		{
//			vals = VALUEDELMITER.split(val);
//		}
//		else
//		{
//			vals = val.split("\\s+"); //legacy
//		}
//
//		Collection collection = Arrays.asList(vals);
//		//if null check parent
//		return collection;
	}
	public Object getValue(String inKey)
	{
		return getMap().get(inKey);
	}
	public void setValue(String inKey, Object inValue)
	{
		getMap().put(inKey,inValue);
	}
	
	/**
	 * @deprecated use setValue
	 */
	public void setValues(String inKey, Collection<String> inValues)
	{
//		StringBuffer values = new StringBuffer();
//		for (Iterator iterator = inValues.iterator(); iterator.hasNext();)
//		{
//			String detail = (String) iterator.next();
//			values.append(detail);
//			if( iterator.hasNext())
//			{
//				values.append(" | ");
//			}
//		}
		getMap().put(inKey, inValues);
	}
	public void addValue(String inKey, Object inNewValue)
	{
		getMap().addValue(inKey, inNewValue);
//		String val = get(inKey);
//		if( val == null )
//		{
//			setValue(inKey, inNewValue);
//		}
//		else 
//		{
//			Collection values = getValues(inKey);
//			if(values.contains(inNewValue))
//			{
//				return;
//			}
//			else
//			{
//				values = new ArrayList(values);
//				values.add(inNewValue);
//			}
//			setValue(inKey, values);
//		}
	}
	public void removeValue(String inKey)
	{
		getMap().remove(inKey);
		
	}

	@Override
	public void removeValue(String inKey, Object inOldValue)
	{
		getMap().removeValue(inKey, inOldValue);
	}
	
}
