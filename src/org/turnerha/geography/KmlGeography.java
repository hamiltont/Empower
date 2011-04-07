package org.turnerha.geography;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.turnerha.ModelView;

/**
 * Allows a kml-based geography file to be painted to the screen. There should
 * only be one of these per run of the simulation, so I am making it a singleton
 * 
 * @author hamiltont
 * 
 */

// TODO Add an applyProjection(Projection p) method
public class KmlGeography {

	private List<MyPolygon> mPolys;

	private GeoLocation topRight = new GeoLocation(39.520992, -74.421386);
	private GeoLocation botLeft = new GeoLocation(36.509636, -83.913574);

	private double latDifference = topRight.lat - botLeft.lat;
	private double lonDifference = botLeft.lon - topRight.lon;

	private int mPixelWidth = 1400;
	private int mPixelHeight = 850;

	public KmlGeography() {
	}

	/**
	 * 
	 * @param polys
	 * @param screen
	 * @param tr
	 * @param bl
	 */
	public void init(List<MyPolygon> polys, GeoLocation tr, GeoLocation bl) {

		topRight = tr;
		botLeft = bl;
		latDifference = topRight.lat - botLeft.lat;
		lonDifference = botLeft.lon - topRight.lon;

		Dimension screen = ModelView.getInstance().getRenderingArea();
		if (screen.width == 0 || screen.height == 0)
			throw new IllegalStateException(
					"ModelView.getInstance is not implemented, "
							+ "or getRenderingArea is being called before it should");

		mPixelWidth = screen.width;
		mPixelHeight = screen.height;

		mPolys = polys;

		for (MyPolygon poly : polys) {
			poly.mPoints = new ArrayList<Point>(poly.mLocations.size());
			int[] xArray = new int[poly.mLocations.size()];
			int[] yArray = new int[poly.mLocations.size()];

			int pos = 0;
			for (GeoLocation dp : poly.mLocations) {

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

	private Color seeThruBlack = new Color(0, 0, 0, 50);

	public void paint(Graphics g, Projection p) {
		g.setColor(seeThruBlack);

		// TODO holy mother of dear god optimize this
		Projection mainP = ModelView.getInstance().getDefaultProjection();
		for (MyPolygon poly : mPolys) {
			for (int i = 0; i < poly.mPoints.size() - 1; i++) {
				Point a = poly.mPoints.get(i);
				Point b = poly.mPoints.get(i + 1);
				GeoLocation al = mainP.getLocationAt(a);
				GeoLocation b1 = mainP.getLocationAt(b);
				Point r1 = p.getPointAt(al);
				Point r2 = p.getPointAt(b1);
				g.drawLine(r1.x, r1.y, r2.x, r2.y);
			}

		}

	}

	/**
	 * Returns true if the pixel is contained within the polygons present on
	 * this map, false otherwise
	 */
	private Polygon lastPoly = null;

	public boolean contains(GeoLocation point) {
		Point p = ModelView.getInstance().getDefaultProjection().getPointAt(
				point);
		return contains(p.x, p.y);
	}

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
