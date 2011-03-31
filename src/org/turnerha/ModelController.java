package org.turnerha;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingWorker;

import org.turnerha.environment.MetricCalculator;
import org.turnerha.environment.RealEnvironment;
import org.turnerha.environment.impl.ImageBackedRealEnvironment;

/**
 * Controls the interactions b/w the Model and the ModelView, and does some
 * other bookkeeping.
 * 
 * The threading model is fairly simple - we can't update the Model in the Event
 * thread without freezing the application, so on start we invalidate the
 * {@link ModelView}. The {@link ModelView}'s contract is that once rendering
 * occurs (through a call to {@link ModelView#paint(java.awt.Graphics)}) the
 * {@link ModelView} calls us back via {@link ModelController#doneRendering()}.
 * This triggers the {@link ModelController} to create a new
 * {@link ModelUpdater} and start it, then return. Once the {@link ModelUpdater}
 * is finished, it updates some bookkeeping variables and invalidates the
 * {@link ModelView}, thus restarting the cycle of
 * render->update->render->update.
 * 
 * @author hamiltont
 * 
 */
public class ModelController {
	public AtomicInteger sleepTime = new AtomicInteger(500);
	private MetricCalculator mMetricCalc;

	private int mHeartbeatCount = 0;
	private HashMap<Integer, File> mRealEnvironMap;
	private RealEnvironment mRealEnvironment;
	private Model mModel;
	private ModelView mModelView;

	private boolean isUpdating = false;

	public ModelController(MetricCalculator mc,
			HashMap<Integer, File> realEnvironmentMap, RealEnvironment re,
			Model model) {

		mMetricCalc = mc;

		mRealEnvironMap = realEnvironmentMap;
		mRealEnvironment = re;
		mModel = model;
	}

	public void start(ModelView view) {
		mModelView = view;
		mModelView.repaint();
		System.out.println("Start Called");
	}

	/**
	 * Called by the model when an update is finished
	 */
	protected void doneRendering() {
		if (isUpdating)
			return;

		System.out.println("Done rendering");
		ModelUpdater updater = new ModelUpdater();
		isUpdating = true;
		updater.execute();
	}

	public int getHeartbeatCount() {
		return mHeartbeatCount;
	}

	// TODO This is a poor approach because it limits the speed of simulation
	// significantly. A much better approach is to a) have the ModelUpdater run
	// in a while(true) loop, and b)set a Timer that calls repaint() on the
	// ModelView every so often e.g. 500ms. However, before calling repaint,
	// interrupt() the running ModelUpdater, so that within one complete model
	// update the interrupt is noticed. Then the ModelUpdater can terminate
	// itself e.g. stop touching the Model, the repaint can occur, and a new
	// ModelUpdater can be used to resume updating the Model. However, this is
	// still a poor solution because we are creating and destroying threads
	// rapidly e.g. once every 500ms. I should think about this some more and
	// figure out how to recover after the ModelUpdater has been interrupted
	// TODO Add in a stopwatch specifically to monitor how fast the model is
	// being updated e.g. how rapidly the simulation is running. This is more
	// important than the framerate, especially if we are running in NO_HEAD
	// mode
	private class ModelUpdater extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			System.out.println("Updator running");

			mModel.update();

			// Slow down the simulation a bit
			// Thread.sleep(sleepTime.get());

			return null;
		}

		@Override
		protected void done() {
			System.out.println("Updator done");
			mHeartbeatCount++;

			// Print out some metrics
			Log.log(Main.hoursPerHeartbeat * mHeartbeatCount);

			// Determine if the real network needs to be replaced
			// TODO move this to a background thread
			if (mRealEnvironMap.containsKey(new Integer(mHeartbeatCount))) {
				((ImageBackedRealEnvironment) mRealEnvironment)
						.loadNewEnvironment(mRealEnvironMap.get(new Integer(
								mHeartbeatCount)));
				mMetricCalc.updateRealEnvironment(mRealEnvironment);
			}

			// Reset the usefulness measures so they are calculated on a
			// per-heartbeat basis
			mMetricCalc.resetUselessReadingCounts();
			mMetricCalc.resetTotalReadingCount();
			// mMetricCalc.resetUsefulnessPerReading();

			isUpdating = false;
			mModelView.repaint();
		}

	}

}
