package org.entermediadb.location;

import java.util.Map;

import org.dom4j.Element;

//Copyright 2003 Princeton Board of Trustees.
//All rights reserved.

/**
 * The <code>Position</code> class represents coordinates given in
 * latitude/longitude pairs.
 */
public class Position {
	private Double latitude;
	private Double longitude;
	private Double accuracy;
	protected Element result;

	public static final double DEG_TO_RAD = Math.PI / 180.0;
	// Unit Constants
	public static final int DEGREES = 0;
	public static final int RADIANS = 1;

	protected static final char[] BASE_32 = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f',
			'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	public Element getResult() {
		return result;
	}

	/**
	 * Finds the distance in meters using the Haversine formula. This is more
	 * accurate for long distances on a sphere.
	 */
	public double getDistanceInMeters(Position inOther) {
		if (inOther == null || !inOther.isDefined() || !this.isDefined()) {
			return Double.MAX_VALUE;
		}

		double lat1 = this.getLatitude();
		double lon1 = this.getLongitude();
		double lat2 = inOther.getLatitude();
		double lon2 = inOther.getLongitude();

		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		// Using your class's defined RADIUS_EARTH (6400000) for consistency
		return RADIUS_EARTH * c;
	}

	/** ~Meters per degree of latitude. */
	public static final double METERS_PER_DEGREE_LAT = 111_320.0;

	/**
	 * Squared short-range distance in m² (equirectangular, mid-latitude scale for
	 * longitude). Faster than {@link #getDistanceInMeters} and adequate for
	 * sub-km gating. Compare to a squared threshold, e.g.
	 * {@code minMeters * minMeters}, to avoid {@link Math#sqrt} in a hot path.
	 */
	public double getFlatDistanceMetersSquaredTo(Position inOther) {
		if (inOther == null || !inOther.isDefined() || !this.isDefined()) {
			return Double.POSITIVE_INFINITY;
		}
		double lat1 = getLatitude();
		double lon1 = getLongitude();
		double lat2 = inOther.getLatitude();
		double lon2 = inOther.getLongitude();
		double latMid = (lat1 + lat2) / 2.0;
		double mPerDegLon = METERS_PER_DEGREE_LAT * Math.cos(Math.toRadians(latMid));
		double deltaLat = (lat1 - lat2) * METERS_PER_DEGREE_LAT;
		double deltaLon = (lon1 - lon2) * mPerDegLon;
		double lonDiff = Math.abs(lon1 - lon2);
		if (lonDiff > 180) {
			deltaLon = (360.0 - lonDiff) * mPerDegLon;
		}
		return deltaLat * deltaLat + deltaLon * deltaLon;
	}

	/**
	 * Short-range distance in meters (equirectangular; see
	 * {@link #getFlatDistanceMetersSquaredTo}).
	 */
	public double getFlatDistanceMetersTo(Position inOther) {
		double s = getFlatDistanceMetersSquaredTo(inOther);
		if (s == Double.POSITIVE_INFINITY) {
			return Double.POSITIVE_INFINITY;
		}
		return Math.sqrt(s);
	}

	public void setResult(Element inResult) {
		result = inResult;
	}

	private final double RADIUS_EARTH = 6400000;

	/**
	 * Constructs a new <code>Position</code> with the position indicated by the
	 * arguments. The arguments can be <code>null</code> if the values are not
	 * known.
	 * 
	 * @param latitude  a <code>Double</code>
	 * @param longitude a <code>Double</code>
	 */
	public Position(Double latitude, Double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/**
	 * Constructs a new <code>Position</code> that represents the same position as
	 * the argument; in other words, the newly created position is a copy of the
	 * argument position.
	 * 
	 * @param p a <code>Position</code>
	 */
	public Position(Position p) {
		longitude = p.longitude;
		latitude = p.latitude;
	}

	public Position(Map inValue) {
		Double lat = (Double) inValue.get("lat");
		latitude = lat;
		Double lon = (Double) inValue.get("lon");
		longitude = lon;
	}

	/**
	 * Returns the latitude.
	 * 
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * Returns the longitude.
	 * 
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * Sets the latitude.
	 * 
	 * @param latitude a <code>Double</code>
	 */
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	/**
	 * Sets the longitude.
	 * 
	 * @param longitude a <code>Double</code>
	 */
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	/**
	 * Generates a Geohash string for this position. Precision 7: ~152m x 152m
	 * Precision 8: ~38m x 19m Precision 9: ~4.7m x 4.7m
	 */
	public String getGeoHash(int inPrecision) {
		if (!isDefined()) {
			return null;
		}

		StringBuilder hash = new StringBuilder();
		double[] latRange = { -90.0, 90.0 };
		double[] lonRange = { -180.0, 180.0 };

		boolean isEven = true;
		int bit = 0;
		int ch = 0;

		while (hash.length() < inPrecision) {
			double mid;
			if (isEven) {
				mid = (lonRange[0] + lonRange[1]) / 2;
				if (longitude > mid) {
					ch |= (1 << (4 - bit));
					lonRange[0] = mid;
				} else {
					lonRange[1] = mid;
				}
			} else {
				mid = (latRange[0] + latRange[1]) / 2;
				if (latitude > mid) {
					ch |= (1 << (4 - bit));
					latRange[0] = mid;
				} else {
					latRange[1] = mid;
				}
			}

			isEven = !isEven;
			if (bit < 4) {
				bit++;
			} else {
				hash.append(BASE_32[ch]);
				bit = 0;
				ch = 0;
			}
		}
		return hash.toString();
	}

	/**
	 * Finds the distance in meters between two positions on earth.
	 * 
	 * @param position a <code>Position</code>
	 * 
	 * @return the distance between two positions
	 */
	public double distanceTo(Position position) {
		double x_A = RADIUS_EARTH * Math.cos(Math.toRadians(latitude.doubleValue()))
				* Math.cos(Math.toRadians(longitude.doubleValue()));
		double y_A = RADIUS_EARTH * Math.cos(Math.toRadians(latitude.doubleValue()))
				* Math.sin(Math.toRadians(longitude.doubleValue()));
		double z_A = RADIUS_EARTH * Math.sin(Math.toRadians(latitude.doubleValue()));

		double x_B = RADIUS_EARTH * Math.cos(Math.toRadians(position.getLatitude()))
				* Math.cos(Math.toRadians(position.getLongitude()));
		double y_B = RADIUS_EARTH * Math.cos(Math.toRadians(position.getLatitude()))
				* Math.sin(Math.toRadians(position.getLongitude()));
		double z_B = RADIUS_EARTH * Math.sin(Math.toRadians(position.getLatitude()));

		double distance = Math.sqrt((x_A - x_B) * (x_A - x_B) + (y_A - y_B) * (y_A - y_B) + (z_A - z_B) * (z_A - z_B));

		return distance;
	}

	public double getLatitudeRadians() {
		return latitude * DEG_TO_RAD;
	}

	public double getLongitudeRadians() {
		return longitude * DEG_TO_RAD;
	}

	public double getBearingToPosition(Position inOther) {
		return getBearingToPositionDegrees(inOther);
	}

	public double getBearingToPositionDegrees(Position inOther) {
		double bearingRad = calculateBearingInternal(inOther);
		double bearingDeg = bearingRad / DEG_TO_RAD;
		return (bearingDeg + 360) % 360;
	}

	public double getBearingToPositionRadians(Position inOther) {
		double bearingRad = calculateBearingInternal(inOther);
		return (bearingRad + (2 * Math.PI)) % (2 * Math.PI);
	}

	/**
	 * Internal math remains DRY (Don't Repeat Yourself)
	 */
	protected double calculateBearingInternal(Position inOther) {
		if (inOther == null || !inOther.isDefined() || !this.isDefined()) {
			return 0.0;
		}

		double lat1 = this.getLatitudeRadians();
		double lon1 = this.getLongitudeRadians();
		double lat2 = inOther.getLatitudeRadians();
		double lon2 = inOther.getLongitudeRadians();

		double dLon = lon2 - lon1;
		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

		return Math.atan2(y, x);
	}

	/**
	 * Finds the square of the distance between two positions, treating them as
	 * points on a flat plane.
	 * 
	 * @param position a <code>Position</code>
	 * 
	 * @return the distance between two positions treated as points on a flat plane
	 */
	public double coordinateDistanceTo(Position position) {
		double x_1 = latitude.doubleValue();
		double y_1 = longitude.doubleValue();
		double x_2 = position.getLatitude();
		double y_2 = position.getLongitude();
		double x_diff = x_1 - x_2;
		double y_diff = y_1 - y_2;
		if (y_diff > 180)
			y_diff = 360 - y_diff;

		return (x_diff) * (x_diff) + (y_diff) * (y_diff);
	}

	/**
	 * Returns true if both latitude and longitude are not null and false otherwise.
	 * 
	 * @return whether the position is defined
	 */
	public boolean isDefined() {
		if (latitude == null || longitude == null)
			return false;
		return true;
	}

	/**
	 * Returns a string representation of this <code>Position</code>. This is for
	 * debugging purposes only.
	 * 
	 * @return a string representation of this <code>Position</code>
	 */
	public String toString() {
		return "{lat: " + latitude + ", lng: " + longitude + "}";
	}

	public Double getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(Double accuracy) {
		this.accuracy = accuracy;
	}

	public String getFormatedAddress() {
		if (result == null) {
			return null;
		}
		return result.elementTextTrim("formatted_address");
	}
}
