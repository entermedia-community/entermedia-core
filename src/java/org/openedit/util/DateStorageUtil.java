package org.openedit.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DateStorageUtil {
	private static final Log log = LogFactory.getLog(DateStorageUtil.class);
	protected Map<String, DateFormat> fieldDateFormats;
	protected DateFormat fieldOldDashFormat;
	// 1 day = 24 hour x 60 minutes x 60 seconds x 1000 millisecond;
	private static final long PERIOR_OF_DAY = 24 * 60 * 60 * 1000;
	static final private ThreadLocal perThreadCache = new ThreadLocal();

	public DateFormat getDateFormat(String inFormat) {
		if (fieldDateFormats == null) {
			fieldDateFormats = new HashMap<String, DateFormat>();
		}
		DateFormat format = fieldDateFormats.get(inFormat);
		if (format == null) {
			format = new SimpleDateFormat(inFormat);
			format.setLenient(true);
			fieldDateFormats.put(inFormat, format);
		}
		return format;
	}

	protected DateFormat getStandardFormat() {
		return getDateFormat("yyyy-MM-dd HH:mm:ss Z");
	}

	protected DateFormat getExifFormat() {
		// 2010:09:20 13:20:53-04:00
		return getDateFormat("yyyy:MM:dd HH:mm:ssZ");
	}
	
	protected DateFormat getExifPhotoshopFormat(){
		//XMP-photoshop:DateCreated 
		return getDateFormat("yyyy:MM:dd HH:mm:ss.S");
	}

	protected DateFormat getStandardLogFormat() {
		return getDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	}

	protected DateFormat getOldColonFormat() {
		return getDateFormat("yyyy:MM:dd HH:mm:ss");
	}

	protected DateFormat getOldDashFormat() {
		return getDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	protected DateFormat getLuceneFormat() {
		if (fieldOldDashFormat == null) {
			fieldOldDashFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			fieldOldDashFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			fieldOldDashFormat.setLenient(true);
		}
		return fieldOldDashFormat;
	}

	protected DateFormat getOldShortStandardFormat() {
		return getDateFormat("MM/dd/yyyy");
	}

	public static DateStorageUtil getStorageUtil() {
		DateStorageUtil format = (DateStorageUtil) perThreadCache.get();
		if (format == null) {
			format = new DateStorageUtil();
			perThreadCache.set(format);
		}
		return format;
	}

	/*
	 * return the number of days between two day
	 */
	public static double compareStorateDateWithCurrentTime(String lastTime) {
		Date lastDateTime = DateStorageUtil.getStorageUtil().parseFromStorage(
				lastTime);
		// String currentDate =
		// DateStorageUtil.getStorageUtil().formatForStorage(new Date());
		// Calendar cal = Calendar.getInstance();
		// Need to add timezone here
		Date current = new Date();
		double duration = (double) (current.getTime() - lastDateTime.getTime())
				/ PERIOR_OF_DAY;
		return duration;
	}

	public Date parseFromStorage(String inStoredDate) {
		if (inStoredDate == null) {
			return null;
		}
		try {

			if (inStoredDate.length() > 21) {
				if (inStoredDate.contains("T")) {
					return getStandardLogFormat().parse(inStoredDate);
				}
				
				if (inStoredDate.substring(inStoredDate.length() - ".000".length()).contains("."))
				{
					return getExifPhotoshopFormat().parse(inStoredDate);
				}
				
				if (inStoredDate.indexOf("-") < 6) {
					return getStandardFormat().parse(inStoredDate);
				}

				if (inStoredDate.contains("+")) {
					inStoredDate = inStoredDate.substring(0,
							inStoredDate.length() - 3)
							+ inStoredDate.substring(inStoredDate.length() - 2); // remove
																					// the
																					// last
																					// :
					return getAlmostStandardPlus().parse(inStoredDate);
				}

				if (inStoredDate.endsWith(":00")) {
					inStoredDate = inStoredDate.substring(0,
							inStoredDate.length() - 3)
							+ "00";
				}
				
				
				
				return getExifFormat().parse(inStoredDate);
			}

			if (inStoredDate.length() > 18) {
				if (inStoredDate.contains("-")) {
					return getOldDashFormat().parse(inStoredDate);
				} else {
					return getOldColonFormat().parse(inStoredDate);
				}
			}

			if (inStoredDate.length() > 16) {
				// 5/16/00, 11:01 AM 17chars
				if (inStoredDate.contains(",")) {
					return getDateFormat("dd/MM/yyyy, hh:mm a").parse(
							inStoredDate);
				}
				// 08.30.00 02:18 AM
				if (inStoredDate.contains(".")) {
					return getDateFormat("MM.dd.yyyy hh:mm a").parse(
							inStoredDate);
				}
			}

			// TODO: Deal with military time?

			if (inStoredDate.length() > 13 && inStoredDate.contains("/")) {
				return getSlashedDateFormat().parse(inStoredDate);
			}

			if (inStoredDate.length() > 13) {
				return getLuceneFormat().parse(inStoredDate);
			}
			if (inStoredDate.length() > 5) {
				return getOldShortStandardFormat().parse(inStoredDate);
			}
		} catch (Exception ex) {
			log.debug("Could not parse date " + inStoredDate);
		}
		return null;
	}

	private DateFormat getAlmostStandardPlus() {
		// 2008:04:09 07:47:24+06:00
		return getDateFormat("yyyy:MM:dd HH:mm:ss+Z");
	}

	public DateFormat getSlashedDateFormat() {
		return getDateFormat("dd/MM/yyyy hh:mm");
	}

	public String formatForStorage(Date inDate) {
		String storage = getStandardFormat().format(inDate);
		return storage;
	}

	public String formatDate(String inDate, String inFormat) {
		Date date = parseFromStorage(inDate);
		DateFormat formater = getDateFormat(inFormat);
		return date != null ? formater.format(date) : null;
	}

	public String checkFormat(String inValue) {
		if (inValue.length() > 21) {
			if (!inValue.contains("T") && inValue.indexOf("-") < 6 && 
					!inValue.substring(inValue.length() - ".000".length()).contains(".")) {
				return inValue;
			}
		}
		Date clean = parseFromStorage(inValue);
		if (clean == null) {
			return inValue;
		}
		return formatForStorage(clean);
	}

	public int getDiffYears(Date first, Date last) {
		Calendar a = getCalendar(first);
		Calendar b = getCalendar(last);
		int diff = b.get(b.YEAR) - a.get(b.YEAR);
		if (a.get(a.MONTH) > b.get(a.MONTH)
				|| (a.get(a.MONTH) == b.get(a.MONTH) && a.get(a.DATE) > b
						.get(a.DATE))) {
			diff--;
		}
		return diff;
	}

	public Calendar getCalendar(Date date) {
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(date);
		return cal;
	}

	public int getDiffYears(String first, String last) {
		Date one = parseFromStorage(first);
		Date two= null;
		
		if("now".equals(last)){
			two = new Date();
		}else{
			two = parseFromStorage(last);
		}
		if(one != null && two != null){
			return getDiffYears(one, two);
			
		} 
		else{ 
			return -1;
		}
		
	}
}
