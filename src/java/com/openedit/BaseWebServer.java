/*
 * Created on Jan 5, 2005
 */
package com.openedit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.openedit.PlugIn;
import org.openedit.repository.Repository;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.UrlResource;
import org.springframework.scripting.config.ScriptingDefaultsParser;

import com.openedit.page.Page;
import com.openedit.page.manage.PageManager;
import com.openedit.servlet.OpenEditEngine;
import com.openedit.users.UserManager;
import com.openedit.util.PathUtilities;
import com.openedit.util.XmlUtil;

/**
 * <p>
 * This class contains the core parts of an OpenEdit website. You should not use 
 * this object if you only need one part of it.  This class is responsible for
 * loading the Spring beans as defined in the openedit.xml and any applicable
 * plugin.xml or pluginoverrides.xml files for extending functionality from
 * the core of OpenEdit.
 * </p>
 * <p>
 * This implementation uses a simple Spring BeanFactory to manage the beans.
 * </p>
 * @see com.openedit.WebServer
 * @author cburkey
 *
 */
public class BaseWebServer implements WebServer
{
	private static final Log log = LogFactory.getLog(WebServer.class);
	protected BeanFactory fieldBeanFactory;
	protected File fieldRootDirectory;
	protected List fieldAllPlugIns;
	
	public BeanFactory getBeanFactory()
	{
//		if ( fieldBeanFactory == null )
//		{
//			synchronized( this ) //the reason for this is when spring is configured it may cause the scheduler to call getBeanFactory() in another thread
//			{ //We put the synchronized at this level to speed up the loading of web pages 
//				if ( fieldBeanFactory == null )
//				{
//					ClassLoader loader = getClass().getClassLoader();
//					if( loader == null)
//					{
//						loader = ClassLoader.getSystemClassLoader();
//					}
//					
//					GenericApplicationContext appContext = new GenericApplicationContext();
//					XmlBeanDefinitionReader xbdr = new XmlBeanDefinitionReader(appContext);
//					xbdr.setValidating(false);					
////					InputStreamResource resource = new InputStreamResource(loader.getResourceAsStream("entermedia.xml"));
////					XmlBeanFactory xmlbeans = new XmlBeanFactory( resource );
////					xmlbeans.setAllowBeanDefinitionOverriding(true);
////					xmlbeans.destroySingletons();
//					appContext.getBeanFactory().registerSingleton("WebServer",this);
//					appContext.getBeanFactory().registerSingleton("root",getRootDirectory() );
//					fieldBeanFactory = xmlbeans;
//				}
//			}
//		}
		return fieldBeanFactory;
	}
	public synchronized void initialize() 
	{
		if ( getRootDirectory() == null)
		{
			String path = System.getProperty("oe.root.path");
			if( path != null)
			{
				setRootDirectory(new File(path).getAbsoluteFile());
			}
		}
		if ( getRootDirectory() == null)
		{
			throw new IllegalStateException("Root directory is not defined");
		}		
		//TODO: Look for duplicate openedit jars and try to delete them
		
		try
		{
			GenericXmlApplicationContext context = new GenericXmlApplicationContext()
			{
				public org.springframework.core.io.Resource getResource(String location) 
				{
					File custom = new File( getRootDirectory(), "/WEB-INF/src/" + location);
					if( custom.exists() )
					{
						return new FileSystemResource(custom);
					}
							
					File folders = new File( getRootDirectory(), "/WEB-INF/base/");
					File[] children = folders.listFiles();
					if( children != null )
					{
						for (int i = 0; i < children.length; i++)
						{
							File script = new File( children[i],"/src/" + location);
							if( script.exists() )
							{
								return new FileSystemResource(script);
							}
						}
					}
					return super.getResource(location);
				};
			};
			context.setValidating(false);
			///context.set
			ClassLoader loader = getClass().getClassLoader();
			if( loader == null)
			{
				loader = ClassLoader.getSystemClassLoader();
			}
			context.setClassLoader(loader);
			
			InputStreamResource resource = new InputStreamResource(loader.getResourceAsStream("entermedia.xml"));
			context.load(new UrlResource(loader.getResource("entermedia.xml")) );
			context.getBeanFactory().registerSingleton("WebServer",this);
			context.getBeanFactory().registerSingleton("root",getRootDirectory() );

			List sorted = getAllPlugIns();
			getBeanFactory();
			for (Iterator iter = sorted.iterator(); iter.hasNext();)
			{
				PlugIn plugin = (PlugIn)iter.next();
				log.info("Loading " + plugin.getPluginXml().getPath() );
				context.load(new UrlResource(plugin.getPluginXml() ) );
			}
			
			File overrideFile = new File( getRootDirectory(), "WEB-INF/pluginoverrides.xml" ); //TODO: Use a directory of files
			context.load(new FileSystemResource(overrideFile));

			context.refresh();			
			
			fieldBeanFactory = context;
			
		} catch ( Throwable ex)
		{
			throw new OpenEditRuntimeException(ex);
		}

		
//		JarReader reader = new JarReader()  //This is for Windows since it locks files. Did not seem to fix the problem
//		{
//			//Call back
//			public void processFile( File inFile)
//			{
//				loadPluginDefs( inFile );
//			}
//		};
//		reader.processInClasspath("plugin.xml");
//		File lib = new File( getRootDirectory(),"WEB-INF/lib");
//		reader.processInLibDir(lib,"plugin.xml");
		
//		overridePlugIns();
		
	    Thread sh = new Thread( new Runnable()  //This is in case the JVM is shut down
	    {
	    	public void run()
	    	{
	    		try
	    		{
	    			getOpenEditEngine().shutdown();
	    		}
	    		catch (Throwable ex)
	    		{
	    			ex.printStackTrace();
	    		}
	    	}
	    });
        Runtime.getRuntime().addShutdownHook(sh);

        reloadMounts();
        
           
       
        
        
		Page page = getPageManager().getPage("/WEB-INF/startup.html");
		BaseWebPageRequest request = new BaseWebPageRequest();
		request.setContentPage(page);
		request.setPage(page);
		if( page.getPageSettings().exists())
		{
			getOpenEditEngine().executePathActions(request);
		}
		else if( getModuleManager().contains("Scheduler") )
		{
			getModuleManager().execute("Scheduler.init", request);
		}
		//getBeanFactory().preInstantiateSingletons();
        
	}
	public List getAllPlugIns() 
	{
		if( fieldAllPlugIns == null)
		{
			try
			{
				ClassLoader loader  = getClass().getClassLoader();
				if( loader == null)
				{
					loader = ClassLoader.getSystemClassLoader();
				}
				Enumeration pluginDefs = loader.getResources( "plugin.xml" ); //This locks jar files on Windows
				fieldAllPlugIns = new ArrayList();
				Map allplugins = new HashMap();
				Map depends  = loadDepends();
				
				while( pluginDefs.hasMoreElements() )
				{
					URL url = (URL) pluginDefs.nextElement();
					PlugIn plugin = new PlugIn();
					plugin.setPluginXml(url);
					plugin.setInstalled(true);

					String pluginpath = url.getPath().replace('\\', '/');
					pluginpath  = PathUtilities.extractDirectoryPath(pluginpath);
					plugin.setPlugInPath(pluginpath);
					
					Element depend = (Element)depends.get(pluginpath);
				
				
					if(depend != null){
						String projectname = depend.attributeValue("projectname");
						plugin.setId(projectname);
						fieldAllPlugIns.add(plugin);
						allplugins.put(projectname,plugin);
					}
					
				}
				populateDependants(allplugins,depends);
				//NOW SORT!
				DependentComparator compare = new DependentComparator();
				Collections.sort(fieldAllPlugIns,compare);
			}
			catch ( IOException ex)
			{
				throw new OpenEditException(ex);
			}
			
		}
		return fieldAllPlugIns;
	}

	private void populateDependants(Map inPlugins, Map inDepends)
	{
		for (Iterator iterator = inPlugins.values().iterator(); iterator.hasNext();)
		{
			PlugIn plugin = (PlugIn) iterator.next();
			Element root = (Element)inDepends.get( plugin.getPlugInPath() );
			if( root == null)
			{
				continue; //no dependancy
			}
			for (Iterator iter = root.elementIterator("depend"); iter.hasNext();)
			{
				Element	child = (Element) iter.next();
				String name = child.getTextTrim();
				if( name.equals("entermedia"))
				{
					continue; //Not needed since it is core
				}
				PlugIn plug = (PlugIn)inPlugins.get(name);
				if( plug == null)
				{
					log.error("Missing dependency:" + name);
				}
				plugin.addDependsOn(plug);
			}
		}
	}
	/**
	 * Here we sort all the plugins depending on their depandecy tree
	 * @param pluginurls
	 * @return
	 * @throws IOException
	 */
	protected Map loadDepends() throws IOException
	{
		ClassLoader loader  = getClass().getClassLoader();
		if( loader == null)
		{
			loader = ClassLoader.getSystemClassLoader();
		}

		Enumeration depends = loader.getResources( "depends.xml" ); //This locks jar files on Windows
		XmlUtil util = new XmlUtil();

		Map dependslist = new HashMap();
		
		//First load any and all depends that are configured
		while( depends.hasMoreElements())
		{
			URL dependurl = (URL)depends.nextElement();
			//PlugIn plugin = (PlugIn)depends.nextElement();
			String pluginpath = dependurl.getPath();
			pluginpath = pluginpath.replace('\\', '/');
			pluginpath  = PathUtilities.extractDirectoryPath(pluginpath);
			
			InputStream in = dependurl.openStream();
			Element root = util.getXml(in,"UTF-8");
			dependslist.put(pluginpath,root);
		}
		return dependslist;
	}
			
//		
//			List dependslist = (List)plugindependson.get(project);
//			if( dependslist == null)
//			{
//				dependslist = new ArrayList();
//				plugindependson.put(project, dependslist);
//			}
//			for (Iterator iter = root.elementIterator("depend"); iter.hasNext();)
//			{
//				Element	child = (Element) iter.next();
//				String name = child.getTextTrim();
//				if( name.equals("openedit"))
//				{
//					continue; //Not needed since it is core
//				}
//				dependslist.add(name);
//			}
//		}
//
//		//Make a Map of all of them for searching
//		Map plugins = new HashMap();
//		for (Iterator iterator = allplugins.iterator(); iterator.hasNext();)
//		{
//			PlugIn plugin = (PlugIn) iterator.next();
//			plugins.put(plugin.getId(), plugin);
//		}
//
//		//Save dependencies for each Plugin
//		for (Iterator iterator = plugindependson.keySet().iterator(); iterator.hasNext();)
//		{
//			String pluginid	 = (String) iterator.next();
//			PlugIn edit = (PlugIn)plugins.get(pluginid);
//			if( edit != null)
//			{
//				List dep = (List)plugindependson.get(edit.getId());
//				if( dep != null)
//				{
//					for (Iterator iterator2 = dep.iterator(); iterator2.hasNext();)
//					{
//						String dependsid = (String) iterator2.next();
//						edit.addDependsOn((PlugIn)plugins.get(dependsid));
//					}
//				}
//			}
//		}
//

//		return plugins;
//	}
	
	
//	protected void loadPluginDefs(File inFile)
//	{
//		try
//		{
//			loadPluginDefs(new FileInputStream( inFile));
//		}
//		catch( IOException ex)
//		{
//			throw new OpenEditRuntimeException( ex);
//		}
//	}
//	
//	protected boolean loadPluginDefs( InputStream inputStream )
//	{
//		try
//		{
//			XmlBeanFactory factory = new XmlBeanFactory( new InputStreamResource( inputStream ), getBeanFactory() );
//			String[] names = factory.getBeanDefinitionNames();
//
//			for (int i = 0; i < names.length; i++)
//			{
//				BeanDefinition def = factory.getBeanDefinition(names[i]);
//				String[] aliases = factory.getAliases(names[i]);
//				fieldBeanFactory.registerBeanDefinition(names[i],def);	
//				
//				for (int j = 0; j < aliases.length; j++)
//				{
//					fieldBeanFactory.registerAlias(names[i], aliases[j]);
//					
//				}
//				
//			}
//			
//			
//			
//			
//			
//		}
//		catch ( Exception ex)
//		{
//			ex.printStackTrace();
//			log.error( ex);
//			return false;
//			
//		}
//		finally
//		{
//			FileUtils.safeClose(inputStream);
//		}
//		return true;
//	}
//	public void overridePlugIns()
//	{
//		try
//		{
//			File overrideFile = new File( getRootDirectory(), "WEB-INF/pluginoverrides.xml" ); //TODO: Use a directory of files
//			if ( overrideFile.exists() )
//			{
//				loadPluginDefs( overrideFile );
//			}
//
//			//I am not sure what this does.
//			PropertyPlaceholderConfigurer configurator = new PropertyPlaceholderConfigurer();
//			configurator.postProcessBeanFactory( getBeanFactory() );
//
//		} catch ( Exception ex )
//		{
//			throw new OpenEditRuntimeException(ex);
//		}
//		
//	}
	
	/**
     * @see com.openedit.WebServer#getPageManager()
     */
	public PageManager getPageManager()
	{
		return (PageManager)getBeanFactory().getBean( "pageManager" );
	}

	/**
     * @see com.openedit.WebServer#getUserManager()
     */
	public UserManager getUserManager()
	{
		return (UserManager)getBeanFactory().getBean( "userManager" );
	}

	
	/**
     * @see com.openedit.WebServer#getModuleManager()
     */
	public ModuleManager getModuleManager()
	{
		return (ModuleManager) getBeanFactory().getBean( "moduleManager" );
	}

	/**
     * @see com.openedit.WebServer#getOpenEditEngine()
     */
	public OpenEditEngine getOpenEditEngine()
	{
		return (OpenEditEngine)getBeanFactory().getBean("OpenEditEngine");
	}

    /**
     * @see com.openedit.WebServer#getRootDirectory()
     */
	public File getRootDirectory()
	{
		if( fieldRootDirectory == null)
		{
			String path = System.getProperty( "oe.root.path");
			if( path != null)
			{
				log.info("Using System default since to path set " + path);
				fieldRootDirectory = new File( path ).getAbsoluteFile();
			}
		}
		return fieldRootDirectory;
	}
	public void setRootDirectory(File inRoot)
	{
		if( inRoot != null)
		{
			try
			{
				inRoot = inRoot.getCanonicalFile();
			}
			catch (IOException ex)
			{
				throw new IllegalArgumentException("Could not convert " + inRoot,ex);
			}
		}
		fieldRootDirectory = inRoot;
	}
	
	class DependentComparator implements Comparator
	{
		public int compare(Object arg0, Object arg1)
		{
			PlugIn plugin1 = (PlugIn)arg0;
			PlugIn plugin2 = (PlugIn)arg1;
			
			if( plugin1.dependsOn( plugin2.getId()))
			{
				return 1;
			}
			if( plugin2.dependsOn( plugin1.getId()))
			{
				return -1;
			}
			return 0;
		}
	}
	
	public void reloadMounts()
	{
		Page mounts = getPageManager().getPage("/WEB-INF/oemounts.xml",true);
		getPageManager().getRepository().getRepositories().clear();
		if( mounts.exists() )
		{
			Element root = new XmlUtil().getXml(mounts.getInputStream(), "UTF-8");
			List children = root.elements("mount");
			for (Iterator iterator = children.iterator(); iterator.hasNext();)
			{
				Element child = (Element) iterator.next();

				String repositorytype = child.attributeValue("repositorytype");
				if( repositorytype == null)
				{
					//For legacy support. New UI uses type drop down
					boolean versioncontrol = Boolean.parseBoolean(child.attributeValue("useversioncontrol"));
					if (versioncontrol)
					{
						repositorytype = "versionRepository";
					}
				}
				if( repositorytype == null)
				{
					repositorytype = "fileRepository";
				}				
				Repository config = (Repository)getModuleManager().getBean(repositorytype);
				config.setRepositoryType(repositorytype);
				String path  = child.attributeValue("path");
				if( path == null)
				{
					path = "/";
				}
				String filter = child.attributeValue("filter");
				if( filter != null)
				{
					if( filter.endsWith("*"))
					{
						//legacy support /* -> /
						path = filter;
					}
					else
					{
						log.error("Invalid: " + filter + " Remove filter entries from /WEB-INF/oemounts.xml" );
						config.setMatchesPostFix(filter);
					}
				}
				path = path.replace("*", "");
				if( path.length() > 1 && path.endsWith("/"))
				{
					path = path.substring(0, path.length()-1);
				}

				config.setPath(path);

				config.setMatchesPostFix(child.attributeValue("matchpostfix")); //*.PDF
				
				config.setFilterIn(child.attributeValue("filterin")); //*.PDF
				if( config.getFilterIn() == null)
				{
					config.setFilterIn(child.attributeValue("importextensions")); 					
				}

				config.setFilterOut(child.attributeValue("filterout")); //*.old
					
				String externalpath = child.attributeValue("externalpath");
				config.setExternalPath(externalpath);
				
				//This is used when no external path is passed in
				if( config.getExternalPath() == null)
				{
					String rootpath = getCleanRootPath(config.getPath());
					config.setExternalPath(rootpath);
				}
				
				List properties = child.elements("property");
				for (Iterator iterator2 = properties.iterator(); iterator2
						.hasNext();)
				{
					Element property = (Element) iterator2.next();
					String propName = property.attributeValue("name");
					String value = property.getText();
					config.setProperty(propName, value);
				}
				
				getPageManager().getRepository().addRepository(config);
				
				//Might need to create the mount so we can find the virtual children
				//TODO: Remove the need to create the folder
				Page local = getPageManager().getPage(config.getPath() + "/");
				if( !local.exists() )
				{
					getPageManager().putPage(local);
				}

				
			}
		}
		else
		{
			//Defaults
			/*
			Repository repos = new XmlVersionRepository(); 
			repos.setPath("/WEB-INF/data");
			repos.setRepositoryType("versionRepository");
			repos.setExternalPath(getCleanRootPath(repos.getPath()));
			getPageManager().getRepository().addRepository(repos);
			
			repos = new FileRepository();
			repos.setPath("/WEB-INF");
			repos.setRepositoryType("fileRepository");
			repos.setExternalPath(getCleanRootPath(repos.getPath()));
			getPageManager().getRepository().addRepository(repos);
		
			repos = new XmlVersionRepository(); 
			repos.setPath("/");
			repos.setRepositoryType("fileRepository");
			repos.setExternalPath(getCleanRootPath(repos.getPath()));
			getPageManager().getRepository().addRepository(repos);
			*/
			
		}
		getPageManager().clearCache();
		getPageManager().getRepository().sort();
		
	}
	protected String getCleanRootPath(String append)
	{
		String root =  getRootDirectory().getAbsolutePath().replace('\\', '/');
		if( root.endsWith("/") )
		{
			root = root.substring(0, root.length()-1);
		}
		if( append != null && append.length() > 0)
		{
			root = root + append;
		}
		return root;
	}
	public void saveMounts(List mounts) 
	{
		Element root = DocumentHelper.createDocument().addElement("mounts");
		for (Iterator iterator = mounts.iterator(); iterator.hasNext();)
		{
			Repository existing = (Repository) iterator.next();
			Element child = root.addElement("mount");
			child.addAttribute("path", existing.getPath());
			
			
			child.addAttribute("filterin", existing.getFilterIn());
			child.addAttribute("filterout", existing.getFilterOut());
			child.addAttribute("matchpostfix",existing.getMatchesPostFix()); //*.PDF

			String external = existing.getExternalPath();
			if( external != null )
			{
				String path =  getCleanRootPath(existing.getPath());
				if( !path.equals(external) )  //does not save any redundant data
				{
					child.addAttribute("externalpath", external);
				}
			}
			child.addAttribute("repositorytype", existing.getRepositoryType());
			
			
			
			
			Map properties = existing.getProperties();
			if (properties != null)
			{
				for (Iterator iterator2 = properties.keySet().iterator(); iterator2.hasNext();)
				{
					String name = (String) iterator2.next();
					String value = (String)properties.get(name);
					Element prop = child.addElement("property");
					prop.addAttribute("name", name);
					prop.setText(value);
				}	
			}
			
		}
		Page mountdata = getPageManager().getPage("/WEB-INF/oemounts.xml");
		OutputStream out = getPageManager().saveToStream(mountdata);
		new XmlUtil().saveXml(root, out, mountdata.getCharacterEncoding());
		reloadMounts();
	}
}
