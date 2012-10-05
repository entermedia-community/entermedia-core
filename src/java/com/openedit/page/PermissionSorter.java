package com.openedit.page;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.openedit.Data;


public class PermissionSorter implements Comparator<Permission>
{
	protected Map<String,Integer> fieldSortOrder;
	
	public PermissionSorter()
	{
	}
	public PermissionSorter(Collection<Data> inPermissions)
	{
		loadPermissions(inPermissions);
	}
	public void loadPermissions(Collection<Data> inPermissions)
	{
		for (Data data : inPermissions)
		{
			String ordering = data.get("ordering");
			if( ordering == null )
			{
				ordering = "0";
			}
			getSortOrder().put(data.getName(), Integer.valueOf( ordering ) );
		}
	}

	public Map<String,Integer> getSortOrder()
	{
		if (fieldSortOrder == null)
		{
			fieldSortOrder = new HashMap<String,Integer>();
		}

		return fieldSortOrder;
	}

	public int compare(Permission inO1, Permission inO2)
	{
		Integer ordering1 = getSortOrder().get(inO1.getName());
		Integer ordering2 = getSortOrder().get(inO2.getName());
		if( ordering1 != null && ordering2 != null )
		{
			return ordering1.compareTo(ordering2);
		}
		if( ordering1 == null )
		{
			return 0; //null means it lame and can go first
		}
		if( ordering2 == null )
		{
			return 1; //null means can go last 
		}
		return 0;
	}

}
