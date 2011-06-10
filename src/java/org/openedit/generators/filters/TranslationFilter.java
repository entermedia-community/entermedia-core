package org.openedit.generators.filters;

import com.openedit.page.Page;

public class TranslationFilter extends TextReaderFilter
{
	protected Page page;
	protected String locale;
	
	public TranslationFilter(Page inPage, String inParams)
	{
		super(inPage.getReader(),inPage.getCharacterEncoding());
		page = inPage;
		int index = inParams.indexOf("&locale=");
		locale = inParams.substring(index + "&locale=".length(), inParams.length());
	}
	public StringBuffer replace(String inLastLine)
	{
		int bracket = inLastLine.indexOf("[[");
		if( bracket == -1)
		{
			return new StringBuffer(inLastLine);
		}
		//look for [[ and get the property to replace it with
		StringBuffer done = new StringBuffer(inLastLine.length() + 20);
		int start = 0;
		char[] line = inLastLine.toCharArray();
		while( bracket != -1 )
		{
			int end = inLastLine.indexOf("]]",bracket);
			if( end != -1 )
			{
				String key = inLastLine.substring(bracket + 2,end);
				String value = page.getText(key, locale);
				
				done.append(line,start,bracket - start); //everything up to this point
				done.append(value);
				start = end + 2;
				bracket = inLastLine.indexOf("[[",start);
			}
			else
			{
				done.append(line,start,line.length);				
				start = line.length;
				break; //no closing ]]
			}
		}
		if( start < line.length)
		{
			done.append(line,start,line.length - start);
		}
		return done;
	}
}
