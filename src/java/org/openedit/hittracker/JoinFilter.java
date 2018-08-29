package org.openedit.hittracker;

public class JoinFilter extends Term
{
	
	public String getCatalog()
	{
		return (String) getValue("catalog");
	}
	public String getColumnType()
	{
		return (String) getValue("type");
	}
	public String getColumn()
	{
		return (String) getValue("column");
	}
	public String getDataPath()
	{
		return (String) getValue("datapath");
	}
	
	@Override
	public String toQuery()
	{
		return getParameters().toString();
	}
	
}
