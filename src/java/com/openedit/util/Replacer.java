package com.openedit.util;

import java.util.Map;

import org.openedit.Data;


public class Replacer
{
	public String replace(String inCode, Map<String, String> inValues)
	{
		if( inCode == null)
		{
			return inCode;
		}
		int start = 0;
		while( (start = inCode.indexOf("${",start)) != -1)
		{
			int end = inCode.indexOf("}",start);
			if( end != -1)
			{
				String key = inCode.substring(start+2,end);
				Object variable = inValues.get(key); //check for property
				if( variable == null )
				{
					int dot = key.indexOf('.');
					if( dot > 0)
					{
						String objectname = key.substring(0,dot);
						
						Object object = inValues.get(objectname);
						if( object instanceof Data)
						{
							Data data = (Data)object;
							String method = key.substring(dot+1);
							variable = data.get(method);
						}
					}
				}
				if( variable != null)
				{
					String sub = variable.toString();
					sub = replace(sub,inValues);
					inCode = inCode.substring(0,start) + sub + inCode.substring(end+1);
					if(sub.length() <= end){
						start = end-sub.length();
					}else{
						start =  sub.length();
					}
				}else{
					start = end;
				}
			}
		
			
		}
		return inCode;
	}
}
