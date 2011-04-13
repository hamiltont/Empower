package org.turnerha.geography;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

public class ProjectionCartesian implements Projection {

	private double mLatDifference;
	private double mLongDifference;

	private GeoBox mGeoBox;
	private Dimension mDesiredSize;

	/**
	 * @param geoBox
	 *            the geographical region being covered
	 * @param desiredDimension
	 *            the screen size we would like to project the geographical
	 *            region into. If margins are desired, they can simply be added
	 *            to the {@link Dimension} before calling this
	 */
	public ProjectionCartesian(GeoBox geoBox, Dimension desiredDimension) {
		
		mGeoBox = geoBox;
		mDesiredSize = desiredDimension;
		
		mLatDifference = mGeoBox.getTopRight().lat - mGeoBox.getBottomLeft().lat;
		mLongDifference = mGeoBox.getBottomLeft().lon - mGeoBox.getTopRight().lon;

	}

	@Override
	public Dimension getDimensionsOf(GeoBox geoBox) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented");
	}

	public GeoBox getGeoBoxOf(Rectangle rect) {
		
		Point tr = new Point(rect.x + rect.width, rect.y);
		Point bl = new Point(rect.x, rect.y - rect.height);
		
		GeoLocation trl = getLocationAt(tr);
		GeoLocation bll = getLocationAt(bl);
		
		return new GeoBox(trl, bll);
	}

	// TODO - check that this works?
	@Override
	public GeoLocation getLocationAt(Point screenPoint) {

		double lat = 0, lon = 0;

		// TODO - Add in checks for the location being outside the GeoBox of
		// interest. For now, we just assume that the projection is being used
		// for the entire geographical area of interest

		int y = screenPoint.y;

		// Flip to align coordinate systems
		y = (int) mDesiredSize.getHeight() - y;

		// Because we want the center geolocation
		double yFloat = y + 0.5;

		// Multiple by px per latitude and add the base
		lat = yFloat * mLatDifference / mDesiredSize.getHeight()
				+ mGeoBox.getBottomLeft().lat;

		int x = screenPoint.x;
		double xFloat = -1 * x + 0.5;

		// Multiple by pixels per latitude, and then remove the base
		lon = xFloat * mLongDifference / mDesiredSize.getWidth()
				+ mGeoBox.getBottomLeft().lon;

		return new GeoLocation(lat, lon);
	}

	@Override
	public Point getPointAt(GeoLocation loc) {

		// TODO - Add in checks for the location being outside the GeoBox of
		// interest. For now, we just assume that the projection is being used
		// for the entire geographical area of interest

		// Remove the base, and then multiple by pixels per latitude
		double yFloat = (loc.lat - mGeoBox.getBottomLeft().lat)
				* mDesiredSize.getHeight() / mLatDifference;

		//int y = (int) Math.round(yFloat);

		// Flip to align coordinate systems
		//y = (int) mDesiredSize.getHeight() - y;
		int y = (int) Math.round(mDesiredSize.getHeight() - yFloat);

		// Remove the base, and then multiple by pixels per latitude
		double xFloat = (loc.lon - mGeoBox.getBottomLeft().lon)
				* mDesiredSize.getWidth() / mLongDifference;

		int x = -1 * (int) Math.round(xFloat);

		return new Point(x, y);
	}
	
	public void zoomIn() {
		
	}
	
	public void zoomOut() {
		
	}
	
	public void panLeft() {
		
	}
	
	public void panRight() {
		
	}
	
	public void panUp() {
		
	}
	
	public void panDown() {
		
	}

}
