package org.openedit.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.openedit.CatalogEnabled;
import org.openedit.Data;
import org.openedit.data.DataWithSearcher;
import org.openedit.data.SearcherManager;


public class Replacer implements CatalogEnabled
{
	protected SearcherManager fieldSearcherManager;
	protected boolean fieldAlwaysReplace =  false;
	protected String fieldCatalogId;
	
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

	public String getCatalogId()
	{
		return fieldCatalogId;
	}

	public void setCatalogId(String inDefaultCatalogId)
	{
		fieldCatalogId = inDefaultCatalogId;
	}


	public String replace(String inCode, Data inValues)
	{
		Map<?,?> props = inValues.getProperties();
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("id",inValues.getId());
		map.put("name",inValues.getName());
		map.put("sourcepath", inValues.getSourcePath());
		for (Iterator itr = props.keySet().iterator(); itr.hasNext(); ){
			String key = itr.next().toString();
			map.put(key,props.get(key));
		}
		return replace(inCode, map);
	}

	public String replace(String inCode, Map<String, Object> inValues)
	{
		
		String done = replace(inCode,inValues,"en");
		return done;
	}

	/**
	 * ${localfield:listid.field} Use this format if your local field maps to a different table 
	 * @param inCode
	 * @param inValues
	 * @return
	 */
	public String replace(String inCode, Map<String, Object> inValues, String inLocale)
	{
		if( inCode == null)
		{
			return inCode;
		}
		if(inValues == null){
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
			Object currentvalue = null;
			ArrayList<String> values = findKeys(key,"||");
			
			for(String mask:values)
			{
				String[] pairs = mask.split("\\.");
				currentvalue = inValues.get(pairs[0]); //check for property
				for (int i = 1; i < pairs.length; i++)
				{
					String nextpart = pairs[i];
					if( currentvalue instanceof Collection)
					{
						Collection col = (Collection)currentvalue;
						if( !col.isEmpty())
						{
							currentvalue = col.iterator().next();
						}
					}
					if( currentvalue instanceof Date)
					{
						Date date = (Date)currentvalue;
						String format = "yyyy-MM-dd";  //TODO: Use locale format?
						if( pairs.length > i)
						{
							//grab the hour?
							format = pairs[i+1]; //TODO: grab all text
						}

						currentvalue = DateStorageUtil.getStorageUtil().formatDateObj(date, format);
						break;
					}
					else if(currentvalue instanceof DataWithSearcher)
					{
						DataWithSearcher smartdata = (DataWithSearcher)currentvalue;
						currentvalue = smartdata.getChildValue(nextpart);
					}
					else if( currentvalue instanceof String )
					{
					}	
					if (currentvalue==null)
					{
						break;
					}
				}
				if( currentvalue != null)
				{
					break;
				}
			}
			
			if( isAlwaysReplace() && currentvalue == null )
			{
				currentvalue="";
			}
			
			
			if( currentvalue != null)
			{
				String sub = null;
				if(currentvalue instanceof DataWithSearcher)
				{
					DataWithSearcher data = (DataWithSearcher)currentvalue;
					sub = data.getData().getName(inLocale);
				}
				else
				{
					sub = currentvalue.toString();
				}
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
	
	protected ArrayList<String> findKeys(String Subject, String Delimiters) 
    {
		StringTokenizer tok = new StringTokenizer(Subject, Delimiters);
		ArrayList<String> list = new ArrayList<String>(Subject.length());
		while(tok.hasMoreTokens()){
			list.add(tok.nextToken());
		}
		return list;
    }

	protected Data getData(String inType, String inId)
	{
		if( fieldSearcherManager == null )
		{
			return null;
		}
		return getSearcherManager().getData(getCatalogId(), inType, inId);
	}
}
