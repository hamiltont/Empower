package org.turnerha.map;

/**
 * 
 */

public class DoublePoint {
	public double lat;
	public double lon;
	
	public DoublePoint() {};

	public DoublePoint(double latit, double longit) {
		lat = latit;
		lon = longit;
	}

	@Override
	public boolean equals(Object o) {
		if (false == (o instanceof DoublePoint))
			return false;

		DoublePoint p = (DoublePoint) o;

		if (p.lat == lat && p.lon == lon)
			return true;
		return false;
	}
}