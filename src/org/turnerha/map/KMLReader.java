package org.turnerha.map;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.xml.sax.SAXException;

import com.keithpower.gekmlib.Configuration;
import com.keithpower.gekmlib.Document;
import com.keithpower.gekmlib.Feature;
import com.keithpower.gekmlib.KMLParser;
import com.keithpower.gekmlib.Kml;
import com.keithpower.gekmlib.Placemark;

public class KMLReader {

	private List<MyPolygon> mPoly;

	public List<MyPolygon> getPoly() {
		return mPoly;
	}

	public KMLReader() {
		KMLParser parser = new KMLParser();
		Configuration.properties.setProperty(Configuration.GENERATE_IDS,
				Configuration.OFF);

		Kml kml;
		try {
			kml = parser.parse(new File("va.kml"));
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
	}

}
