/*
 * Created on Dec 29, 2004
 */
package com.openedit.modules;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openedit.data.SearcherManager;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import com.openedit.ModuleManager;
import com.openedit.Secured;
import com.openedit.WebPageRequest;
import com.openedit.page.manage.PageManager;
import com.openedit.users.User;
import com.openedit.users.UserManager;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public abstract class BaseModule implements BeanFactoryAware, Secured
{
	protected BeanFactory fieldBeanFactory;
	protected List fieldPermissions;
	protected List fieldRestrictedMethods;
	
	public PageManager getPageManager()
	{
		return (PageManager)getBeanFactory().getBean( "pageManager" );
	}

	public UserManager getUserManager()
	{
		return (UserManager)getBeanFactory().getBean( "userManager" );
	}
	
	protected SearcherManager getSearcherManager()
	{
		return (SearcherManager)getBeanFactory().getBean( "searcherManager" );
	}

	public BeanFactory getBeanFactory()
	{
		return fieldBeanFactory;
	}
	public void setBeanFactory( BeanFactory beanFactory )
	{
		fieldBeanFactory = beanFactory;
	}
	
	public ModuleManager getModuleManager()
	{
		return (ModuleManager) getBeanFactory().getBean( "moduleManager" );
	}
	public BaseModule getModule(String inName)
	{
		return getModuleManager().getModule( inName );
	}
	
	public File getRoot()
	{
		return (File) getBeanFactory().getBean( "root" );
	}

	/**
	 * 
	 */
	public void shutdown()
	{
	}

	public List getPermissions()
	{
		return fieldPermissions;
	}

	public void setPermissions(List inPermissions)
	{
		fieldPermissions = inPermissions;
	}
	
	public void setPermission(String inPermission)
	{
		fieldPermissions = new ArrayList();
		fieldPermissions.add(inPermission);
	}
	
	public void setRestrictedMethods(List inMethods)
	{
		fieldRestrictedMethods = inMethods;
	}
	
	public boolean canRun(WebPageRequest inReq, String inMethodName)
	{
		if (fieldPermissions == null)
		{
			return true;
		}
		
		if (getRestrictedMethods() == null || getRestrictedMethods().contains(inMethodName))
		{
			User user = inReq.getUser();
			if (user == null)
			{
				return false;
			}
			
			for (Iterator iter = getPermissions().iterator(); iter.hasNext();)
			{
				String permission = (String) iter.next();
				if (!user.hasPermission(permission))
				{
					return false;
				}
			}
		}
		
		return true;
	}

	public List getRestrictedMethods()
	{
		return fieldRestrictedMethods;
	}

	/**
	 * 
	 * @deprecated see WebPageRequest.findValue
	 * @param inName
	 * @param inRequest
	 * @return
	 */
	protected String findValue(String inName, WebPageRequest inRequest)
	{
		return inRequest.findValue(inName);
	}
}
