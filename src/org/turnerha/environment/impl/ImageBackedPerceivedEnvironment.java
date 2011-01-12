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
import java.awt.image.RescaleOp;
import java.util.HashMap;

import javax.swing.JFrame;

import org.turnerha.Main;
import org.turnerha.environment.MetricCalculator;
import org.turnerha.environment.PerceivedEnvironment;
import org.turnerha.environment.utils.BlendComposite;
import org.turnerha.environment.utils.EnvironUtils;
import org.turnerha.environment.utils.ImagePanel;
import org.turnerha.geography.GeoBox;
import org.turnerha.geography.GeoLocation;
import org.turnerha.geography.KmlGeography;
import org.turnerha.geography.Projection;
import org.turnerha.geography.ProjectionCartesian;

// TODO - Hide the Perceived network behind a server object. The server object can then
// monitor the 'bandwidth' consumed
public class ImageBackedPerceivedEnvironment implements PerceivedEnvironment {
	BufferedImage mNetwork;

	HashMap<Integer, BufferedImage> mReadingCircles = new HashMap<Integer, BufferedImage>();

	/**
	 * Counts the number of readings that have been input, which we can then
	 * treat as the number of communications between the clients and the servers
	 */
	private int mReadings = 0;

	/** Used to make the network image paint at 50% opacity */
	private RescaleOp mAlphaOp;

	/**
	 * The projection between the pixel values in the backing image model data
	 * and real-world latitude longitude
	 */
	private ProjectionCartesian mProjection;

	private JFrame mDebugFrame;
	private ImagePanel mDebugImagePanel;

	private MetricCalculator mMetricCalc;

	public ImageBackedPerceivedEnvironment(Dimension size, KmlGeography kml,
			MetricCalculator mc) {

		mProjection = new ProjectionCartesian(kml.getGeoBox(), size);
		mMetricCalc = mc;

		mNetwork = createCompatibleTranslucentImage(size.width, size.height);

		// Create a rescale filter operation to makes the image 50% opaque
		float[] scales = { 1f, 1f, 1f, 0.5f };
		float[] offsets = new float[4];
		mAlphaOp = new RescaleOp(scales, offsets, null);

		if (Main.DEBUG) {
			mDebugFrame = new JFrame("Perceived black/white");
			mDebugImagePanel = new ImagePanel(mNetwork);
			mDebugFrame.getContentPane().add(mDebugImagePanel);
			mDebugFrame.setSize(800, 500);
			mDebugFrame.setVisible(true);
		}

	}

	@Override
	public void addReading(int value, GeoLocation loc) {

		Point p = mProjection.getPointAt(loc);

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

		if (Main.DEBUG) {
			mDebugImagePanel.setImage(mNetwork);
		}
	}

	@Override
	public void paintInto(Graphics g, Projection proj) {
		// Draw the image, applying an alpha filter
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawImage(mNetwork, mAlphaOp, 0, 0);

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

		return mProjection.getGeoBoxOf(new Rectangle(w, h));
	}

	@Override
	public int getValueAt(GeoLocation location) {
		Point p = mProjection.getPointAt(location);
		return mNetwork.getRGB(p.x, p.y);
	}

	@Override
	public BufferedImage renderFullImage() {
		return mNetwork;
	}
}
