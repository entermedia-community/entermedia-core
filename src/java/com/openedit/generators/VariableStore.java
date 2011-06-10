package com.openedit.generators;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class VariableStore
{
	protected Queue fieldPackages;
	protected int idnum = 0;

	protected Queue getPackages()
	{
		if (fieldPackages == null)
		{
			fieldPackages = new LinkedList();
		}

		return fieldPackages;
	}
	
	public String addPackage(VariablePackage inPackage)
	{
		idnum++;
		String id = "" + idnum;
		inPackage.setId(id);
		getPackages().add(inPackage);
		if (getPackages().size() > 100)
		{
			getPackages().remove();
		}
		return id;
	}
	
	public VariablePackage getPackage(String inId)
	{
		for (Iterator iterator = getPackages().iterator(); iterator.hasNext();)
		{
			VariablePackage varPackage = (VariablePackage) iterator.next();
			if (varPackage.getId().equals(inId))
			{
				return varPackage;
			}
		}
		return null;
	}
}
