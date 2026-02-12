package org.openedit.modules.translations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openedit.Data;
import org.openedit.ModuleManager;
import org.openedit.data.SearcherManager;
import org.openedit.util.URLUtilities;

public class GoogleTranslate implements Translator
{
	private static final Log log = LogFactory.getLog(GoogleTranslate.class);

	protected ModuleManager fieldModuleManager;
	
	
	public ModuleManager getModuleManager()
	{
		return fieldModuleManager;
	}

	public void setModuleManager(ModuleManager inModuleManager)
	{
		fieldModuleManager = inModuleManager;
	}

	public String webTranslate(String text, String sourcelang, String inLocale)
	{
		try
		{
			HttpClient client = URLUtilities.createTrustingHttpClient().setUserAgent("Mozilla/5.0 (Mobile; rv:14.0) Gecko/14.0 Firefox/14.0").build();
			String response = webTranslate(client,text,sourcelang, inLocale);
			return response;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public String webTranslate(HttpClient client,String text, String sourcelang, String inLocale)
	{
		//text.replace("$", "_");
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
		HttpPost httpPost = new HttpPost( googlePage );
		
		//post.setEntity(new StringEntity(inBody.toString(), "UTF-8"));
		
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		
		
		//https://www.googleapis.com/language/translate/v2?key=INSERT-YOUR-KEY&q=hello%20world&source=en&target=de
		
		String key = lookUpKey();
		
		nvps.add(new BasicNameValuePair("key",key));
		nvps.add(new BasicNameValuePair("q", text));
		nvps.add(new BasicNameValuePair("source", sourcelang));
		nvps.add(new BasicNameValuePair("target", inLocale));
//		nvps.add(new BasicNameValuePair("format", "text"));
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
		httpPost.setEntity(new UrlEncodedFormEntity(nvps,"UTF-8"));
			HttpResponse response2 = client.execute(httpPost);
		
		    if(response2.getStatusLine().getStatusCode() != -1 ) 
		    {
		    	HttpEntity entity2 = response2.getEntity();
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
		    	//String encodedas = entity2.getContentEncoding().getValue();
		    	
		    	String returned = EntityUtils.toString(entity2);
		    	
		    	//JSONObject config = (JSONObject)new JSONParser().parse(new InputStreamReader( entity2.getContent(), charset ));
		    	JSONObject config = (JSONObject)new JSONParser().parse(returned);
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
    protected String lookUpKey()
	{
    	SearcherManager searchermanager = (SearcherManager)getModuleManager().getBean("system", "searcherManager");
    	Data translatekey = searchermanager.getCachedData("system", "systemsettings", "translatekey");
    	String key = "AIzaSyDOH0U9DIhnSrhhGlvCB6jD_orOjvVG5lw";// "AIzaSyD5Hjc70IgTTbcgZK5HSbtxMu2oTzTILps";
    	if( translatekey  != null && translatekey.get("value") != null && !translatekey .get("value").isEmpty())
    	{
    		key = translatekey.get("value");
    	}
    	return key;
	}
	
}
