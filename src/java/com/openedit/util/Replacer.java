package com.openedit.util;

import java.util.Map;

import org.openedit.Data;
import org.openedit.data.SearcherManager;

import com.openedit.WebPageRequest;


public class Replacer
{
	protected SearcherManager fieldSearcherManager;
	protected boolean fieldAlwaysReplace;
	
	public boolean isAlwaysReplace() {
		return fieldAlwaysReplace;
	}

	public void setAlwaysReplace(boolean inAlwaysReplace) {
		fieldAlwaysReplace = inAlwaysReplace;
	}

	public SearcherManager getSearcherManager()
	{
		return fieldSearcherManager;
	}

	public void setSearcherManager(SearcherManager inSearcherManager)
	{
		fieldSearcherManager = inSearcherManager;
	}

	public String getDefaultCatalogId()
	{
		return fieldDefaultCatalogId;
	}

	public void setDefaultCatalogId(String inDefaultCatalogId)
	{
		fieldDefaultCatalogId = inDefaultCatalogId;
	}

	protected String fieldDefaultCatalogId;
	
	public String replace(String inCode, Map<String, Object> inValues)
	{
		if( inCode == null)
		{
			return inCode;
		}
		int start = 0;
		while( (start = inCode.indexOf("${",start)) != -1)
		{
			int end = inCode.indexOf("}",start);
			if( end == -1)
			{
				break;
			}
			String key = inCode.substring(start+2,end);
			Object variable = inValues.get(key); //check for property
			if( variable == null )
			{
				int dot = key.indexOf('.');
				if( dot > 0)
				{
					String objectname = key.substring(0,dot);
					
					Object object = inValues.get(objectname);  //${division.folder}
					if( object instanceof String )
					{
						object = getData(objectname,(String)object); //division
						if(isAlwaysReplace() && object == null)
						{
							variable="";
						}
					}
					if(object instanceof Data)
					{
						Data data = (Data)object;
						String method = key.substring(dot+1);
						variable = data.get(method);
					}
				}
			}
			if( isAlwaysReplace() && variable == null )
			{
				variable="";
			}
			
			
			if( variable != null)
			{
				String sub = variable.toString();
				sub = replace(sub,inValues);
				inCode = inCode.substring(0,start) + sub + inCode.substring(end+1);
				start = start + sub.length();
			}
			else
			{
				start = end; //could not find a hit, go to the next one
			}
		}
		return inCode;
	}

	protected Data getData(String inType, String inId)
	{
		if( fieldSearcherManager == null )
		{
			return null;
		}
		return getSearcherManager().getData(getDefaultCatalogId(), inType, inId);
	}
}
