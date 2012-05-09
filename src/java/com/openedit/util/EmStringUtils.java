package com.openedit.util;

import java.util.Arrays;
import java.util.List;

public class EmStringUtils
{
	public static List<String> split(String inText)
	{
		if( inText == null )
		{
			return null;
		}
		String value = inText.replace(',', ' ').replace('\r', ' ').replace('\n', ' ');

		String[] paths = org.apache.commons.lang.StringUtils.split(value);
		return Arrays.asList(paths);

	}
}
