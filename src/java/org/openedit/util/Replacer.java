package org.openedit.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.openedit.CatalogEnabled;
import org.openedit.Data;
import org.openedit.data.Searcher;
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
		map.put("data",inValues);
		map.put("id",inValues.getId());
		map.put("name",inValues.getName());
		map.put("sourcepath", inValues.getSourcePath());
		for (Iterator itr = props.keySet().iterator(); itr.hasNext(); ){
			String key = itr.next().toString();
			map.put(key,props.get(key));
		}
		return replace(inCode, map);
	}

	/**
	 * ${localfield:listid.field} Use this format if your local field maps to a different table 
	 * @param inCode
	 * @param inValues
	 * @return
	 */
	public String replace(String inCode, Map<String, Object> inValues)
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
			Object variable = null;
			ArrayList<String> values = findKeys(key,"||");
			
			for(String value:values)
			{
				variable = inValues.get(value); //check for property
				if( variable == null )
				{
					//TODO: Loop over each of the dots to find the final object
					int dot = value.indexOf('.');
					if( dot > 0)
					{
						String objectname = value.substring(0,dot);
						Object object = null;
						
						String localproperty = null;
						String foreigntable = null;
						
						if(objectname.contains(":"))
						{
							//${localfield:listid.field} Use this format if your local field maps to a different table 
							String [] splits = objectname.split(":");
							if (splits.length==2)
							{
								localproperty = splits[0];
							    foreigntable = splits[1];
								object = inValues.get(localproperty);
								value = value.substring(value.indexOf(":")+ 1);
								dot = value.indexOf('.');
							}
						}
						else
						{
							object = inValues.get(objectname);  //${division.folder}
						}
						if( object instanceof Collection)
						{
							Collection col = (Collection)object;
							if( !col.isEmpty())
							{
								object = col.iterator().next();
							}
						}
						if( object instanceof String )
						{
							if (localproperty!=null &&  foreigntable!=null)
							{
								String localval = (String) inValues.get(localproperty);
								object = getData(foreigntable,localval);
							}
							else
							{
								object = getData(objectname,(String)object); //division
							}
							if(isAlwaysReplace() && object == null)
							{
								variable="";
							}
						}
						if(object instanceof Data)
						{
							Data data = (Data)object;
							String[] pairs = value.split("\\.");
							//Already found the first level
							for (int i = 1; i < pairs.length; i++)
							{
								String otherdatavalue = data.get(pairs[i]);
								if(otherdatavalue == null)
								{
									break;
								}
								if (getSearcherManager() != null) {
									Searcher searcher = getSearcherManager().getExistingSearcher(getCatalogId(), pairs[i]);
									if( searcher == null)
									{
										variable = otherdatavalue;
										break;
									}
								}
								data = getData(pairs[i], otherdatavalue);
								if( data == null)
								{
									variable = otherdatavalue;
									break;
								}
							}
						}
						else
						{
							//?? INT?
						}
					}
				}
				if (variable!=null){
					break;
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
