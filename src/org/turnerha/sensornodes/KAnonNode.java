package org.turnerha.sensornodes;

import java.awt.Point;

import org.turnerha.geography.GeoBox;
import org.turnerha.geography.GeoLocation;
import org.turnerha.geography.Projection;
import org.turnerha.policys.movement.NodeMovementPolicy;

import ch.hsr.geohash.GeoHash;

/**
 * Represents a node that is 'k-anonymous'. In our universe, this implies that a
 * given node is always anonymous to some kth degree, where k indicates the
 * number of people that could be that person. So a 10-anonymous node would mean
 * that whenever a data reading is input, we know that it came from one of ten
 * users, but not which user specifically.
 * 
 * However, this implementation is more focused on locational anonymity. The
 * client (e.g. the node) reports it's location with some of the precision bits
 * removed. The end result is that the server knows the general region the node
 * is in, but does not have the exact location of the node. Therefore, the data
 * readings have to be approximated across the entire region.
 * 
 * In this implementation, we use a geohash with a fixed number of bits to
 * determine the appropriate geohash bounding box. Then, we convert the geohash
 * box into a {@link GeoBox} and return that for the location
 * 
 * @author hamiltont
 * @see https://github.com/kungfoo/geohash-java
 */
public class KAnonNode implements SensorNode {

	private GeoLocation mLocation;

	public KAnonNode(GeoLocation startingLocation) {
		mLocation = startingLocation;
	}

	@Override
	public GeoLocation getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeMovementPolicy getMovementPolicy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point getPointUsing(Projection p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasNetworkConnectivity() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

	@Override
	public GeoBox getLocationBoundingBox() {
		GeoHash hash = GeoHash.withBitPrecision(mLocation.lat, mLocation.lon,
				10);
		hash.toString();
		return null;
	}

}
