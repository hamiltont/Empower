package org.turnerha.environment;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.RescaleOp;
import java.util.HashMap;

import org.jdesktop.swingx.graphics.BlendComposite;
import org.turnerha.geography.KmlGeography;

// TODO - Hide the Perceived network behind a server object. The server object can then
// monitor the 'bandwidth' consumed
public class PerceivedEnviron implements Environment {
	BufferedImage mNetworkBlackWhite;

	HashMap<Integer, BufferedImage> mReadingCircles = new HashMap<Integer, BufferedImage>();

	LookupOp mColorize;

	/**
	 * Counts the number of readings that have been input, which we can then
	 * treat as the number of communications between the clients and the servers
	 */
	private int mReadings = 0;

	/* Create a rescale filter op that makes the image 50% opaque */
	float[] scales = { 1f, 1f, 1f, 0.5f };
	float[] offsets = new float[4];
	RescaleOp mRealpha = new RescaleOp(scales, offsets, null);

	private KmlGeography mKmlGeography;

	public PerceivedEnviron(Dimension size, KmlGeography m) {

		mKmlGeography = m;

		mNetworkBlackWhite = createCompatibleTranslucentImage(size.width,
				size.height);
		Graphics g = mNetworkBlackWhite.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, size.width, size.height);
		g.dispose();

		// mReading = createFadedCircleImage(10);

		BufferedImage gradient = EnvironUtils.createGradientImage(null, null);

		LookupTable lookupTable = EnvironUtils.createColorLookupTable(gradient,
				0.5f);
		mColorize = new LookupOp(lookupTable, null);
	}

	public void addReading(int value, Point p) {

		BufferedImage mReading = mReadingCircles.get(new Integer(value));

		if (mReading == null) {
			Color c = new Color(value);
			mReading = createFadedCircleImage(10, c);
			mReadingCircles.put(new Integer(value), mReading);
		}

		int circleRadius = mReading.getWidth() / 2;
		Graphics2D g = (Graphics2D) mNetworkBlackWhite.getGraphics();

		g.setComposite(BlendComposite.Average.derive(0.1f));
		g.drawImage(mReading, null, p.x - circleRadius, p.y - circleRadius);
		mReadings++;
	}

	@Override
	public void paint(Graphics g) {

		// BufferedImage heatMap = mColorize.filter(mNetworkBlackWhite, null);
		// The only color difference b/w real and perceived is that perceived is
		// based on a base-white image and real is based on a base-black. I
		// should
		// create a filter that simply converts all completely white pixels into
		// completely black pixels

		/* Draw the image, applying an alpha filter */
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawImage(mNetworkBlackWhite, mRealpha, 0, 0);

	}

	public static BufferedImage createFadedCircleImage(int size,
			Color focusColor) {
		BufferedImage im = createCompatibleTranslucentImage(size, size);
		float radius = size / 2f;

		// RadialGradientPaint gradient = new RadialGradientPaint(radius,
		// radius,
		// radius, new float[] { 0f, 1f }, new Color[] { focusColor,
		// new Color(0xffffffff, true) });

		Graphics2D g = (Graphics2D) im.getGraphics();

		// g.setPaint(gradient);
		g.setColor(focusColor);
		g.fillRect(0, 0, size, size);

		return im;
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

	@Override
	public int getValue(int x, int y) {
		return mNetworkBlackWhite.getRGB(x, y);
	}

	@Override
	public Dimension getSize() {
		int w = mNetworkBlackWhite.getWidth();
		int h = mNetworkBlackWhite.getHeight();

		return new Dimension(w, h);
	}

	/**
	 * This is just a totally crap method that builds a copy of the perceived
	 * environment image, and removes every pixel that is not within the
	 * geographic polygons. Also, it converts every pixel that is white into a
	 * pixel that is black
	 */
	public BufferedImage generateForAccuracyCheck() {
		BufferedImage temp = createCompatibleTranslucentImage(
				mNetworkBlackWhite.getWidth(), mNetworkBlackWhite.getHeight());

		Graphics g = temp.getGraphics();
		g.drawImage(mNetworkBlackWhite, 0, 0, null);

		for (int x = 0; x < temp.getWidth(); x++)
			for (int y = 0; y < temp.getHeight(); y++) {

				if (isWhite(temp.getRGB(x, y))) {
					temp.setRGB(x, y, Color.BLACK.getRGB());
					continue;
				}

				if (false == mKmlGeography.contains(x, y))
					temp.setRGB(x, y, Color.BLACK.getRGB());

			}

		return temp;
	}

	/** Given a pixel, reports if it is white */
	public static boolean isWhite(int pixel) {
		int red = (pixel >> 16) & 0xff;
		int green = (pixel >> 8) & 0xff;
		int blue = (pixel) & 0xff;

		if (red == 255 && blue == 255 && green == 255)
			return true;

		return false;
	}
}
