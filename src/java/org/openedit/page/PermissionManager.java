package org.openedit.page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.openedit.WebPageRequest;
import org.openedit.data.SearcherManager;

public class PermissionManager
{
	protected PermissionSorter fieldPermissionSorter;
	protected String fieldCatalogId;
	public String getCatalogId()
	{
		return fieldCatalogId;
	}

	public void setCatalogId(String inCatalogId)
	{
		fieldCatalogId = inCatalogId;
	}

	public SearcherManager getSearcherManager()
	{
		return fieldSearcherManager;
	}

	public void setSearcherManager(SearcherManager inSearcherManager)
	{
		fieldSearcherManager = inSearcherManager;
	}

	protected SearcherManager fieldSearcherManager;
	
	public PermissionSorter getPermissionSorter()
	{
		if (fieldPermissionSorter == null)
		{
			fieldPermissionSorter = new PermissionSorter();
			
			Collection items = getSearcherManager().getList(getCatalogId(),"permissionsapp");
			fieldPermissionSorter.loadPermissions(items);
			
		}
		return fieldPermissionSorter;
	}

	public void setPermissionSorter(PermissionSorter inPermissionSorter)
	{
		fieldPermissionSorter = inPermissionSorter;
	}
	
	public void loadPermissions(WebPageRequest inReq, Page inPage, String limited)
	{
		List permissions = null;
		if( limited == null )
		{
			permissions = inPage.getPermissions();
		}
		else
		{
			permissions = new ArrayList();
			String[] array = limited.split("\\s+");
			for (int i = 0; i < array.length; i++)
			{
				Permission permission = inPage.getPermission( array[i] );
				if( permission != null )
				{
					permissions.add(permission);
				}
			}
		}
		if (permissions != null)
		{
			Collections.sort(permissions,getPermissionSorter() );
			
			for (Iterator iterator = permissions.iterator(); iterator.hasNext();)
			{
				Permission per = (Permission) iterator.next();
				boolean value = per.passes(inReq);
				inReq.putPageValue("can" + per.getName(), Boolean.valueOf(value));
			}
		}
	}

}
