package org.openedit;

import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;

public interface MultiValued extends Data
{
	
	public static final Pattern VALUEDELMITER = Pattern.compile("\\s*\\|\\s*");
	
	public Collection<String> getValues(String inPreference);
	//public void setValues(String inKey, Collection<String> inValues);
	public void addValue(String inKey, Object inNewValue); //adds to an existing collection
	public void removeValue(String inKey, Object inNewValue); 
	
	public Date getDate(String inField);
	public String getText(String inId, String inLocale);
	public boolean getBoolean(String inId);
	public float getFloat(String inId);
	public long getLong(String inId);
	



}
