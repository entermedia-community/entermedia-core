package org.openedit.util;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

public class HttpSharedConnection
{
	
	Charset UTF8 = Charset.forName("UTF-8");
	ContentType contentType = ContentType.create("text/plain", UTF8);
	ContentType octectType = ContentType.create("application/octect-stream", UTF8);

	protected HttpClient fieldHttpClient;
	
	public HttpClient getSharedClient()
	{
		if (fieldHttpClient == null)
		{
			RequestConfig globalConfig = RequestConfig.custom()
		            .setCookieSpec(CookieSpecs.DEFAULT)
		            .build();
			fieldHttpClient = HttpClients.custom()
		            .setDefaultRequestConfig(globalConfig)
		            .build();
		}

		return fieldHttpClient;
	}



	public void reset()
	{
		fieldHttpClient = null;
	}
	
	public HttpResponse sharedPost(String path, Map<String,String> inParams)
	{
		try
		{
			HttpPost method = new HttpPost(path);
			method.setEntity(build(inParams));
			HttpResponse response2 = getSharedClient().execute(method);
			return response2;
		}
		catch ( Exception ex )
		{
			throw new RuntimeException(ex);
		}
	}

	public HttpResponse sharedGet(String inUrl)
	{
		try
		{
			HttpGet method = new HttpGet(inUrl);
			HttpResponse response2 = getSharedClient().execute(method);
			return response2;
		}
		catch ( Exception ex )
		{
			throw new RuntimeException(ex);
		}
	}
	
	protected HttpEntity build(Map <String, String> inMap){
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

		for (Iterator iterator = inMap.keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			String val = inMap.get(key);
			  nameValuePairs.add(new BasicNameValuePair(key, val));

			
		}
		 return new UrlEncodedFormEntity(nameValuePairs, UTF8);

		
		
	}
}
