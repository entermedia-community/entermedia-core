package org.openedit.hittracker;

public class Join
{
	protected String fieldChildColumn;
	protected String fieldEqualsValue;
	protected String fieldChildTable;
	
	public String getChildTable()
	{
		return fieldChildTable;
	}


	public void setChildTable(String inChildTable)
	{
		fieldChildTable = inChildTable;
	}


	public String getChildColumn()
	{
		return fieldChildColumn;
	}


	public void setChildColumn(String inChildColumn)
	{
		fieldChildColumn = inChildColumn;
	}


	public String getEqualsValue()
	{
		return fieldEqualsValue;
	}


	public void setEqualsValue(String inEqualsValue)
	{
		fieldEqualsValue = inEqualsValue;
	}
	
	public boolean equals(Object inObj)
	{
		Join copy = (Join)inObj;
		return fieldEqualsValue == copy.fieldEqualsValue;
	}
}
