package org.openedit.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class DateStorageUtil
{
	private static final Log log = LogFactory.getLog(DateStorageUtil.class);

	protected DateFormat fieldStandardFormat;
	protected DateFormat fieldExifFormat;
	protected DateFormat fieldStandardLogFormat;
	protected DateFormat fieldOldDashFormat;
	protected DateFormat fieldOldColonFormat;
	protected DateFormat fieldOldShortStandardFormat;
	protected DateFormat fieldSlashedDateFormat;
	
	static final private ThreadLocal perThreadCache = new ThreadLocal();	
	
	protected DateFormat getStandardFormat()
	{
		if( fieldStandardFormat == null)
		{
			//2010-05-21 13:22:11 -0400
			fieldStandardFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
			fieldStandardFormat.setLenient(true);
		}
		return fieldStandardFormat;
	}
	protected DateFormat getExifFormat()
	{
		if( fieldExifFormat == null)
		{
			//2010:09:20 13:20:53-04:00
			//2010:09:20 13:20:53-04:00
			fieldExifFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ssZ");
			fieldExifFormat.setLenient(true);
		}
		return fieldExifFormat;
	}
	protected DateFormat getStandardLogFormat()
	{
		if( fieldStandardLogFormat == null)
		{
			//2010-05-21 13:22:11 -0400
			fieldStandardLogFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
			fieldStandardLogFormat.setLenient(true);
		}
		return fieldStandardLogFormat;
	}
	protected DateFormat getOldColonFormat()
	{
		if( fieldOldColonFormat == null)
		{
			fieldOldColonFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
		}
		return fieldOldColonFormat;
	}
	protected DateFormat getOldDashFormat()
	{
		if( fieldOldDashFormat == null)
		{
			fieldOldDashFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			fieldOldDashFormat.setLenient(true);
		}
		return fieldOldDashFormat;
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
		if( fieldOldShortStandardFormat == null)
		{
			fieldOldShortStandardFormat = new SimpleDateFormat("MM/dd/yyyy");
			fieldOldShortStandardFormat.setLenient(true);
		}
		return fieldOldShortStandardFormat;
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
	if (fieldSlashedDateFormat == null)
	{
		fieldSlashedDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
		
	}

	return fieldSlashedDateFormat;
	}
	public void setSlashedDateFormat(DateFormat inSlashedDateFormat)
	{
		fieldSlashedDateFormat = inSlashedDateFormat;
	}
	public String formatForStorage(Date inDate)
	{
		String storage = getStandardFormat().format(inDate);
		return storage;
	}
	
	
}
