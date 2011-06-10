/*
 * Created on Nov 19, 2004
 */
package com.openedit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.openedit.xml.XmlArchive;
import org.openedit.xml.XmlFile;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;

import com.openedit.modules.BaseModule;
import com.openedit.page.Page;
import com.openedit.page.PageAction;
import com.openedit.page.PageRequestKeys;
import com.openedit.page.PageSettings;
import com.openedit.page.manage.PageManager;
import com.openedit.util.PathUtilities;

/**
 * @author Matthew Avery, mavery@einnovation.com
 */
public class ModuleManager implements BeanFactoryAware, ShutdownList
{
	protected ConfigurableListableBeanFactory fieldBeanFactory;
	protected Set fieldLoadedBeans;
	protected XmlArchive fieldXmlArchive;
	protected Map fieldCatalogIdBeans;
	
	private static final Log log = LogFactory.getLog(ModuleManager.class);
	
	public void executePageAction( PageAction inAction, WebPageRequest inReq ) throws OpenEditException
	{
		if( log.isDebugEnabled() )
		{
			log.debug("Running "  +  inAction.getActionName() + " from  " + inAction.getPath()  + " " + inReq.getPath());
		}
		Object module = getBean( inAction.getModuleName() );
		if ( module == null )
		{
			throw new OpenEditException("Error attempting to execute page action " + inAction.getActionName() + ".  No module found: " + inAction.getModuleName());
		}
		String methodName = inAction.getMethodName();
		execMethod(module,methodName,  inAction,inReq);
		
	}

	public Object execute( String inFullName, WebPageRequest inReq) throws OpenEditException
	{
		int dot = inFullName.indexOf(".");

		if (dot == -1)
		{
			throw new OpenEditException("No command found" + inFullName);
		}
		else
		{
			String mod = inFullName.substring(0, dot);
			String function = inFullName.substring(dot + 1);
			Object module = getBean(mod);
			return execMethod(module,function,inReq);
		}
	}
	
	protected Object execMethod(Object module, String methodName,WebPageRequest inReq ) throws OpenEditException
	{
		return execMethod(module,methodName,null,inReq);
	}
	protected Object execMethod(Object module, String methodName,PageAction inAction,  WebPageRequest inReq ) throws OpenEditException
	{
		if (module instanceof Secured)
		{
			Secured secured = (Secured) module;
			if (!secured.canRun(inReq, methodName))
			{
				throw new OpenEditException("User does not have permission to run method " + methodName + " on " + module.getClass());
			}
		}
		Object returned = null;
		if ( inAction != null)
		{
			inReq.setCurrentAction(inAction);
		}
		try
		{
			//we only want to support webpagereq method for security reasons
			Method method = module.getClass().getMethod( methodName, new Class[]{ WebPageRequest.class } );
			if ( method != null )
			{			
				returned = method.invoke( module, new Object[]{ inReq } );
			}
		}
		catch ( InvocationTargetException ex)
		{
			Throwable cause = ex.getTargetException();
			if ( cause instanceof OpenEditException)
			{
				throw (OpenEditException)cause;
			}
			else if ( inAction!=null)
			{
				throw new OpenEditException(cause,inAction.getPath() +"#" + inAction.getActionName());
			}
			else
			{
				throw new OpenEditException(cause);
			}
		}
		catch (Exception e)
		{
			log.error(e);
			if ( inReq != null )
			{					
				log.error("When loading: " + inReq.getPath() + "#" + module.getClass().getName() + "." +  methodName);
			}
			throw new OpenEditException(e);
		}
		return returned;
	}
	
	public boolean runActions(WebPageRequest inReq)
	{
		String runpath = inReq.findValue("runpath");
		Page page  = getPageManager().getPage(runpath);
		WebPageRequest child = inReq.copy(page);
		
		if(page == null)
		{
			return false;
		}
		
		executePathActions(page, child);
		if( child.hasCancelActions() && !child.hasRedirected())
		{
			executePageActions(page, child);
		}
		return !child.hasCancelActions();
	}
	
	public void executePageActions( Page inPage, WebPageRequest inPageRequest ) throws OpenEditException
	{
		List actions = inPage.getPageActions();
		if (actions == null)
		{
			return;
		}
		for (Iterator iter = actions.iterator(); iter.hasNext();)
		{
			PageAction pageAction = (PageAction) iter.next();
			if ( inPageRequest.hasCancelActions() || inPageRequest.hasRedirected() || inPageRequest.hasForwarded() )
			{
				return;
			}
			executePageAction( pageAction, inPageRequest );
		}
	}
	public void executePathActions( Page inPage, WebPageRequest inPageRequest ) throws OpenEditException
	{
		List actions = inPage.getPathActions(); //a list of action sorted from lower pages up to the root
		List copy = condenseActions(actions );  //reverse the list with the root run first
		for (Iterator iter = copy.iterator(); iter.hasNext();)
		{
			//TODO: Add mime type checks to speed this up
			PageAction pageAction = (PageAction) iter.next();
			executePageAction( pageAction, inPageRequest );
			if ( inPageRequest.hasCancelActions() || inPageRequest.hasRedirected() || inPageRequest.hasForwarded() )
			{
				return;
			}
		}
		//This includes request actions
		String[] actionNames = inPageRequest.getRequestActions();
		if ( actionNames != null && actionNames.length > 0)
		{
			//check permissions
			boolean ok = false;
			String reqactions = (String)inPageRequest.getContentPage().get(PageRequestKeys.ALLOWPATHREQUESTACTIONS);
			if ( reqactions != null)
			{
				ok = reqactions.equalsIgnoreCase("true");
			}				
			else if ( inPageRequest.getUser() != null )
			{
				ok = true;
			}
			if ( ok )
			{
				for (int i = 0; i < actionNames.length; i++)
				{
					if ( actionNames[i].length() > 0 )
					{
						executePageAction(new PageAction( actionNames[i] ), inPageRequest );
						if ( inPageRequest.hasCancelActions() || inPageRequest.hasRedirected() )
						{
							return;
						}
					}
				}
			}
		}		
	}
	public List condenseActions(List inActions)
	{
		//remove any duplicates keeping the ones on the end first
		if( inActions.size() < 2)
		{
			return inActions;
		}
		List copy = new ArrayList(inActions.size() );
		Set copynames = new HashSet(inActions.size());
		Set cancellist = new HashSet( 2 );
		for (int i = inActions.size()-1; i >= 0; i--) //start at the end from /sub/sdfds.xconf first
		{
			PageAction pageAction = (PageAction) inActions.get(i);
			String cancel = pageAction.getConfig().getAttribute("cancel");
			if ( Boolean.parseBoolean(cancel) )
			{
				cancellist.add(pageAction.getActionName());
			}
			if( cancellist.contains(pageAction.getActionName()))
			{
				continue;
			}
			//This does not wowrk as expected. Since the child action is added first then the parent is excluded unless it has allowduplicated turned on
			String allow = pageAction.getConfig().getAttribute("allowduplicates");
			if( Boolean.parseBoolean(allow))
			{
				copy.add(pageAction);
				copynames.add(pageAction.getActionName());
			}
			else
			{
				if( !copynames.contains(pageAction.getActionName()))
				{
					copy.add(pageAction);
					copynames.add(pageAction.getActionName());
				}
			}
		}
		Collections.reverse(copy);
		return copy;
	}

	public boolean contains(String inCatalogId, String inBeanName)
	{
		String beanName = resolveBean(inCatalogId, inBeanName);
		return contains(beanName);
	}

	
	public Object getBean( String inCatalogId, String inBeanName )
	{
		Object bean = getCatalogIdBeans().get(inCatalogId + "_" + inBeanName);
		if( bean == null)
		{
			String beanName = resolveBean(inCatalogId, inBeanName);
			bean = getBean(beanName);
			try
			{
				Method catalogSetter = bean.getClass().getMethod("setCatalogId", new Class[] {String.class});
				catalogSetter.invoke(bean, new Object[] {inCatalogId});
			}
			catch (Exception e)
			{
				//Could not set catalogId
			}
			getCatalogIdBeans().put(inCatalogId + "_" + inBeanName, bean);
		}
		return bean;
	}

	public String resolveBean(String inCatalogId, String inBeanName)
	{
		//TODO: Cache this lookup
		String beanName = inBeanName;

		String parentlocation = "/" + inCatalogId + "/configuration/beans.xml";
		Page page = getPageManager().getPage(parentlocation);
		//log.info("BEANS PAGE: " + page);
		//log.info("BEANS EXISTS?: " + page.exists());
		//log.info("BEANS LOOKING FOR: " + inBeanName);
		
		int loopcheck = 0; //
		while(page.exists())
		{
			loopcheck++;
			if( loopcheck > 10)
			{
				throw new OpenEditRuntimeException("Infinite loop 1");
			}
			XmlFile file = getXmlArchive().getXml(page.getPath());
			Element field = file.getElementById(inBeanName);
			if( field != null)
			{
				//log.info("BEANS FOUND: " + field + ", with bean: " + field.attributeValue("bean"));
				beanName = field.attributeValue("bean");
				break;
			}
			
			//look up the tree. Make sure we do not find ourself again. Look out for infinte loops
			PageSettings fallback = page.getPageSettings().getFallback();
			int loopcheck2 = 0;
			while( fallback != null )
			{
				loopcheck2++;
				if( loopcheck2 > 10)
				{
					throw new OpenEditRuntimeException("Infinite loop 2");
				}
				String fallbacklocation = PathUtilities.extractDirectoryPath(fallback.getPath()) + "/beans.xml";
				if( !fallbacklocation.equals(page.getContentItem().getPath()))
				{
					//we found a new fallback
					page = getPageManager().getPage(fallbacklocation);
					break;
				}
				fallback = fallback.getFallback(); //look up a level
			}
			if( fallback == null)
			{
				break;
			}
		}
		return beanName;
	}

	protected PageManager getPageManager()
	{
		return (PageManager)getBean("pageManager");
	}
	
	public Object getBean( String inBeanName )
	{
		try
		{
			Object bean = getBeanFactory().getBean( inBeanName );
			if( bean != null)
			{
				getLoadedBeans().add(bean);
			}
			return bean;
		} catch ( NoSuchBeanDefinitionException ex)
		{
			throw new OpenEditRuntimeException("Could not find bean named " + inBeanName, ex);
		}
	}
	

	/**
	 * returns "null" if the module does not exist.
	 * 
	 * @author Matthew Avery, mavery@einnovation.com
	 */
	public BaseModule getModule( String inName )
	{
		Object bean = getBean( inName );
		if ( bean != null && bean instanceof BaseModule )
		{
			return (BaseModule) bean;
		}
		return null;
	}
	protected ConfigurableListableBeanFactory getBeanFactory()
	{
		return fieldBeanFactory;
	}
	public void setBeanFactory(BeanFactory inBeanFactory)
	{
		//I assume this is true most of the times since OpenEdit is loading up this ModuleManager
		fieldBeanFactory = (ConfigurableListableBeanFactory)inBeanFactory;
	}

	/**
	 * @param inKey
	 * @return
	 */
	public boolean contains(String inKey)
	{
		return getBeanFactory().containsBean(inKey);
	}

	public Set getLoadedBeans()
	{
		if (fieldLoadedBeans == null)
		{
			fieldLoadedBeans = new HashSet();
		}
		return fieldLoadedBeans;
	}
	public void addForShutdown(Shutdownable inAble)
	{
		getLoadedBeans().add(inAble);
	}
	
//	public List listAllBeans()
//	{
//		List allModules = new ArrayList();
//		List sortedNames = new ArrayList();
//
//		String[] names = getBeanFactory().getBeanDefinitionNames();
//		sortedNames = Arrays.asList(names);
//
//		for (int i = 0; i < sortedNames.size(); i++) {
//			String name = (String) sortedNames.get(i);
//			Bean bean = createBean(name);
//			if (bean != null) {
//				allModules.add(bean);
//			}
//		}
//		return allModules;
//	}
	
	public String getVersion(String inBeanName) 
	{
		if (getBeanFactory().containsBean(inBeanName)) {
			BeanDefinition beanDe = getBeanFactory().getBeanDefinition(inBeanName);
			if (!(beanDe instanceof AbstractBeanDefinition)) {
				throw new OpenEditRuntimeException("Spring version not supported yet");

			}
			AbstractBeanDefinition beanDef = (AbstractBeanDefinition) beanDe;

			try {
				beanDef.resolveBeanClass(Thread.currentThread().getContextClassLoader());
			} catch (Exception ex) {
				log.info("Could not load: " + inBeanName + " " + ex);
				return null;
			}

			// get version of module
			String version = beanDef.getBeanClass().getPackage().getImplementationVersion();
			return version;
			// get title of module
//			String title = beanDef.getBeanClass().getPackage().getImplementationTitle();
//			bean.setTitle(title);
			//return bean;
		}
		return null;
	}

	public XmlArchive getXmlArchive()
	{
		if( fieldXmlArchive == null)
		{
			fieldXmlArchive = (XmlArchive)getBean("xmlArchive");
		}
		return fieldXmlArchive;
	}

	public void setXmlArchive(XmlArchive inXmlArchive)
	{
		fieldXmlArchive = inXmlArchive;
	}

	public Map getCatalogIdBeans()
	{
		if (fieldCatalogIdBeans == null)
		{
			fieldCatalogIdBeans = new HashMap();
		}

		return fieldCatalogIdBeans;
	}

	public void setCatalogIdBeans(Map inCatalogIdBeans)
	{
		fieldCatalogIdBeans = inCatalogIdBeans;
	}

	public void clearBean( String inCatalogId, String inBeanName )
	{
		getCatalogIdBeans().remove(inCatalogId + "_" + inBeanName);		
	}
	
}
