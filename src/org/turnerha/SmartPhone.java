package org.turnerha;

import java.awt.Point;
import java.util.List;
import java.util.Random;

import org.turnerha.environment.PerceivedEnviron;
import org.turnerha.environment.RealEnviron;
import org.turnerha.geography.MyPolygon;

public class SmartPhone {

	private Point mPoint;
	private Random mRandom = new Random();
	private List<MyPolygon> mCounties;
	private boolean mShouldRemove = false;
	private PerceivedEnviron mPerceivedNetwork;
	private RealEnviron mRealNetwork;
	private float mMoveTendenancy;
	private float mInputFrequency;

	public SmartPhone(Point p, List<MyPolygon> counties, PerceivedEnviron pn, RealEnviron rn, float moveTendenancy, float inputFrequency) {
		mPoint = p;
		mCounties = counties;
		mPerceivedNetwork = pn;
		mRealNetwork = rn;
		
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
				
				int rgb = mRealNetwork.getValue(mPoint.x, mPoint.y);
				mPerceivedNetwork.addReading(rgb, mPoint);
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
