package org.turnerha.environment;

import org.turnerha.geography.GeoLocation;

public interface PerceivedEnvironment extends Environment {

	public void addReading(int value, GeoLocation loc);
}
