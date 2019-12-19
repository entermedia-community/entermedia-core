package org.openedit.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.openedit.WebPageRequest;
import org.openedit.data.BaseData;
import org.openedit.util.PathUtilities;
import org.openedit.util.strainer.Filter;
import org.openedit.util.strainer.OrFilter;

public class Permission extends BaseData implements Comparable 
{
	protected String fieldName;
	protected Filter fieldRootFilter;
	protected String fieldPath;
	
	public String getName()
	{
		if(fieldName == null) {
			return getId();
		}
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
	
	//Goes into the children
	public String findTree(Filter inFilter)
	{
		StringBuffer tree = new StringBuffer();
		tree.append("0");
		Filter thisnode = getRootFilter();
		if( thisnode != inFilter)
		{
			if( thisnode instanceof OrFilter)
			{
				OrFilter or = (OrFilter)thisnode;
				for (int i = 0; i < or.getFilters().length; i++)
				{
					Filter myself = or.getFilters()[i];
					if( myself == inFilter)
					{
						tree.append("/" + i);
						break;
					}				
				}
			}
		}	
		return tree.toString();
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
//	public String getId()
//	{
//		return PathUtilities.makeId(getName() + getPath());
//	}
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
