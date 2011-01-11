package org.turnerha.environment;

import java.awt.Rectangle;

import org.turnerha.environment.impl.ImageBackedPerceivedEnvironment;
import org.turnerha.environment.impl.ImageBackedRealEnvironment;
import org.turnerha.geography.KmlGeography;

public class MetricCalculator {

	private ImageBackedRealEnvironment mReal;
	private ImageBackedPerceivedEnvironment mPerceived;

	private double mCoverageTotal = 0;
	private double mCoverageCurrent = 0;

	private double mAccuracyMaxDifference = 0;
	private double mAccuracyCurrentDifference = 0;

	public MetricCalculator() {
	}

	/** Used to inject a new real environment */
	public void updateRealEnvironment(RealEnvironment re) {
		if (re instanceof ImageBackedRealEnvironment)
			mReal = (ImageBackedRealEnvironment) re;
		else
			throw new IllegalArgumentException("Must be image backed for now");
	}

	/** Used to inject a new perceived environment */
	public void updatePerceivedEnvironment(PerceivedEnvironment pe) {
		if (pe instanceof ImageBackedPerceivedEnvironment)
			mPerceived = (ImageBackedPerceivedEnvironment) pe;
		else
			throw new IllegalArgumentException("Must be image backed for now");
	}

	public void setupAccuracy(KmlGeography kml, RealEnvironment real) {
		// Determine the absolute worst accuracy value that we could have on this real environment
		Rectangle geoSize = kml.getPixelSize();
		ImageBackedRealEnvironment ibre = (ImageBackedRealEnvironment) real;
		for (int x = geoSize.x; x < (geoSize.width + geoSize.x); x++)
			for (int y = geoSize.y; y < (geoSize.height + geoSize.y); y++)
				if (kml.contains(x, y)) {
					int pixel = ibre.getValueAt(x, y);
					int red = (pixel >> 16) & 0xff;
					int green = (pixel >> 8) & 0xff;
					int blue = (pixel) & 0xff;

					int diff = Math.max(Math.abs(red - 0), Math.abs(red - 255));
					diff += Math
							.max(Math.abs(green - 0), Math.abs(green - 255));
					diff += Math.max(Math.abs(blue - 0), Math.abs(blue - 255));

					mAccuracyMaxDifference += diff;

				}
		
		mAccuracyCurrentDifference = mAccuracyMaxDifference;
	}

	public void setupCoverage(KmlGeography geo) {
		// Determine the total possible coverage
		Rectangle geoSize = geo.getPixelSize();
		for (int x = geoSize.x; x < (geoSize.width + geoSize.x); x++)
			for (int y = geoSize.y; y < (geoSize.height + geoSize.y); y++)
				if (geo.contains(x, y))
					mCoverageTotal++;
	}

	/**
	 * Allows a network to raise/lower the current accuracy by a given amount.
	 * Lower current accuracy is better. This method is used for rapid,
	 * incremental updates of the coverage
	 * 
	 * @param amount
	 */
	public void changeCurrentAccuracy(double amount) {
		mAccuracyCurrentDifference += amount;
	}

	public double getAccuracy() {
		return (mAccuracyMaxDifference - mAccuracyCurrentDifference)
				/ mAccuracyMaxDifference;
	}

	/**
	 * Allows a network to raise/lower the current coverage by a given amount.
	 * Higher current coverage is better. This method is used for rapid,
	 * incremental updates of the coverage
	 * 
	 * @param amount
	 */
	public void changeCurrentCoverage(double amount) {
		mCoverageCurrent += amount;
	}

	/** Returns the current coverage */
	public double getCoverage() {
		if (mCoverageTotal == 0)
			return -1;

		return mCoverageCurrent / mCoverageTotal;
	}

}
