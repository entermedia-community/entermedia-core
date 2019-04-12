/*
 * Created on Dec 6, 2004
 */
package org.openedit.page;

import java.util.Iterator;

import org.openedit.OpenEditRuntimeException;
import org.openedit.config.Configuration;
import org.openedit.config.XMLConfiguration;

/**
 * Basic store for an action
 */
public class PageAction
{
	protected String fieldActionName;
	protected String fieldMethodName;
	protected String fieldModuleName;
	protected boolean fieldIncludesAll = false;
	protected String fieldPath;
	
	protected Configuration fieldConfig;
	
	public PageAction()
	{
	}
	
	public PageAction( String inActionName ) 
	{
		fieldActionName = inActionName;
		int dotIndex = getActionName().indexOf('.');
		
		if (dotIndex == -1)
		{
			throw new OpenEditRuntimeException("Page action " + getActionName() + " not found, check format, e.g. \"ModuleName.method\" ");
		}
	
		fieldModuleName = getActionName().substring( 0, dotIndex );
		fieldMethodName = getActionName().substring( dotIndex + 1 );
	}

	public String getMethodName()
	{
		return fieldMethodName;
	}
	public void setMethodName( String actionName )
	{
		fieldMethodName = actionName;
	}
	public Configuration getConfig()
	{
		if( fieldConfig == null)
		{
			fieldConfig = new XMLConfiguration();
		}
		return fieldConfig;
	}
	
	public void setConfig( Configuration element )
	{
		fieldConfig = element;
	}
	
	public String getModuleName()
	{
		return fieldModuleName;
	}
	public void setModuleName( String moduleName )
	{
		fieldModuleName = moduleName;
	}
	public String getActionName()
	{
		return fieldActionName;
	}

	public String toString()
	{
		return getActionName();
	}

	/**
	 * @param inString
	 * @return
	 */
	public String getChildValue(String inString)
	{
		if ( getConfig() == null)
		{
			return null;
		}
		return getConfig().getChildValue(inString);
	}
	
	public String getProperty(String inName)
	{
		String val = getConfig().getAttribute(inName);
		if( val != null)
		{
			return val;
		}
		for (Iterator iterator = getConfig().getChildIterator("property"); iterator.hasNext();)
		{
			Configuration	config = (Configuration) iterator.next();
			if( inName.equals(config.getAttribute("name")) )
			{
				return config.getValue();
			}
		}
		return null;
	}

	public String get(String inKey)
	{
		String val = getProperty(inKey);
		if( val == null)
		{
			val = getChildValue(inKey);
		}
		return val;
	}
	public void setProperty(String inId, String inValue)
	{
		getConfig().setAttribute(inId, inValue);
	}
	public boolean isIncludesAll()
	{
		return fieldIncludesAll;
	}

	public void setIncludesAll(boolean inIncludesAll)
	{
		fieldIncludesAll = inIncludesAll;
	}

	public String getPath()
	{
		return fieldPath;
	}

	public void setPath(String inPath)
	{
		fieldPath = inPath;
	}
}
