package org.turnerha.sensornodes;

import java.awt.Point;
import java.util.List;
import java.util.Random;

import org.turnerha.environment.impl.ImageBackedPerceivedEnvironment;
import org.turnerha.environment.impl.ImageBackedRealEnvironment;
import org.turnerha.geography.GeoBox;
import org.turnerha.geography.GeoLocation;
import org.turnerha.geography.MyPolygon;
import org.turnerha.geography.Projection;
import org.turnerha.policys.collection.DataCollectionPolicy;
import org.turnerha.policys.movement.NodeMovementPolicy;
import org.turnerha.server.Server;

public class SmartPhone implements SensorNode {

	private GeoLocation mLocation;	
	private Random mRandom;
	private List<MyPolygon> mCounties;
	private ImageBackedRealEnvironment mRealNetwork;
	private double mProbabilityOfMoving;
	private Projection mProjection;
	private Server mServer;

	private DataCollectionPolicy mDataCollectionPolicy;
	private NodeMovementPolicy mMovementPolicy;

	public SmartPhone(Point p, List<MyPolygon> counties,
			Server s, ImageBackedRealEnvironment rn,
			Projection proj,
			DataCollectionPolicy dataPolicy, NodeMovementPolicy movementPolicy,
			Random generator) {
		mLocation = proj.getLocationAt(p);
		mCounties = counties;
		mRealNetwork = rn;
		mProjection = proj;
		mDataCollectionPolicy = dataPolicy;
		mRandom = generator;
		mMovementPolicy = movementPolicy;
		mServer = s;
	}

	/**
	 * Gives the smartphone a chance to perform some actions e.g. moving, data
	 * collection, data reporting, etc
	 */
	public void update() {
		if (mMovementPolicy.shouldMove(this)) {
			// Move
			double xChange = mRandom.nextDouble() / 100d;
			double yChange = mRandom.nextDouble() / 100d;
			boolean xRight = mRandom.nextBoolean();
			boolean yUp = mRandom.nextBoolean();

			if (xRight)
				mLocation.lon += xChange;
			else
				mLocation.lon -= xChange;

			if (yUp)
				mLocation.lat += yChange;
			else
				mLocation.lat -= yChange;

			// Check that we are still in the right geospatial area
			boolean inGeospatialArea = false;
			for (MyPolygon poly : mCounties)
				if (poly.mPoly.contains(mProjection.getPointAt(mLocation))) {
					inGeospatialArea = true;
					break;
				}

			// Correct if we are not
			// For now we just undo the move. Eventually the movement policy
			// should tell us what to do
			if (inGeospatialArea == false) {
				if (xRight)
					mLocation.lon -= xChange;
				else
					mLocation.lon += xChange;

				if (yUp)
					mLocation.lat -= yChange;
				else
					mLocation.lat += yChange;
			}
		}

		// Move complete. Should we collect data?
		// TODO eventually we should integrate the reporting policy
		if (mDataCollectionPolicy.shouldCollectData(this)) {
			int rgb = mRealNetwork.getValueAt(mLocation);
			mServer.addReading(rgb, mLocation);
		}

	}

	public GeoLocation getLocation() {
		return mLocation;
	}

	public Point getPointLocation() {
		return mProjection.getPointAt(mLocation);
	}

	public double getProbabilityOfMoving() {
		return mProbabilityOfMoving;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder("[");
		// TODO complete this
		return super.toString();
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

	@Override
	public GeoBox getLocationBoundingBox() {
		return new GeoBox(mLocation, mLocation);
	}
}
