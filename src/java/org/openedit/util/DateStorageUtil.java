package org.openedit.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class DateStorageUtil
{
	private static final Log log = LogFactory.getLog(DateStorageUtil.class);
    protected Map<String, DateFormat> fieldDateFormats;
    protected DateFormat fieldOldDashFormat;
	
	static final private ThreadLocal perThreadCache = new ThreadLocal();	
	
	public DateFormat getDateFormat(String inFormat)
	{
		if( fieldDateFormats == null)
		{
			fieldDateFormats = new HashMap<String,DateFormat>();
		}
		DateFormat format = fieldDateFormats.get(inFormat);
		if( format == null)
		{
			format = new SimpleDateFormat(inFormat);
			format.setLenient(true);
			fieldDateFormats.put(inFormat, format);
		}
		return format;
	}
	protected DateFormat getStandardFormat()
	{
		return getDateFormat("yyyy-MM-dd HH:mm:ss Z");
	}
	protected DateFormat getExifFormat()
	{
		//2010:09:20 13:20:53-04:00
		return getDateFormat("yyyy:MM:dd HH:mm:ssZ");
	}
	protected DateFormat getStandardLogFormat()
	{
		return getDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	}
	protected DateFormat getOldColonFormat()
	{
		return getDateFormat("yyyy:MM:dd HH:mm:ss");
	}
	protected DateFormat getOldDashFormat()
	{
		return getDateFormat("yyyy-MM-dd HH:mm:ss");
	}
	protected DateFormat getLuceneFormat()
	{
		if( fieldOldDashFormat == null)
		{
			fieldOldDashFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			fieldOldDashFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			fieldOldDashFormat.setLenient(true);
		}
		return fieldOldDashFormat;
	}
	protected DateFormat getOldShortStandardFormat()
	{
		return getDateFormat("MM/dd/yyyy");
	}
	
	public static DateStorageUtil getStorageUtil()
	{
		DateStorageUtil format = (DateStorageUtil)perThreadCache.get();
		if( format == null)
		{
			format = new DateStorageUtil();
			perThreadCache.set(format);
		}
		return format;
	}

	public Date parseFromStorage(String inStoredDate)
	{
		if( inStoredDate == null)
		{
			return null;
		}
		try
		{
			if( inStoredDate.length() > 21)
			{
				if( inStoredDate.contains("T") )
				{
					return getStandardLogFormat().parse(inStoredDate);
				}
				if( inStoredDate.indexOf("-") < 6)
				{
					return getStandardFormat().parse(inStoredDate);
				}
				else
				{
					if( inStoredDate.endsWith(":00"))
					{
						inStoredDate = inStoredDate.substring(0,inStoredDate.length() - 3) + "00";
					}
					return getExifFormat().parse(inStoredDate);
				}
			}
			
			if( inStoredDate.length() > 18)
			{	//2009:05:15 10:58:55 
				if( inStoredDate.contains("-"))
				{
					return getOldDashFormat().parse(inStoredDate);
				}
				else
				{
					return getOldColonFormat().parse(inStoredDate);
				}
			}
			
			if(inStoredDate.length()> 13 && inStoredDate.contains("/"))
			{
				return getSlashedDateFormat().parse(inStoredDate);
			}
			
			if(inStoredDate.length()> 13)
			{
				return getLuceneFormat().parse(inStoredDate);
			}
			if( inStoredDate.length() > 6)
			{
				return getOldShortStandardFormat().parse(inStoredDate);
			}
		}
		catch( Exception ex)
		{
			log.error("Could not parse date " + inStoredDate);
		}
		return null;
	}
	
	public DateFormat getSlashedDateFormat()
	{
		return getDateFormat("dd/MM/yyyy hh:mm");
	}

	public String formatForStorage(Date inDate)
	{
		String storage = getStandardFormat().format(inDate);
		return storage;
	}
	public String formatDate(String inDate,String inFormat)
	{
		Date date = parseFromStorage(inDate);
		DateFormat formater = getDateFormat(inFormat);
		return formater.format(date);
	}
	
	
}
