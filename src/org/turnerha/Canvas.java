package org.turnerha;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
class Canvas extends Component {

	private List<Long> updateTimes = new ArrayList<Long>();
	private float currentFrameRate_ = 0;
	private ModelFrontBuffer frontBuffer;

	public Canvas(ModelFrontBuffer builder) {
		frontBuffer = builder;

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

		setIgnoreRepaint(false);
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

	@Override
	public void paint(Graphics g) {

		calculateFrameRate();

		Dimension size = getSize();

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, size.width, size.height);

		g.setColor(Color.white);
		g.drawString("Framerate: " + currentFrameRate_, 10, 10);

		// Set Alpha. 0.0f is 100% transparent and 1.0f is 100% opaque.
		// Color myColor = new Color(255, 0, 0, 200);
		Color myColor = new Color(255, 255, 255, 255);
		g.setColor(myColor);

		frontBuffer.modelLock.lock();
		try {
			
			ShallowSlice[][] slices = frontBuffer.getModel();
			for (int row : Util.range(slices.length))
				for (int col : Util.range(slices[0].length)) {
					ShallowSlice s = slices[row][col];
					if (s == null)
						System.out.println("crap");

					for (Point p : s.getPoints()) {
						g.drawLine(p.x, p.y, p.x, p.y);
					}

				}
			
		} finally {
			frontBuffer.modelLock.unlock();
		}

	}

	private void calculateFrameRate() {
		long time = System.currentTimeMillis();

		updateTimes.add(new Long(time));

		// We will have the wrong framerate for the first 30 draws. No big.
		float timeInSec = (time - updateTimes.get(0)) / 1000f;

		currentFrameRate_ = 30f / timeInSec;

		if (updateTimes.size() == 31)
			updateTimes.remove(0);

	}

	public void start() {

	}

}
