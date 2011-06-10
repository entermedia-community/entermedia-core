package com.openedit.page;

import com.openedit.WebPageRequest;
import com.openedit.util.PathUtilities;
import com.openedit.util.strainer.Filter;

public class Permission implements Comparable
{
	protected String fieldName;
	protected Filter fieldRootFilter;
	protected String fieldPath;
	
	public String getName()
	{
		return fieldName;
	}

	public void setName(String inName)
	{
		fieldName = inName;
	}

	public Filter getRootFilter()
	{
		return fieldRootFilter;
	}

	public void setRootFilter(Filter inRootFilter)
	{
		fieldRootFilter = inRootFilter;
	}
	public boolean passes(WebPageRequest inReq)
	{
		if( fieldRootFilter == null)
		{
			return false;
		}
		boolean passed = getRootFilter().passes(inReq);
		return passed;
	}

	//Goes into the children
	public Filter findCondition(int[] inTree)
	{
		Filter thisnode = getRootFilter();
		for (int i = 1; i < inTree.length; i++)
		{
			thisnode = thisnode.getFilters()[inTree[i]];
		}
		return thisnode;
	}
	//finds a parent node
	public Filter findConditionParent(int[] inList)
	{
		Filter thisnode = getRootFilter();
		Filter parent = null;
		for (int i = 1; i < inList.length; i++)
		{
			parent = thisnode;
			if( parent.getFilters() == null || parent.getFilters().length <= inList[i])
			{
				return null;
			}
			thisnode = thisnode.getFilters()[inList[i]];
			
		}
		return parent;
	}

	public String getPath()
	{
		return fieldPath;
	}

	public void setPath(String inPath)
	{
		fieldPath = inPath;
	}
	public String getPathName()
	{
		String name = "";
		if( isFolder() )
		{
			String val = PathUtilities.extractDirectoryPath(getPath());
			if( val.length() == 0)
			{
				return "/";
			}
			name = val;
		}
		else
		{
			name = PathUtilities.extractPagePath(getPath());
		}
		if( name.length() > 100)
		{
			name = name.substring(name.length() -  100);
			name =  ".." + name;
		}
		return name;
	}
	public String toDisplay()
	{
		if (getRootFilter() == null)
			return "";
		return getRootFilter().toString();
	}
	public String getId()
	{
		return PathUtilities.makeId(getName() + getPath());
	}
	public boolean isFolder()
	{
		return getPath().endsWith("_site.xconf");
	}

	public int compareTo(Object arg0)
	{
		Permission per = (Permission)arg0;
		int val =  getName().compareTo(per.getName());
		return val;
	}
	public String toString()
	{
		return toDisplay();
	}	
}
