package org.turnerha.metrics;

import java.awt.Point;

import org.turnerha.environment.Environment;
import org.turnerha.geography.KmlGeography;

/**
 * The API definition that allows metrics to be calculated during simulation
 * execution
 * 
 * @author hamiltont
 * 
 */
public interface Metric {

	/** Returns this @link{Metric}s unique name */
	public String getName();

	/**
	 * Indicates that it is safe to access any static variables, such as the
	 * {@link KmlGeography} or the {@link Environment}s
	 */
	public void init();

	public void preUpdate();

	public void postUpdate();

	/**
	 * Called before a new data reading is input by a sensor node
	 * 
	 * @param affectedPixels
	 *            The {@link Point}s in the environment that will be affected by
	 *            the new reading. These {@link Point}s should be calculated by
	 *            using the default projection, not the current projection
	 * 
	 *            TODO - Currently some of the passed affectedPixels are *not*
	 *            actually contained in the KML geography. The MetricRunner
	 *            should ensure that each affectedPixel is actually contained
	 *            before passing it. This current error causes the coverage and
	 *            accuracy metrics to not peak at 1, but rather around 1.12...
	 */
	public void preNewReading(Point[] affectedPixels);

	public void postNewReading(Point[] affectedPixels);

	public void updateRealNetwork();

	public void updateKML();
}
