package com.openedit.modules;

import com.openedit.WebPageRequest;

public class TestObject
{
	String localString;
	String[] localStringArray;
	int localInt;
	public void setString(String inString)
	{
		localString = inString;
	}

	public String getString()
	{
		return localString;
	}
	
	public void setStringArray(String[] inStringArray)
	{
		localStringArray = inStringArray;
	}

	public String[] getStringArray()
	{
		return localStringArray;
	}
	
	public void setInt( int inInt )
	{
		localInt = inInt;
	}
	
	public int getInt()
	{
		return localInt;
	}
	
	boolean invalidSetterCalled;
	public void setInvalidSetter( String inString1, String inString2 )
	{
		invalidSetterCalled = true;
	}
	
}
