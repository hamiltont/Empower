package org.turnerha.environment.impl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import org.turnerha.Model;
import org.turnerha.ModelView;
import org.turnerha.Util;
import org.turnerha.environment.MetricCalculator;
import org.turnerha.environment.PerceivedEnvironment;
import org.turnerha.environment.utils.BlendComposite;
import org.turnerha.environment.utils.EnvironUtils;
import org.turnerha.geography.GeoBox;
import org.turnerha.geography.GeoLocation;
import org.turnerha.geography.Projection;
import org.turnerha.geography.ProjectionCartesian;

public class ImageBackedPerceivedEnvironment implements PerceivedEnvironment {
	BufferedImage mNetwork;

	HashMap<Integer, BufferedImage> mReadingCircles = new HashMap<Integer, BufferedImage>();

	/**
	 * Counts the number of readings that have been input, which we can then
	 * treat as the number of communications between the clients and the servers
	 */
	private int mReadings = 0;

	private MetricCalculator mMetricCalc;

	public ImageBackedPerceivedEnvironment(MetricCalculator mc) {
		Dimension size = Util.getRenderingAreaSize();

		mMetricCalc = mc;

		// Setup Perceived Network Image
		mNetwork = createCompatibleTranslucentImage(size.width, size.height);
	}

	@Override
	public void addReading(int value, GeoLocation loc) {

		Point p = ModelView.getInstance().getProjection().getPointAt(loc);

		// Let the metric calculator remove the effect due to p
		Point[] affectedPoints = new Point[5 * 5];
		int pos = 0;
		for (int x = p.x - 2; x < p.x + 3; x++)
			for (int y = p.y - 2; y < p.y + 3; y++)
				affectedPoints[pos++] = new Point(x, y);
		mMetricCalc.preNewReading(affectedPoints);

		BufferedImage mReading = mReadingCircles.get(new Integer(value));

		if (mReading == null) {
			Color c = new Color(value);
			mReading = EnvironUtils.createReadingImage(5, c);
			mReadingCircles.put(new Integer(value), mReading);
		}

		Graphics2D g = (Graphics2D) mNetwork.getGraphics();
		// g.setColor(new Color(value));
		// g.drawLine(p.x, p.y, p.x, p.y);
		g.setComposite(BlendComposite.AverageInclAlpha.derive(1f));
		g.drawImage(mReading, null, p.x - 2, p.y - 2);
		mReadings++;

		// Let the metric calculator add back in the effect due to p
		mMetricCalc.postNewReading(affectedPoints);
	}

	@Override
	public void paintInto(Graphics g, Projection proj) {
		g.drawImage(mNetwork, 0, 0, null);
	}

	public static BufferedImage createCompatibleTranslucentImage(int width,
			int height) {
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();

		return gc
				.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
	}

	/** Temporary method for an image backed environment */
	public int getRGB(int x, int y) {
		return mNetwork.getRGB(x, y);
	}

	@Override
	public GeoBox getSize() {
		int w = mNetwork.getWidth();
		int h = mNetwork.getHeight();

		return ModelView.getInstance().getProjection().getGeoBoxOf(new Rectangle(w, h));
	}

	@Override
	public int getValueAt(GeoLocation location) {
		Point p = ModelView.getInstance().getProjection().getPointAt(location);
		return mNetwork.getRGB(p.x, p.y);
	}

	@Override
	public BufferedImage renderFullImage() {
		return mNetwork;
	}
}
