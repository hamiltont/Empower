package org.turnerha;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.turnerha.environment.MetricCalculator;
import org.turnerha.environment.RealEnvironment;
import org.turnerha.environment.impl.ImageBackedRealEnvironment;

public class ModelController {
	private Slice[][] mFinishedSlices;
	private ModelProxy proxy;
	public AtomicInteger sleepTime = new AtomicInteger(100);
	private MetricCalculator mMetricCalc;

	private int mHeartbeatCount = 0;
	private HashMap<Integer, File> mRealEnvironMap;
	private RealEnvironment mRealEnvironment;

	public ModelController(ModelProxy proxy, int rows, int columns,
			MetricCalculator mc, HashMap<Integer, File> realEnvironmentMap,
			RealEnvironment re) {
		mFinishedSlices = new Slice[rows][columns];
		this.proxy = proxy;

		mMetricCalc = mc;

		mRealEnvironMap = realEnvironmentMap;
		mRealEnvironment = re;

	}

	public synchronized void completeSlice(Slice slice)
			throws InterruptedException {

		mFinishedSlices[slice.getRow()][slice.getColumn()] = slice;

		if (allSlicesAreReady()) {
			mHeartbeatCount++;

			// Print out some metrics
			Log.log(Main.hoursPerHeartbeat * mHeartbeatCount);

			// Determine if the real network needs to be replaced
			if (mRealEnvironMap.containsKey(new Integer(mHeartbeatCount))) {
				((ImageBackedRealEnvironment) mRealEnvironment)
						.loadNewEnvironment(mRealEnvironMap.get(new Integer(
								mHeartbeatCount)));
				mMetricCalc.updateRealEnvironment(mRealEnvironment);
			}

			// Reset the usefulness variable
			mMetricCalc.resetUsefulnessPerReading();

			// Slow down the simulation a bit
			Thread.sleep(sleepTime.get());

			// Swap front and back model pointers
			proxy.modelLock.lock();
			mFinishedSlices = proxy.swapModel(mFinishedSlices);
			proxy.modelLock.unlock();

			// Toss out old model memory
			for (int row : Util.range(mFinishedSlices.length))
				for (int col : Util.range(mFinishedSlices[0].length))
					mFinishedSlices[row][col] = null;

			// Let all of the other slices resume working
			notifyAll();
		} else
			// Pause this slice until the others are ready
			wait();

	}

	private boolean allSlicesAreReady() {
		for (int row : Util.range(mFinishedSlices.length))
			for (int col : Util.range(mFinishedSlices[0].length))
				if (mFinishedSlices[row][col] == null)
					return false;

		return true;
	}

	public int getHeartbeatCount() {
		return mHeartbeatCount;
	}

}
