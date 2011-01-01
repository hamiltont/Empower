package org.turnerha.geography;

/**
 * 
 */

public class GeoLocation {
	public double lat;
	public double lon;

	public GeoLocation() {
	};

	public GeoLocation(double latit, double longit) {
		lat = latit;
		lon = longit;
	}

	@Override
	public boolean equals(Object o) {
		if (false == (o instanceof GeoLocation))
			return false;

		GeoLocation p = (GeoLocation) o;

		if (p.lat == lat && p.lon == lon)
			return true;
		return false;
	}

	@Override
	public String toString() {
		return "[" + lat + ", " + lon + "]";
	}
}