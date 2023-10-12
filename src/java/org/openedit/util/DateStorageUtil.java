package org.openedit.util;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DateStorageUtil
{
	private static final Log log = LogFactory.getLog(DateStorageUtil.class);
	protected Map<String, DateFormat> fieldDateFormats;
	protected DateFormat fieldOldDashFormat;
	// 1 day = 24 hour x 60 minutes x 60 seconds x 1000 millisecond;
	private static final long PERIOD_OF_DAY = 24 * 60 * 60 * 1000;
	static final private ThreadLocal perThreadCache = new ThreadLocal();

	public DateFormat getDateFormat(String inFormat)
	{
		if (fieldDateFormats == null)
		{
			fieldDateFormats = new HashMap<String, DateFormat>();
		}
		DateFormat format = fieldDateFormats.get(inFormat);
		if (format == null)
		{
			format = new SimpleDateFormat(inFormat);
			format.setLenient(true);
			if( inFormat.equals("yyyy-MM-dd'T'HH:mm:ss.SSSXXX") || inFormat.equals("yyyy-MM-dd'T'HH:mm:ss.SSSX") )
			{
				format.setTimeZone(TimeZone.getTimeZone("UTC"));		
			}
			fieldDateFormats.put(inFormat, format);
		}
		return format;
	}

	protected DateFormat getStandardFormat()
	{
		return getDateFormat("yyyy-MM-dd HH:mm:ss Z");
	}

	public DateFormat getJsonFormat()
	{
		return getDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	}
	public DateFormat getJsonSqlFormat()
	{
		return getDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	}


	protected DateFormat getExifFormat()
	{
		// 2010:09:20 13:20:53-04:00
		return getDateFormat("yyyy:MM:dd HH:mm:ssZ");
	}

	protected DateFormat getExifPhotoshopFormat()
	{
		//XMP-photoshop:DateCreated 
		return getDateFormat("yyyy:MM:dd HH:mm:ss.S");
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
		if (fieldOldDashFormat == null)
		{
			fieldOldDashFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			fieldOldDashFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
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
		DateStorageUtil format = (DateStorageUtil) perThreadCache.get();
		if (format == null)
		{
			format = new DateStorageUtil();
			perThreadCache.set(format);
		}
		return format;
	}

	/*
	 * return the number of days between two day
	 */
	public static double compareStorateDateWithCurrentTime(String lastTime)
	{
		Date lastDateTime = DateStorageUtil.getStorageUtil().parseFromStorage(lastTime);
		// String currentDate =
		// DateStorageUtil.getStorageUtil().formatForStorage(new Date());
		// Calendar cal = Calendar.getInstance();
		// Need to add timezone here
		Date current = new Date();
		double duration = (double) (current.getTime() - lastDateTime.getTime()) / PERIOD_OF_DAY;
		return duration;
	}
	public Date parseFromObject(Object inStoredDate)
	{
		if( inStoredDate == null)
		{
			return null;
		}
		
		if( inStoredDate instanceof String)
		{
			return parseFromStorage((String)inStoredDate);
		}
		else
		{
			return (Date)inStoredDate;
		}
	}

	public Date parseFromStorage(String inStoredDate)
	{
		if (inStoredDate == null)
		{
			return null;
		}
		try
		{

			if (inStoredDate.length() == 25 && inStoredDate.contains(":") && !inStoredDate.contains("-"))
			{

				return getDateFormat("yyyy:MM:dd hh:mm:ssX").parse(inStoredDate);

			}

			if (inStoredDate.length() > 20)
			{
				if (inStoredDate.endsWith("Z"))
				{
					inStoredDate = inStoredDate.replaceAll("Z$", "+0000");
				}

				if (inStoredDate.contains("T"))
				{
					if( inStoredDate.contains("+") || inStoredDate.contains("-"))
					{
//					    TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(inStoredDate);
//					    Instant i = Instant.from(ta);
//					    Date d = Date.from(i);
						Date d = getDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").parse(inStoredDate);
						return d;
					}
					//If its ends with Z then this is the time in UTC, no offset needed
					return getJsonFormat().parse(inStoredDate); //Also works for ElasticSearch
				}

				if (inStoredDate.indexOf("-") < 6)
				{
					return getStandardFormat().parse(inStoredDate);
				}

				if (inStoredDate.substring(inStoredDate.length() - ".000".length()).contains(".")) //What is this??!!
				{
					return getExifPhotoshopFormat().parse(inStoredDate);
				}

				String ending = inStoredDate.substring(inStoredDate.length() - 5, inStoredDate.length());
				if (ending.contains(":"))
				{
					ending = ending.replaceAll(":", "");
					inStoredDate = inStoredDate.substring(0, inStoredDate.length() - 5) + ending;
				}

				return getExifFormat().parse(inStoredDate);
			}

			if (inStoredDate.length() > 18)
			{
				if (inStoredDate.contains("T"))
				{
					return getJsonSqlFormat().parse(inStoredDate); //Also works for ElasticSearch
				}
				if (inStoredDate.contains("-"))
				{
					return getOldDashFormat().parse(inStoredDate);
				}
				else
				{
					return getOldColonFormat().parse(inStoredDate);
				}
			}

			if (inStoredDate.length() > 16)
			{
				// 5/16/00, 11:01 AM 17chars
				if (inStoredDate.contains(","))
				{
					return getDateFormat("dd/MM/yyyy, hh:mm a").parse(inStoredDate);
				}
				// 08.30.00 02:18 AM
				if (inStoredDate.contains("."))
				{
					return getDateFormat("MM.dd.yyyy hh:mm a").parse(inStoredDate);
				}
			}

			// TODO: Deal with military time?

			if (inStoredDate.length() > 13 && inStoredDate.contains("/"))
			{
				return getSlashedDateFormat().parse(inStoredDate);
			}

			if (inStoredDate.length() > 13)
			{
				return getLuceneFormat().parse(inStoredDate);
			}
			if (inStoredDate.length() == 10 && inStoredDate.indexOf("-") == 4)
			{
				return parse(inStoredDate,"yyyy-MM-dd");
			}
			String format = determineDateFormat(inStoredDate);
			if( format != null)
			{
				Date old = parse(inStoredDate,format);
				if( old.getYear() < 100)
				{
					old.setYear(old.getYear()  + 2000);
				}
				return old;
			}
		}
		catch (Exception ex)
		{
			log.info("Could not parse date " + inStoredDate);
		}

		return null;
	}

	public DateFormat getSlashedDateFormat()
	{
		return getDateFormat("dd/MM/yyyy HH:mm");
	}

	public String formatForStorage(String inDate, String inFormat)
	{
		DateFormat format = getDateFormat(inFormat);
		try
		{
			Date parsed = format.parse(inDate);
			return formatForStorage(parsed);
		}
		catch (Exception ex)
		{
			log.info("Could not parse date " + inDate);
			return null;
		}
	}

	public String formatForStorage(Date inDate)
	{
		if( inDate == null)
		{
			return null;
		}
		String storage = getStandardFormat().format(inDate);
		return storage;
	}

	public String formatDate(String inDate, String inFormat)
	{
		Date date = parseFromStorage(inDate);
		DateFormat formater = getDateFormat(inFormat);
		return date != null ? formater.format(date) : null;
	}

	public String formatDateObj(Date date, String inFormat)
	{
		DateFormat formater = getDateFormat(inFormat);
		return date != null ? formater.format(date) : null;
	}

	public String checkFormat(String inValue)
	{
		if (inValue == null)
		{
			return null;
		}
		if (inValue.length() > 21)
		{
			if (!inValue.contains("T") && inValue.indexOf("-") < 6 && !inValue.substring(inValue.length() - ".000".length()).contains("."))
			{
				return inValue;
			}
		}
		Date clean = parseFromStorage(inValue);
		if (clean == null)
		{
			return inValue;
		}
		return formatForStorage(clean);
	}

	public int getDiffYears(Date first, Date last)
	{
		Calendar a = getCalendar(first);
		Calendar b = getCalendar(last);
		int diff = b.get(b.YEAR) - a.get(b.YEAR);
		if (a.get(a.MONTH) > b.get(a.MONTH) || (a.get(a.MONTH) == b.get(a.MONTH) && a.get(a.DATE) > b.get(a.DATE)))
		{
			diff--;
		}
		return diff;
	}
	public Date subtractFromNow(long millis)
	{
		long subtracted = System.currentTimeMillis() - millis;
		if( subtracted < 1)
		{
			subtracted = 0;
		}
		Date newdate = new Date(subtracted);
		return newdate;
	}
	public Calendar getCalendar(Date date)
	{
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(date);
		return cal;
	}

	public int getDiffYears(String first, String last)
	{
		Date one = parseFromStorage(first);
		Date two = null;

		if ("now".equals(last))
		{
			two = new Date();
		}
		else
		{
			two = parseFromStorage(last);
		}
		if (one != null && two != null)
		{
			return getDiffYears(one, two);

		}
		else
		{
			return -1;
		}

	}

	public Date parse(String inDate, String inFormat)
	{

		DateFormat format = getDateFormat(inFormat);
		try
		{
			Date parsed = format.parse(inDate);
			return parsed;
		}
		catch (Exception ex)
		{
			log.info("Could not parse date " + inDate);
			return null;
		}

	}

	private static final Map<String, String> DATE_FORMAT_REGEXPS = new HashMap<String, String>()
	{
		{
			put("^\\d{8}$", "yyyyMMdd");
			put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy");
			put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
			put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "MM/dd/yyyy");
			put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd");
			put("^\\d{4}\\.\\d{1,2}\\.\\d{1,2}$", "yyyy.MM.dd");
			put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", "dd MMM yyyy");
			put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$", "dd MMMM yyyy");
			put("^\\d{12}$", "yyyyMMddHHmm");
			put("^\\d{8}\\s\\d{4}$", "yyyyMMdd HHmm");
			put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", "dd-MM-yyyy HH:mm");
			put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy-MM-dd HH:mm");
			put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$", "MM/dd/yyyy HH:mm");
			put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy/MM/dd HH:mm");
			put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMM yyyy HH:mm");
			put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMMM yyyy HH:mm");
			put("^\\d{14}$", "yyyyMMddHHmmss");
			put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss");
			put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd-MM-yyyy HH:mm:ss");
			put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd HH:mm:ss");
			put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MM/dd/yyyy HH:mm:ss");
			put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy/MM/dd HH:mm:ss");
			put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMM yyyy HH:mm:ss");
			put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMMM yyyy HH:mm:ss");
		}
	};

	/**
	 * Determine SimpleDateFormat pattern matching with the given date string.
	 * Returns null if format is unknown. You can simply extend DateUtil with
	 * more formats if needed.
	 * 
	 * @param dateString
	 *            The date string to determine the SimpleDateFormat pattern for.
	 * @return The matching SimpleDateFormat pattern, or null if format is
	 *         unknown.
	 * @see SimpleDateFormat
	 */
	public static String determineDateFormat(String dateString)
	{
		for (String regexp : DATE_FORMAT_REGEXPS.keySet())
		{
			if (dateString.toLowerCase().matches(regexp))
			{
				return DATE_FORMAT_REGEXPS.get(regexp);
			}
		}
		return null; // Unknown format.
	}

	public Date substractDaysToDate(Date date, Integer days)
	{
		Calendar cal = null;
		try
		{
			if (date == null || days == null)
			{
				return null;
			}

			cal = getCalendar(date);
			cal.add(Calendar.DATE, -days);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		return cal.getTime();
	}

	public Date addDaysToDate(Date date, Integer days)
	{
		Calendar cal = null;
		try
		{
			if (date == null || days == null)
			{
				return null;
			}
			cal = getCalendar(date);
			cal.add(Calendar.DATE, +days);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		return cal.getTime();
	}

	public Float daysBetweenDates(Date from, Date to)
	{
		if (from == null || to == null)
		{
			return null;
		}
		float difference = from.getTime() - to.getTime();
		return (difference / (1000 * 60 * 60 * 24));
	}

	public Date getToday()
	{
		return new Date();
	}

	public String getTodayForStorage()
	{
		return formatForStorage( new Date() );
	}
	public Date getThisMonday()
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		return c.getTime();
	}
	public Date getThisMonday(String inDate)
	{
		Calendar c = Calendar.getInstance();
		c.setTime(parseFromStorage(inDate));
		c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		return c.getTime();
	}
	public Collection getWeeks(int inCount)
	{
		Collection weeks = new ArrayList();
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		c.add(Calendar.DAY_OF_YEAR, inCount * -7);

		for (int i = 0; i < inCount; i++)
		{
			weeks.add(c.getTime());
			c.add(Calendar.DAY_OF_YEAR, 7);
		}
		for (int i = 0; i < inCount; i++)
		{
			weeks.add(c.getTime());
			c.add(Calendar.DAY_OF_YEAR, 7);
		}
		return weeks;
		
	}

	public boolean newerThan(Date inDate, Date inDate2)
	{
		if( inDate == null && inDate2 != null)
		{
			return false;
		}
		if( inDate != null && inDate2 == null)
		{
			return true;
		}
		int newer = inDate.compareTo(inDate2);
		return newer == 1;
	}
	

	

}
