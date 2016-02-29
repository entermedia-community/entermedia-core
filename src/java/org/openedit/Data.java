package org.openedit;

import java.util.Collection;
import java.util.Map;

public interface Data
{
	String getId();
	void setId(String inNewid);
	String getName();
	void setName(String inName);
	//This is the place that the data originated from
	void setSourcePath(String inSourcepath);
	String getSourcePath();

	//deprecated
	void setProperty(String inId, String inValue);
	
	String get(String inId);
	Object getValue(String inKey);
	
	void setValue(String inKey, Object inValue);
	
	Map getProperties();
	void setProperties(Map inProperties);


}
