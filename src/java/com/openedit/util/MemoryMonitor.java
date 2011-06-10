/*
 * Created on Dec 4, 2004
 */
package com.openedit.util;

import java.math.BigDecimal;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class MemoryMonitor
{
	public static final BigDecimal MEG = new BigDecimal( 1024 * 1024 );
	
	public String getTotalMemory()
	{
		return print(  Runtime.getRuntime().totalMemory() );
	}
	private String print( long inTotal )
	{
		BigDecimal total = new BigDecimal( inTotal );
		return  total.divide( MEG, 2, BigDecimal.ROUND_HALF_UP ) + " MB";
	}
	public String getFreeMemory()
	{
		return print( Runtime.getRuntime().freeMemory() );
	}
	public String getMaxMemory()
	{
		return print( Runtime.getRuntime().maxMemory() );
	}


}
