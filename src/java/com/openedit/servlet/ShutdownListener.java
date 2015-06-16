package com.openedit.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.openedit.WebServer;

public class ShutdownListener implements ServletContextListener
{

	public void contextDestroyed(ServletContextEvent inArg0)
	{
		// TODO Auto-generated method stub
		WebServer server = (WebServer)inArg0.getServletContext().getAttribute(WebServer.class.getName()); 
		if( server != null)
		{
			OpenEditEngine engine = server.getOpenEditEngine();
			engine.shutdown();
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent inArg0)
	{
		// TODO Auto-generated method stub

	}

}
