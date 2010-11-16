package org.turnerha;

import java.awt.Point;
import java.util.Random;

public class SmartPhone {
	
	private Point mPoint;
	private Random mRandom = new Random();
	
	public SmartPhone(Point p) {
		mPoint = p;
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
		
	}
	
	public Point getLocation() {
		return mPoint;
	}
}
