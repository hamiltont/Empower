package org.turnerha.policys.collection;

import org.turnerha.SmartPhone;

public class ConstantDataCollection implements DataCollectionPolicy {

	@Override
	public boolean shouldCollectData(SmartPhone phone) {
		return true;
	}

}
