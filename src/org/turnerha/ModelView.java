package org.turnerha;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.turnerha.map.Map;
import org.turnerha.network.Network;
import org.turnerha.network.PerceivedNetwork;

@SuppressWarnings( { "serial" })
class ModelView extends Component {

	private List<Long> updateTimes = new ArrayList<Long>();
	private double framesPerSecond_ = 0;
	private ModelProxy proxy;
	private Color mStaticPoint = (new Color(0, 255, 0, 100)).brighter()
			.brighter().brighter().brighter().brighter();
	private ModelController controller_;

	private Map mMap;
	private Network mNetwork;

	public ModelView(ModelProxy proxy, ModelController cont, Map m, Network rn) {
		this.proxy = proxy;
		controller_ = cont;

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

			ShallowSlice[][] slices = proxy.getModel();

			for (int row : Util.range(slices.length))
				for (int col : Util.range(slices[0].length)) {
					ShallowSlice s = slices[row][col];
					if (s == null)
						System.out.println("crap");

					for (Point p : s.getPoints()) {
						g.drawLine(p.x, p.y, p.x, p.y);
					}

				}

			g.setColor(Color.white);
			g.drawString("Heartbeats: " + proxy.getFrameCount(), 10, 40);

			long timeInMs = Math.round(Main.hoursPerHeartbeat * 60f * 1000f)
					* proxy.getFrameCount();
			double timeInDays = (timeInMs * 1.0) / (1000d * 60d * 60d * 24d);
			String days = String.format("%1$5.3f", timeInDays);
			g.drawString("Simulation Time (days):" + days, 10, 55);
			g.drawString("Slowdown Factor: " + controller_.sleepTime.get()
					+ " (Press + or - to change)", 10, 70);

		} finally {
			proxy.modelLock.unlock();
		}

		mNetwork.paint(g);

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

		if (mNetwork instanceof PerceivedNetwork)
			g.drawString("Viewing Perceived Network (Press R to change)", 10, 130);
		else
			g.drawString("Viewing Perceived Network (Press P to change)", 10, 130);
		
		g.drawString("Press (esc) to quit", 10, 145);
	}
	
	public void setNetwork(Network n) {
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
