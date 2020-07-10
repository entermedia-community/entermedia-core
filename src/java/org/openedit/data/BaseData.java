package org.openedit.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

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
	
	protected ValuesMap fieldMap;

	public BaseData() {
	}


	
	public BaseData(Map inMap) 
	{
		fieldMap = new ValuesMap(inMap);
	}

	public String get(String inId) 
	{
		if( fieldMap == null)
		{
			return null;
		}
		Object value = getValue(inId);
		return getMap().toString(value);
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
		if( fieldMap == null)
		{
			return null;
		}
		Object value = getValue(inId);
		if( value instanceof LanguageMap )
		{
			LanguageMap map = (LanguageMap)value;
			return map.getText(inLocale);
		}
		return getMap().toString(value);
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
		return Integer.parseInt(val.toString());
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
	
	
	

	public String getName() {
		String name = get("name");
		return name;
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
			getMap().remove(inId);
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
	public ValuesMap getMap() 
	{
		if (fieldMap == null) {
			fieldMap = new ValuesMap();
		}
		return fieldMap;
	}

	public void setId(int inNewid) {
		setProperty("id", String.valueOf( inNewid) );
	}

	public void setId(String inNewid) {
		setProperty("id", inNewid);
	}

	public String getSourcePath() {
		return get("sourcepath");
	}

	public void setSourcePath(String inSourcepath) {
		setProperty("sourcepath", inSourcepath);
	}

	public ValuesMap getProperties() 
	{
		return getMap();
	}
	public void setProperties(Map inProperties)
	{
		getMap().putAll(inProperties);
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
		return getMap().getValues(inPreference);
	}
	
	public Date getDate(String inField)
	{
		Object date = getValue(inField);
		if(date == null){
			return null;
		}

		if(date instanceof Date){
			return (Date) date;
		}
		
		return DateStorageUtil.getStorageUtil().parseFromStorage((String)date);
		
		
	}
	
	public Object getValue(String inKey)
	{
		Object val = getMap().getValue(inKey);
		
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
		getMap().put(inKey,inValue);
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
		getMap().put(inKey, inValues);
	}
	public void addValue(String inKey, Object inNewValue)
	{
		
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
	public void removeValue(String inKey)
	{
		getMap().remove(inKey);
		
	}

	@Override
	public void removeValue(String inKey, Object inOldValue)
	{
		getMap().removeValue(inKey, inOldValue);
	}

	
	@Override
	public Set keySet()
	{
		return getMap().keySet();
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
		Date createdon = getDate("creationdate");
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
			time = String.format("%2dd:%2dh:%2dm", (int)days,hour, minute);
		}
		else
		{
			time = String.format("%2dh:%02dm", hour, minute);
		}
		return time;
	}
}
