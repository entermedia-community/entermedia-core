package org.openedit.data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openedit.Data;
import org.openedit.MultiValued;
import org.openedit.OpenEditException;
import org.openedit.WebPageRequest;
import org.openedit.modules.translations.LanguageMap;
import org.openedit.util.DateStorageUtil;


public class BaseData implements MultiValued, Comparable, Cloneable
{
	public static final Data NULL = new BaseData(); 
	
	protected ValuesMap fieldProperties;

	public BaseData() {
	}


	
	public BaseData(Map inMap) 
	{
		fieldProperties = new ValuesMap(inMap);
	}

	public String get(String inId) 
	{
		if( fieldProperties == null || inId == null)
		{
			return null;
		}
		String value = getProperties().getString(inId);
		return value;
	}
	
	public String getText(String inId, WebPageRequest inContext) {
		if(Boolean.parseBoolean(inContext.getPageProperty("auto_translate"))){
			String locale =  inContext.getLocale();

			return getText(inId, locale);
		}
		
		return getText(inId, "en");
	}
	
	
	public String getText(String inId, String inLocale) 
	{
		if( fieldProperties == null)
		{
			return null;
		}
		Object value = getValue(inId);
		if (value == null)
		{
			return null;
		}
		
		if (value instanceof LanguageMap)
		{
			LanguageMap map = getLanguageMap(inId, value);
			if (map == null)
			{
				return null;
			}
			
			return map.getText(inLocale);
		}
		if (value instanceof Map)
		{
			return value.toString();
		}
		return value.toString();
	}
	
	public LanguageMap getLanguageMap(String inId) 
	{
		if( fieldProperties == null)
		{
			return null;
		}
		Object value = getValue(inId);
		if (value == null)
		{
			return null;
		}
		return getLanguageMap(inId, value);
	}



	protected LanguageMap getLanguageMap(String inId, Object value)
	{
		if( value instanceof LanguageMap )
		{
			LanguageMap map = (LanguageMap)value;
			return map;
		}
		else if( value instanceof Map)
		{
			LanguageMap map = new LanguageMap((Map)value);
			return map;
		}
		LanguageMap map = new LanguageMap();
		map.setText("en", (String) value);
		return map;
	}
	
	public boolean getBoolean(String inId)
	{
		Object val = getValue(inId);
		if( val == null  )
		{
			return false;
		}
		if( val instanceof Boolean)
		{
			return (boolean)val;
		}
		return Boolean.valueOf(val.toString());
	}
	
	public float getFloat(String inId)
	{
		Object val = getValue(inId);
		if( val == null )
		{
			return 0;
		}
		if( val instanceof Float)
		{
			return (float)val;
		}
		return Float.parseFloat(val.toString());
	}
	
	
	public Double getDouble(String inId)
	{
		Object val = getValue(inId);
		if( val == null )
		{
			return null;
		}
		if( val instanceof Double)
		{
			return (double)val;
		}
		return Double.parseDouble(val.toString());
	}
	
	public long getLong(String inId)
	{
		Object val = getValue(inId);
		if( val == null )
		{
			return 0;
		}
		if( val instanceof Long)
		{
			return (long)val;
		}
		return Long.parseLong(val.toString());
	}
	
	
	
	
	public int getInt(String inId)
	{
		Object val = getValue(inId);
		if( val == null )
		{
			return 0;
		}
		if( val instanceof Integer)
		{
			return (int)val;
		}
		return Integer.parseInt(val.toString().trim());
	}
	
	
	public String getId() {
		String name = get("id");
		return name;
	}
	
	
	
	
	public String getName(String locale) 
	{
		Object name = getValue("name");
		if( name instanceof LanguageMap)
		{
			LanguageMap values = (LanguageMap)name;
			String val = values.getDefaultText(locale);
			return val;
		}
		return (String)name;
	}
	
	public String getName(WebPageRequest inReq) {
		if(Boolean.parseBoolean(inReq.getPageProperty("auto_translate"))){
			String locale =  inReq.getLocale();

			return getName( locale);
		}
		return getName("en");
	}
	
	
	

	public String getName() 
	{
		return get("name");
	}

	public void setName(String inName) {
		setProperty("name", inName);
	}

	public String toString() {
		String name = getName();
		if (name == null) {
			return super.toString();
		}
		return name;
	}

	public void setProperty(String inId, String inValue) 
	{
		if (inValue == null) 
		{
			getProperties().remove(inId);
		} else {
			setValue(inId, inValue);
		}
	}
	/**
	 * @deprecated This API should be String based for now
	 * @param inId
	 * @param inValue
	 */
	public void setPropertyValues(String inId, String[] inValues) 
	{
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < inValues.length; i++)
		{
			if( i > 0)
			{
				buffer.append(" | ");
			}
			buffer.append(inValues[i]);
		}
		setProperty(inId, buffer.toString());
	}

	public Object clone()
	{
		try
		{
			return super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			throw new OpenEditException(e);
		}
	}

	public void setId(int inNewid) {
		setProperty("id", String.valueOf( inNewid) );
	}

	public void setId(String inNewid) 
	{
		if( inNewid == null)
		{
			inNewid = ValuesMap.NULLSTRING;
		}
		setProperty("id", inNewid);
	}

	public String getSourcePath() {
		return get("sourcepath");
	}

	public void setSourcePath(String inSourcepath) {
		setProperty("sourcepath", inSourcepath);
	}
	
	//DO not call
	public ValuesMap getProperties() 
	{
		if (fieldProperties == null) {
			fieldProperties = new ValuesMap();
		}
		return fieldProperties;

	}
	//only Adds properties does not remove any
	public void setProperties(Map inProperties)
	{
		getProperties().putAll(inProperties);
	}
	public int compareTo(Object inO) {
		BaseData inData = (BaseData) inO;
		if (getName() != null && inData.getName() != null) {
			return getName().toLowerCase().compareTo(inData.getName().toLowerCase());
		}
		return 0;
	}
	
	public Collection<String> getValues(String inPreference)
	{
		return getProperties().getValues(inPreference);
	}
	public boolean hasValue(String inField, String inId)
	{
		if( getProperties().containsKey(inField) )
		{
			return getProperties().containsInValues(inField,inId);
		}
		Object object = getValue(inField);
		if( object == null)
		{
			return false;
		}
		if( object instanceof Collection)
		{
			boolean had =  ((Collection<String>)object).contains(inId);
			return had;
		}
		if( object instanceof String[])
		{
			Collection<String> values = Arrays.asList((String[])object);
			boolean had =  values.contains(inId);
			return had;
		}
		if( inId.equals(object) )
		{
			return true;
		}
		return false;
	}
	
	public Date getDate(String inField)
	{
		Object date = getValue(inField);
		if(date == null)
		{
			return null;
		}

		if(date instanceof Date){
			return (Date) date;
		}
		
		return DateStorageUtil.getStorageUtil().parseFromStorage((String)date);
		
		
	}
	
	public Object getValue(String inKey)
	{
		Object val = getProperties().getValue(inKey);
		if( val == ValuesMap.NULLSTRING)
		{
			val = null;
		}
		if( val == null && inKey.equals("name"))
		{
			Map map = (Map)getProperties().getValue(inKey  + "_int");
			return map;
		}
		return val;
	}
	public void setValue(String inKey, Object inValue)
	{
		if( inKey.equals("emrecordstatus") && inValue instanceof String)
		{
			//Spreadsheet importing?
			String val = (String)inValue;
			if( val.trim().isEmpty())
			{
				return;
			}
			try
			{
				JSONParser parser = new JSONParser();
				inValue = (Map)parser.parse(val);
			}
			catch (ParseException e)
			{
				throw new OpenEditException(e);
			}
		}
		getProperties().put(inKey,inValue);
	}
	
	/**
	 * @deprecated use setValue
	 */
	public void setValues(String inKey, Collection<String> inValues)
	{
//		StringBuffer values = new StringBuffer();
//		for (Iterator iterator = inValues.iterator(); iterator.hasNext();)
//		{
//			String detail = (String) iterator.next();
//			values.append(detail);
//			if( iterator.hasNext())
//			{
//				values.append(" | ");
//			}
//		}
		getProperties().put(inKey, inValues);
	}
	public void addValue(String inKey, Object inNewValue)
	{
		if( inNewValue instanceof Collection)
		{
			for (Iterator iterator = ((Collection)inNewValue).iterator(); iterator.hasNext();)
			{
				Object value = (Object) iterator.next();
				addValue(inKey, value);
			}
			return;
		}
		Collection values = getValues(inKey);
		if(values == null) 
		{
			values = new ArrayList();
		}
		
		if(!values.contains(inNewValue)) 
		{
			values = new ArrayList(values);
			values.add(inNewValue);			
			setValue(inKey, values);
		}
			
	}
	
	public void addValues(String inKey, Collection inNewValues)
	{
		if (inNewValues == null)
		{
			return;
		}
		for (Iterator iterator = inNewValues.iterator(); iterator.hasNext();)
		{
			Object value = (Object) iterator.next();
				addValue(inKey, value);
		}
	}
	
	public void removeValue(String inKey)
	{
		getProperties().remove(inKey);
		
	}

	@Override
	public void removeValue(String inKey, Object inOldValue)
	{
		getProperties().removeValue(inKey, inOldValue);
	}

	
	@Override
	public Set keySet()
	{
		return getProperties().keySet();
	}
	
	protected Long toLong(Number inNumber)
	{
		if( inNumber instanceof Long)
		{
			return (Long)inNumber;
		}
		if( inNumber instanceof Integer)
		{
			return ((Integer)inNumber).longValue();
		}
		throw new OpenEditException("Number is not a valid Long");
		
	}
	
	public String getAge()
	{
		return getAge("creationdate");
	}
	
	public String getAge(String InDateField)
	{
		Date createdon = getDate(InDateField);
		if( createdon == null)
		{
			return null;
		}
		//MathUtils util = new MathUtils();
		long diff = System.currentTimeMillis() - createdon.getTime();
		
		long minute = (diff / (1000 * 60)) % 60;
		long hour = (diff / (1000 * 60 * 60));
		String time = null;
		if( hour > 24)
		{
			double days = (double)hour / 24d;
			hour = hour % 24;
			time = String.format("%2dd:%2dh:%2dm", (int)days, hour, minute);
		}
		else
		{
			time = String.format("%2dh:%02dm", hour, minute);
		}
		return time;
	}
	

	public boolean containsValue(String inKey, Object inValue)
	{
		Collection current = getValues(inKey);
		
		if( current != null && current.contains(inValue))
		{
			return true;
		}
		return false;
	}

	
	public Data copy()
	{
		Data copy = (Data)clone();
		return copy;
	}
	
	
	
	public String toJsonString()
	{
		/*StringBuffer output = new StringBuffer();
		output.append("{ \"_id\": \"" + getId() + "\",");
		output.append(" \"map\" :");
		JSONObject object = new JSONObject(getProperties());  //TODO: Deal with Java Objects. Loop over stuff?
		output.append(object.toJSONString());
		output.append(" \n}");
		return output.toString();*/
		
		JSONObject output = new JSONObject();
		output.put("_id", getId());
		Map properties = getProperties();
		
		for (Iterator iterator = properties.keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			Object value = properties.get(key);
			
			if (value instanceof Date)
			{
				String jsondate = DateStorageUtil.getStorageUtil().formatDateObj((Date)value, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
				properties.put(key, jsondate);
			}
			
		}
				
		JSONObject object = new JSONObject(properties);  //TODO: Deal with Java Objects. Loop over stuff?
		output.put("map", object);

		return output.toJSONString();
	}	
	
	public boolean hasValue(String inKey)
	{
		Object value = getValue(inKey);
		if (value != null)
		{
			if( value instanceof String)
			{
				boolean empty = ((String)value).trim().isEmpty();
				return !empty;
			}
			return true;
		}
		return false;
	}	
	public Instant getInstant(String key) {
	    Date d = getDate(key); // existing
	    return d != null ? d.toInstant() : null;
	}
	
}
