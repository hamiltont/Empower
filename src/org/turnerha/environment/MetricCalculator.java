package org.turnerha.environment;

import java.awt.Point;
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
		// Determine the absolute worst accuracy value that we could have on
		// this real environment
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
	 * This method should be called immediately before a new reading is input.
	 * It modifies the coverage and accuracy metrics to remove the amounts due
	 * to the pixels that are about to be updated.
	 * 
	 * After this method returns, the reading should be added to the
	 * {@link PerceivedEnvironment}, and then the method
	 * {@link MetricCalculator#postNewReading(Point[])} should be called to add
	 * the effect of the new reading into the coverage and accuracy metrics.
	 * This entire process effectively replaces the effect of the old pixels
	 * with the effect due to the new pixels
	 * 
	 * @param affectedPixels
	 */
	// TODO - Point[] is a pretty poor way of passing in the points. We allocate
	// a ton of new point objects, which are then just converted back into x/y.
	// Why not just allocate the x/y array (or better yet, pass in the top left
	// x/y and the w/h)
	public void preNewReading(Point[] affectedPixels) {
		for (Point p : affectedPixels) {
			int percPixel = mPerceived.getRGB(p.x, p.y);
			int realPixel = mReal.getValueAt(p.x, p.y);

			// Update accuracy
			// Because we are about to input a new reading, we subtract the
			// effect of the current pixels on the accuracy
			double accuracyOfGivenPixels = findChangeInAccuracy(percPixel,
					realPixel);
			mAccuracyCurrentDifference -= accuracyOfGivenPixels;

			// Update coverage by removing the effect of the current pixels
			int alpha = (percPixel >> 24) & 0xff;
			double localCoverage = (double) alpha / 255d;
			mCoverageCurrent -= localCoverage;
		}
	}

	private double findChangeInAccuracy(int percPixel, int realPixel) {
		int pRed = (percPixel >> 16) & 0xff;
		int pGreen = (percPixel >> 8) & 0xff;
		int pBlue = (percPixel) & 0xff;

		int rRed = (realPixel >> 16) & 0xff;
		int rGreen = (realPixel >> 8) & 0xff;
		int rBlue = (realPixel) & 0xff;

		// How much the real and perceived differ
		double sumOfCurrentPixelDifferences = Math.abs(pRed - rRed);
		sumOfCurrentPixelDifferences += Math.abs(pBlue - rBlue);
		sumOfCurrentPixelDifferences += Math.abs(pGreen - rGreen);

		// The maximum worst difference we could have
		// NOTE This could be cached if it would help performance
		double sumOfWorstPixelDifferences = Math.max(Math.abs(rRed - 0), Math
				.abs(rRed - 255));
		sumOfWorstPixelDifferences += Math.max(Math.abs(rGreen - 0), Math
				.abs(rGreen - 255));
		sumOfWorstPixelDifferences += Math.max(Math.abs(rBlue - 0), Math
				.abs(rBlue - 255));

		// The alpha determines how much we 'trust' the current perceived
		// pixel.
		// If we don't trust it at all, then the current accuracy estimate
		// assumes the worst for this pixel. Therefore, we have to subtract
		// the worst. If we trust it completely, then the current accuracy
		// estimate includes the difference of this pixel and the real
		// network with 0 worst-case assumption, so we have to entirely
		// remove this pixel difference. Following this logic, the alpha is
		// used to weight both the worst and current pixel differences so
		// that we remove the correct amount of each from the estimate
		int alpha = (percPixel >> 24) & 0xff;
		double weight = (double) alpha / 255d;
		// weight == closeness to 1 e.g. full trust
		sumOfCurrentPixelDifferences *= weight;
		// (1-weight) == closeness to 0 e.g. no trust
		sumOfWorstPixelDifferences *= (1d - weight);

		final double amountOfChange = sumOfCurrentPixelDifferences
				+ sumOfWorstPixelDifferences;
		return amountOfChange;
	}

	public void postNewReading(Point[] affectedPixels) {
		for (Point p : affectedPixels) {
			int percPixel = mPerceived.getRGB(p.x, p.y);
			int realPixel = mReal.getValueAt(p.x, p.y);

			// Update accuracy by calculating the effect of the given pixels and
			// adding that in to the current difference
			double accuracyOfGivenPixels = findChangeInAccuracy(percPixel,
					realPixel);
			mAccuracyCurrentDifference += accuracyOfGivenPixels;

			// Update coverage by adding the effect of the current pixels
			int alpha = (percPixel >> 24) & 0xff;
			double localCoverage = (double) alpha / 255d;
			mCoverageCurrent += localCoverage;
		}
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
