package org.turnerha.policys.collection;

import java.util.Random;

import org.turnerha.policys.movement.ProbabilisticMovementPolicy;
import org.turnerha.sensornodes.SensorNode;

public class MobilityBasedDataCollection implements DataCollectionPolicy {
	private Random generator;

	/** The generator controls where this policy gets random numbers */
	public MobilityBasedDataCollection(Random rGenerator) {
		generator = rGenerator;
	}

	@Override
	public boolean shouldCollectData(SensorNode node) {
		if (node.getMovementPolicy() instanceof ProbabilisticMovementPolicy) {
			ProbabilisticMovementPolicy p = (ProbabilisticMovementPolicy) node
					.getMovementPolicy();
			if (generator.nextDouble() < p.getProbabilityOfMovement())
				return true;
			return false;
		}

		System.err.println("The NodeMovementPolicy for this node is not a "
				+ "ProbabilisticMovementPolicy, and therefore the "
				+ "MobilityBasedDataCollectionPolicy cannot operate properly "
				+ "for the following node");
		System.err.println("\t" + node.toString());
		System.err.println("\tDefaulting to always recommending collection");

		return true;
	}

}
