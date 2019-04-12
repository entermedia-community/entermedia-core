/*
 * Created on Dec 4, 2004
 */
package org.openedit.util;

import java.util.Date;

/**
 * @author Matthew Avery, mavery@einnovation.com
 * 
 * This is class is just a page-value for testing.
 */
public class Uptime
{
	private static final long DAY = 3600000 * 24;
	private static final long HOUR = 3600000;
	private static final long MINUTE = 60000;
	private static final long SECOND = 1000;
	protected Date fieldStartDate;
	
	public Uptime()
	{
		fieldStartDate = new Date();
	}
	
	public String toString()
	{
		Date now = new Date();
		long uptime = now.getTime() - getStartDate().getTime();
		long days = uptime / DAY;
		long remainder = uptime - ( days * DAY );
		long hours = remainder / HOUR;
		remainder = remainder - ( hours * HOUR );
		long minutes = remainder / MINUTE;
		remainder = remainder - ( minutes * MINUTE );
		long seconds = remainder / SECOND;
		return days + " days, " + hours + " hours, " + minutes + " minutes, " + seconds + " seconds" ; 
	}

	public Date getStartDate()
	{
		return fieldStartDate;
	}
}
