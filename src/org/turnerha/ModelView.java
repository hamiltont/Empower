package org.turnerha;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings( { "serial" })
class ModelView extends Component {

	private List<Long> updateTimes = new ArrayList<Long>();
	private double framesPerSecond_ = 0;
	private ModelProxy proxy;
	private Color mStaticPoint = (new Color(0, 255, 0, 100)).brighter()
			.brighter().brighter().brighter().brighter();

	public ModelView(ModelProxy proxy) {
		this.proxy = proxy;

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

		setPreferredSize(screen);

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

		Dimension size = getSize();

		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, size.width, size.height);

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
			g.drawString("Heartbeats: " + proxy.getFrameCount(), 10,
					40);
			
			long timeInMs = Main.millisecondsPerHeartbeat * proxy.getFrameCount();
			double timeInDays = (timeInMs * 1.0) / (1000d * 60d * 60d * 24d);
			String days = String.format("%1$5.3f", timeInDays);
			g.drawString("Simulation Time (days):" + days, 10,55);

		} finally {
			proxy.modelLock.unlock();
		}

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
