/*
 * Created on Dec 6, 2005
 */
package org.openedit.modules.translations;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openedit.page.PageProperty;
import org.openedit.util.URLUtilities;

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
		String googlePage = "https://www.googleapis.com/language/translate/v2";
		//http://translate.google.com/translate_a/t?client=t&text=Friends%20of&hl=en&sl=en&tl=es&pc=0&oc=0
		//DefaultHttpClient client = new DefaultHttpClient();
		//HttpClient client = HttpClientBuilder.create().setUserAgent("Mozilla/5.0 (Mobile; rv:14.0) Gecko/14.0 Firefox/14.0").build();
		String translated = null;

		try
		{
		HttpClient client = URLUtilities.createTrustingHttpClient().setUserAgent("Mozilla/5.0 (Mobile; rv:14.0) Gecko/14.0 Firefox/14.0").build();
		HttpPost httpPost = new HttpPost( googlePage );
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		
		
		//https://www.googleapis.com/language/translate/v2?key=INSERT-YOUR-KEY&q=hello%20world&source=en&target=de
		
		nvps.add(new BasicNameValuePair("key","AIzaSyD5Hjc70IgTTbcgZK5HSbtxMu2oTzTILps"));
		nvps.add(new BasicNameValuePair("q", text));
		nvps.add(new BasicNameValuePair("source", "en"));
		nvps.add(new BasicNameValuePair("target", inLocale));
//		nvps.add(new BasicNameValuePair("client", "t"));
//		nvps.add(new BasicNameValuePair("text", text));
//		nvps.add(new BasicNameValuePair("hl", "en"));
//		nvps.add(new BasicNameValuePair("sl", "en"));
//		nvps.add(new BasicNameValuePair("tl", inLocale));
//		nvps.add(new BasicNameValuePair("pc", "0"));
//		nvps.add(new BasicNameValuePair("oc", "1"));
//		nvps.add(new BasicNameValuePair("ie", "UTF-8"));
		//		try
//		{
//			log.info(method.getURI());
//		}
//		catch (URIException e1)
//		{
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		httpPost.setHeader("X-HTTP-Method-Override","GET");
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			HttpResponse response2 = client.execute(httpPost);
		
		    if(response2.getStatusLine().getStatusCode() != -1 ) 
		    {
		    	HttpEntity entity2 = response2.getEntity();
		    	String charset = "UTF-8";
//		    	Header[] headers = response2.getHeaders("Content-Type");
//		    	if (headers!=null && headers.length > 0)
//		    	{
//		    		String val = headers[0].getValue();
//			    	int index =-1;
//			    	if ((index = val.indexOf("charset=")) != -1)
//			    	{
//			    		charset = val.substring(index+"charset=".length()).trim();
//			    		if (charset.isEmpty())//fail-safe
//		    			{
//			    			charset = "UTF-8";
//		    			}
//			    	}
//		    	}
		    	//log.info(EntityUtils.toString(entity2));
		    	//StringWriter out = new StringWriter();
		    	JSONObject config = (JSONObject)new JSONParser().parse(new InputStreamReader( entity2.getContent(), charset ));
		    	JSONObject data = (JSONObject)config.get("data");
		    	if( data != null)
		    	{
			    	Collection trans = (Collection)data.get("translations");
			    	if( trans.size() > 0)
			    	{
	    				JSONObject found = (JSONObject)trans.iterator().next();
			    		translated = (String)found.get("translatedText"); 
			    	}
		    	}
		    	else
		    	{
		    		log.info(config.toJSONString());
		    	}
		    	httpPost.releaseConnection();
		    }
		} catch (Exception e)
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
