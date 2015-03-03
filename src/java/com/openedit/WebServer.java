/*
 * Created on May 16, 2006
 */
package com.openedit;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.BeanFactory;

import com.openedit.page.manage.PageManager;
import com.openedit.servlet.OpenEditEngine;
import com.openedit.users.UserManager;

/**
 * <p>
 * This interface contains the core parts of an OpenEdit website. In most cases
 * the default implementation {@link com.openedit.BaseWebServer} will be
 * sufficient. If for any reason you need to customize it to suit your needs you
 * can write a custom implementation that implements this interface (e.g. if you
 * wanted to use a Spring WebApplicationContext rather than just a BeanFactory).
 * </p>
 * <p>
 * The implementation of this interface is responsible for loading the Spring
 * beans as defined in the openedit.xml and any applicable plugin.xml or
 * pluginoverrides.xml files for extending functionality from the core of
 * OpenEdit.
 * </p>
 * 
 * @author Eric Broyles <eric@sandra.com>
 * @version $Id: WebServer.java,v 1.23 2010/09/08 18:40:37 cburkey Exp $
 */
public interface WebServer
{
    public BeanFactory getBeanFactory();

    public PageManager getPageManager();

    public UserManager getUserManager();

    public ModuleManager getModuleManager();

    public OpenEditEngine getOpenEditEngine();

    public File getRootDirectory();
    
    public void setRootDirectory(File inFile);

	public List getAllPlugIns();

	public void reloadMounts();
	
	public void saveMounts(List inConfigs);

	public String getNodeId();
	
}