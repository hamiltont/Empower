package org.turnerha;

import java.awt.Point;
import java.util.List;
import java.util.Random;

import org.turnerha.environment.impl.ImageBackedPerceivedEnvironment;
import org.turnerha.environment.impl.ImageBackedRealEnvironment;
import org.turnerha.geography.GeoLocation;
import org.turnerha.geography.MyPolygon;
import org.turnerha.geography.Projection;

public class SmartPhone {

	private GeoLocation mPoint;
	private Random mRandom = new Random();
	private List<MyPolygon> mCounties;
	private boolean mShouldRemove = false;
	private ImageBackedPerceivedEnvironment mPerceivedNetwork;
	private ImageBackedRealEnvironment mRealNetwork;
	private float mMoveTendenancy;
	private float mInputFrequency;
	private Projection mProjection;

	public SmartPhone(Point p, List<MyPolygon> counties,
			ImageBackedPerceivedEnvironment pn, ImageBackedRealEnvironment rn,
			float moveTendenancy, float inputFrequency, Projection proj) {
		mPoint = proj.getLocationAt(p);
		mCounties = counties;
		mPerceivedNetwork = pn;
		mRealNetwork = rn;
		mProjection = proj;

		mMoveTendenancy = moveTendenancy;
		mInputFrequency = inputFrequency;
	}

	/**
	 * Gives the smartphone a chance to perform some actions e.g. moving, data
	 * collection, data reporting, etc
	 */
	public void update() {
		if (mRandom.nextFloat() > mMoveTendenancy)
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

		for (MyPolygon poly : mCounties)
			if (poly.mPoly.contains(mProjection.getPointAt(mPoint))) {

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
}
