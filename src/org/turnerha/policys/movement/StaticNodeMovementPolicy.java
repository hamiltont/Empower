package org.turnerha.policys.movement;

import org.turnerha.sensornodes.SensorNode;

public class StaticNodeMovementPolicy implements NodeMovementPolicy {

	private static StaticNodeMovementPolicy instance_;

	private StaticNodeMovementPolicy() {
	};

	public static StaticNodeMovementPolicy getInstance() {
		if (instance_ == null)
			instance_ = new StaticNodeMovementPolicy();
		return instance_;
	}

	@Override
	public boolean shouldMove(SensorNode node) {
		return false;
	}

}
