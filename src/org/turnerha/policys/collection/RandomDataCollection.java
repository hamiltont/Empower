package org.turnerha.policys.collection;

import java.util.Random;

import org.turnerha.sensornodes.SensorNode;

public class RandomDataCollection implements DataCollectionPolicy {

	private double mProbabilityOfCollection;
	private Random generator;

	/**
	 * 
	 * @param randomnessGeneration
	 *            Where this policy gets its random numbers
	 * @param probabilityOfCollection
	 *            The probability that data collection will happen e.g.
	 *            inserting 0.75 here would imply that data collection occurs
	 *            75% of the time
	 */
	public RandomDataCollection(Random randomnessGeneration,
			double probabilityOfCollection) {
		mProbabilityOfCollection = probabilityOfCollection;
		generator = randomnessGeneration;
	}

	@Override
	public boolean shouldCollectData(SensorNode node) {
		if (generator.nextDouble() < mProbabilityOfCollection)
			return true;
		else
			return false;
	}

}
