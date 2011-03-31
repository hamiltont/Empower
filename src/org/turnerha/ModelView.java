package org.turnerha;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.turnerha.environment.Environment;
import org.turnerha.environment.MetricCalculator;
import org.turnerha.environment.RealEnvironment;
import org.turnerha.environment.impl.ImageBackedPerceivedEnvironment;
import org.turnerha.geography.KmlGeography;
import org.turnerha.geography.Projection;
import org.turnerha.sensornodes.SensorNode;

@SuppressWarnings( { "serial" })
class ModelView extends Component {

	private List<Long> updateTimes = new ArrayList<Long>();
	private double framesPerSecond_ = 0;
	private Color mStaticPoint = (new Color(0, 255, 0, 100)).brighter()
			.brighter().brighter().brighter().brighter();
	private ModelController mController;

	private KmlGeography mMap;
	private Environment mNetwork;
	private MetricCalculator mMetric;
	private Projection mProjection;

	public ModelView(ModelController cont, KmlGeography m, Environment visualizedEnvironment,
			MetricCalculator mc, Model model, Projection p) {
		mController = cont;
		mMetric = mc;

		mMap = m;
		mModel = model;
		mNetwork = visualizedEnvironment;
		mProjection = p;

		// If our parent does not set our size, then we should do it manually
		// Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		// setPreferredSize(screen);
	}

	private int frameCount = 0;
	private static final int FRAMES = 30;
	private long totalTime = 0;

	double fps = 0;
	private Model mModel;

	// Draws background, then model, then overlays
	@Override
	public void paint(Graphics g) {
		long start = System.nanoTime();
		calculateFrameRate();

		g.setColor(Color.GRAY);
		g.fillRect(0, 0, getWidth(), getHeight());

		mNetwork.paintInto(g, null);

		mMap.paint(g);

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
		g.drawString("Heartbeats: " + mController.getHeartbeatCount(), 10,
				multiplier * 1 + base);
		g.drawString("Slowdown Factor: " + mController.sleepTime.get(), 10,
				multiplier * 2 + base);

		g.drawString("Coverage: "
				+ String.format("%1$5.2f", mMetric.getCoverage()), 10,
				multiplier * 4 + base);
		g.drawString("Total Accuracy: "
				+ String.format("%1$5.2f", mMetric.getAccuracy()), 10,
				multiplier * 5 + base);

		// g.drawString("Accuracy within coverage: xx%", 10, 100);

		if (mNetwork instanceof ImageBackedPerceivedEnvironment)
			g.drawString("Viewing Perceived Environment (Press R to change)",
					10, multiplier * 6 + base);
		else
			g.drawString("Viewing Perceived Environment (Press P to change)",
					10, multiplier * 6 + base);

		g.drawString("Press (esc) to quit", 10, multiplier * 7 + base);

		// Draw all nodes
		// TODO pass the graphics to all nodes and let them render themselves
		g.setColor(Color.RED);
		for (SensorNode node : mModel.getNodes()) {
			Point p = node.getPointUsing(mProjection);
			g.drawLine(p.x, p.y, p.x, p.y);
		}
		
		mController.doneRendering();
	}

	public void setDisplayNetwork(Environment n) {
		mNetwork = n;
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
}
