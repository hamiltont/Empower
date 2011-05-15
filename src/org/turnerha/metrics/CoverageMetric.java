package org.turnerha.metrics;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import org.turnerha.Model;
import org.turnerha.ModelView;
import org.turnerha.environment.impl.ImageBackedPerceivedEnvironment;
import org.turnerha.environment.impl.ImageBackedRealEnvironment;
import org.turnerha.geography.KmlGeography;
import org.turnerha.geography.Projection;

/**
 * Tracks the coverage, which is defined as
 * 
 * <pre>
 * amount of environment we have readings on
 * -----------------------------------------
 * 	  total amount of environment
 * </pre>
 * 
 * where environment is measured in units of pixels, with each fully opaque
 * pixel being counted as 1 and each fully transparent pixel being counted as 0.
 * Pixels with in-between transparencies (transparency ranges from 0...255) are
 * converted to fractional amounts between 1 and 0.
 * 
 * <p>
 * Remember that currently we only support {@link ImageBackedRealEnvironment}
 * and {@link ImageBackedPerceivedEnvironment}, so the coverage starts at zero,
 * because none of the perceived environment pixels have non-zero alpha.
 * </p>
 * 
 * <p>
 * The total amount of environment depends upon both the real environment chosen
 * and the KML geography. While the real environment image can have pixels that
 * have any transparency between 0% and 100%, the current implementation of
 * getValueAt() only returns the RGB value, and ignores the alpha component.
 * Therefore, for our purposes, we can act as though all real environments have
 * a fully 1 alpha.
 * </p>
 * 
 * <p>
 * The {@link KmlGeography} outlines the region we are interested in, and we are
 * only interested in the coverage within the {@link KmlGeography}, so two
 * different {@link KmlGeography}s that are loaded on top of the same real
 * environment image can result in different total environment counts. We find
 * the total environment count by checking, for each pixel in the real
 * environment, if that pixel is contained within the {@link KmlGeography}. If
 * it is then we add one to the total possible environment.
 * </p>
 * 
 * @author hamiltont
 * 
 */
public class CoverageMetric extends DefaultMetric {

	private double mCoverageTotal = 0;
	private double mCoverageCurrent = 0;

	@Override
	public void init() {
		updateKML();
	}

	@Override
	public String getName() {
		return "Coverage";
	}

	/**
	 * Removing the effect of the pixels that are about to be changed on the
	 * current coverage
	 * 
	 * @see Metric#preNewReading(Point[])
	 */
	@Override
	public void preNewReading(Point[] affectedPixels) {
		ImageBackedPerceivedEnvironment pe = (ImageBackedPerceivedEnvironment) Model
				.getInstance().getServer().getPerceivedEnvironment();

		for (Point p : affectedPixels) {
			int perceived = pe.getRGB(p.x, p.y);

			int alpha = (perceived >> 24) & 0xff;
			double localCoverage = ((double) alpha) / 255d;
			mCoverageCurrent -= localCoverage;

		}
	}

	/**
	 * Adds the effect of the new pixels to the current coverage
	 * 
	 * @see Metric#postNewReading(Point[])
	 */
	@Override
	public void postNewReading(Point[] affectedPixels) {
		ImageBackedPerceivedEnvironment pe = (ImageBackedPerceivedEnvironment) Model
				.getInstance().getServer().getPerceivedEnvironment();

		for (Point p : affectedPixels) {
			int perceived = pe.getRGB(p.x, p.y);

			int alpha = (perceived >> 24) & 0xff;
			double localCoverage = ((double) alpha) / 255d;
			mCoverageCurrent += localCoverage;

		}

	}

	public double getCoverage() {
		return mCoverageCurrent / mCoverageTotal;
	}

	@Override
	public void updateKML() {

		// The geography has changed, so we need to know how many of the pixels
		// could possible be covered given this new coverage. For each pixel in
		// the real network we convert that pixel into a latitude/longitude
		// value and then ask if it's contained in the KMLGeography
		ImageBackedRealEnvironment real = (ImageBackedRealEnvironment) Model
				.getInstance().getRealEnvironment();

		KmlGeography geo = Model.getInstance().getKml();
		Projection defaultRenderingProjection = ModelView.getInstance()
				.getDefaultProjection();

		Dimension realSize = real.getPixelSize();
		for (int x = 0; x < realSize.width; x++)
			for (int y = 0; y < realSize.height; y++)
				if (geo
						.contains(defaultRenderingProjection
								.getLocationAt(x, y)))
					mCoverageTotal++;

	}

}
