package org.openedit;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.openedit.data.ValuesMap;

public interface Data
{
	String getId();
	void setId(String inNewid);
	String getName();
	String getName(String inLocale);

	void setName(String inName);
	//This is the place that the data originated from
	void setSourcePath(String inSourcepath);
	String getSourcePath();

	//deprecated
	void setProperty(String inId, String inValue);
	
	String get(String inId);
	Object getValue(String inKey);
	
	void setValue(String inKey, Object inValue);
	
	/** This is fast */
	ValuesMap getProperties();

	/**
	 * Only add properties. Does not clear
	 * @param inObjects
	 */
	void setProperties(Map inObjects);
	
	Set keySet();

	public Collection getValues(String inField);
	public String toJsonString();

}
