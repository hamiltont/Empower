package org.turnerha;

import java.io.File;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingWorker;

import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.RegularTimePeriod;
import org.turnerha.metrics.MetricCalculator;

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
	private MetricCalculator mMetricCalc;
	private int mHeartbeatCount = 0;

	private HashMap<Integer, File> mRealEnvironMap;
	private Model mModel;
	private ModelView mModelView;

	private static ModelController instance_;

	private boolean isUpdating = false;
	private boolean isPaused = true;

	private RegularTimePeriod currentSimulationHour = new Hour();

	public static ModelController getInstance() {
		return instance_;
	}

	public ModelController(ModelView view) {
		instance_ = this;
		mModelView = view;
		mMetricCalc = Model.getInstance().getServer().getMetricCalculator();
	}

	public void setDynamicEnvironmentMap(HashMap<Integer, File> environments) {
		mRealEnvironMap = environments;
	}

	public void start() {
		mModelView.repaint();
		isPaused = false;
	}

	public void pause() {
		isPaused = true;
	}

	public boolean isPaused() {
		return isPaused;
	}

	/**
	 * Called by the model when an update is finished
	 */
	protected void doneRendering() {
		String s = Integer.toString(mHeartbeatCount) + " hours";
		Main.sHours.setText(s);
		Main.sHours.invalidate();
		currentSimulationHour = currentSimulationHour.next();
		MetricCalculator mc = Model.getInstance().getServer().getMetricCalculator();
		Main.sAccuracy.add(currentSimulationHour, mc.getAccuracy());
		Main.sCoverage.add(currentSimulationHour, mc.getCoverage());
		
		

		if (isUpdating)
			return;

		if (isPaused)
			return;

		Timer t = new Timer("foo");
		t.schedule(new TimerTask() {

			@Override
			public void run() {
				ModelUpdater updater = new ModelUpdater();
				isUpdating = true;
				updater.execute();
			}
		}, 1000);

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
		protected Void doInBackground() {
			try {

				Model.getInstance().update();

			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void done() {
			mHeartbeatCount++;

			// Print out some metrics
			Log.log(Main.hoursPerHeartbeat * mHeartbeatCount);

			// Determine if the real network needs to be replaced
			// TODO move this to a background thread
			// TODO remove the outer if check once I have added the GUI to
			// actually specify that there is a real environment
			if (mRealEnvironMap != null)
				if (mRealEnvironMap.containsKey(new Integer(mHeartbeatCount))) {
					mModel.getRealEnvironment().loadNewEnvironment(
							mRealEnvironMap.get(new Integer(mHeartbeatCount)));
					mMetricCalc.updateRealEnvironment(mModel
							.getRealEnvironment());
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
