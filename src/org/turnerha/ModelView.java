package org.turnerha;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.turnerha.environment.Environment;
import org.turnerha.environment.impl.ImageBackedPerceivedEnvironment;
import org.turnerha.environment.impl.ImageBackedRealEnvironment;
import org.turnerha.geography.KmlGeography;

@SuppressWarnings( { "serial" })
class ModelView extends Component {

	private List<Long> updateTimes = new ArrayList<Long>();
	private double framesPerSecond_ = 0;
	private ModelProxy proxy;
	private Color mStaticPoint = (new Color(0, 255, 0, 100)).brighter()
			.brighter().brighter().brighter().brighter();
	private ModelController controller_;

	private KmlGeography mMap;
	private Environment mNetwork;
	private ImageBackedRealEnvironment mRealEnviron;

	public ModelView(ModelProxy proxy, ModelController cont, KmlGeography m,
			Environment rn,
			ImageBackedRealEnvironment imageBackedRealEnvironment) {
		this.proxy = proxy;
		controller_ = cont;
		mRealEnviron = imageBackedRealEnvironment;

		mMap = m;
		mNetwork = rn;

		// If our parent does not set our size, then we should do it manually
		// Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		// setPreferredSize(screen);

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					invalidate();
					repaint();
				}
			}
		});

		t.start();
	}

	private int frameCount = 0;
	private static final int FRAMES = 30;
	private long totalTime = 0;

	double fps = 0;

	// Draws background, then model, then overlays
	@Override
	public void paint(Graphics g) {
		long start = System.nanoTime();
		calculateFrameRate();

		mMap.paint(g);

		// Set Alpha. 0.0f is 100% transparent and 1.0f is 100% opaque.
		g.setColor(mStaticPoint);

		proxy.modelLock.lock();
		try {

			Slice[][] slices = proxy.getModel();

			for (int row : Util.range(slices.length))
				for (int col : Util.range(slices[0].length)) {
					Slice s = slices[row][col];
					if (s == null) {
						System.out.println("Proxy returned null Slice");
						continue;
					}

					for (Point p : s.getPoints()) {
						g.drawLine(p.x, p.y, p.x, p.y);
					}

				}

			g.setColor(Color.white);
			
			g.drawString("Heartbeats: " + controller_.getHeartbeatCount(), 10, 40);

			// if (proxy.getFrameCount() == 200)
			// mRealEnviron.loadNewEnvironment("foo");

			//String days = String.format("%1$5.3f", timeInDays);
			//g.drawString("Simulation Time (days):" + days, 10, 55);
			g.drawString("Slowdown Factor: " + controller_.sleepTime.get()
					+ " (Press + or - to change)", 10, 70);

		} finally {
			proxy.modelLock.unlock();
		}

		mNetwork.paintInto(g, null);

		if (frameCount == FRAMES) {
			long timeInMilliSec = totalTime / 1000000;
			double framesInMilliSeconds = (double) FRAMES
					/ (double) timeInMilliSec;
			fps = framesInMilliSeconds * 1000;

			// fps = (FRAMES / totalTime) * 1000000000;
			totalTime = 0;
			frameCount = 0;
		} else {
			totalTime += System.nanoTime() - start;
			frameCount++;
		}
		String f2 = String.format("%1$5.3f", fps);

		g.setColor(Color.white);
		String f1 = String.format("%1$5.3f", framesPerSecond_);
		g.drawString("Framerate (inclusive): " + f1, 10, 10);
		g.drawString("Framerate (exclusive): " + f2, 10, 25);

		g.drawString("Coverage: xx%", 10, 85);
		g.drawString("Accuracy within coverage: xx%", 10, 100);
		g.drawString("Accuracy total: xx%", 10, 115);

		if (mNetwork instanceof ImageBackedPerceivedEnvironment)
			g.drawString("Viewing Perceived Environment (Press R to change)",
					10, 130);
		else
			g.drawString("Viewing Perceived Environment (Press P to change)",
					10, 130);

		g.drawString("Press (esc) to quit", 10, 145);
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
