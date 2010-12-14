package org.turnerha.map;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.xml.sax.SAXException;

import com.keithpower.gekmlib.Configuration;
import com.keithpower.gekmlib.Document;
import com.keithpower.gekmlib.Feature;
import com.keithpower.gekmlib.KMLParser;
import com.keithpower.gekmlib.Kml;
import com.keithpower.gekmlib.Placemark;

public class Main {

	private List<MyPolygon> mPoly;

	public static void main(String[] args) {

		new Main();
	}

	public Main() {
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

			final List<MyPolygon> polys = mPoly;
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					JFrame f = new JFrame();
					Map m = new Map(polys);
					f.add(m);
					f.pack();
					f.setVisible(true);
				}
			});

		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}

}
