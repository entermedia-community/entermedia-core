package org.openedit.hittracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.openedit.WebPageRequest;
import org.openedit.data.BaseData;
import org.openedit.data.PropertyDetail;
import org.openedit.modules.translations.LanguageMap;

public class FilterNode extends BaseData
{
	
	protected Collection fieldChildren;
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
	public List getChildrenByCount()
	{
		List sorted = new ArrayList(getChildren());
		sorted.sort(new Comparator<FilterNode>()
		{
			@Override
			public int compare(FilterNode inArg1, FilterNode inArg2)
			{
				String count1 = inArg1.get("count");
				if( count1 != null)
				{
					int c1 = Integer.parseInt(count1);
					String count2 = inArg2.get("count");
					if( count2 == null)  //Needed?
					{
						return 1; //reversed
					}
					int c2 = Integer.parseInt(count2);
					if( c1 > c2)
					{
						return -1;
					}
					else if( c1 < c2)
					{
						return 1;
					}
					else
					{
						return 0;
					}
				}
				return 0;
			}
		});
		return sorted;
	}
	public Collection getChildren()
	{
		if (fieldChildren == null)
		{
			fieldChildren = new ArrayList();
			
		}

		return fieldChildren;
	}
	public void setChildren(Collection inChildren)
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
	public int getCount()
	{
		String count = get("count");
		if( count != null)
		{
			return Integer.parseInt(count);
		}
		return 0;
	}
	
	public int getCount(String inId)
	{
		FilterNode child = getChild(inId);
		if( child != null)
		{
			return child.getCount();
		}
		return 0;
	}
	
	public FilterNode getChild(String inId)
	{
		if( inId == null)
		{
			return null;
		}
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
	public void addChildToStart(FilterNode inChild)
	{
		if(getChildren().isEmpty())
		{
			getChildren().add(inChild);
		}
		else
		{
			List newcollection = new ArrayList(getChildren());
			newcollection.add(0,inChild);
			setChildren(newcollection);
		}
		
	}
	public boolean isEmpty()
	{
		return getChildren().isEmpty();
	}
	
}
