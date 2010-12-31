package org.turnerha.geography;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class KmlGeography {

	private List<MyPolygon> mPolys;

	private static DoublePoint topRight = new DoublePoint(39.520992,
			-74.421386);
	private static DoublePoint botLeft = new DoublePoint(36.509636,
			-83.913574);

	private static double latDifference = topRight.lat - botLeft.lat;
	private static double lonDifference = botLeft.lon - topRight.lon;

	private static int mPixelWidth = 1400;
	private static int mPixelHeight = 850;

	public KmlGeography(List<MyPolygon> polys, Dimension screen, DoublePoint tr, DoublePoint bl) {
		topRight = tr;
		botLeft = bl;
		latDifference = topRight.lat - botLeft.lat;
		lonDifference = botLeft.lon - topRight.lon;
		
		mPixelWidth = screen.width;
		mPixelHeight = screen.height;

		mPolys = polys;
		
		for (MyPolygon poly : polys) {
			poly.mPoints = new ArrayList<Point>(poly.mLocations.size());
			int[] xArray = new int[poly.mLocations.size()];
			int[] yArray = new int[poly.mLocations.size()];

			int pos = 0;
			for (DoublePoint dp : poly.mLocations) {

				// Remove the base, and then multiple by pixels per latitude
				double yFloat = (dp.lat - botLeft.lat)
						* (double) (mPixelHeight) / latDifference;

				int y = (int) Math.round(yFloat);
				// Flip to align coordinate systems
				y = mPixelHeight - y;

				// Remove the base, and then multiple by pixels per latitude
				double xFloat = (dp.lon - botLeft.lon) * (double) (mPixelWidth)
						/ lonDifference;

				int x = -1 * (int) Math.round(xFloat);

				xArray[pos] = x;
				yArray[pos] = y;

				poly.mPoints.add(new Point(x, y));
				pos++;

			}
			poly.mPoly = new Polygon(xArray, yArray, xArray.length);
		}
	}

	public void paint(Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0, 0, mPixelWidth, mPixelHeight);

		g.setColor(Color.DARK_GRAY);

		for (MyPolygon poly : mPolys) {
			for (int i = 0; i < poly.mPoints.size() - 1; i++) {
				Point a = poly.mPoints.get(i);
				Point b = poly.mPoints.get(i + 1);
				g.drawLine(a.x, a.y, b.x, b.y);
			}

		}

	}

	public Point convert(float lat, float lon) {
		return null;
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
