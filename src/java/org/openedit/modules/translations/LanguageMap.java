package org.openedit.modules.translations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.json.simple.JSONObject;

public class LanguageMap extends TreeMap
{

	public LanguageMap(Map inVals)
	{
		for (Iterator iterator = inVals.keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			String value = (String)inVals.get(key);
			if( value == null || value.trim().isEmpty() )
			{
				continue;
			}
			put(key,value);
		}
	}
	
	public LanguageMap()
	{
	}

	public String getText(String inLocale)
	{
		if( inLocale == null)
		{
			return getText("en");
		}
		String val = (String) get(inLocale);
		if(val == null && inLocale.contains("_"))
		{
			String[] splits = inLocale.split("_");
			if(splits.length == 3)
			{
				val = (String) get(splits[0]+ "_" + splits[1]);
			}
			if(val == null && splits.length == 2)
			{
				val = (String) get(splits[0]);
			}
		}
		return val;
	}

	public String getDefaultText(String inLocale)
	{
		String val = getText(inLocale);
		if( val == null && inLocale != null && !inLocale.equals("en") )
		{
			val = getText("en");
		}
		return val;
	}
	public void setText(String inLocale, String inVal)
	{
		if( inVal == null)
		{
			remove(inLocale);
		}
		else
		{
			put(inLocale, inVal);
		}
	}
	
	@Override
	public String toString()
	{
		if( size() == 1)
		{
			return (String)values().iterator().next();
		}
		String json = toJson();
		if(json != null){
			return json;
		} 
		return "";		
	}

	public String toJson()
	{
		if( isEmpty())
		{
			return null;
		}
		JSONObject json = new JSONObject();
		json.putAll(this);	
		return json.toJSONString();
	}
	
	@Override
	public boolean equals(Object inO) {
		if( inO instanceof LanguageMap)
		{
			LanguageMap map = (LanguageMap)inO;
			if(map.size() != size())
			{
				return false;
			}
			Collection copy = new ArrayList(values());
			copy.removeAll(map.values());
			if( !copy.isEmpty())
			{
				return false;
			}
			return true;
		}
		return super.equals(inO);
	}

	@Override
	public boolean isEmpty()
	{
	
		for (Iterator iterator = keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			Object val = get(key);
			if(val != null){
				return false;
			}
		}
		
		return true;
	}
	
	
	
}
