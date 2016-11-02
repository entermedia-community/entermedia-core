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

	public String getText(String inLocale){
		
		String val = (String) get(inLocale);
		return val;
	}

	public String getDefaultText(String inLocale)
	{
		String val = getText(inLocale);
		if( val == null && (inLocale == null || !inLocale.equals("en") ) )
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
		JSONObject json = new JSONObject();
		json.putAll(this);
//		StringBuffer values = new StringBuffer();
//		for (Iterator iterator = keySet().iterator(); iterator.hasNext();)
//		{
//			String key = (String) iterator.next();
//			values.append(key);
//			values.append(":");
//			values.append(get(key));
//			if( iterator.hasNext() )
//			{
//				values.append(" ");
//			}
//		}
		return json.toJSONString();
		
	}
	
	
}
