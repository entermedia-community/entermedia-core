package org.openedit.hittracker;

public class ChildFilter extends Term
{
	protected String fieldChildColumn;
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

	
	public boolean equals(Object inObj)
	{
		JoinFilter copy = (JoinFilter)inObj;
		return fieldValue == copy.fieldValue;
	}
	@Override
	public String toQuery()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
