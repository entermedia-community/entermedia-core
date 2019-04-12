package org.openedit.util;

public class Counter
{
	protected int fieldCount = 0;

	public int getCount()
	{
		return fieldCount;
	}

	public void setCount(int inCount)
	{
		fieldCount = inCount;
	}
	
	public int next()
	{
		return fieldCount++;
	}
	
	public String printNext()
	{
		return String.valueOf(next());
	}
}
