package com.openedit.util.strainer;

public class BooleanFilter extends BaseFilter
{

	public boolean isTrue()
	{
		return Boolean.parseBoolean(getValue());
	}

	public void setTrue(boolean inTrue)
	{
		setValue(String.valueOf(inTrue));
	}
	
	public boolean passes(Object inObj) throws FilterException
	{
		return isTrue();
	}
	public String toString() 
	{
		return String.valueOf(isTrue());
	}
}
