package org.turnerha;

import java.awt.Point;
import java.util.List;
import java.util.Random;

import org.turnerha.map.MyPolygon;

public class SmartPhone {
	
	private Point mPoint;
	private Random mRandom = new Random();
	private List<MyPolygon> mCounties;
	private boolean mShouldRemove = false;
	
	public SmartPhone(Point p, List<MyPolygon> counties) {
		mPoint = p;
		mCounties = counties;
	}
	
	public void move() {
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
			if (poly.mPoly.contains(mPoint))
				return;
		
		mShouldRemove = true;
	}
	
	public Point getLocation() {
		return mPoint;
	}
	
	public boolean getShouldRemove() {
		return mShouldRemove;
	}
}
