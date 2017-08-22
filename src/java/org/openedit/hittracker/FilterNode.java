package org.openedit.hittracker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openedit.WebPageRequest;
import org.openedit.data.BaseData;
import org.openedit.data.PropertyDetail;
import org.openedit.modules.translations.LanguageMap;

public class FilterNode extends BaseData
{
	
	protected List fieldChildren;
	protected boolean isSelected;
	protected PropertyDetail fieldPropertyDetail;
	
	
	public PropertyDetail getPropertyDetail()
	{
		return fieldPropertyDetail;
	}
	public void setPropertyDetail(PropertyDetail inPropertyDetail)
	{
		fieldPropertyDetail = inPropertyDetail;
	}
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
		return -1;
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
	
	public String getText(WebPageRequest inReq)
	{
		String val = null;
		if( getPropertyDetail() != null && getPropertyDetail().isBoolean())
		{
			if( getName().equals( "1" ) )
			{
				val = inReq.getText("True");
			}
			else
			{
				val = inReq.getText("False");
			}
		}
		else
		{
			val = getName(inReq.getLocale());
		}
		return val;
	}
	
	@Override
	public String getName(String inLocale)
	{
		String val =  super.getName(inLocale);
		if( val == null)
		{
			Object name = getValue("name_int");
			if( name instanceof LanguageMap)
			{
				LanguageMap values = (LanguageMap)name;
				val = values.getDefaultText(inLocale);
			}
			else
			{
				val = (String)name;
			}	
		}
		return val;
	}
	
}
