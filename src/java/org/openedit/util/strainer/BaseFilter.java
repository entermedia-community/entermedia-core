package org.openedit.util.strainer;

import java.util.HashMap;
import java.util.Map;

import org.openedit.config.Configuration;
import org.openedit.config.XMLConfiguration;

public class BaseFilter implements Filter
{
	protected Filter[] fieldFilters;
	protected String fieldValue;
	protected Map<String,String> fieldProperties;
	protected Configuration fieldConfiguration;
	
	public Filter[] getFilters()
	{
		return fieldFilters;
	}

	public boolean passes(Object inObj) throws FilterException
	{
		// TODO Auto-generated method stub
		return false;
	}

	public String getType()
	{
		String name = getClass().getSimpleName();
		if( name.endsWith("Filter"))
		{
			name = name.substring(0,name.length() - "Filter".length());
		}
		return name;
	}
	public boolean isContainer()
	{
		return this instanceof CompositeFilter;
	}
	public boolean isDelegator()
	{
		return this instanceof DecoratorFilter;
	}

	public void addFilter(Filter inNode)
	{
		if( getFilters() != null)
		{
			Filter[] newlist = new Filter[getFilters().length + 1];
			System.arraycopy(getFilters(), 0, newlist, 0, getFilters().length);
			newlist[newlist.length-1] = inNode;
			fieldFilters = newlist;
		}
		else
		{
			fieldFilters = new Filter[] { inNode };
		}
	}

	public void removeFilter(Filter inNode)
	{
		if( getFilters() != null)
		{
			Filter[] newlist = new Filter[getFilters().length - 1];
			int count = 0;
			for (int i = 0; i < getFilters().length; i++)
			{
				Filter old = getFilters()[i];
				if( old != inNode)
				{
					newlist[count++] = old;
				}
			}
			fieldFilters = newlist;
		}
		
	}

	public void setValue(String inValue)
	{
		fieldValue = inValue;
	}
	
	public String getValue()
	{
		return fieldValue;
	}

	public Filter copy(FilterReader inReader, String inName)
	{
		XMLConfiguration config = new XMLConfiguration();
		
		FilterWriter writer = new FilterWriter();
		writer.addFilter(config,this);
		
	
		//Configuration child = (Configuration)config.getChildren().get(0);
		Filter done = inReader.readFilterCollection(config, inName);
		return done;
	}

	public void setProperty(String inKey, String inValue)
	{
		getProperties().put(inKey, inValue);
	}
	public Map<String,String> getProperties()
	{
		if (fieldProperties == null)
		{
			fieldProperties = new HashMap<String,String>(1);
		}

		return fieldProperties;
	}
	public String get(String inType)
	{
		String prop = getProperties().get(inType);
		if( prop == null && fieldConfiguration != null)
		{
			prop = getConfiguration().get(inType);
		}
		return prop;
	}
	public String toString()
	{
		return String.valueOf( getValue() );
	}

	@Override
	public void setConfiguration(Configuration inConfig) {
		fieldConfiguration = inConfig;
	}

	@Override
	public Configuration getConfiguration() {
		return fieldConfiguration;
	}

	
}
