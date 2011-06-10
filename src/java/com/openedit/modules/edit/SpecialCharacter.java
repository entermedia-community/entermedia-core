/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

/*
 * Created on Apr 22, 2003
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.openedit.modules.edit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * DOCUMENT ME!
 *
 * @author avery
 */
public class SpecialCharacter
{
	/**
	 * This function is a work around to deal with non UTF-8 encoding
	 * It will convert the most commonly found "strange" characters into
	 * standard HTML text
	 *
	 * @param s
	 *
	 * @return
	 */
	public static String escapeSpecialCharacters(String s)
	{
		if ( s == null )
		{
			return null;
		}
		StringBuffer sb = new StringBuffer();
		int n = s.length();

		for (int i = 0; i < n; i++)
		{
			char c = s.charAt(i);

			if (c > 127)
			{
				switch (c)
				{
					//	 case '<': sb.append("&lt;"); break;  // 
					//	 case '>': sb.append("&gt;"); break;  // // 
					//   case '&': sb.append("&amp;"); break;  // 
					//	 case '"': sb.append("&quot;"); break;  // 
					//	 case '\'': sb.append("&apos;"); break;  // 
					// try alpha substitutions first
					case '\u00E0':
						sb.append("&agrave;");

						break; // 

					case '\u00C0':
						sb.append("&Agrave;");

						break; // 

					case '\u00E2':
						sb.append("&acirc;");

						break; // 

					case '\u00C2':
						sb.append("&Acirc;");

						break; // 

					case '\u00E4':
						sb.append("&auml;");

						break; // 

					case '\u00C4':
						sb.append("&Auml;");

						break; // 

					case '\u00E5':
						sb.append("&aring;");

						break; // 

					case '\u00C5':
						sb.append("&Aring;");

						break; // 

					case '\u00E6':
						sb.append("&aelig;");

						break; // 

					case '\u00C6':
						sb.append("&AElig;");

						break; // 

					case '\u00E7':
						sb.append("&ccedil;");

						break; // 

					case '\u00C7':
						sb.append("&Ccedil;");

						break; // 

					case '\u00E8':
						sb.append("&eacute;");

						break; // 

					case '\u00C8':
						sb.append("&Eacute;");

						break; // 

					case '\u00E9':
						sb.append("&egrave;");

						break; // 

					case '\u00C9':
						sb.append("&Egrave;");

						break; // 

					case '\u00EA':
						sb.append("&ecirc;");

						break; // 

					case '\u00CA':
						sb.append("&Ecirc;");

						break; // 

					case '\u00EB':
						sb.append("&euml;");

						break; // 

					case '\u00CB':
						sb.append("&Euml;");

						break; // 

					case '\u00EF':
						sb.append("&iuml;");

						break; // 

					case '\u00CF':
						sb.append("&Iuml;");

						break; // 

					case '\u00F5':
						sb.append("&ocirc;");

						break; // 

					case '\u00D5':
						sb.append("&Ocirc;");

						break; // 

					case '\u00F6':
						sb.append("&ouml;");

						break; // 

					case '\u00D6':
						sb.append("&Ouml;");

						break; // 

					case '\u00F8':
						sb.append("&oslash;");

						break; // 

					case '\u00D8':
						sb.append("&Oslash;");

						break; // 

					case '\u00DF':
						sb.append("&szlig;");

						break; // 

					case '\u00F9':
						sb.append("&ugrave;");

						break; // 

					case '\u00D9':
						sb.append("&Ugrave;");

						break; //          

					case '\u00FB':
						sb.append("&ucirc;");

						break; //         

					case '\u00DB':
						sb.append("&Ucirc;");

						break; // 

					case '\u00FC':
						sb.append("&uuml;");

						break; // 

					case '\u00DC':
						sb.append("&Uuml;");

						break; // 

					case '\u00AE':
						sb.append("&reg;");

						break; //         

					case '\u00A9':
						sb.append("&copy;");

						break; //    

					case '\u20AC':
						sb.append("&euro;");

						break; // euro

					case '\u2013':
						sb.append("&ndash;");

						break; // euro
						
					case '\u2014':
						sb.append("&mdash;");

						break; // euro
						
					case '\u2018':
						sb.append("&lsquo;");

						break; // euro
						
					case '\u2019':
						sb.append("&rsquo;");

						break; // euro
						
					case '\u201C':
						sb.append("&ldquo;");

						break; // euro
						
					case '\u201D':
						sb.append("&rdquo;");

						break; // euro
						
					default:
						sb.append("&#" + Integer.toString(c) + ";");

						break; // 
				}
			}

			// end if
			else
			{
				if ( c == '&' )
				{
					sb.append( "&amp;" );
				}
				else
				{
					sb.append(c);
				}
			}
		}

		return sb.toString();
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param inString
	 *
	 * @return
	 */
	public static String toUnicode(String inString)
	{
		Map matches = new HashMap();

		// how big should this number be?
		for (int i = 0; i < 10000; i++)
		{
			String symbol = "&#" + Integer.toString(i) + ";";

			if (inString.indexOf(symbol) > -1)
			{
				matches.put(symbol, Integer.toHexString(i));
			}
		}

		addSpecialMatch(inString, matches, "&apos;", "'");
		addSpecialMatch(inString, matches, "&lt;", "<");
		addSpecialMatch(inString, matches, "&gt;", ">");
		addSpecialMatch(inString, matches, "&quote;", "\"");
		addSpecialMatch(inString, matches, "&amp;", "&");
		addSpecialMatch(inString, matches, "&agrave;", "\u00E0"); // 
		addSpecialMatch(inString, matches, "&Agrave;", "\u00C0"); // 
		addSpecialMatch(inString, matches, "&acirc;", "\u00E2"); // 
		addSpecialMatch(inString, matches, "&Acirc;", "\u00C2"); // 
		addSpecialMatch(inString, matches, "&auml;", "\u00E4"); // 
		addSpecialMatch(inString, matches, "&Auml;", "\u00C4"); // 
		addSpecialMatch(inString, matches, "&aring;", "\u00E5"); // 
		addSpecialMatch(inString, matches, "&Aring;", "\u00C5"); // 
		addSpecialMatch(inString, matches, "&aelig;", "\u00E6"); // 
		addSpecialMatch(inString, matches, "&AElig;", "\u00C6"); // 
		addSpecialMatch(inString, matches, "&ccedil;", "\u00E7"); // 
		addSpecialMatch(inString, matches, "&Ccedil;", "\u00C7"); // 
		addSpecialMatch(inString, matches, "&eacute;", "\u00E8"); // 
		addSpecialMatch(inString, matches, "&Eacute;", "\u00C8"); // 
		addSpecialMatch(inString, matches, "&egrave;", "\u00E9"); // 
		addSpecialMatch(inString, matches, "&Egrave;", "\u00C9"); // 
		addSpecialMatch(inString, matches, "&ecirc;", "\u00EA"); // 
		addSpecialMatch(inString, matches, "&Ecirc;", "\u00CA"); // 
		addSpecialMatch(inString, matches, "&euml;", "\u00EB"); // 
		addSpecialMatch(inString, matches, "&Euml;", "\u00CB"); // 
		addSpecialMatch(inString, matches, "&iuml;", "\u00EF"); // 
		addSpecialMatch(inString, matches, "&Iuml;", "\u00CF"); // 
		addSpecialMatch(inString, matches, "&ocirc;", "\u00F5"); // 
		addSpecialMatch(inString, matches, "&Ocirc;", "\u00D5"); // 
		addSpecialMatch(inString, matches, "&ouml;", "\u00F6"); // 
		addSpecialMatch(inString, matches, "&Ouml;", "\u00D6"); // 
		addSpecialMatch(inString, matches, "&oslash;", "\u00F8"); // 
		addSpecialMatch(inString, matches, "&Oslash;", "\u00D8"); // 
		addSpecialMatch(inString, matches, "&szlig;", "\u00DF"); // 
		addSpecialMatch(inString, matches, "&ugrave;", "\u00F9"); // 
		addSpecialMatch(inString, matches, "&Ugrave;", "\u00D9"); //          
		addSpecialMatch(inString, matches, "&ucirc;", "\u00FB"); //          
		addSpecialMatch(inString, matches, "&Ucirc;", "\u00DB"); // 
		addSpecialMatch(inString, matches, "&uuml;", "\u00FC"); // 
		addSpecialMatch(inString, matches, "&Uuml;", "\u00DC"); // 
		addSpecialMatch(inString, matches, "&reg;", "\u00AE"); //          
		addSpecialMatch(inString, matches, "&copy;", "\u00A9"); //   
		addSpecialMatch(inString, matches, "&euro;", "\u20AC"); // euro

		for (Iterator iter = matches.keySet().iterator(); iter.hasNext();)
		{
			String symbol = (String) iter.next();
			inString = inString.replaceAll(symbol, (String) matches.get(symbol));
		}

		return inString;
	}

	private static void addSpecialMatch(
		String inString, Map matches, String inSymbol, String inUnicode)
	{
		if (inString.indexOf(inSymbol) > -1)
		{
			matches.put(inSymbol, inUnicode);
		}
	}
}
