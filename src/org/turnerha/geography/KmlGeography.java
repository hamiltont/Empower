package org.turnerha.geography;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows a kml-based geography file to be painted to the screen. There should
 * only be one of these per run of the simulation, so I am making it a singleton
 * 
 * @author hamiltont
 * 
 */
public class KmlGeography {

	private List<MyPolygon> mPolys;

	private GeoLocation topRight = new GeoLocation(39.520992, -74.421386);
	private GeoLocation botLeft = new GeoLocation(36.509636, -83.913574);

	private double latDifference = topRight.lat - botLeft.lat;
	private double lonDifference = botLeft.lon - topRight.lon;

	private int mPixelWidth = 1400;
	private int mPixelHeight = 850;

	private static KmlGeography instance_ = null;

	public static KmlGeography getInstance() {
		if (instance_ == null)
			throw new IllegalStateException("Init has not been called");
		return instance_;
	}

	private KmlGeography() {
	}

	public static KmlGeography init(List<MyPolygon> polys, Dimension screen,
			GeoLocation tr, GeoLocation bl) {

		if (instance_ != null)
			throw new IllegalStateException("Init has already been called");

		instance_ = new KmlGeography();

		instance_.topRight = tr;
		instance_.botLeft = bl;
		instance_.latDifference = instance_.topRight.lat
				- instance_.botLeft.lat;
		instance_.lonDifference = instance_.botLeft.lon
				- instance_.topRight.lon;

		instance_.mPixelWidth = screen.width;
		instance_.mPixelHeight = screen.height;

		instance_.mPolys = polys;

		for (MyPolygon poly : polys) {
			poly.mPoints = new ArrayList<Point>(poly.mLocations.size());
			int[] xArray = new int[poly.mLocations.size()];
			int[] yArray = new int[poly.mLocations.size()];

			int pos = 0;
			for (GeoLocation dp : poly.mLocations) {

				// Remove the base, and then multiple by pixels per latitude
				double yFloat = (dp.lat - instance_.botLeft.lat)
						* (double) (instance_.mPixelHeight)
						/ instance_.latDifference;

				int y = (int) Math.round(yFloat);
				// Flip to align coordinate systems
				y = instance_.mPixelHeight - y;

				// Remove the base, and then multiple by pixels per latitude
				double xFloat = (dp.lon - instance_.botLeft.lon)
						* (double) (instance_.mPixelWidth)
						/ instance_.lonDifference;

				int x = -1 * (int) Math.round(xFloat);

				xArray[pos] = x;
				yArray[pos] = y;

				poly.mPoints.add(new Point(x, y));
				pos++;

			}
			poly.mPoly = new Polygon(xArray, yArray, xArray.length);
		}
		return instance_;
	}

	private Color seeThruBlack = new Color(0, 0, 0, 50);

	public void paint(Graphics g) {
		g.setColor(seeThruBlack);

		for (MyPolygon poly : mPolys) {
			for (int i = 0; i < poly.mPoints.size() - 1; i++) {
				Point a = poly.mPoints.get(i);
				Point b = poly.mPoints.get(i + 1);
				g.drawLine(a.x, a.y, b.x, b.y);
			}

		}

	}

	/**
	 * Returns true if the pixel is contained within the polygons present on
	 * this map, false otherwise
	 */
	private Polygon lastPoly = null;

	public boolean contains(int x, int y) {
		if (lastPoly != null)
			if (lastPoly.contains(x, y))
				return true;

		for (MyPolygon poly : mPolys) {
			Polygon javaPoly = poly.mPoly;
			if (javaPoly.contains(x, y)) {
				lastPoly = javaPoly;
				return true;
			}
		}
		return false;
	}

	public GeoBox getGeoBox() {
		return new GeoBox(topRight, botLeft);
	}

	public Rectangle getPixelSize() {
		Rectangle totalUnion = null;
		for (MyPolygon poly : mPolys)
			if (totalUnion == null)
				totalUnion = new Rectangle(poly.mPoly.getBounds());
			else
				totalUnion = totalUnion.union(poly.mPoly.getBounds());

		return totalUnion;
	}
}
