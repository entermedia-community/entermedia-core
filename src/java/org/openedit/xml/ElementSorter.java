package org.openedit.xml;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ElementSorter implements Comparator
{
	List<Comparator> stack = new ArrayList();
	
	public ElementSorter(List<String> inSorts)
	{
		for(String sortby: inSorts)
		{
			if (sortby.endsWith("Up"))
			{
				sortby = sortby.substring(0, sortby.length() - 2);
				stack.add( createComparator(true, sortby) );
			}
			else if (sortby.endsWith("Down"))
			{
				sortby = sortby.substring(0, sortby.length() - 4);
				stack.add( createComparator(false, sortby) );
			}
			else
			{
				stack.add( createComparator(true, sortby) );
			}
		}
	}

	protected Comparator createComparator(final boolean reverse,final String inField)
	{
		Comparator created = new Comparator() 
		{
			public int compare(Object o1, Object o2) 
			{
				ElementData ed1 = null;
				ElementData ed2 = null;

				if( reverse )
				{
					ed1 = (ElementData) o1;
					ed2 = (ElementData) o2;
				}
				else
				{
					ed2 = (ElementData) o1;
					ed1 = (ElementData) o2;
				}				
				String s1, s2;
				if ("text".equals(inField) || "name".equals(inField))
				{
					s1 = ed1.getName();
					s2 = ed2.getName();	
				}
				else
				{
					s1 = ed1.get(inField);
					s2 = ed2.get(inField);
				}
				if(s1 == null && s2 == null)
				{
					return 0;
				}
				if (s1 == null)
				{
					return -s2.compareTo(s1);
				}
				if (s2 == null)
				{
					return -s1.compareTo(s2);
				}
				return s1.toLowerCase().compareTo(s2.toLowerCase());
			}
		};	
		return created;
	}

	@Override
	public int compare(Object inO1, Object inO2)
	{
		for(Comparator compare : stack)
		{
			int found = compare.compare(inO1, inO2);
			if( found != 0)
			{
				return found;
			}
		}
		return 0;
	}
}
