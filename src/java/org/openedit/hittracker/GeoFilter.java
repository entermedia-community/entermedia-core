package org.openedit.hittracker;

import org.entermediadb.location.Position;

public class GeoFilter extends Term
{
	protected double fieldLatitude;
	protected double fieldLongitude;
	protected String fieldType;
	protected long fieldDistance;
	protected Position fieldCenter;
	
	public Position getCenter()
	{
		return fieldCenter;
	}
	public void setCenter(Position inCenter)
	{
		fieldCenter = inCenter;
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
	public long getDistance()
	{
		return fieldDistance;
	}
	public void setDistance(long inDistance)
	{
		fieldDistance = inDistance;
	}
	@Override
	public String toQuery()
	{
		String fin = getDetail().getId() + "location = " + " " + getValue() + " within " + getDistance();
		return fin;
	}

}
