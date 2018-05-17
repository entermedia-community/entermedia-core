package org.openedit.util;

import java.io.File;
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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.openedit.OpenEditException;

public class HttpRequestBuilder
{
	MultipartEntityBuilder builder = MultipartEntityBuilder.create();
	
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


	public void addPart(String inKey, String inValue, String inType)
	{
		if( inValue == null)
		{
			return;
		}
		ContentType type = ContentType.create(inType, UTF8);

		builder.addPart(inKey,new StringBody(inValue, type));
	}
	
	
	public void addPart(String inKey, String inValue)
	{
		if( inValue == null)
		{
			return;
		}
		builder.addPart(inKey,new StringBody(inValue,contentType));
	}
	public void addPart(String inKey, File file)
	{
		addPart(inKey, file, file.getName());
	}
	public void addPart(String inKey, File file, String inName)
	{
		FileBody fileBody = new FileBody(file, octectType, inName);
		builder.addPart(inKey, fileBody);
	}	
	public HttpEntity build()
	{
		return builder.build();
	}
	
	public HttpEntity build(Map <String, String> inMap){
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

		for (Iterator iterator = inMap.keySet().iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			String val = inMap.get(key);
			  nameValuePairs.add(new BasicNameValuePair(key, val));

			
		}
		 return new UrlEncodedFormEntity(nameValuePairs, UTF8);

		
		
	}
	
	public HttpResponse post(String inUrl, Map inParams)
	{
		try
		{
			RequestConfig globalConfig = RequestConfig.custom()
		            .setCookieSpec(CookieSpecs.DEFAULT)
		            .build();
			HttpClient client = HttpClients.custom()
		            .setDefaultRequestConfig(globalConfig)
		            .build();
	
			HttpPost method = new HttpPost(inUrl);
			HttpResponse response2 = client.execute(method);
			return response2;
		}
		catch ( Exception ex )
		{
			throw new OpenEditException(ex);
		}
	}
	
	public void reset()
	{
		fieldHttpClient = null;
	}
	
	public HttpResponse sharedPost(String inUrl, Map inParams)
	{
		try
		{
			HttpPost method = new HttpPost(inUrl);
			HttpResponse response2 = getSharedClient().execute(method);
			return response2;
		}
		catch ( Exception ex )
		{
			throw new OpenEditException(ex);
		}
	}

	public HttpResponse sharedConnection(String inUrl)
	{
		try
		{
			HttpPost method = new HttpPost(inUrl);
			HttpResponse response2 = getSharedClient().execute(method);
			return response2;
		}
		catch ( Exception ex )
		{
			throw new OpenEditException(ex);
		}
	}
	
}
