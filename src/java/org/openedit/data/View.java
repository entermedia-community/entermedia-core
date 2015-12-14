package org.openedit.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.openedit.Data;
import org.openedit.OpenEditException;

/**
 * This is essentially a tree that may contain other View objects,
 * or PropertyDetails (leaves).
 * @author axis
 *
 */

public class View extends ArrayList implements ViewItem
{
	
	protected String fieldId;
	
	public String getId()
	{
		return fieldId;
	}

	public void setId(String inId)
	{
		fieldId = inId;
	}

	public String getTitle()
	{
		return fieldTitle;
	}

	public void setTitle(String inTitle)
	{
		fieldTitle = inTitle;
	}

	protected String fieldTitle; //displayed as a section header
	protected Data fieldViewFile;
	
	public Data getViewFile()
	{
		return fieldViewFile;
	}

	public void setViewFile(Data inViewFile)
	{
		fieldViewFile = inViewFile;
	}

	/*
	 * Should View and PropertyDetail share a superclass?
	 */
	protected boolean isViewItem(Object inObj)
	{
		return (inObj instanceof ViewItem);
	}
	
	public boolean add(Object arg0)
	{
		if (isViewItem(arg0))
		{
			super.add(arg0);
			return true;
		}
		else
		{
			throw new OpenEditException("Can not add " + arg0 + " to View");
		}
	}

	public boolean addAll(Collection arg0)
	{
		for (Iterator iterator = arg0.iterator(); iterator.hasNext();)
		{
			Object item = (Object) iterator.next();
			add(item);
		}
		return true;
	}
	
	public boolean hasChildren()
	{
		return (!isEmpty());
	}
	
	public boolean isLeaf()
	{
		return false;
	}
	
	public PropertyDetail findDetail(String inId)
	{
		for (Iterator iterator = iterator(); iterator.hasNext();)
		{
			ViewItem item = (ViewItem) iterator.next();
			if (item.isLeaf())
			{
				PropertyDetail leaf = (PropertyDetail)item;
				if (leaf.getId().equals(inId))
				{
					return leaf;
				}
			}
			else
			{
				View child = (View)item;
				return child.findDetail(inId);
			}
			
		}
		return null;
	}
	
	public void sortAlphabetically()
	{
		Collections.sort(this, 
				new Comparator<PropertyDetail>()
                {
		            public int compare(PropertyDetail f1, PropertyDetail f2)
		            {
		                return f1.getText().compareTo(f2.getText());
		            }        
		        }
		);
	}

}
