package org.openedit.data;

import java.util.Iterator;
import java.util.Map;

import org.openedit.Data;

public interface CompositeData extends Data
{
	
	public void addData(Data inData);
	public String get(String inId);


	public String getName();	
	public void setName(String inName);


	public void setProperty(String inId, String inValue);

	public Map getProperties();
	public Iterator<Data> iterator();
	public int size();
	
	
}
