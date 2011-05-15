package org.turnerha.geography;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import org.turnerha.ModelView;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Data structure for interacting with a KML Geography that the user has
 * selected. The most important methods are
 * {@link KmlGeography#paint(Graphics, Projection)} (where this geography will
 * be painted into the provided graphics using the supplied {@link Projection})
 * and {@link KmlGeography#contains(GeoLocation)}, which allows someone to ask
 * the {@link KmlGeography} if that location is contained within. The result of
 * contains is useful for avoiding extra processing.
 * 
 * NOTE: Internally this actually throws away all of the Latitude longitude
 * data. When the file is read in, a projection (internal and private to this
 * {@link KmlGeography} instance) is used to convert everything to a 2D
 * geometry. All incoming points are converted (using the same internal private
 * projection) to the 2D geometry. This allows the use of the JTS Topology
 * Suite, which has significantly more advanced 2D topology operations, such as
 * arbitrary Geometry unions, contains, intersections, etc. Naturally this
 * internal data structure means that the results of this class are only as good
 * as the internal projection used, and more specifically the amount of 2D space
 * we allow to represent the latitude longitude data. I'm not sure how much
 * error this introduces currently
 * 
 * @author hamiltont
 * 
 */
// TODO Add an intersection method. Once this is added, data readings can be
// input with arbitrary geo-polygonal regions, that can then be converted to
// arbitrary Geometry elements, and the intersection of the full e.g. unioned
// KML and the Geometry can be calculated, and then the appropriate region can
// be returned. Instead of passing affectedPixels[] to the Metrics, we can
// actually pass the correct region. For now this is a bit premature - I don't
// quite know what values to return for now
public class KmlGeography {

	private List<Polygon> mPolys;
	private Geometry mUnion;

	private Projection mProjection;

	private GeoLocation topRight;
	private GeoLocation botLeft;

	private Color seeThruBlack = new Color(0, 0, 0, 50);

	/**
	 * Used to create JTS Points from java.awt.Points in
	 * {@link KmlGeography#contains(GeoLocation)}
	 */
	private GeometryFactory mFactory = new GeometryFactory();

	/**
	 * Used to create JTS Points from java.awt.Points in
	 * {@link KmlGeography#contains(GeoLocation)}
	 */
	private Coordinate mTempCoord = new Coordinate();

	public KmlGeography(List<Polygon> polys, GeoLocation tr, GeoLocation bl) {

		topRight = tr;
		botLeft = bl;
		mPolys = polys;

		// double latDifference = topRight.lat - botLeft.lat;
		// double lonDifference = botLeft.lon - topRight.lon;

		// TODO determine if there is any dis/advantage to making this larger. I
		// just chose a value that makes some sense - it's a fairly large
		// coordinate space to project stuff into
		Dimension projectionSpace = new Dimension(Short.MAX_VALUE,
				Short.MAX_VALUE);

		GeoBox projectionBox = new GeoBox(tr, bl);
		mProjection = new ProjectionCartesian(projectionBox, projectionSpace);

		Geometry union = new Polygon(null, null, new GeometryFactory());
		for (Polygon p : polys)
			union = union.union(p);
		mUnion = union;
	}

	public void paint(Graphics g, Projection p) {
		g.setColor(seeThruBlack);

		// TODO holy mother of dear god optimize this
		Projection mainP = ModelView.getInstance().getDefaultProjection();
		for (Polygon poly : mPolys) {
			{
				Coordinate[] coords = poly.getCoordinates();
				for (int i = 0; i < coords.length; i++) {
					Coordinate a = coords[i];
					int indexb = (i + 1 == coords.length) ? 0 : i + 1;
					Coordinate b = coords[indexb];

					Point pa = mainP.getPointAt(new GeoLocation(a.y, a.x));
					Point pb = mainP.getPointAt(new GeoLocation(b.y, b.x));
					g.drawLine(pa.x, pa.y, pb.x, pb.y);
				}
			}

		}

	}

	public boolean contains(GeoLocation location) {
		// First project it into our 2dim space
		Point p = mProjection.getPointAt(location);

		// Then convert to the JTS Topology Lib version of Point data
		mTempCoord.x = p.x;
		mTempCoord.y = p.y;

		// Then create the JTS Topology version of Point
		com.vividsolutions.jts.geom.Point twoDimPoint = mFactory
				.createPoint(mTempCoord);

		// Then ask JTS if this point is contained
		return mUnion.contains(twoDimPoint);
	}

	public GeoBox getGeoBox() {
		return new GeoBox(topRight, botLeft);
	}

	public Rectangle getPixelSize() {
		Rectangle totalUnion = new Rectangle(5, 5);
		// TODO implement this if everything works
		/*
		 * for (MyPolygon poly : mPolys) if (totalUnion == null) totalUnion =
		 * new Rectangle(poly.mPoly.getBounds()); else totalUnion =
		 * totalUnion.union(poly.mPoly.getBounds());
		 */
		return totalUnion;
	}
}
