package org.turnerha.policys.movement;

import java.util.Random;

import org.turnerha.sensornodes.SensorNode;

public class ProbabilisticMovementPolicy implements NodeMovementPolicy {

	private double mProbabilityOfMovement;
	private Random mRandom;

	public ProbabilisticMovementPolicy(double probabilityOfMoving,
			Random generator) {
		mProbabilityOfMovement = probabilityOfMoving;
		mRandom = generator;
	}

	@Override
	public boolean shouldMove(SensorNode node) {
		if (mRandom.nextDouble() > mProbabilityOfMovement)
			return false;
		return true;
	}
	
	public double getProbabilityOfMovement() {
		return mProbabilityOfMovement;
	}

}
