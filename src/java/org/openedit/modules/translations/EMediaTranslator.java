package org.openedit.modules.translations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openedit.Data;
import org.openedit.ModuleManager;
import org.openedit.data.SearcherManager;
import org.openedit.util.HttpSharedConnection;
import org.openedit.util.URLUtilities;

public class EMediaTranslator implements Translator
{
	private static final Log log = LogFactory.getLog(EMediaTranslator.class);
	protected HttpSharedConnection fieldConnection;
	protected String fieldApiKey;
	
	public HttpSharedConnection getConnection()
	{
		if (fieldConnection == null)
		{
			fieldConnection = new HttpSharedConnection();
			fieldConnection.addSharedHeader("Authorization", "Bearer " + getApiKey());
		}
		return fieldConnection;
	}
	
	protected String getApiKey()
	{
	  	if( fieldApiKey == null)
	  	{
	    	SearcherManager searchermanager = (SearcherManager)getModuleManager().getBean("system", "searcherManager");
			Data translatekey = searchermanager.getCachedData("system", "systemsettings", "translatekey");
			fieldApiKey = "4b37163c-11dd-4a14-be24-f8fc318f703e"; //Old
			if( translatekey  != null && translatekey.get("value") != null && !translatekey .get("value").isEmpty())
			{
				fieldApiKey = translatekey.get("value");
	    	}
	  	}
		return fieldApiKey;
	}

	public void setConnection(HttpSharedConnection inConnection)
	{
		fieldConnection = inConnection;
	}

	//TODO Support an array of translations
	@Override
	public String webTranslate(String inText, String inSourcelang, String inTargetLang)
	{
		long start = System.currentTimeMillis();
		HttpClient client = URLUtilities.createTrustingHttpClient().setUserAgent("Mozilla/5.0 (Mobile; rv:14.0) Gecko/14.0 Firefox/14.0").build();
		JSONObject payload = new JSONObject();
		
		payload.put("source", inSourcelang);
		
		JSONArray targets = new JSONArray();
		targets.add(inTargetLang);
		payload.put("target", targets);

		JSONArray q = new JSONArray();
		q.add(inText);
		payload.put("q", q);
		String url = "https://translate.emediaworkspace.com/translate";
		
		CloseableHttpResponse resp = getConnection().sharedPostWithJson(url, payload);
		StatusLine filestatus = resp.getStatusLine();
		if (filestatus.getStatusCode() != 200)
		{
			//Problem
			log.info( filestatus.getStatusCode() + " URL issue " + " " + url);
			return null;
		}
		JSONObject translations = getConnection().parseMap(resp);
		JSONObject fieldTranslations = (JSONObject) translations.get("translatedText");
		JSONArray inorder = (JSONArray)fieldTranslations.get(inTargetLang);
		String translatedText = (String)inorder.get(0); //Only one for now
		translatedText = translatedText.trim();
		long end = System.currentTimeMillis();
		log.info("Translated: [[" + inText + "]] to: [[" + translatedText  + "]] in " + inTargetLang + " time: " + (end - start) + "ms");

		return translatedText;
	}

	protected ModuleManager fieldModuleManager;
	
	
	public ModuleManager getModuleManager()
	{
		return fieldModuleManager;
	}

	public void setModuleManager(ModuleManager inModuleManager)
	{
		fieldModuleManager = inModuleManager;
	}
}
