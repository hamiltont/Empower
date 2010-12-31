package org.turnerha.geography;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import com.keithpower.gekmlib.Configuration;
import com.keithpower.gekmlib.Document;
import com.keithpower.gekmlib.Feature;
import com.keithpower.gekmlib.KMLParser;
import com.keithpower.gekmlib.Kml;
import com.keithpower.gekmlib.Placemark;

public class KmlReader {

	private List<MyPolygon> mPoly;
	public DoublePoint mTopRight;
	public DoublePoint mBottomLeft;

	public List<MyPolygon> getPoly() {
		return mPoly;
	}

	public KmlReader(File inputFile) {
		KMLParser parser = new KMLParser();
		Configuration.properties.setProperty(Configuration.GENERATE_IDS,
				Configuration.OFF);

		Kml kml;

		DoublePoint topRight = null;
		DoublePoint bottomLeft = null;

		try {
			kml = parser.parse(inputFile);
			Document d = kml.getDocument();
			Feature[] features = d.getFeatures();

			mPoly = new ArrayList<MyPolygon>(features.length);

			for (Feature f : features) {
				Placemark p = (Placemark) f;

				double[] coords = p.getPolygon().getOuterBoundaryIs()
						.getLinearRing().getNumericalCoordinates();

				MyPolygon poly = new MyPolygon();
				ArrayList<DoublePoint> polyPoints = new ArrayList<DoublePoint>(
						Math.round((float) coords.length * 0.666666f));

				for (int i = 0; i < coords.length; i = i + 3) {

					DoublePoint dp = new DoublePoint();
					dp.lon = coords[i];
					dp.lat = coords[i + 1];

					if (topRight == null) {
						topRight = new DoublePoint(dp.lat, dp.lon);
						bottomLeft = new DoublePoint(dp.lat, dp.lon);
					}

					if (dp.lat > topRight.lat)
						topRight.lat = dp.lat;
					if (dp.lat < bottomLeft.lat)
						bottomLeft.lat = dp.lat;

					if (dp.lon > topRight.lon)
						topRight.lon = dp.lon;
					if (dp.lon < bottomLeft.lon)
						bottomLeft.lon = dp.lon;

					polyPoints.add(dp);
				}

				poly.mLocations = polyPoints;
				mPoly.add(poly);
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		
		double margin = .5d;
		
		// Add Horiz Margins
		bottomLeft.lon -= margin;
		topRight.lon += margin;
		
		// Add Vert margins
		bottomLeft.lat -= margin;
		topRight.lat += margin;
		
		mTopRight = topRight;
		mBottomLeft = bottomLeft;
	}
}