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
		return val;
		
		
	}

	public void setText(String inVal, String inLocale)
	{
		put(inLocale, inVal); 
	}
	
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
	return getText("en");
	}
	
	
}
