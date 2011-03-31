package org.turnerha.policys.collection;

import org.turnerha.sensornodes.SensorNode;

public interface DataCollectionPolicy {

	public boolean shouldCollectData(SensorNode node);
}
