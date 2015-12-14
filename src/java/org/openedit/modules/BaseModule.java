/*
 * Created on Dec 29, 2004
 */
package org.openedit.modules;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openedit.ModuleManager;
import org.openedit.Secured;
import org.openedit.WebPageRequest;
import org.openedit.data.SearcherManager;
import org.openedit.page.manage.PageManager;
import org.openedit.users.User;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

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

//	public UserManager getUserManager(WebPageRequest inReq)
//	{
//		String catalogid = inReq.findValue("catalogid");
//		
//		return (UserManager) getModuleManager().getBean(catalogid, "userManager");
//		
//	}
	
	
	
	protected SearcherManager getSearcherManager()
	{
		return (SearcherManager)getBeanFactory().getBean( "searcherManager" );
	}

	public BeanFactory getBeanFactory()
	{
		return fieldBeanFactory;
	}
	public void setBeanFactory( BeanFactory beanFactory ) throws BeansException
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
