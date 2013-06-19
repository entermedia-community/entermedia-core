package com.openedit.hittracker;

public class Join
{
	protected String fieldRemoteColumn; //id
	protected String fieldRemoteSearchType;
	protected SearchQuery fieldRemoteQuery;
	protected String fieldLocalColumn; //division_id
	protected boolean fieldRemoteHasMultiValues;
	
	public boolean isRemoteHasMultiValues()
	{
		return fieldRemoteHasMultiValues;
	}
	public void setRemoteHasMultiValues(boolean inRemoteHasMultiValues)
	{
		fieldRemoteHasMultiValues = inRemoteHasMultiValues;
	}
	public String getRemoteColumn()
	{
		return fieldRemoteColumn;
	}
	public void setRemoteColumn(String inRemoteColumn)
	{
		fieldRemoteColumn = inRemoteColumn;
	}
	public String getRemoteSearchType()
	{
		return fieldRemoteSearchType;
	}
	public void setRemoteSearchType(String inRemoteSearchType)
	{
		fieldRemoteSearchType = inRemoteSearchType;
	}
	public SearchQuery getRemoteQuery()
	{
		return fieldRemoteQuery;
	}
	public void setRemoteQuery(SearchQuery inRemoteQuery)
	{
		fieldRemoteQuery = inRemoteQuery;
	}
	public String getLocalColumn()
	{
		return fieldLocalColumn;
	}
	public void setLocalColumn(String inLocalColumn)
	{
		fieldLocalColumn = inLocalColumn;
	}
}
