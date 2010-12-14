package org.turnerha.map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

public class Map extends JPanel {

	private List<MyPolygon> mPolys;

	private static final DoublePoint topRight = new DoublePoint(39.520992,
			-74.421386);
	private static final DoublePoint botLeft = new DoublePoint(36.509636,
			-83.913574);

	private static final double latDifference = topRight.lat - botLeft.lat;
	private static final double lonDifference = botLeft.lon - topRight.lon;

	private static final int mPixelWidth = 1400;
	private static final int mPixelHeight = 850;

	public Map(List<MyPolygon> polys) {
		mPolys = polys;

		setPreferredSize(new Dimension(mPixelWidth, mPixelHeight));

		for (MyPolygon poly : polys) {
			poly.mPoints = new ArrayList<Point>(poly.mLocations.size());

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

				poly.mPoints.add(new Point(x, y));
			}
		}
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0, 0, mPixelWidth, mPixelHeight);

		g.setColor(Color.white);

		for (MyPolygon poly : mPolys)
		{
			for (int i = 0; i < poly.mPoints.size() - 1; i++) {
				Point a = poly.mPoints.get(i);
				Point b = poly.mPoints.get(i+1);
				g.drawLine(a.x, a.y, b.x, b.y);
			}
			
		}

	}
}
