package org.openedit.modules.translations;

import java.util.Map;
import java.util.TreeMap;

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
		if(val == null){
			val =  (String) get("en");
		}
		return val;
		
		
	}

	public void setText(String inVal, String inLocale)
	{
		put(inLocale, inVal); 
	}
	
	
	
	
}
