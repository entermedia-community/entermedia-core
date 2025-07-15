package org.openedit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public interface MultiValued extends Data
{
	
	public static final Pattern VALUEDELMITER = Pattern.compile("\\s*\\|\\s*");
	
	public Collection<String> getValues(String inPreference);
	//public void setValues(String inKey, Collection<String> inValues);
	public void addValue(String inKey, Object inNewValue); //adds to an existing collection
	public void removeValue(String inKey, Object inNewValue); 
	public boolean containsValue(String inKey, Object inNewValue); 
	
	public Date getDate(String inField);
	public String getText(String inId, String inLocale);
	public boolean getBoolean(String inId);
	public Double getDouble(String inId);
	public int getInt(String inId);
	public float getFloat(String inId);
	public long getLong(String inId);
	public void addValues(String inString, Collection inValues);
	
	public static List<Double> collectDoubles(String[] vector) 
	{
		List<Double> doubles = new ArrayList(vector.length );
		for (int i = 0; i < vector.length; i++)
		{
			double f = Double.parseDouble(vector[i]);
			doubles.add(f);
		}
		return doubles;
	}
	public static List<Double> collectDoubles(Collection vector) 
	{
		List<Double> floats = new ArrayList(vector.size());
		for (Iterator iterator = vector.iterator(); iterator.hasNext();)
		{
			Object floatobj = iterator.next();
			double f;
			if( floatobj instanceof Double)
			{
				f = (Double)floatobj;
			}
			else
			{
				f = Double.parseDouble(floatobj.toString());
			}
			floats.add(f);
		}
		return floats;
	}

}
