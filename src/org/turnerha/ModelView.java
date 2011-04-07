package org.turnerha;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.turnerha.environment.Environment;
import org.turnerha.geography.KmlGeography;
import org.turnerha.geography.Projection;
import org.turnerha.geography.ProjectionCartesian;
import org.turnerha.sensornodes.SensorNode;

/**
 * Allows part of the {@link Model} to be visualized, using a {@link Projection}
 * to convert between 3-dimensional latitude/longitude coordinates and
 * 2-dimensional cartesian pixel coordinates.
 * 
 * This renders multiple parts of the model, including the underlying kml, the
 * sensor nodes, and either the perceived or the real network.
 * 
 * @author hamiltont
 * 
 */
// TODO At some point it would be very useful to see if the kml can be rendered
// only as needed, and saved into a persistent buffer, so that the buffer can
// simply be copied upon consecutive redraw operations
@SuppressWarnings( { "serial" })
public class ModelView extends JPanel {

	private List<Long> updateTimes = new ArrayList<Long>();
	private double framesPerSecond_ = 0;

	private Projection mProjection;
	private Model mModel;
	private ModelController mController;

	private Projection defaultProjection;

	/**
	 * If true, then the perceived network is displayed. If false, then the real
	 * network
	 */
	private boolean is_displaying_perceived = true;

	private static ModelView instance_ = null;

	public ModelView() {
	}

	public void resetDefaultProjection() {
		mProjection = getDefaultProjection();
	}

	public Projection getDefaultProjection() {
		if (defaultProjection != null)
			return defaultProjection;

		KmlGeography kml = mModel.getKml();
		if (kml == null)
			throw new IllegalStateException(
					"Trying to reset the projection before KML has been created");

		defaultProjection = new ProjectionCartesian(
				mModel.getKml().getGeoBox(), getRenderingArea());
		return defaultProjection;
	}

	public static ModelView getInstance() {
		if (instance_ == null)
			instance_ = new ModelView();
		return instance_;
	}

	/**
	 * @return The area in pixels of the region in which the {@link ModelView}
	 *         is rendered
	 */
	public Dimension getRenderingArea() {
		return getSize();
	}

	public Projection getProjection() {
		return mProjection;
	}

	private int frameCount = 0;
	private static final int FRAMES = 30;
	private long totalTime = 0;

	double fps = 0;

	// Draws background, then model, then overlays
	private boolean is_initialized = false;

	@Override
	public void paint(Graphics g) {
		// This is nasty, but I need a way to hook all my user-driven control
		// flow into the event-driven flow, and the first time ModelView is
		// painted is perfect. I hate myself for this

		if (is_initialized == false) {
			new Model(Model.getGeo(new File("geo-areas/va_counties.kml")));
			Model.getInstance().buildServer();
			mModel = Model.getInstance();
			resetDefaultProjection();
			mController = new ModelController(this);
			is_initialized = true;
		}

		if (mController.isPaused())
			return;

		long start = System.nanoTime();
		calculateFrameRate();

		g.setColor(Color.GRAY);
		g.fillRect(0, 0, getWidth(), getHeight());

		if (mModel.getKml() != null && mProjection == null)
			resetDefaultProjection();

		Environment environ;
		if (is_displaying_perceived)
			environ = mModel.getPerceivedEnvironment();
		else
			environ = mModel.getRealEnvironment();

		environ.paintInto(g, null);

		if (mModel.getKml() == null)
			System.out.println("no kml");
		else
			mModel.getKml().paint(g, mProjection);

		if (frameCount == FRAMES) {
			long timeInMilliSec = totalTime / 1000000;
			double framesInMilliSeconds = (double) FRAMES
					/ (double) timeInMilliSec;
			fps = framesInMilliSeconds * 1000;
			totalTime = 0;
			frameCount = 0;
		} else {
			totalTime += System.nanoTime() - start;
			frameCount++;
		}

		g.setColor(Color.white);

		int multiplier = 15;
		int base = 15;

		g.drawString(
				"Framerate: " + String.format("%1$5.2f", framesPerSecond_), 10,
				multiplier * 0 + base);

		/*
		 * g.drawString("Coverage: " + String.format("%1$5.2f",
		 * mMetric.getCoverage()), 10, multiplier * 4 + base);
		 * g.drawString("Total Accuracy: " + String.format("%1$5.2f",
		 * mMetric.getAccuracy()), 10, multiplier * 5 + base);
		 */

		// g.drawString("Accuracy within coverage: xx%", 10, 100);

		/*
		 * if (mNetwork instanceof ImageBackedPerceivedEnvironment)
		 * g.drawString("Viewing Perceived Environment (Press R to change)", 10,
		 * multiplier * 6 + base); else
		 * g.drawString("Viewing Perceived Environment (Press P to change)", 10,
		 * multiplier * 6 + base);
		 */

		// Draw all nodes
		// TODO pass the graphics to all nodes and let them render themselves
		if (toggle)
			g.setColor(Color.RED);
		else
			g.setColor(Color.GREEN);
		toggle = !toggle;
		for (SensorNode node : mModel.getNodes()) {
			Point p = node.getPointUsing(mProjection);
			g.drawLine(p.x, p.y, p.x, p.y);
		}

		mController.doneRendering();
	}

	boolean toggle = false;

	/**
	 * Sets whether the {@link ModelView} is showing the real environment or the
	 * perceived environment
	 * 
	 * @param should_display_perceived_environ
	 *            if true, the {@link ModelView} will display the perceived
	 *            environment. If false, it will display the real environment
	 */
	public void setDisplayNetwork(boolean should_display_perceived_environ) {
		is_displaying_perceived = should_display_perceived_environ;
	}

	private void calculateFrameRate() {
		long time = System.currentTimeMillis();

		updateTimes.add(new Long(time));

		// We will have the wrong framerate for the first 30 draws. No big.
		double totalTimeInSec = (time - updateTimes.get(0)) / 1000d;

		framesPerSecond_ = 30d / totalTimeInSec;

		if (updateTimes.size() == 31)
			updateTimes.remove(0);

	}

	public void update(Graphics g) {
		System.out.println("Update called");
		super.update(g);
	}
}
