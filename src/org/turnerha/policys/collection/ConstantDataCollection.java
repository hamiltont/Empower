package org.turnerha.policys.collection;

import org.turnerha.sensornodes.SensorNode;

public class ConstantDataCollection implements DataCollectionPolicy {

	@Override
	public boolean shouldCollectData(SensorNode node) {
		return true;
	}

}
