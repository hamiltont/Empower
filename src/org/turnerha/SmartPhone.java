package org.turnerha;

import java.awt.Point;
import java.util.List;
import java.util.Random;

import org.turnerha.environment.impl.ImageBackedPerceivedEnvironment;
import org.turnerha.environment.impl.ImageBackedRealEnvironment;
import org.turnerha.geography.MyPolygon;
import org.turnerha.geography.Projection;

public class SmartPhone {

	private Point mPoint;
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
		mPoint = p;
		mCounties = counties;
		mPerceivedNetwork = pn;
		mRealNetwork = rn;
		mProjection = proj;

		mMoveTendenancy = moveTendenancy;
		mInputFrequency = inputFrequency;
	}

	public void move() {
		if (mRandom.nextFloat() > mMoveTendenancy)
			return;

		int xChange = mRandom.nextInt(3);
		int yChange = mRandom.nextInt(3);
		boolean xRight = mRandom.nextBoolean();
		boolean yUp = mRandom.nextBoolean();

		if (xRight)
			mPoint.x += xChange;
		else
			mPoint.x -= xChange;

		if (yUp)
			mPoint.y += yChange;
		else
			mPoint.y -= yChange;

		for (MyPolygon poly : mCounties)
			if (poly.mPoly.contains(mPoint)) {
				if (mRandom.nextFloat() > mInputFrequency)
					return;

				int rgb = mRealNetwork.getValueAt(mProjection
						.getLocationAt(mPoint));
				mPerceivedNetwork.addReading(rgb, mProjection
						.getLocationAt(mPoint));

				return;
			}

		mShouldRemove = true;
	}

	public Point getLocation() {
		return mPoint;
	}

	public boolean getShouldRemove() {
		return mShouldRemove;
	}
}
