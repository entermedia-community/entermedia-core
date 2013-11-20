package com.openedit.hittracker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openedit.data.BaseData;

public class FilterNode extends BaseData
{
	
	protected List fieldChildren;
	protected boolean isSelected;
	
	
	public List getChildren()
	{
		if (fieldChildren == null)
		{
			fieldChildren = new ArrayList();
			
		}

		return fieldChildren;
	}
	public void setChildren(List inChildren)
	{
		fieldChildren = inChildren;
	}
	public boolean isSelected()
	{
		return isSelected;
	}
	public void setSelected(boolean inIsSelected)
	{
		isSelected = inIsSelected;
	}
	public void addChild(FilterNode inFilterNode)
	{
		getChildren().add(inFilterNode);
		
	}
	
	public int getCount(String inId)
	{
		FilterNode child = getChild(inId);
		if( child != null)
		{
			String count = child.get("count");
			if( count != null)
			{
				return Integer.parseInt(count);
			}
		}
		return 0;
	}
	
	public FilterNode getChild(String inId)
	{
		for (Iterator iterator = getChildren().iterator(); iterator.hasNext();)
		{
			FilterNode child = (FilterNode) iterator.next();
			if(inId.equals( child.getId() ) )
			{
				return child;
			}
		}
		return null;
	}
	
	public boolean hasSelections(FilterNode node){
		if(node.isSelected()){
			return true;
		}
		if(node.getChildren().size() > 0){
			for (Iterator iterator = getChildren().iterator(); iterator.hasNext();)
			{
				FilterNode child = (FilterNode) iterator.next();
				if(child.isSelected()){
					return true;
				}
			}
		}
		return false;
	}
	
}
