package com.openedit.util.strainer;

import com.openedit.ModuleManager;
import com.openedit.OpenEditException;
import com.openedit.WebPageRequest;
import com.openedit.config.Configuration;
import com.openedit.config.XMLConfiguration;
import com.openedit.page.PageAction;

public class ActionFilter extends BaseFilter
{
	protected String fieldActionName;
	protected String fieldPermissionName;
	protected ModuleManager fieldModuleManager;
	protected Configuration fieldProperties;

	public ActionFilter(String inTarget, ModuleManager inModuleManager)
	{
		setActionName(inTarget);
		setModuleManager(inModuleManager);
		// TODO Auto-generated constructor stub
	}

	public boolean passes(Object inObj) throws FilterException
	{
		WebPageRequest req = (WebPageRequest)inObj;
		try
		{
			PageAction action = new PageAction(getActionName());
			action.setConfig(getConfig());
			req.setCurrentAction(action);
			Object returned = getModuleManager().execute(getActionName(), req);
			if( returned != null)
			{
				if( returned instanceof Boolean)
				{
					return ((Boolean)returned).booleanValue();
				}
			}
		}
		catch (OpenEditException e)
		{
			throw new FilterException(e);
		}
		Boolean value = (Boolean)req.getPageValue("can" + getPermissionName());
		if( value != null)
		{
			return value.booleanValue();
		}
		return false;
		
	}

	public Filter[] getFilters()
	{
		// TODO Auto-generated method stub
		return null;
	}


	public String getActionName()
	{
		return fieldActionName;
	}

	public void setActionName(String inActionName)
	{
		fieldActionName = inActionName;
	}

	public ModuleManager getModuleManager()
	{
		return fieldModuleManager;
	}

	public void setModuleManager(ModuleManager inModuleManager)
	{
		fieldModuleManager = inModuleManager;
	}

	public String getPermissionName()
	{
		return fieldPermissionName;
	}

	public void setPermissionName(String inPermissionName)
	{
		fieldPermissionName = inPermissionName;
	}

	public void addProperty(String inKey, String inValue)
	{
		if (fieldProperties == null)
		{
			fieldProperties = new XMLConfiguration("action");
			fieldProperties.setAttribute("name", getActionName());
		}
		fieldProperties.addChild(inKey).setValue(inValue);
	}

	public Configuration getConfig()
	{
		return fieldProperties;
	}

	public String getProperty(String inKey)
	{
		if( fieldProperties == null)
		{
			return null;
		}
		return (String)getConfig().getChildValue(inKey);
	}

	public void setConfiguration(Configuration inConfig)
	{
		fieldProperties = inConfig;
		
	}
	public String toString() {
		return "Action: " + getActionName();
	}

}
