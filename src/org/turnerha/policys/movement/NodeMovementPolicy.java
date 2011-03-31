package org.turnerha.policys.movement;

import org.turnerha.sensornodes.SensorNode;

public interface NodeMovementPolicy {
	
	public boolean shouldMove(SensorNode node);
}
