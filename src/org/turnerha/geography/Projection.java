package org.turnerha.geography;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * Allows conversions between latitude/longitude coordinates to pixels.
 * 
 * @author hamiltont
 * 
 */
public interface Projection {

	public Point getPointAt(GeoLocation location);

	/**
	 * Returns a {@link GeoLocation} that is inside the given {@link Point}, and
	 * optimally close to the center of the {@link Point}.
	 * 
	 * @param screenPoint
	 * @return
	 */
	public GeoLocation getLocationAt(Point screenPoint);
	
	public GeoLocation getLocationAt(int x, int y);

	public Dimension getDimensionsOf(GeoBox geoBox);
	
	/**
	 * Returns a {@link GeoBox} that approximately correlates to the given
	 * {@link Dimension}. The corners of the {@link GeoBox} are based in the
	 * center of the {@link Dimension} pixels
	 * 
	 * @param d
	 * @return
	 */
	public GeoBox getGeoBoxOf(Rectangle rect);
}
