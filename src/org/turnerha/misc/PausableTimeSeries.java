package org.turnerha.misc;

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;

@SuppressWarnings("serial")
public class PausableTimeSeries extends TimeSeries {

	private boolean isPaused = true;
	
	@SuppressWarnings("unchecked")
	public PausableTimeSeries(Comparable name) {
		super(name);
	}
	
	public void setPaused(boolean paused) {
		if (false == paused)
			fireSeriesChanged();
		
		this.isPaused = paused;
	}
	
	@Override
	public void add(RegularTimePeriod period, double value) {
		if (isPaused)
			super.add(period, value, false);
		else
			super.add(period, value, true);
	}
	
	@Override
	public void add(RegularTimePeriod period, java.lang.Number value) {
		if (isPaused)
			super.add(period, value, false);
		else
			super.add(period, value, true);
	}
}
