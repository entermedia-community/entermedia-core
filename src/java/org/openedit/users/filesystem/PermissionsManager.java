package org.openedit.users.filesystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.openedit.OpenEditException;
import org.openedit.OpenEditRuntimeException;
import org.openedit.config.Configuration;
import org.openedit.config.XMLConfiguration;
import org.openedit.page.Page;
import org.openedit.page.manage.PageManager;
import org.openedit.users.Permission;
import org.openedit.util.XmlUtil;

public class PermissionsManager
{
	protected List fieldSystemPermissionGroups;
	protected PermissionGroup fieldUserPermissionGroup;
	protected long fieldLastEditTime;
	protected PageManager fieldPageManager;
	protected File fieldRootDir;
	
	public PermissionsManager()
	{
	}
	
		
	public void addSystemPermissions(Reader inReader)
	{
		PermissionGroup group = loadPermissions(inReader);
		getSystemPermissionGroups().add(group);
	}
	
	protected PermissionGroup loadPermissions(Reader inReader)
	{
		List permissions = new ArrayList();
		XMLConfiguration userManagerConfig = new XMLConfiguration();
		userManagerConfig.populate(new XmlUtil().getXml(inReader,"UTF-8"));

		PermissionGroup pgroup = new PermissionGroup();
		String name = userManagerConfig.getAttribute("name");
		pgroup.setName(name);
		
		permissions = new ArrayList( );
		
		for (Iterator iter = userManagerConfig.getChildren("permission").iterator(); iter.hasNext();)
		{
			Configuration permissionElem = (Configuration)iter.next(); 
			
			String desc = permissionElem.getAttribute("description");
			if ( desc == null)
			{
				desc = permissionElem.getValue();
			}
			Permission perm = new Permission(permissionElem.getAttribute("id"),desc);
			
			perm.setDescription("");
			desc = permissionElem.getValue();
			if (desc != null)
			{
				perm.setDescription(desc);
			}
			permissions.add( perm );
		}
		pgroup.setPermissions(permissions);
		return pgroup;
	}
	
	public PermissionGroup loadPermissions(Page inPage) throws OpenEditException
	{
		if( !inPage.exists())
		{
			return new PermissionGroup();
		}
		return loadPermissions(inPage.getReader());
	}
	
	public List getSystemPermissionGroups()
	{
		if (fieldSystemPermissionGroups == null)
		{
			fieldSystemPermissionGroups = new ArrayList();
		}
		return fieldSystemPermissionGroups;
	}

	public void setSystemPermissionGroups(List inSystemPermissions)
	{
		fieldSystemPermissionGroups = inSystemPermissions;
	}
	
	public List getSystemPermissions()
	{
		List permissions = new ArrayList();
		for (Iterator iter = getSystemPermissionGroups().iterator(); iter.hasNext();)
		{
			PermissionGroup group = (PermissionGroup) iter.next();
			permissions.addAll(group.getPermissions());
		}
		return permissions;
	}

	public PageManager getPageManager()
	{
		return fieldPageManager;
	}

	public void setPageManager(PageManager inPageManager)
	{
		fieldPageManager = inPageManager;
	}

	public void loadPermissions()
	{
		try
		{
			try
			{
				ClassLoader loader  = getClass().getClassLoader();
				if( loader == null)
				{
					loader = ClassLoader.getSystemClassLoader();
				}
				Enumeration pluginDefs = loader.getResources( "permissions.xml" );
				while( pluginDefs.hasMoreElements() )
				{
					URL url = (URL) pluginDefs.nextElement();
					addSystemPermissions( new InputStreamReader(url.openStream() ) );
				}
			} catch ( IOException ex)
			{
				throw new OpenEditRuntimeException(ex);
			}

		
		}
		catch ( Exception ex)
		{
			throw new OpenEditRuntimeException(ex);
		}
	}
//	protected void loadPermissionsDefs(Reader inUrl) 
//	{
//
//		FileReader reader;
//		try
//		{
//			reader = new FileReader(inUrl);
//		} catch (Exception ex)
//		{
//			throw new OpenEditRuntimeException(ex);
//		}
//		addSystemPermissions(reader); 
//	}

}
