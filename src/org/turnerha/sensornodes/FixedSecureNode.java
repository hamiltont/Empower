package org.turnerha.sensornodes;

import java.awt.Point;

import org.turnerha.environment.impl.ImageBackedPerceivedEnvironment;
import org.turnerha.environment.impl.ImageBackedRealEnvironment;
import org.turnerha.geography.GeoLocation;
import org.turnerha.geography.Projection;
import org.turnerha.policys.collection.DataCollectionPolicy;
import org.turnerha.policys.movement.NodeMovementPolicy;
import org.turnerha.policys.movement.StaticNodeMovementPolicy;

/**
 * Represents a geographically static node that has some guarantee of always
 * returning accurate sensor measurements (within it's tolerances), e.g. the
 * physical sensors are perhaps secured inside a lockbox. This type of node is
 * used to evaluate the trustworthiness of other nodes in the network
 * 
 * @author hamiltont
 * 
 */
public class FixedSecureNode implements SensorNode {

	private GeoLocation mLocation;
	private ImageBackedPerceivedEnvironment mPerceivedEnv;
	private ImageBackedRealEnvironment mRealEnv;
	private DataCollectionPolicy mCollectionPolicy;
	private NodeMovementPolicy mMovementPolicy;

	public FixedSecureNode(GeoLocation location,
			ImageBackedPerceivedEnvironment perceivedEnv,
			ImageBackedRealEnvironment realEnv,
			DataCollectionPolicy collectionPolicy) {

		mLocation = location;
		mPerceivedEnv = perceivedEnv;
		mRealEnv = realEnv;
		mCollectionPolicy = collectionPolicy;
		mMovementPolicy = StaticNodeMovementPolicy.getInstance();
	}

	@Override
	public GeoLocation getLocation() {
		return mLocation;
	}

	@Override
	public void update() {
		if (false == mCollectionPolicy.shouldCollectData(this))
			return;
		
		int rgb = mRealEnv.getValueAt(mLocation);
		mPerceivedEnv.addReading(rgb, mLocation);
	}

	@Override
	public NodeMovementPolicy getMovementPolicy() {
		return mMovementPolicy;
	}

	@Override
	public boolean hasNetworkConnectivity() {
		return true;
	}

	@Override
	public Point getPointUsing(Projection p) {
		return p.getPointAt(mLocation);
	}

}
