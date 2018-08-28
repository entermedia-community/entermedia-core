package org.openedit.modules.translations;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.json.simple.JSONObject;

public class LanguageMap extends TreeMap
{

	public LanguageMap(Map inVals)
	{
		super(inVals);
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
		put(inLocale, inVal); 
	}
	
	@Override
	public String toString()
	{
		if( size() == 1)
		{
			return (String)values().iterator().next();
		}
		return toJson();
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
