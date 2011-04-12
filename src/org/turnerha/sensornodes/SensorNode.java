package org.turnerha.sensornodes;

import java.awt.Point;

import org.turnerha.geography.GeoBox;
import org.turnerha.geography.GeoLocation;
import org.turnerha.geography.Projection;
import org.turnerha.policys.collection.DataCollectionPolicy;
import org.turnerha.policys.movement.NodeMovementPolicy;

/**
 * Interface for any sort of generic node. 
 * 
 * @author hamiltont
 *
 */
public interface SensorNode {

	/**
	 * @return The location that this sensor node is currently at
	 */
	public GeoLocation getLocation();

	/**
	 * @return A {@link GeoBox} that defines the possible locations this
	 *         {@link SensorNode} could be reporting data from
	 */
	public GeoBox getLocationBoundingBox();

	/**
	 * @param p
	 *            a {@link Projection} that can convert {@link GeoLocation}s to
	 *            pixels
	 * @return the location of this {@link SensorNode} in pixels
	 */
	public Point getPointUsing(Projection p);

	/**
	 * Called by a controller to tell this sensor node to update itself. The
	 * update typically includes moving (if this node wants to or can move),
	 * collecting data (if the {@link DataCollectionPolicy} says so), and
	 * reporting a reading (if the data reporting policy says so)
	 */
	public void update();

	/**
	 * For now, this should always return true (we are not experimenting with
	 * the reporting policies just yet)
	 * 
	 * For now this is just a boolean option. Later on network connectivity can
	 * be expanded to include properties of the network as well, so that the
	 * policies can make decisions based on more fine-grained network states
	 * 
	 * @return
	 */
	public boolean hasNetworkConnectivity();

	/**
	 * @return A handle to the {@link NodeMovementPolicy} that governs this
	 *         node's movement
	 */
	public NodeMovementPolicy getMovementPolicy();
}
