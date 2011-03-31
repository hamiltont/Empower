package org.turnerha.policys.reporting;

import org.turnerha.sensornodes.SensorNode;

public interface DataReportingPolicy {

	public int ACTION_TRASH_DATA = 0;
	public int ACTION_SEND_DATA = 1;
	// Not going to implement caching for now
	//public int ACTION_CACHE_DATA = 2;
	
	public int shouldNodeReport(SensorNode node);
}
