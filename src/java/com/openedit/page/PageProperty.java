/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/

/*--

 Copyright (C) 2001-2002 Anthony Eden.
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions, and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions, and the disclaimer that follows
    these conditions in the documentation and/or other materials
    provided with the distribution.

 3. The name "JPublish" must not be used to endorse or promote products
    derived from this software without prior written permission.  For
    written permission, please contact me@anthonyeden.com.

 4. Products derived from this software may not be called "JPublish", nor
    may "JPublish" appear in their name, without prior written permission
    from Anthony Eden (me@anthonyeden.com).

 In addition, I request (but do not require) that you include in the
 end-user documentation provided with the redistribution and/or in the
 software itself an acknowledgement equivalent to the following:
     "This product includes software developed by
      Anthony Eden (http://www.anthonyeden.com/)."

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR(S) BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.

 For more information on JPublish, please see <http://www.jpublish.org/>.

 */
package com.openedit.page;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * A name/value property which is accessible to a specific page.
 *
 * @author Anthony Eden
 */
public class PageProperty
{
	private Map values;
	private String name;
	protected String fieldPath;

	/**
	 * Construct a page property with the given name.  The name must be a non-null value.
	 *
	 * @param name The name of the page property
	 *
	 * @throws IllegalArgumentException DOCME
	 */
	public PageProperty(String name)
	{
		if (name == null)
		{
			throw new IllegalArgumentException("Property name cannot be null");
		}

		this.name = name;
		values = new HashMap();
	}

	/**
	 * Get the property name.
	 *
	 * @return The property name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Set the property value for the given Locale.
	 *
	 * @param value The value
	 * @param locale The Locale
	 */
	public void setValue(String value, Locale locale)
	{
		StringBuffer localeString = new StringBuffer();
		if (locale != null)
		{
			String language = locale.getLanguage();
			String country = locale.getCountry();
			String variant = locale.getVariant();

			localeString.append(language );
			if ( country != null)
			{
				localeString.append("_");
				localeString.append(country);
			}
			if ( variant != null)
			{
				localeString.append("_");
				localeString.append(variant);
			}
		}
		setValue(value, localeString.toString() );
	}

	/**
	 * Set the property value for the given locale String.  The locale String should be in the form
	 * language_country_variant as described in the Locale.toString() method.
	 *
	 * @param value The value
	 * @param locale The locale String
	 */
	public void setValue(String value, String locale)
	{
		if (locale == null)
		{
			locale = "";
		}

		values.put(locale, value);
	}

	/**
	 * Get the property value using the default Locale.
	 *
	 * @return The property value using the default Locale
	 */
	public String getValue()
	{
		return getValue(Locale.getDefault());
	}

	/**
	 * Get the value for the given locale.  This method will try to find the most suitable locale
	 * by searching the property values in the following manner:
	 * 
	 * <p>
	 * result of locale.toString() language + "_" + country + "_" + variant<br>
	 * language + "_" + country<br>
	 * langauge<br>
	 * ""
	 * </p>
	 * This method may return null if no suitable value is found.
	 *
	 * @param locale The locale
	 *
	 * @return The value or null
	 */
	public String getValue(Locale locale)
	{
		if( locale == null)
		{
			locale = Locale.getDefault();
		}
		String language = locale.getLanguage();
		String country = locale.getCountry();
		String variant = locale.getVariant();
		return getValue( language, country, variant);
	}
	public String getValue(String inLocale)
	{
		if (inLocale == null)
		{
			inLocale = Locale.getDefault().toString();
		}
		
		String lang = "";
		String country = "";
		String variant = "";

		int first = inLocale.indexOf('_');
		if( first == -1)
		{
			lang = inLocale;
		}
		else
		{
			lang = inLocale.substring(0,first);
			int second = inLocale.indexOf(first, '_');
			if( second == -1 )
			{
				country = inLocale.substring(first + 1);
			}
			else
			{
				country = inLocale.substring(first, second);
				variant = inLocale.substring(second + 1);
			}
		}
		return getValue(lang,country, variant);
	}

	public String getValue(String language, String country, String variant)
	{
		if( "default".equals(language) )
		{
			language = "";
		}
		String value = null;

		/*		//this check might be a duplicate of the next one below. TODO: Remove it?
				value = (String) values.get(locale.toString());

				if (value != null)
				{
					return value;
				}
		*/
				//Go from Specific to generatic
				if (variant != null)
				{
					value = (String) values.get(language + "_" + country + "_" + variant);

					if (value != null)
					{
						return value;
					}
				}

				if (country != null)
				{
					value = (String) values.get(language + "_" + country);

					if (value != null)
					{
						return value;
					}
				}

				if (language != null)
				{
					value = (String) values.get(language);

					if (value != null)
					{
						return value;
					}
				}

				return (String) values.get("");
	}

	/**
	 * Get the Map of all locale values.
	 *
	 * @return A map of locale String/value pairs
	 */
	public Map getValues()
	{
		return values;
	}
	public String toString()
	{
		return getValue();
	}
	/**
	 * @return Returns the path.
	 */
	public String getPath() {
		return fieldPath;
	}
	/**
	 * @param inPath The path to set.
	 */
	public void setPath(String inPath) {
		fieldPath = inPath;
	}

	public void setValue(String inString)
	{
		setValue(inString,(String)null);
	
	}
	public boolean hasEntryForLocale(String locale){
		
		if(locale == null){
			locale = "";
		}
		String language = null;
		String country = null;
		String variant = null;
		
		String[] splits = locale.split("_");
		if(splits.length == 1){
			language = splits[0];
		}
		else if(splits.length==2){
			language = splits[0];
			country = splits[1];
		}
		else if(splits.length==3){
			language = splits[0];
			country = splits[1];
			variant = splits[2];
		}
		
		
		
		String value = null;

				if (variant != null)
				{
					value = (String) values.get(language + "_" + country + "_" + variant);

					if (value != null)
					{
						return true;
					}
				}

				if (country != null)
				{
					value = (String) values.get(language + "_" + country);

					if (value != null)
					{
						return true;
					}
				}

				if (language != null)
				{
					value = (String) values.get(language);

					if (value != null)
					{
						return true;
					}
				}
				return false;
	}
}
