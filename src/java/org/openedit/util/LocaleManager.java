package org.openedit.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.openedit.page.manage.TextLabelManager;

public class LocaleManager
{
	protected Map fieldCache;
	protected DateStorageUtil fieldDateStorageUtil;
	public LocaleManager()
	{
	}
	public DateStorageUtil getDateStorageUtil()
	{
		if( fieldDateStorageUtil == null)
		{
			fieldDateStorageUtil = DateStorageUtil.getStorageUtil();
		}
		return fieldDateStorageUtil;
	}

	public void setDateStorageUtil(DateStorageUtil inDateStorageUtil)
	{
		fieldDateStorageUtil = inDateStorageUtil;
	}

	public Map getCache()
	{
		if (fieldCache == null)
		{
			fieldCache = new HashMap();
		}
		return fieldCache;
	}

	public void setCache(Map inCache)
	{
		fieldCache = inCache;
	}

	public Locale getLocale(String inLocale)
	{
		Locale loc = (Locale) getCache().get(inLocale);
		if (loc == null)
		{
			//TODO: parse out the parts of the string
			loc = parseLocaleString(inLocale);
			getCache().put(inLocale, loc);
		}
		return loc;
	}

	public final String getLang(String inLocale)
	{
		if (inLocale == null)
		{
			return "en";
		}

		Locale locale = getLocale(inLocale);
		return locale.getLanguage();
	}

	/**
	 * Parse the given localeString into a {@link java.util.Locale}.
	 * 
	 * This is the inverse operation of {@link java.util.Locale#toString 
	 * Locale's toString}.
	 * 
	 * @param localeString
	 *            the locale string
	 * @return a corresponding Locale instance
	 */
	public static Locale parseLocaleString(String localeString)
	{
		String[] parts = tokenizeToStringArray(localeString, "_ ", false, false);
		String language = (parts.length > 0 ? parts[0] : "");
		String country = (parts.length > 1 ? parts[1] : "");
		String variant = "";
		if (parts.length >= 2)
		{
			// There is definitely a variant, and it is everything after the country
			// code sans the separator between the country code and the variant.
			int endIndexOfCountryCode = localeString.indexOf(country) + country.length();
			// Strip off any leading '_' and whitespace, what's left is the variant.
			variant = trimLeadingCharacter(localeString.substring(endIndexOfCountryCode));
			if (variant.startsWith("_"))
			{
				variant = trimLeadingCharacter(variant);
			}
		}
		return (language.length() > 0 ? new Locale(language, country, variant) : null);
	}

	/**
	 * Tokenize the given String into a String array via a StringTokenizer.
	 * 
	 * The given delimiters string is supposed to consist of any number of
	 * delimiter characters. Each of those characters can be used to separate
	 * tokens. A delimiter is always a single character; for multi-character
	 * delimiters, consider using delimitedListToStringArray
	 * 
	 * @param str
	 *            the String to tokenize
	 * @param delimiters
	 *            the delimiter characters, assembled as String (each of those
	 *            characters is individually considered as delimiter)
	 * @param trimTokens
	 *            trim the tokens via String's trim
	 * @param ignoreEmptyTokens
	 *            omit empty tokens from the result array (only applies to
	 *            tokens that are empty after trimming; StringTokenizer will not
	 *            consider subsequent delimiters as token in the first place).
	 * @return an array of the tokens (null if the input String was null)
	 */
	public static String[] tokenizeToStringArray(String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens)
	{
		if (str == null)
		{
			return null;
		}
		StringTokenizer st = new StringTokenizer(str, delimiters);
		List<String> tokens = new ArrayList<String>();
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			if (trimTokens)
			{
				token = token.trim();
			}
			if (!ignoreEmptyTokens || token.length() > 0)
			{
				tokens.add(token);
			}
		}
		return tokens.toArray(new String[tokens.size()]);
	}

	/**
	 * Trim all occurences of the supplied leading character from the given
	 * String.
	 * 
	 * @param str
	 *            the string to check
	 * @param checker
	 *            the character checker
	 * @return the trimmed String
	 */
	public static String trimLeadingCharacter(String str)
	{
		if (hasLength(str) == false)
		{
			return str;
		}

		StringBuffer buf = new StringBuffer(str);
		while (buf.length() > 0 && Character.isLetterOrDigit(buf.charAt(0)))
		{
			buf.deleteCharAt(0);
		}
		return buf.toString();
	}

	/**
	 * Check that the given string param is neither null nor of length 0.
	 * 
	 * @param string
	 *            the string
	 * @return true if the String is not null and has length
	 */
	public static boolean hasLength(String string)
	{
		return (string != null && string.length() > 0);
	}
	public String formatDateForDisplay(String inStoredDate, String inLocale)
	{
		Date stored = getDateStorageUtil().parseFromStorage(inStoredDate);
		return formatDateForDisplay(stored, inLocale);
	}
	public String formatDateForDisplay(Date inDate, String inLocale)
	{
		if( inDate == null)
		{
			return "";
		}

		Locale loc = getLocale(inLocale);
		DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT, loc); 
				//new SimpleDateFormat("MM/dd/yyyy", loc);
		String formated = format.format(inDate);
		return formated;
	}
	public String formatDateTimeForDisplay(String inStoredDate, String inLocale)
	{
		Date stored = getDateStorageUtil().parseFromStorage(inStoredDate);
		return formatDateTimeForDisplay(stored, inLocale);
	}
	public String formatDateTimeForDisplay(Date inDate, String inLocale)
	{
		if( inDate == null)
		{
			return "";
		}
		Locale loc = getLocale(inLocale);
		DateFormat format = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.LONG, SimpleDateFormat.LONG,loc);
		String formated = format.format(inDate);
		return formated;
	}
	
	public String getAge(Date inDate, String inLocale )
	{
		long diff = System.currentTimeMillis() - inDate.getTime();
		String label = null;
		long minute = (diff / (1000 * 60)) % 60;
		long hour = (diff / (1000 * 60 * 60));
		String time = null;
		if( hour > 1)
		{
			//Now or minutes
			if( minute < 1)
			{
				label = "Now";
			}
			else
			{
				label = "Minutes";
				time = String.valueOf(minute);
			}
		}
		else if( hour < 24)
		{
			label = "Hours";
			time = String.valueOf( hour );
		}
		else
		{
			double days = (double)hour / 24d;
			time = String.valueOf( days );
			label = "Days";
		}
		String translated = getTextLabelManager().getAutoText("/system/data/","minutes", inLocale);
		return time + " " + translated;
	}
	protected TextLabelManager fieldTextLabelManager;
	public TextLabelManager getTextLabelManager()
	{
		return fieldTextLabelManager;
	}

	public void setTextLabelManager(TextLabelManager inTextLabelManager)
	{
		fieldTextLabelManager = inTextLabelManager;
	}

}
