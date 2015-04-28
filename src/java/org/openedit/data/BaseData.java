package org.openedit.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openedit.Data;
import org.openedit.MultiValued;

import com.openedit.OpenEditException;

public class BaseData implements MultiValued, Comparable, Cloneable
{
	public static final Data NULL = new BaseData(); 
	
	protected Map fieldMap;

	public BaseData() {
	}

	public BaseData(Map inMap) {
		fieldMap = inMap;
	}

	public String get(String inId) {
		if( fieldMap == null)
		{
			return null;
		}
		Object object = getMap().get(inId);
		if (object == null) {
			return null;
		}
		if (object instanceof String) {
			return (String) object;
		}
		if (object instanceof Date) {
			return String.valueOf((Date) object);
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
		return String.valueOf(object);
	}
	public boolean getBoolean(String inId)
	{
		String val = get(inId);
		if( val != null)
		{
			return Boolean.parseBoolean(val);
		}
		return false;
		
	}
	
	public float getFloat(String inId)
	{
		String val = get(inId);
		if( val != null)
		{
			return Float.parseFloat(val);
		}
		return 0;
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
//		if ("id".equals(inId)) {
//			setId(inValue);
//			return;
//		}
		if (inValue == null) {
			getMap().remove(inId);
		} else {
			getMap().put(inId, inValue);
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
	protected Map getMap() 
	{
		if (fieldMap == null) {
			fieldMap = new HashMap(5);
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
	public void setProperties(Map<String,String> inProperties)
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
		String val = get(inPreference);
		
		if (val == null)
		{
			return null;
		}
		String[] vals = null;
		if( val.contains("|") )
		{
			vals = VALUEDELMITER.split(val);
		}
		else
		{
			vals = val.split("\\s+"); //legacy
		}

		Collection collection = Arrays.asList(vals);
		//if null check parent
		return collection;
	}
	
	public void setValues(String inKey, Collection<String> inValues)
	{
		StringBuffer values = new StringBuffer();
		for (Iterator iterator = inValues.iterator(); iterator.hasNext();)
		{
			String detail = (String) iterator.next();
			values.append(detail);
			if( iterator.hasNext())
			{
				values.append(" | ");
			}
		}
		setProperty(inKey,values.toString());
	}
	
}
