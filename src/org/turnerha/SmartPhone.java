package org.turnerha;

import java.awt.Point;
import java.util.List;
import java.util.Random;

import org.turnerha.environment.impl.ImageBackedPerceivedEnvironment;
import org.turnerha.environment.impl.ImageBackedRealEnvironment;
import org.turnerha.geography.GeoLocation;
import org.turnerha.geography.MyPolygon;
import org.turnerha.geography.Projection;
import org.turnerha.policys.collection.DataCollectionPolicy;

public class SmartPhone {

	private GeoLocation mPoint;
	private Random mRandom = new Random();
	private List<MyPolygon> mCounties;
	private boolean mShouldRemove = false;
	private ImageBackedPerceivedEnvironment mPerceivedNetwork;
	private ImageBackedRealEnvironment mRealNetwork;
	private double mProbabilityOfMoving;
	private float mInputFrequency;
	private Projection mProjection;
	
	private DataCollectionPolicy mDataCollectionPolicy;

	public SmartPhone(Point p, List<MyPolygon> counties,
			ImageBackedPerceivedEnvironment pn, ImageBackedRealEnvironment rn,
			double probabilityOfMovig, float inputFrequency, Projection proj,
			DataCollectionPolicy dataPolicy) {
		mPoint = proj.getLocationAt(p);
		mCounties = counties;
		mPerceivedNetwork = pn;
		mRealNetwork = rn;
		mProjection = proj;
		mDataCollectionPolicy = dataPolicy;

		mProbabilityOfMoving = probabilityOfMovig;
		mInputFrequency = inputFrequency;
	}

	/**
	 * Gives the smartphone a chance to perform some actions e.g. moving, data
	 * collection, data reporting, etc
	 */
	public void update() {
		if (mRandom.nextDouble() > mProbabilityOfMoving)
			return;

		// Always report for now
		// if (mRandom.nextFloat() > mInputFrequency)
		// return;

		double xChange = mRandom.nextDouble() / 100d;
		double yChange = mRandom.nextDouble() / 100d;
		boolean xRight = mRandom.nextBoolean();
		boolean yUp = mRandom.nextBoolean();

		if (xRight)
			mPoint.lon += xChange;
		else
			mPoint.lon -= xChange;

		if (yUp)
			mPoint.lat += yChange;
		else
			mPoint.lat -= yChange;

		// No sense collecting data if we have moved ourself out of the network
		for (MyPolygon poly : mCounties)
			if (poly.mPoly.contains(mProjection.getPointAt(mPoint))) {
				
				if (mDataCollectionPolicy.shouldCollectData(this) == false)
					return;
				
				int rgb = mRealNetwork.getValueAt(mPoint);
				mPerceivedNetwork.addReading(rgb, mPoint);

				return;
			}

		mShouldRemove = true;
	}

	public Point getLocation() {
		return mProjection.getPointAt(mPoint);
	}

	public boolean getShouldRemove() {
		return mShouldRemove;
	}

	public double getProbabilityOfMoving() {
		return mProbabilityOfMoving;
	}
}
