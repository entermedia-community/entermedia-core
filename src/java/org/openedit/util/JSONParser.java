package org.openedit.util;

import java.io.StringReader;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openedit.OpenEditException;

public class JSONParser
{
	private static final Log log = LogFactory.getLog(JSONParser.class);

	public Collection parseCollection(String inText)
	{
		org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
		try
		{
			Collection structureDef = (Collection) parser.parse(inText);
			return structureDef;
		}
		catch(Throwable ex)
		{
			log.error("Could not parse " + inText);
			throw new OpenEditException(ex);
		}
	}

	
	public Map parseMap(String inText)
	{
		try
		{
			org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
			Map structureDef = (Map) parser.parse(inText);
			return structureDef;
		}
		catch(Throwable ex)
		{
			log.error("Could not parse " + inText);
			throw new OpenEditException(ex);
		}
	}

	public JSONObject parse(String inText)
	{
		try
		{
			org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
			JSONObject structureDef = (JSONObject) parser.parse(inText);
			return structureDef;
		}
		catch(Throwable ex)
		{
			log.error("Could not parse " + inText);
			throw new OpenEditException(ex);
		}
	}
	
	public JSONArray parseJSONArray(String inText)
	{
		try
		{
			org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
			JSONArray structureDef = (JSONArray) parser.parse(inText);
			return structureDef;
		}
		catch(Throwable ex)
		{
			log.error("Could not parse " + inText);
			throw new OpenEditException(ex);
		}
	}

	public JSONObject parse(StringReader inText)
	{
		try
		{
			org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
			JSONObject structureDef = (JSONObject) parser.parse(inText);
			return structureDef;
		}
		catch(Throwable ex)
		{
			log.error("Could not parse " + inText);
			throw new OpenEditException(ex);
		}
	}

	
}
