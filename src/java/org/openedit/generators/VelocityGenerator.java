/*
 Copyright (c) 2003 eInnovation Inc. All rights reserved

 This library is free software; you can redistribute it and/or modify it under the terms
 of the GNU Lesser General Public License as published by the Free Software Foundation;
 either version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU Lesser General Public License for more details.
 */

/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.openedit.generators;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.openedit.Generator;
import org.openedit.OpenEditException;
import org.openedit.OpenEditRuntimeException;
import org.openedit.WebPageRequest;
import org.openedit.error.ContentNotAvailableException;
import org.openedit.page.Page;
import org.openedit.repository.filesystem.FileItem;
import org.openedit.servlet.OpenEditEngine;
import org.openedit.util.OutputFiller;

/**
 * This generator uses Velocity to process a template file, which (hopefully) uses the content on
 * the given page.
 *
 * @author Chris Burkey
 */
public class VelocityGenerator extends BaseGenerator implements Generator
{
	public static Log log = LogFactory.getLog(VelocityGenerator.class);
	public File fieldRootDirectory;
	protected VelocityEngine fieldEngine;
	protected OpenEditEngine fieldOpenEditEngine;
	Properties velocityProperties;
	
	public VelocityGenerator()
	{
	}

	public void generate( WebPageRequest inContext, Page inPage, Output inOut )
		throws OpenEditException
	{
		if( log.isDebugEnabled())
		{
			log.debug( "Rendering " + inPage );
		}
		
		if( !inPage.exists() ) //TODO: Create a new existed() method to speed up checks
		{
			String vir = inPage.get("virtual");
			if ( !Boolean.parseBoolean(vir) )
			{
				log.info("Missing: " +inPage.getPath());
				try
				{
					inOut.getWriter().write("404: " + inPage.getPath());
					return;
				}
				catch (IOException e)
				{
					throw new ContentNotAvailableException("Missing: " +inPage.getPath(),inPage.getPath());
				}
			}
			else
			{
				return; //do nothing
			}
		}
		try
		{
			
			if ( inPage.isBinary() )
			{
				//this should not happen
				InputStream in = inPage.getInputStream();
				new OutputFiller().fill(in, inContext.getOutputStream());
				return;
			}
			//inContext.unpackageVariables(); //TODO: Remove this silliness
			VelocityContext context = new VelocityContext(inContext.getPageMap());
			Writer writer = inOut.getWriter();
			
			if ( inPage.getContentItem() instanceof FileItem) //Faster and does cache
			{
				String path = inPage.getContentItem().getPath();
				//log.info( "Rendering " + inPage );
				String filter = inPage.getProperty("oetextfilter");
				if( filter != null )
				{
					path = path + "?filter=" + filter + "&locale=" + inContext.getLocale();
				}
				
				getEngine().mergeTemplate( path, inPage.getCharacterEncoding(),context, writer ); 
			}
			else //do a string eval MUCH SLOWER
			{
				Reader in = inPage.getReader();
				//log.info( "Eval " + inPage );
				getEngine().evaluate(context, writer, inPage.getPath(), in);
			}
			writer.flush(); 
		}
		catch (MethodInvocationException ex)
		{
			Throwable wrapped = ex.getWrappedThrowable();
			if (wrapped instanceof ContentNotAvailableException)
			{
				throw (ContentNotAvailableException)wrapped;
			}
			if (wrapped instanceof RuntimeException)
			{
				throw (RuntimeException) wrapped;
			}
			if( ignoreError( wrapped ))
			{
				//ignore
				return;
			}
			throw new OpenEditException(ex.getMessage() + " on " + inPage.getPath() , wrapped);
		}
		catch ( ParseErrorException pex )
		{
			throw new OpenEditException(pex.getMessage() + " on " + inPage.getPath(),pex);
		}
		catch ( Exception ioex )
		{
			if( ignoreError(ioex) )
			{
				log.debug("Browser canceled request");
				return;
			}
			if (ioex instanceof OpenEditException)
			{
				throw (OpenEditException) ioex;
			}
			throw new OpenEditException(ioex.getMessage() + " on " + inPage.getPath(),ioex);
		}
	}
	protected void init() 
	{
		//Velocity.setProperty( "runtime.log.logsystem.class", getClass().getName() );
		//RuntimeSingleton.init();
		// initialize velocity

		/**
		 * Load the Velocity properties file.  If the argument given is null then no properties
		 * will be used (i.e. the getVelocityProperties() methfod will return an empty Properties
		 * object.)
		 */
		//Map velocityProperties = new HashMap();
		
		
		velocityProperties = new Properties();

		File root = new File( getRootDirectory(),"WEB-INF/velocityprod.properties");
		if( root.exists() )
		{
			try
			{
				FileReader reader = new FileReader(root,StandardCharsets.UTF_8);
				velocityProperties.load(reader);
				reader.close();
			}
			catch (Throwable ex)
			{
				log.error(ex,ex);
			}
		}
		else
		{
			velocityProperties.put("velocimacro.library.autoreload", "false");
	 		velocityProperties.put("file.resource.loader.cache", "true");
			velocityProperties.put("file.resource.loader.modificationCheckInterval", "2"); // 2 seconds of cache
			
		}
//		velocityProperties.put(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
//			"org.openedit.generators.VelocityLogger");

		/*
		 velocityProperties.put(
		 RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
		 "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");

		 velocityProperties.put("runtime.log.logsystem.log4j.category", "syslog");
		 */
		velocityProperties.put("resource.loader","file" );  //valid options might be file, class, jar
		velocityProperties.put("runtime.log.invalid.references", "false");
		velocityProperties.put("resource.manager.logwhenfound", "false");
		
		
		//https://stackoverflow.com/questions/2334628/apache-velocity-performance-pitfalls
		
		velocityProperties.put("parser.pool.size", "50");
		velocityProperties.put(RuntimeConstants.RESOURCE_MANAGER_DEFAULTCACHE_SIZE, "300"); //89 is the default
 
		velocityProperties.put("velocimacro.permissions.allow.inline", "true");
		velocityProperties.put("velocimacro.permissions.allow.inline.to.replace.global", "true");
		
		velocityProperties.put("velocimacro.context.localscope", "true");
		velocityProperties.setProperty("file.resource.loader.path", getRootDirectory()
			.getAbsolutePath());
		String vmpath = "/system/display/velocitymacros.vm";
		File vm = new File(getRootDirectory(),vmpath);
		if( !vm.exists())
		{
			vmpath = "/WEB-INF/base/system/display/velocitymacros.vm";
			vm = new File(getRootDirectory(),vmpath);
			if(!vm.exists())
			{
				vmpath = "/WEB-INF/base/openedit/display/velocitymacros.vm";
				vm = new File(getRootDirectory(),vmpath);
			}
		}
		if(vm.exists())
		{
			velocityProperties.put("velocimacro.library", vmpath);
		}
		velocityProperties.put("file.resource.loader.description","OpenEdit File Resource Loader" );
		velocityProperties.put("file.resource.loader.class","org.openedit.generators.GeneratedResourceLoader");
		velocityProperties.put("userdirective","org.openedit.generators.IfNull,org.openedit.generators.IfNotNull" );
		//velocityProperties.put("userdirective","org.openedit.generators.Label" );
		
		//		velocityProperties.put("directive.foreach.counter.name","velocityCount"); //Does not work
		//		velocityProperties.put("directive.foreach.counter.initial.value","1");
		
		
//		file.resource.loader.cache=true
//		file.resource.loader.modificationCheckInterval=60
//
//		#
//		# Class resource loader information
//		#
//		class.resource.loader.description=Velocity Classpath Resource Loader
//		class.resource.loader.class=org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
//		class.resource.loader.cache=true
//		class.resource.loader.modificationCheckInterval=0

		
		//Velocity had a bug that it required log4j unless you use the static instance.
		//getEngine().set(eprops);

//		runtime.log.logsystem.class = org.apache.velocity.runtime.log.ServletLogChute
//				runtime.log.logsystem.servlet.level = info
				
		 //private Logger log = LoggerFactory.getLogger(DEFAULT_RUNTIME_LOG_NAME);		
//		velocityProperties.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE,
//		velocityProperties.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,"org.apache.velocity.runtime.log.Log4JLogChute" );
//		velocityProperties.setProperty("runtime.log.logsystem.log4j.logger",LOGGER_NAME);
//		
//		org.apache.velocity.tools.generic.log.CommonsLogLogSystem
		
//		runtime.log.logsystem.class = org.apache.velocity.runtime.log.ServletLogChute
//				runtime.log.logsystem.servlet.level = info
				
//		velocityProperties.put("runtime.log.logsystem.class","org.apache.velocity.runtime.log.NullLogChute" );
		
		//fieldEngine = new VelocityEngine(velocityProperties);
		
	}
	protected void initEngine() 
	{

		try
		{
		   //velocityProperties.put("file.resource.loader.instance", getGeneratedResourceLoader());
		   getEngine().setApplicationAttribute("openEditEngine", getOpenEditEngine());
		   getEngine().init(velocityProperties);
		   //getEngine().reset();
			
/*		   Class c = getEngine().getClass();
		   // get the reflected object 
		   Field field = c.getDeclaredField("rt");
		   field.setAccessible(true);
		   RuntimeInstance rt = (RuntimeInstance)field.get(getEngine());

		   //Set resourceManager
		   Class rtclass = rt.getClass();
		   Field rmfield = rtclass.getDeclaredField("resourceManager");
		   rmfield.setAccessible(true);
		   rmfield.set(rt, getGeneratedResourceLoader());
*/		}
		catch( Exception ex)
		{
			throw new OpenEditRuntimeException(ex);
		}
	}

	public File getRootDirectory()
	{
		return fieldRootDirectory;
	}

	public void setRootDirectory(File inRootDirectory)
	{
		fieldRootDirectory = inRootDirectory;
	}

	public VelocityEngine getEngine()
	{
		if( fieldEngine == null)
		{
			fieldEngine = new VelocityEngine();
			init();
			initEngine();
		}
		return fieldEngine;
	}

	public void setEngine(VelocityEngine inEngine)
	{
		fieldEngine = inEngine;
	}

	public OpenEditEngine getOpenEditEngine()
	{
		return fieldOpenEditEngine;
	}

	public void setOpenEditEngine(OpenEditEngine inOpenEditEngine)
	{
		fieldOpenEditEngine = inOpenEditEngine;
	}

	public void enableEditMode(boolean inEditor)
	{
		fieldEngine = new VelocityEngine();
		init();

		if( inEditor)
		{
			velocityProperties.put("file.resource.loader.modificationCheckInterval","2");
			velocityProperties.put("file.resource.loader.cache","true");
		}
		else   //Production
		{
			velocityProperties.put("file.resource.loader.cache","true");
			velocityProperties.put("file.resource.loader.modificationCheckInterval","60");
		}
		initEngine();
//		
//		 = 50

	}

}
