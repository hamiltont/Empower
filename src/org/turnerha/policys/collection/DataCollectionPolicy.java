package org.turnerha.policys.collection;

import org.turnerha.SmartPhone;

public interface DataCollectionPolicy {

	public boolean shouldCollectData(SmartPhone phone);
}
