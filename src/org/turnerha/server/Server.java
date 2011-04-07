package org.turnerha.server;

import org.turnerha.environment.MetricCalculator;
import org.turnerha.environment.PerceivedEnvironment;
import org.turnerha.environment.impl.ImageBackedPerceivedEnvironment;
import org.turnerha.geography.GeoLocation;

/**
 * Controls access to the perceived network, drives the {@link MetricCalculator}
 * 
 * @author hamiltont
 * 
 */
public class Server {
	PerceivedEnvironment mPerceivedEnv;
	MetricCalculator mMetricCalculator;

	public Server() {
		mMetricCalculator = new MetricCalculator();

		mPerceivedEnv = new ImageBackedPerceivedEnvironment(mMetricCalculator);
		
		mMetricCalculator.updatePerceivedEnvironment(mPerceivedEnv);
	}

	public PerceivedEnvironment getPerceivedEnvironment() {
		return mPerceivedEnv;
	}
	
	public MetricCalculator getMetricCalculator() {
		if (mMetricCalculator == null)
			mMetricCalculator = new MetricCalculator();
		return mMetricCalculator;
	}

	// TODO use this call to monitor the 'bandwidth' consumed (or implement it
	// as another metric)
	public void addReading(int value, GeoLocation location) {
		// TODO it would be better if the server controlled the metric
		// calculator e.g. called preNewReading, postNewReading, etc
		mPerceivedEnv.addReading(value, location);
	}
}
