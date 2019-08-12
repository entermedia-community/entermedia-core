package org.openedit.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import jdk.internal.jline.internal.Log;

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
		            .setConnectTimeout(15 * 1000)
		            .setSocketTimeout(120 * 1000)
		            .build();
			fieldHttpClient = HttpClients.custom().useSystemProperties()
		            .setDefaultRequestConfig(globalConfig)
		            .build();
		}
		return fieldHttpClient;
	}



	public void reset()
	{
		fieldHttpClient = null;
	}
	
	public CloseableHttpResponse sharedPost(String path, Map<String,String> inParams)
	{
		try
		{
			HttpPost method = new HttpPost(path);
			method.setEntity(build(inParams));
			CloseableHttpResponse response2 = (CloseableHttpResponse)getSharedClient().execute(method);
			return response2;
		}
		catch ( Exception ex )
		{
			throw new RuntimeException(ex);
		}
	}
	public CloseableHttpResponse sharedPost(HttpPost inPost)
	{
		try
		{
			CloseableHttpResponse response2 = (CloseableHttpResponse)getSharedClient().execute(inPost);
			return response2;
		}
		catch ( Exception ex )
		{
			throw new RuntimeException(ex);
		}
	}
	public void release(CloseableHttpResponse response2)
	{
		if( response2 == null)
		{
			return;
		}

		try
		{
			response2.close();
		}
		catch (IOException e)
		{
			Log.error("Could not close" ,e);
		}
	}
	public CloseableHttpResponse sharedGet(String inUrl)
	{
		try
		{
			HttpGet method = new HttpGet(inUrl);
			CloseableHttpResponse response2 = (CloseableHttpResponse) getSharedClient().execute(method);
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
