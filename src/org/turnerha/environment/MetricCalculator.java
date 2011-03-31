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

	// Measures the total number of readings
	private int mTotalReadings = 0;

	// These measure the total accuracy
	private double mAccuracyMaxDifference = 0;
	private double mAccuracyCurrentDifference = 0;

	// These measure accuracy within coverage
	// private double mAccuracyInCoverageMaxDifference = 0;
	// private double mAccuracyInCoverageCurrentDifference = 0;

	// The following two are used to calculate the number of "useless" reading
	// inputs. If a pre-reading change plus a post-reading change sum to zero,
	// then a particular reading was useless and the mWasted counts are updated
	private double mWasted_lastChangeInAccuracy = 0;
	private double mWasted_lastChangeInCoverage = 0;

	private int mWastedDueToAccuracy = 0;
	private int mWastedDueToCoverage = 0;
	private int mWastedDueToBoth = 0;

	// Keeps track of the total change that has occurred in both coverage and
	// accuracy
	private double mUsefulnessTotal = 0;
	private int mUsefulnessCount = 0; // Counts the total number of readings

	/** Used when loading in a new real environment */
	private boolean mThisIsFirstEnvironment = true;

	public MetricCalculator() {

		KmlGeography geo = KmlGeography.getInstance();
		
		// Determine the total possible coverage
		Rectangle geoSize = geo.getPixelSize();
		for (int x = geoSize.x; x < (geoSize.width + geoSize.x); x++)
			for (int y = geoSize.y; y < (geoSize.height + geoSize.y); y++)
				if (geo.contains(x, y))
					mCoverageTotal++;

	}

	/** Used to inject a new real environment */
	public void updateRealEnvironment(RealEnvironment re) {
		if (re instanceof ImageBackedRealEnvironment) {
			mReal = (ImageBackedRealEnvironment) re;
			setupAccuracy(mReal.getKml(), mReal);
		} else
			throw new IllegalArgumentException("Must be image backed for now");
	}

	/** Used to inject a new perceived environment */
	public void updatePerceivedEnvironment(PerceivedEnvironment pe) {
		if (pe instanceof ImageBackedPerceivedEnvironment)
			mPerceived = (ImageBackedPerceivedEnvironment) pe;
		else
			throw new IllegalArgumentException("Must be image backed for now");
	}

	private void setupAccuracy(KmlGeography kml, RealEnvironment real) {
		mAccuracyMaxDifference = 0;

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

		// If this is the first environment, we know that we know absolutely
		// nothing about this environment so we can assume the worst. If this is
		// not the first environment, we have to walk each pixel and determine
		// how accurate our current perceived environment is to this new real
		// environment
		if (mThisIsFirstEnvironment) {
			mAccuracyCurrentDifference = mAccuracyMaxDifference;
			mThisIsFirstEnvironment = false;
		} else {
			mAccuracyCurrentDifference = 0;
			// mAccuracyInCoverageMaxDifference = 0;
			// mAccuracyInCoverageCurrentDifference = 0;

			Rectangle s = kml.getPixelSize();
			for (int x = s.x; x < s.x + s.width; x++)
				for (int y = s.y; y < s.y + s.height; y++) {
					if (kml.contains(x, y) == false)
						continue;

					int perc = mPerceived.getRGB(x, y);
					int realp = ((ImageBackedRealEnvironment) real).getValueAt(
							x, y);

					mAccuracyCurrentDifference += findChangeInTotalAccuracy(
							perc, realp);
				}
		}

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
			double accuracyOfGivenPixels = findChangeInTotalAccuracy(percPixel,
					realPixel);
			mAccuracyCurrentDifference -= accuracyOfGivenPixels;
			mWasted_lastChangeInAccuracy -= accuracyOfGivenPixels;

			// Update coverage by removing the effect of the current pixels
			int alpha = (percPixel >> 24) & 0xff;
			double localCoverage = (double) alpha / 255d;
			mCoverageCurrent -= localCoverage;
			mWasted_lastChangeInCoverage -= localCoverage;
		}
	}

	private double findChangeInTotalAccuracy(int percPixel, int realPixel) {
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

		// The alpha determines how much coverage we have at the current
		// perceived
		// pixel e.g. do we have a few readings at that location. Essentially,
		// the alpha value is used to transition the accuracy metric from the
		// zone of "we have no knowledge about this area" to a zone of
		// "we have some knowledge. It may be wildly wrong, but we we do have a number".

		// If we don't have any readings, then the current accuracy estimate
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
			double accuracyOfGivenPixels = findChangeInTotalAccuracy(percPixel,
					realPixel);
			mAccuracyCurrentDifference += accuracyOfGivenPixels;
			mWasted_lastChangeInAccuracy += accuracyOfGivenPixels;

			// Update coverage by adding the effect of the current pixels
			int alpha = (percPixel >> 24) & 0xff;
			double localCoverage = (double) alpha / 255d;
			mCoverageCurrent += localCoverage;
			mWasted_lastChangeInCoverage += localCoverage;
		}

		// Calculate the number of wasted readings
		if (mWasted_lastChangeInAccuracy == 0
				&& mWasted_lastChangeInCoverage == 0)
			mWastedDueToBoth++;
		else if (mWasted_lastChangeInAccuracy == 0)
			mWastedDueToAccuracy++;
		else if (mWasted_lastChangeInCoverage == 0)
			mWastedDueToCoverage++;
		

		// Update our numbers for usefulness, scaling appropriately
		mUsefulnessTotal += Math.abs(mWasted_lastChangeInAccuracy
				/ mAccuracyMaxDifference)
				+ Math.abs(mWasted_lastChangeInCoverage / mCoverageTotal);
		mUsefulnessCount++;

		mWasted_lastChangeInAccuracy = 0;
		mWasted_lastChangeInCoverage = 0;

		mTotalReadings++;
	}

	public double getAccuracy() {
		return (mAccuracyMaxDifference - mAccuracyCurrentDifference)
				/ mAccuracyMaxDifference;
	}

	/** Returns the current coverage */
	public double getCoverage() {
		if (mCoverageTotal == 0)
			return -1;

		return mCoverageCurrent / mCoverageTotal;
	}

	public int getTotalReadings() {
		return mTotalReadings;
	}

	/**
	 * Returns the number of readings that were partially useless because they
	 * provided no change in coverage. These readings may or may not have had a
	 * role in changing the current accuracy, and therefore they may have been
	 * partially useful
	 */
	public int getUselessReadingsDueToCoverage() {
		return mWastedDueToCoverage + mWastedDueToBoth;
	}

	/**
	 * Returns the number of readings that were partially useless because they
	 * provided no change in accuracy. These readings may or may not have had a
	 * role in changing the current coverage, and therefore they may have been
	 * partially useful
	 */
	public int getUselessReadingsDueToAccuracy() {
		return mWastedDueToAccuracy + mWastedDueToBoth;
	}

	/**
	 * Returns the total count of readings that were partially useless. Note
	 * that this is not the same as the sum of
	 * {@link MetricCalculator#getUselessReadingsDueToAccuracy()} and
	 * {@link MetricCalculator#getUselessReadingsDueToCoverage()}, because some
	 * of the readings may have been useless to both coverage and accuracy and
	 * therefore been double counted
	 * 
	 * @return
	 */
	public int getPartiallyUselessReadingsTotalCount() {
		return getUselessReadingsDueToAccuracy()
				+ getUselessReadingsDueToCoverage()
				- getUselessReadingsDueToBoth();
	}

	public int getUselessReadingsDueToBoth() {
		return mWastedDueToBoth;
	}

	public void resetUselessReadingCounts() {
		mWastedDueToAccuracy = 0;
		mWastedDueToCoverage = 0;
		mWastedDueToBoth = 0;
	}

	/**
	 * Returns a number that represents the amount of change introduced per
	 * reading. This is not anchored to any real-world construct, but does allow
	 * comparisons within a single simulation of the amount of data gathered
	 * from each reading on average
	 */
	public double getUsefulnessPerReading() {
		return mUsefulnessTotal / (double) mUsefulnessCount;
	}

	/*public void resetUsefulnessPerReading() {
		mUsefulnessCount = 0;
		mUsefulnessTotal = 0;
	}*/

	public void resetTotalReadingCount() {
		mTotalReadings = 0;
	}
}
