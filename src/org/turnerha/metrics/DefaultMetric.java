package org.turnerha.metrics;

import java.awt.Point;

/**
 * Implementation of {@link Metric} interface that does nothing. Implementations
 * of {@link Metric} that don't need all of the methods can subclass
 * {@link DefaultMetric} and only implement the methods they are interested in
 * 
 * @author hamiltont
 * 
 */
public class DefaultMetric implements Metric {

	@Override
	public String getName() {
		return "";
	}

	@Override
	public void init() {
	}

	@Override
	public void postNewReading(Point[] affectedPixels) {
	}

	@Override
	public void postUpdate() {

	}

	@Override
	public void preNewReading(Point[] affectedPixels) {
	}

	@Override
	public void preUpdate() {
	}

	@Override
	public void updateKML() {
	}

	@Override
	public void updateRealNetwork() {
	}

}
