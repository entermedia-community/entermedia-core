package org.openedit.util;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

public class HttpRequestBuilder
{
	MultipartEntityBuilder builder = MultipartEntityBuilder.create();
	Charset UTF8 = Charset.forName("UTF-8");
	ContentType contentType = ContentType.create("text/plain", UTF8);
	ContentType octectType = ContentType.create("application/octect-stream", UTF8);

	public void addPart(String inKey, String inValue)
	{
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
}