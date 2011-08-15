package org.openedit.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openedit.Data;

public class BaseData implements Data, Comparable {
	protected Map fieldMap;
	protected String fieldId;

	public BaseData() {
	}

	public BaseData(Map inMap) {
		setMap(inMap);
	}

	public String get(String inId) {
		if ("id".equals(inId)) {
			return getId();
		}
		Object object = getProperties().get(inId);
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
		return fieldId;
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

	public void setProperty(String inId, String inValue) {
		if ("id".equals(inId)) {
			setId(inValue);
			return;
		}
		if (inValue == null) {
			getMap().remove(inId);
		} else {
			getMap().put(inId, inValue);
		}
	}

	public void setProperty(String inId, Object inValue) {
		if ("id".equals(inId)) {
			setId(inValue.toString());
			return;
		}
		getMap().put(inId, inValue);
	}

	public Map getMap() {
		return getProperties();
	}

	public void setMap(Map inMap) {
		fieldMap = inMap;
	}

	public void setId(int inNewid) {
		fieldId = String.valueOf(inNewid);
	}

	public void setId(String inNewid) {
		fieldId = inNewid;
	}

	public String getSourcePath() {
		return get("sourcepath");
	}

	public void setSourcePath(String inSourcepath) {
		setProperty("sourcepath", inSourcepath);
	}

	public Map getProperties() {
		if (fieldMap == null) {
			fieldMap = new HashMap(5);
		}
		return fieldMap;
	}

	public int compareTo(Object inO) {
		BaseData inData = (BaseData) inO;
		if (getName() != null && inData.getName() != null) {
			return getName().compareTo(inData.getName());
		}
		return 0;
	}

	public Collection getValues(String inPreference)
	{
		String val = get(inPreference);
		
		if (val == null)
			return null;
		
		String[] vals = val.split("\\s+");

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
				values.append(" ");
			}
		}
		setProperty(inKey,values.toString());
	}
	
}
