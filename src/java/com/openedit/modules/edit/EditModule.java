/*
Copyright (c) 2003 eInnovation Inc. All rights reserved

This library is free software; you can redistribute it and/or modify it under the terms
of the GNU Lesser General Public License as published by the Free Software Foundation;
either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Lesser General Public License for more details.
*/
package com.openedit.modules.edit;
import java.net.URLEncoder;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openedit.OpenEditException;
import com.openedit.WebPageRequest;
import com.openedit.users.User;
/**
 * This module provides the page editing functionality, and several actions to support it.
 *
 * @author Eric Galluzzo
 */
public class EditModule extends BaseEditorModule
{
	protected static final String WARNING_PAGE_PATH = "/system/components/openedit/lock-warning.html";
	private static Log log = LogFactory.getLog(EditModule.class);
	protected EditLockRegistry fieldEditLockRegistry;
	protected String fieldEditLockWarningPath = WARNING_PAGE_PATH;
	/**
	 * Returns the edit lock registry used when claiming and releasing locks.
	 *
	 * @return EditLockRegistry
	 */
	public EditLockRegistry getEditLockRegistry()
	{
		if (fieldEditLockRegistry == null)
		{
			fieldEditLockRegistry = new EditLockRegistry();
		}
		return fieldEditLockRegistry;
	}
	/**
	 * This command claims the edit lock for a path, which should be given via the
	 * <code>editPath</code> request parameter, unless the <code>doNotCheckLock</code> request
	 * parameter is set to <code>true</code>, in which case nothing will be done.  If the edit lock is
	 * already claimed, the user will be redirected to a warning page.
	 * @throws ServletException
	 * @throws OpenEditException
	 *
	 * @author Eric Galluzzo
	 */
	public void claimEditLock( WebPageRequest inReq ) throws OpenEditException, ServletException
	{
		String doNotCheckLockStr = inReq.getRequestParameter("doNotCheckLock");

		if ((doNotCheckLockStr == null) || !doNotCheckLockStr.equals("true"))
		{
			User user = inReq.getUser();

			if (user == null)
			{
				inReq.forward("/openedit/authentication/logon.html");
				inReq.getRequest().setAttribute(
					"oe-exception", "You must log in as an editor in order to edit pages.");

				//throw new WSPException( "Cannot edit a page without logging in" );
				return;
			}

			//String editPath = getPath(inParameters, "editPath");
			String editPath = inReq.getRequiredParameter( "editPath" );
			editPath = normalizePath( editPath );
			User oldUser = getEditLockRegistry().getLockOwner(editPath);

			if (!getEditLockRegistry().canLock(editPath, user))
			{
				String redirectURL = getEditLockWarningPath(); // FIXME: Externalize this.
				redirectURL += ("?editPath='" + editPath +"'&origURL=" + URLEncoder.encode(inReq.getPathUrl()) );
					
				if (oldUser != null)
				{
					redirectURL += ("&oldUsername=" + oldUser.getUserName());
				}

				try
				{
					inReq.redirect( redirectURL );
				}
				catch (Exception e)
				{
					throw new OpenEditException(e);
				}
			}
			else
			{
				getEditLockRegistry().lockPath(editPath, user);
			}
		}
	}
	
	protected String normalizePath(String inPath)
	{
		String path = inPath;

		if ((path != null) && !path.startsWith("/"))
		{
			path = "/" + path;
		}
		path = path.replaceAll("\\.draft\\.", ".");
		return path;
	}
	public String getEditLockWarningPath()
	{
		return fieldEditLockWarningPath;
	}
	public void setEditLockWarningPath( String editLockWarningPath )
	{
		fieldEditLockWarningPath = editLockWarningPath;
	}
	
	public void releaseEditLock( WebPageRequest inReq ) throws OpenEditException
	{
		String editPath = inReq.getRequestParameter("editPath");
		if(editPath != null){
		editPath = normalizePath( editPath );
		getEditLockRegistry().unlockPath(editPath, inReq.getUser() );
		}
	}
	public void forciblyClaimEditLock( WebPageRequest inReq ) throws OpenEditException
	{
		User user = inReq.getUser();

		if (user == null)
		{
			throw new OpenEditException("Cannot edit a page without logging in");
		}

		String editPath = inReq.getRequestParameter("editPath");
		editPath = normalizePath( editPath );
		getEditLockRegistry().forciblyLockPath(editPath, user);
	}
}
