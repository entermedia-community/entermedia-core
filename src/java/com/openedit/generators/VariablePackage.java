package com.openedit.generators;

import java.util.HashMap;
import java.util.Map;

public class VariablePackage
{
	protected Map fieldVariables;
	String fieldId;
	
	public Map getVariables()
	{
		if (fieldVariables == null)
		{
			fieldVariables = new HashMap();
		}
		return fieldVariables;
	}
	
	public void addVariable(String inKey, Object inValue)
	{
		getVariables().put(inKey, inValue);
	}

	public String getId()
	{
		return fieldId;
	}

	public void setId(String inId)
	{
		fieldId = inId;
	}
}
