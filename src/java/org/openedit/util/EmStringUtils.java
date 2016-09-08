package org.openedit.util;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.text.StrTokenizer;

public class EmStringUtils
{
	public static List<String> split(String inText)
	{
		if( inText == null )
		{
			return null;
		}
		String text= inText.replace("\r", "");
		text= text.replace("\n", ",").trim();
//		String value = inText.replace(',', '\n').replace('\r', '\n').replace('\n', ' ');
//
//		String[] paths = org.apache.commons.lang.StringUtils.split(value,'\n');
//		return Arrays.asList(paths);
		String[] vals = text.split("\\s*\\,\\s*");
		//StrTokenizer str = new StrTokenizer(text,'\n');
		//str.isIgnoreEmptyTokens();
		
		return Arrays.asList(vals);
		
	}
}
