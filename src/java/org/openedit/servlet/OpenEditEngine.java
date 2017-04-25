/*
 * Created on May 12, 2006
 */
package org.openedit.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openedit.ModuleManager;
import org.openedit.OpenEditException;
import org.openedit.WebPageRequest;
import org.openedit.error.ErrorHandler;
import org.openedit.event.EventManager;
import org.openedit.page.Page;
import org.openedit.page.PageStreamer;
import org.openedit.page.manage.PageManager;

/**
 * <p>
 * OpenEditEngineI.java
 * </p>
 * 
 * @author Eric Broyles <eric.broyles@ugs.com>
 * @version $Id: OpenEditEngine.java,v 1.65 2009/10/21 21:53:04 cburkey Exp $
 */
public interface OpenEditEngine
{

    public static final String CONTEXT_ATTR_NAME = "OpenEditEngine";

    public void render(HttpServletRequest inRequest, HttpServletResponse inResponse) throws IOException,
            OpenEditException;

    public boolean hideFolders();

    public void setHideFolders(boolean inFlag);

    public void beginRender(WebPageRequest pageRequest) throws OpenEditException;

    //public void render(WebPageRequest inPageRequest) throws OpenEditException;

    public PageStreamer createPageStreamer(Page inPage, WebPageRequest inPageRequest) throws OpenEditException;

    public void executePageActions(WebPageRequest inPageRequest) throws OpenEditException;

    public void executePathActions(WebPageRequest inPageRequest) throws OpenEditException;

    public ModuleManager getModuleManager();

    public void setModuleManager(ModuleManager moduleManager);

    public PageManager getPageManager();

    public void setPageManager(PageManager pageManager);

    public ErrorHandler getErrorHandler();

    public void setErrorHandler(ErrorHandler errorHandler);
 
    public String getVersion();

    /**
     * @param inPath
     * @param inPageRequest
     */
    //public void forward(Page inPage, WebPageRequest inOldContext) throws OpenEditException;

    /**
     * 
     */
    public void shutdown();

	public void setPageEventHandler(EventManager inWebEventListener);

}
