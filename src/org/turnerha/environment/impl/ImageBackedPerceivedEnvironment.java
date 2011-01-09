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

	/* Create a rescale filter op that makes the image 50% opaque */
	float[] scales = { 1f, 1f, 1f, 0.5f };
	float[] offsets = new float[4];
	RescaleOp mRealpha = new RescaleOp(scales, offsets, null);

	private KmlGeography mKmlGeography;

	/**
	 * The projection between the pixel values in the backing image model data
	 * and real-world latitude longitude
	 */
	private ProjectionCartesian mProjection;

	private JFrame mDebugFrame;
	private ImagePanel mDebugImagePanel;

	private int mCoverageTotal = 0;
	private double mCoverageCurrent = 0;

	public ImageBackedPerceivedEnvironment(Dimension size, KmlGeography m) {

		mKmlGeography = m;
		mProjection = new ProjectionCartesian(m.getGeoBox(), size);

		mNetwork = createCompatibleTranslucentImage(size.width, size.height);

		if (Main.DEBUG) {
			mDebugFrame = new JFrame("Perceived black/white");
			mDebugImagePanel = new ImagePanel(mNetwork);
			mDebugFrame.getContentPane().add(mDebugImagePanel);
			mDebugFrame.setSize(800, 500);
			mDebugFrame.setVisible(true);
		}

		Rectangle geoSize = mKmlGeography.getPixelSize();

		for (int x = geoSize.x; x < (geoSize.width + geoSize.x); x++)
			for (int y = geoSize.y; y < (geoSize.height + geoSize.y); y++)
				if (mKmlGeography.contains(x, y))
					mCoverageTotal++;

	}

	@Override
	public void addReading(int value, GeoLocation loc) {

		Point p = mProjection.getPointAt(loc);

		BufferedImage mReading = mReadingCircles.get(new Integer(value));

		if (mReading == null) {
			Color c = new Color(value);
			mReading = EnvironUtils.createReadingImage(3, c);
			mReadingCircles.put(new Integer(value), mReading);
		}

		// Subtract current coverage of the 3x3 grid from the total coverage
		int[] alphas = new int[9];
		mNetwork.getAlphaRaster().getPixels(p.x - 1, p.y - 1, 3, 3, alphas);
		double localCoverage = 0;
		for (int alpha : alphas)
			localCoverage += (double) alpha / 255d;
		mCoverageCurrent -= localCoverage;

		Graphics2D g = (Graphics2D) mNetwork.getGraphics();
		g.setComposite(BlendComposite.AverageInclAlpha.derive(1f));
		g.drawImage(mReading, null, p.x - 1, p.y - 1);
		mReadings++;

		// Add back in the coverage
		alphas = new int[9];
		mNetwork.getAlphaRaster().getPixels(p.x - 1, p.y - 1, 3, 3, alphas);
		localCoverage = 0;
		for (int alpha : alphas)
			localCoverage += (double) alpha / 255d;
		mCoverageCurrent += localCoverage;

		System.out.println("Total Coverage is " + mCoverageCurrent
				/ (double) mCoverageTotal);

		if (Main.DEBUG)
			mDebugImagePanel.setImage(mNetwork);
	}

	@Override
	public void paintInto(Graphics g, Projection proj) {

		// BufferedImage heatMap = mColorize.filter(mNetwork, null);
		// The only color difference b/w real and perceived is that perceived is
		// based on a base-white image and real is based on a base-black. I
		// should
		// create a filter that simply converts all completely white pixels into
		// completely black pixels

		/* Draw the image, applying an alpha filter */
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawImage(mNetwork, mRealpha, 0, 0);

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
	public GeoBox getSize() {
		int w = mNetwork.getWidth();
		int h = mNetwork.getHeight();

		return mProjection.getGeoBoxOf(new Rectangle(w, h));
	}

	/**
	 * This is just a totally crap method that builds a copy of the perceived
	 * environment image, and removes every pixel that is not within the
	 * geographic polygons. Also, it converts every pixel that is white into a
	 * pixel that is black
	 */
	public BufferedImage generateForAccuracyCheck() {
		BufferedImage temp = createCompatibleTranslucentImage(mNetwork
				.getWidth(), mNetwork.getHeight());

		Graphics g = temp.getGraphics();
		g.drawImage(mNetwork, 0, 0, null);

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

	@Override
	public int getValueAt(GeoLocation location) {
		Point p = mProjection.getPointAt(location);
		return mNetwork.getRGB(p.x, p.y);
	}
}
