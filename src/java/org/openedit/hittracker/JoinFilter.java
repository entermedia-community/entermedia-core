package org.openedit.hittracker;

public class JoinFilter extends Term
{
	
	public String getCatalog()
	{
		return getParameter("catalog");
	}
	public String getColumnType()
	{
		return getParameter("type");
	}
	public String getColumn()
	{
		return getParameter("column");
	}
	public String getDataPath()
	{
		return getParameter("datapath");
	}
	
	@Override
	public String toQuery()
	{
		return getParameters().toString();
	}
	
}
