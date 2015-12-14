package org.openedit.hittracker;

public class Join
{
	protected String fieldFilterColumn; //id
	protected String fieldFilterSearchType;
	protected SearchQuery fieldFilterQuery;
	protected String fieldResultsColumn; //division_id
	protected boolean fieldFilterHasMultiValues;
	
	public boolean isFilterHasMultiValues()
	{
		return fieldFilterHasMultiValues;
	}
	public void setFilterHasMultiValues(boolean inRemoteHasMultiValues)
	{
		fieldFilterHasMultiValues = inRemoteHasMultiValues;
	}
	public String getFilterColumn()
	{
		return fieldFilterColumn;
	}
	public void setFilterColumn(String inRemoteColumn)
	{
		fieldFilterColumn = inRemoteColumn;
	}
	public String getFilterSearchType()
	{
		return fieldFilterSearchType;
	}
	public void setFilterSearchType(String inRemoteSearchType)
	{
		fieldFilterSearchType = inRemoteSearchType;
	}
	public SearchQuery getFilterQuery()
	{
		return fieldFilterQuery;
	}
	public void setFilterQuery(SearchQuery inRemoteQuery)
	{
		fieldFilterQuery = inRemoteQuery;
	}
	public String getResultsColumn()
	{
		return fieldResultsColumn;
	}
	public void setResultsColumn(String inLocalColumn)
	{
		fieldResultsColumn = inLocalColumn;
	}
	public boolean equals(Object inObj)
	{
		Join join = (Join)inObj;
		return join.getFilterQuery().equals(getFilterQuery());
	}
}
