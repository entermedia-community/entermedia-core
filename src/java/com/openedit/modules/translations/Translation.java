/*
 * Created on Dec 6, 2005
 */
package com.openedit.modules.translations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openedit.page.PageProperty;

public class Translation
{
	public static final String PREFIX = "text.";
	protected List fieldLanguages;
	protected String  fieldSelectedLang;
	private static final Log log = LogFactory.getLog(Translation.class);
	public List getLanguages()
	{
		if (fieldLanguages == null)
		{
			fieldLanguages = new ArrayList();
		}
		return fieldLanguages;
	}
	public List getEditLanguages()
	{
		if( getLanguages().size() == 0)
		{
			return Collections.EMPTY_LIST;
		}
		
		List sub = new ArrayList();
		for (Iterator iter = getLanguages().iterator(); iter.hasNext();)
		{
			Language lang = (Language) iter.next();
			if( lang.getRootDirectory().length() > 0)
			{
				sub.add(lang);
			}
		}
		return sub;
	}

	public void setLanguages(List inLanguages)
	{
		fieldLanguages = inLanguages;
	}

	public void addLanguage(Language inLang)
	{
		getLanguages().add(inLang);
	}
	public boolean isSelected(Language inLang)
	{
		if( inLang.getId().equals(getSelectedLang()))
		{
			return true;
		}
		return false;
	}
	public String getSelectedLang()
	{
		return fieldSelectedLang;
	}

	public void setSelectedLang(String inSelectedLocale)
	{
		fieldSelectedLang = inSelectedLocale;
	}

	public void sort()
	{
		Collections.sort(getLanguages(), new Comparator() 
		{
			public int compare(Object inO1, Object inO2)
			{
				Language lang1 = (Language)inO1;
				Language lang2 = (Language)inO2;
				return lang1.getId().compareTo(lang2.getId());
			}
		});
	}

	public Language getLanguage(String inEid)
	{
		for (Iterator iter = getLanguages().iterator(); iter.hasNext();)
		{
			Language lang = (Language) iter.next();
			if( lang.getId().equals(inEid))
			{
				return lang;
			}
		}
		return null;
	}

	public void removeLanguage(Language inSelectedlan)
	{
		getLanguages().remove(inSelectedlan);
		setSelectedLang(null);
	}
	
	public List webTranslateProperties(Map inProperties, String inLocale)
	{
		for (Iterator iter = inProperties.keySet().iterator(); iter.hasNext();)
		{
			String name = (String) iter.next();
			PageProperty property = (PageProperty)inProperties.get(name);
			
			if ((name.startsWith(PREFIX) || name.equals("title")) 
					&& property.getValues().get(inLocale) == null)
			{
				String text = property.getValue();
				String translated = webTranslate(text, inLocale);
			
				property.setValue(translated, inLocale);
			}
		}
		 
		return createTranslationList(inProperties, inLocale);
	}

	public String webTranslate(String text, String inLocale)
	{
		text.replace("$", "_");
		if( inLocale.equals("de"))
		{
			//we found that google would not translate mixed case words
			text = text.toLowerCase();
		}
		String googlePage = "http://translate.google.com/translate_a/t";
		//http://translate.google.com/translate_a/t?client=t&text=Friends%20of&hl=en&sl=en&tl=es&pc=0&oc=0
		HttpClient client = new HttpClient();
		PostMethod method = new PostMethod( googlePage );
		method.addParameter(new NameValuePair("client", "t"));
		method.addParameter(new NameValuePair("text", text));
		method.addParameter(new NameValuePair("hl", "en"));
		method.addParameter(new NameValuePair("sl", "en"));
		method.addParameter(new NameValuePair("tl", inLocale));
		method.addParameter(new NameValuePair("pc", "0"));
		method.addParameter(new NameValuePair("oc", "1"));
//		try
//		{
//			log.info(method.getURI());
//		}
//		catch (URIException e1)
//		{
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		String translated = null;
		try
		{
			int statusCode = client.executeMethod( method );
		
		    if( statusCode != -1 ) 
		    {
		      String contents = method.getResponseBodyAsString();
			  method.releaseConnection();
			  
			 // {"trans":
			  int start =  4;
			  int end = contents.indexOf("\",\"",start);
			  if( start != -1 && end != -1)
			  {
		    	  translated = contents.substring(start, end);
		      }
		      else
		      {
				log.info("Could not translate into " + inLocale + " " + text );
		      }
		    }
		} catch (HttpException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return translated;
	}
	
//	This method takes a map of page properties and returns a list containing triples of:
	//1. The name of the property 
	//2. The default version of the property
	//3. The version of the property in the language specified by inLocale
	private List createTranslationList(Map inProperties, String inLocale)
	{
		List transList = new ArrayList();
		for (Iterator iter = inProperties.keySet().iterator(); iter.hasNext();)
		{
			String name = (String) iter.next();
			if (name.startsWith(PREFIX) || name.equals("title"))
			{
				PageProperty property = (PageProperty)inProperties.get(name);
				List propertyTriple = new ArrayList();
				propertyTriple.add(name);
				propertyTriple.add(property.getValue());
				propertyTriple.add(property.getValue(inLocale));
				transList.add(propertyTriple);
			}
		}
		return transList;
	}

}
