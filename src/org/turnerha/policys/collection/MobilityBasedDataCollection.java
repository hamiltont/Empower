package org.turnerha.policys.collection;

import java.util.Random;

import org.turnerha.SmartPhone;

public class MobilityBasedDataCollection implements DataCollectionPolicy {
	private Random generator;

	/** The generator controls where this policy gets random numbers */
	public MobilityBasedDataCollection(Random rGenerator) {
		generator = rGenerator;
	}

	@Override
	public boolean shouldCollectData(SmartPhone phone) {
		if (generator.nextDouble() < phone.getProbabilityOfMoving())
			return true;
		else
			return false;
	}

}
