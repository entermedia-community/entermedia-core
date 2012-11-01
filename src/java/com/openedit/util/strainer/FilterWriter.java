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
 * Created on Jun 18, 2003
 *
 */
package com.openedit.util.strainer;

import java.util.Iterator;

import com.openedit.ModuleManager;
import com.openedit.OpenEditRuntimeException;
import com.openedit.config.Configuration;
import com.openedit.page.Permission;

public class FilterWriter
{
	protected ModuleManager fieldModuleManager;

	public void writeFilterCollection(Permission inPermission, Configuration inParent) 
	{
		Configuration child = inParent.addChild("permission");
		child.setAttribute("name", inPermission.getName());
		if (inPermission.getRootFilter() != null)
		{
			addFilter( child, inPermission.getRootFilter());
		}

	}
	
	public void addFilter(Configuration inParent, Filter inFilter)
	{
		if( inFilter == null)
		{
			return;
		}
		String elemName = inFilter.getType();
		elemName = elemName.toLowerCase();
		Configuration newChild = inParent.addChild(elemName);
		if (elemName.equals("and") || elemName.equals("or"))
		{
			for (int i = 0; i < inFilter.getFilters().length; i++)
			{
				addFilter(newChild, inFilter.getFilters()[i]);
			}
		}
		else if (elemName.equals("not"))
		{
			//NotFilter not = (NotFilter)inRootFilter;
			if( inFilter.getFilters() != null && inFilter.getFilters().length > 0)
			{
				addFilter(newChild, inFilter.getFilters()[0]);
			}
		}
		else if (elemName.equals("group"))
		{
			newChild.setAttribute("id",((GroupFilter)inFilter).getGroupId());
		}
		else if (elemName.equals("settingsgroup"))
		{
			newChild.setAttribute("id",((SettingsGroupFilter)inFilter).getGroupId());
		}
		else if (elemName.equals("userprofile"))
		{
			UserProfileFilter filter = (UserProfileFilter)inFilter;
			newChild.setAttribute("name",filter.getPropertyName());
			newChild.setAttribute("value",filter.getValue());
		}
		else if (elemName.equals("settingsgroup"))
		{
			SettingsGroupFilter filter = (SettingsGroupFilter)inFilter;
			newChild.setAttribute("id",filter.getGroupId());
		}
		else if (elemName.equals("user"))
		{
			newChild.setAttribute("name",((UserFilter)inFilter).getUsername());
		}
		else if (elemName.equals("permission"))
		{
			newChild.setAttribute("name",((PermissionFilter)inFilter).getPermission());
		}
		else if (elemName.equals("path"))
		{
			newChild.setAttribute("name",((PathFilter)inFilter).getPath());
		}
		else if (elemName.equals("referer"))
		{
			RefererFilter filter = (RefererFilter)inFilter;
			newChild.setAttribute("value",filter.getValue());
		}
		else if (elemName.equals("pageproperty"))
		{
			PagePropertyFilter filter = (PagePropertyFilter)inFilter;
			newChild.setAttribute("name", filter.getProperty());
			newChild.setAttribute("equals", filter.getEquals() );
		}
		else if (elemName.equals("pagevalue"))
		{
			//newChild = new PageValueFilter(inConfig.getAttribute("name"), inConfig.getAttribute("equals"));
			PageValueFilter filter = (PageValueFilter)inFilter;
			newChild.setAttribute("name", filter.getProperty());
			newChild.setAttribute("equals", filter.getEquals() );
			
		}
//		else if (elemName.equals("request-attribute"))
//		{
//			result = new RequestAttributeFilter(
//					inConfig.getAttribute("name"), inConfig.getAttribute("equals"));
//		}
		else if (elemName.equals("blank") )				
		{
			newChild.setAttribute("value","true");			
		}
		else if ( elemName.equals("boolean") )
		{	
			BooleanFilter bool = (BooleanFilter)inFilter;
			newChild.setAttribute("value",String.valueOf(bool.isTrue()));
		}
		else if (elemName.equals("dataproperty"))
		{
			DataPropertyFilter filter = (DataPropertyFilter)inFilter;
			newChild.setAttribute("value",filter.getValue());
			newChild.setAttribute("name",filter.getPropertyName());
		}
		
		else if (elemName.equals("userproperty"))
		{
			UserPropertyFilter filter = (UserPropertyFilter)inFilter;
			newChild.setAttribute("value",filter.getValue());
			newChild.setAttribute("name",filter.getPropertyName());
		}
		
		
		else if (elemName.equals("action"))
		{
			ActionFilter action = (ActionFilter)inFilter;
			if( action.getActionName() != null )
			{
				newChild.setAttribute("name",action.getActionName());
			}
			if( action.getConfig() != null)
			{
				for (Iterator iterator = action.getConfig().getChildren().iterator(); iterator.hasNext();)
				{
					Configuration	conf = (Configuration)iterator.next();
					newChild.addChild(conf);
				}
			}
		}
		else
		{
			throw new OpenEditRuntimeException("Unrecognized filter element <" + elemName + ">");
		}
	}

}
