package org.openedit.hittracker;

public class GeoFilter
{
	
	
	protected String fieldPropertyDetail;
	protected double fieldLatitude;
	protected double fieldLongitude;
	protected String fieldType;
	protected String fieldDistance;
	
	public String getPropertyDetail()
	{
		return fieldPropertyDetail;
	}
	public void setPropertyDetail(String inPropertyDetail)
	{
		fieldPropertyDetail = inPropertyDetail;
	}
	public double getLatitude()
	{
		return fieldLatitude;
	}
	public void setLatitude(double inLatitude)
	{
		fieldLatitude = inLatitude;
	}
	public double getLongitude()
	{
		return fieldLongitude;
	}
	public void setLongitude(double inLongitude)
	{
		fieldLongitude = inLongitude;
	}
	public String getType()
	{
		return fieldType;
	}
	public void setType(String inType)
	{
		fieldType = inType;
	}
	public String getDistance()
	{
		return fieldDistance;
	}
	public void setDistance(String inDistance)
	{
		fieldDistance = inDistance;
	}


}
