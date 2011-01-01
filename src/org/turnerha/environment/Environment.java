package org.turnerha.environment;

import java.awt.Graphics;

import org.turnerha.geography.GeoBox;
import org.turnerha.geography.GeoLocation;
import org.turnerha.geography.Projection;

/**
 * The simplest interface that every {@link Environment} should have. Currently
 * only supports lat/lon based environment coordinates. 
 * 
 * @author hamiltont
 * 
 */
public interface Environment {

	/**
	 * Requests that the {@link Environment} paint itself into the provided
	 * {@link Graphics} object
	 * 
	 * @param proj
	 *            The Projection that should be used to draw this environment.
	 */
	public void paintInto(Graphics g, Projection proj);

	/**
	 * Given a GPS fix (ignoring altitude for now) this returns the value of the
	 * network at that location. Eventually we may type the return value so that
	 * it can be something other than an integer
	 */
	public int getValueAt(GeoLocation location);

	/** Returns the minimum bounding box for the real network */
	public GeoBox getSize();
}
