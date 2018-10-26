package org.openedit.di;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.openedit.OpenEditException;
import org.openedit.WebServer;
import org.openedit.util.FileUtils;
import org.openedit.util.XmlUtil;

import groovy.lang.GroovyClassLoader;

public class BeanLoader
{
	private static final Log log = LogFactory.getLog(BeanLoader.class);

	protected Map fieldLoadedBeans = new HashMap();
	protected GroovyClassLoader fieldClassloader;
	XmlUtil xml = new XmlUtil();
	protected WebServer fieldWebServer;
	
	public WebServer getWebServer()
	{
		return fieldWebServer;
	}

	public void setWebServer(WebServer inWebServer)
	{
		fieldWebServer = inWebServer;
	}

	public GroovyClassLoader getClassloader()
	{
		return fieldClassloader;
	}

	public void setClassloader(GroovyClassLoader inClassloader)
	{
		fieldClassloader = inClassloader;
	}

	public Object getBean(String inName)
	{
		if("WebServer".equals(inName) && fieldWebServer!= null){
			return fieldWebServer;
		}
		Pojo bean = (Pojo)fieldLoadedBeans.get(inName);
		if( bean == null)
		{
			throw new OpenEditException("No such bean " + inName);
		}
		return createObject( bean );
	}
	
	
	
	private Object createObject(Pojo inBean)
	{
		synchronized (inBean)
		{
			Object loaded = null;
			String classPath = inBean.getClassPath();
			if( inBean.isSingleton() )
			{
				loaded = inBean.getSingleton();
				if( loaded == null)
				{
					loaded = createObject(classPath);
					inBean.setSingleton(loaded);
					loadProperties(inBean, loaded);
				}
			}
			else
			{
				loaded = createObject(classPath);
				loadProperties(inBean, loaded);
			}
			return loaded;
		}	
	}

	private void loadProperties(Pojo inBean, Object loaded)
	{
		//look over all the dependencies and create thos recursively
		for (Iterator iterator = inBean.getProperties().iterator(); iterator.hasNext();)
		{
			Element property = (Element) iterator.next();
			Element child = property.element("ref");
			String attributeName = property.attributeValue("name");
			if( child != null)
			{
				Object dep = getBean(child.attributeValue("bean"));
				setProperty(loaded,attributeName,dep);
			}
			else
			{
				child = property.element("list");
				if( child != null)
				{
					List children = new ArrayList();
					for (Iterator iterator2 = child.elementIterator("value"); iterator2.hasNext();)
					{
						Element value = (Element) iterator2.next();
						children.add(value.getTextTrim());
					}
					for (Iterator iterator2 = child.elementIterator("ref"); iterator2.hasNext();)
					{
						Element value = (Element) iterator2.next();
						Object dep = getBean(value.attributeValue("bean"));
						children.add(dep);
					}
					setProperty(loaded,attributeName,children);
				}
				else
				{
					child = property.element("value");
					if( child != null)
					{
						Object val = child.getTextTrim();
						setProperty(loaded,attributeName,val);
					}
				}
			}
		}
	}
	public static boolean setProperty(Object object, String fieldName, Object fieldValue) {
	   
		try
		{
			String name = fieldName.toUpperCase();
			name = "set" + name.substring(0, 1) + fieldName.substring(1,fieldName.length());
			Method[] methods = object.getClass().getMethods();

            for (int i = 0; i < methods.length; i++) 
            {
                if (methods[i].getName().startsWith(name) && methods[i].getParameterTypes().length == 1) 
                {
                	String clas = methods[i].getParameterTypes()[0].getName();
                	if( "boolean".equals( clas ) )
                	{
                		methods[i].invoke(object, Boolean.valueOf( (String)fieldValue) );
                	}
                	else if( "int".equals( clas ) || "java.lang.Integer".equals( clas ))
                	{
                		methods[i].invoke(object, Integer.valueOf( (String)fieldValue) );
                	}
                	else
                	{
                		methods[i].invoke(object, fieldValue);
                	}
                	return true;
                }
            } 
		}
		catch( Exception ex)
		{
			throw new OpenEditException(ex);
		}
		return false;
		
	}
	private Object createObject(String inClassPath)
	{
		try
		{
			if( inClassPath.endsWith(".groovy"))
			{
				inClassPath = inClassPath.replace("/",".").replace(".groovy","");
			}
			Object created =  getClassloader().loadClass(inClassPath).newInstance();
			if( created instanceof BeanLoaderAware)
			{
				setProperty(created,"beanLoader",this);
			}
			return created;

		}
		catch (Throwable e)
		{
			throw new OpenEditException(e);
		}
	}
	
	public void loadBeans(Element inRoot)
	{
		for (Iterator iterator = inRoot.elementIterator("bean"); iterator.hasNext();)
		{
			Element node = (Element) iterator.next();
			Pojo p = new Pojo();
			p.fieldConfig = node;
			String id = node.attributeValue("id");
			//log.info("Loaded " + id);
			fieldLoadedBeans.put(id, p);
		}

		for (Iterator iterator = inRoot.elementIterator("groovy"); iterator.hasNext();) //Pojo.groovy
		{
			Element node = (Element) iterator.next();
			Pojo p = new Pojo();
			p.fieldConfig = node;
			String id = node.attributeValue("id");
			fieldLoadedBeans.put(id, p);
		}
		//<lang:groovy id="cmykpreprocessorCreator" script-source=
	}

	public void load(URL inResource, String inPath)
	{
		InputStream in = null;
		try
		{
			in = inResource.openStream(); 
			Element root = xml.getXml(in, "UTF-8");
			loadBeans(root);
			log.info("loaded " + inPath + " "  + root.elements().size() + " total: " + fieldLoadedBeans.size());
		}
		catch( Exception ex)
		{
			throw new OpenEditException(ex);
		}
		finally
		{
			FileUtils.safeClose(in);
		}
	}
	public void registerSingleton( String inName, Object inObject)
	{
		Pojo bean = (Pojo)fieldLoadedBeans.get(inName);
		if( bean == null)
		{
			bean = new Pojo();
			bean.fieldConfig = DocumentHelper.createElement("bean");
			String canonicalName = inObject.getClass().getCanonicalName();
			bean.fieldConfig.addAttribute("class", canonicalName);
			bean.fieldConfig.addAttribute("name", inName);

			bean.setSingleton(inObject);
			fieldLoadedBeans.put(inName, bean);	
		}
		bean.setSingleton(inObject);
	}

	public boolean containsBean(String inKey)
	{
		return fieldLoadedBeans.containsKey(inKey);
	}

	public Map getLoadedBeans()
	{
		return fieldLoadedBeans;
	}
}
