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
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

// TODO At some point, I could convert this to a GMLReader and use a library like JTS to do all of the reading / entering
public class KmlReader {

	private List<Polygon> mPolyList;
	public GeoLocation mTopRight;
	public GeoLocation mBottomLeft;

	public List<Polygon> getPoly() {
		return mPolyList;
	}

	public KmlReader() {
	}

	/**
	 * 
	 * @param inputFile
	 * @return true if the file was read and parsed properly, false otherwise
	 */
	public boolean read(File inputFile) {
		KMLParser parser = new KMLParser();
		Configuration.properties.setProperty(Configuration.GENERATE_IDS,
				Configuration.OFF);

		Kml kml;

		GeoLocation topRight = null;
		GeoLocation bottomLeft = null;

		try {
			kml = parser.parse(inputFile);
			Document d = kml.getDocument();
			Feature[] features = d.getFeatures();

			mPolyList = new ArrayList<Polygon>(features.length);

			// Iterate over all features
			ArrayList<Coordinate> tempCoordinateStorage = new ArrayList<Coordinate>();
			for (Feature f : features) {
				Placemark p = (Placemark) f;

				// Parse the outer boundary first
				double[] coords = p.getPolygon().getOuterBoundaryIs()
						.getLinearRing().getNumericalCoordinates();

				tempCoordinateStorage.clear();
				for (int i = 0; i < coords.length; i = i + 3) {

					double lon = coords[i];
					double lat = coords[i + 1];

					if (topRight == null) {
						topRight = new GeoLocation(lat, lon);
						bottomLeft = new GeoLocation(lat, lon);
					}

					if (lat > topRight.lat)
						topRight.lat = lat;
					if (lat < bottomLeft.lat)
						bottomLeft.lat = lat;

					if (lon > topRight.lon)
						topRight.lon = lon;
					if (lon < bottomLeft.lon)
						bottomLeft.lon = lon;

					Coordinate c = new Coordinate(lon, lat);
					tempCoordinateStorage.add(c);
				}

				// TODO fix this...
				Coordinate[] obCoords = new Coordinate[tempCoordinateStorage
						.size()];
				int j = 0;
				for (Coordinate c : tempCoordinateStorage)
					obCoords[j++] = c;
				CoordinateSequence obSequence = new CoordinateArraySequence(
						obCoords);
				LinearRing obRing = new LinearRing(obSequence,
						new GeometryFactory());

				// Now parse the inner boundary
				tempCoordinateStorage.clear();
				LinearRing ibRing = null;
				if (p.getPolygon().getInnerBoundaryIs() != null) {
					coords = p.getPolygon().getInnerBoundaryIs()
							.getLinearRing().getNumericalCoordinates();
					for (int i = 0; i < coords.length; i = i + 3) {
						double lon = coords[i];
						double lat = coords[i + 1];
						tempCoordinateStorage.add(new Coordinate(lon, lat));
					}
					// TODO fix this...
					Coordinate[] ibCoords = new Coordinate[tempCoordinateStorage
							.size()];
					int k = 0;
					for (Coordinate c : tempCoordinateStorage)
						ibCoords[k++] = c;
					CoordinateSequence ibSequence = new CoordinateArraySequence(
							ibCoords);
					ibRing = new LinearRing(ibSequence, new GeometryFactory());
				}

				// Finally build the polygon
				Polygon poly = new Polygon(obRing, ibRing == null ? null
						: new LinearRing[] { ibRing }, new GeometryFactory());

				mPolyList.add(poly);
			}

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (SAXException e) {
			e.printStackTrace();
			return false;
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

		return true;
	}
}
