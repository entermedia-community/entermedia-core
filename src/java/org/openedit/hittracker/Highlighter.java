package org.openedit.hittracker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openedit.MultiValued;

public class Highlighter
{
	public static final Pattern WORDS = Pattern.compile("[a-zA-Z\\d]+");

	public String highlight(String input, String text, int cutoff, boolean addhtml)
	{
		//StringBuffer output = new StringBuffer();

		if (text != null && input != null && input.length() > 1)
		{
			Matcher m = WORDS.matcher(input);

			String[] parsedkeywords = MultiValued.VALUEDELMITER.split(text);
			String[] parsedkeywordslower = MultiValued.VALUEDELMITER.split(text.toLowerCase());
			StringBuffer out = new StringBuffer();

			while (m.find())
			{
				String searchfor = m.group();

				for (int i = 0; i < parsedkeywordslower.length; i++)
				{
					int start = parsedkeywordslower[i].indexOf(searchfor.toLowerCase());
					if (start > -1)
					{
						if (addhtml)
						{
							if (start > 0)
							{
								String part1 = parsedkeywords[i].substring(0, start);
								out.append(part1);
							}
							out.append("<b>");
							out.append(searchfor);
							out.append("</b>");
							String partend = parsedkeywords[i].substring(start + searchfor.length(), parsedkeywords[i].length());
							out.append(partend);
						}
						else
						{
							out.append(parsedkeywords[i]);
						}
						out.append(" ");
						continue;
					}

				}
			}
			/*
			 * Matcher mnext = WORDS.matcher(input); mnext.find(); int
			 * foundchars = 0; int maxchars = 52; StringBuffer out = new
			 * StringBuffer(); String ending = null; while( m.find() &&
			 * foundchars < maxchars ) { String searchfor = m.group(); int start
			 * = text.toLowerCase().indexOf(searchfor.toLowerCase()); if( start
			 * > -1) { if( out.length() == 0) { int cutstart = start - cutoff/2;
			 * cutstart = Math.max(0,cutstart); if(cutstart > 0) {
			 * out.append("..."); } out.append( text.substring(cutstart,start));
			 * } else { //out.append("..."); } if( addhtml) { out.append("<b>");
			 * } out.append(searchfor); if( addhtml) { out.append("</b>"); }
			 * //get the next space int startspace = 0; //if( start > 0) ////
			 * if( out.length() > 0) { startspace = text.indexOf("|",start);
			 * //Next space } int max = Math.min(start + searchfor.length() +
			 * startspace, text.length()); ending= text.substring(start +
			 * searchfor.length() ,max); if( mnext.find() ) { if(
			 * !ending.toLowerCase().contains(mnext.group().toLowerCase()) ) {
			 * out.append(ending); } } } foundchars = out.length(); } if( out !=
			 * null && !out.toString().endsWith(ending) ) { out.append(ending);
			 * if(text.length() > 0) { out.append("..."); } }
			 * 
			 * if( out.length() > 0) { return out.toString(); }
			 */
			if (out.length() > 0)
			{
				return out.toString();
			}
		}
		return null;
	}
}
