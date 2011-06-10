package com.openedit.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Allows thread safe access to a shared date format
 * @author cburkey
 */

public class SimpleDateFormatPerThread 
{
	protected ThreadLocal<DateFormat> perThreadCache = new ThreadLocal<DateFormat>();
    protected String fieldFormat;
    protected Locale fieldLocale;
    
	public String getFormat() 
	{
		return fieldFormat;
	}

	public void setFormat(String inFormat) 
	{
		fieldFormat = inFormat;
	}

	public SimpleDateFormatPerThread(String inFormat) 
	{
		setFormat(inFormat);
	}
	
	public SimpleDateFormatPerThread(String inFormat, Locale inLocale) 
	{
		setFormat(inFormat);
		setLocale(inLocale);
	}
	
	
	public Date parse(String inDate) 
    {
		DateFormat format = perThreadCache.get();
		if( format == null)
		{
			format = new SimpleDateFormat(fieldFormat, getLocale());
			perThreadCache.set(format);
		}
		try
		{	
			return format.parse(inDate);
		}
		catch( ParseException ex)
		{
			throw new RuntimeException(ex);
		}
    }

	public Locale getLocale()
	{
		if(fieldLocale == null)
		{
			fieldLocale = Locale.getDefault();
		}
		return fieldLocale;
	}

	public void setLocale(Locale locale)
	{
		fieldLocale = locale;
	}

}
