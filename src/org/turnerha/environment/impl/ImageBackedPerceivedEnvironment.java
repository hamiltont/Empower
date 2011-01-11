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
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JFrame;

import org.turnerha.Main;
import org.turnerha.environment.MetricCalculator;
import org.turnerha.environment.PerceivedEnvironment;
import org.turnerha.environment.RealEnvironment;
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

	// private double mMaximumAccuracyDifference = 0;
	// private double mCurrentAccuracyDifference = 0;

	private RealEnvironment mRealEnvironment;

	private FileWriter mLog = null;

	private MetricCalculator mMetricCalc;

	public ImageBackedPerceivedEnvironment(Dimension size, KmlGeography m,
			RealEnvironment re, MetricCalculator mc) {

		mKmlGeography = m;
		mProjection = new ProjectionCartesian(m.getGeoBox(), size);
		mRealEnvironment = re;
		mMetricCalc = mc;

		mNetwork = createCompatibleTranslucentImage(size.width, size.height);

		if (Main.DEBUG) {
			try {
				mLog = new FileWriter("myfile.csv");
			} catch (IOException e) {
				e.printStackTrace();
			}

			mDebugFrame = new JFrame("Perceived black/white");
			mDebugImagePanel = new ImagePanel(mNetwork);
			mDebugFrame.getContentPane().add(mDebugImagePanel);
			mDebugFrame.setSize(800, 500);
			mDebugFrame.setVisible(true);
			/*
			 * try { mLog.append("worst,current,accuracy,coverage\n");
			 * mLog.append(Double.toString(mMaximumAccuracyDifference))
			 * .append(',').append( Double.toString(mCurrentAccuracyDifference))
			 * .append(','); double acc = mCurrentAccuracyDifference /
			 * mMaximumAccuracyDifference;
			 * mLog.append(Double.toString(acc)).append(',').append(
			 * Double.toString(0d)).append('\n'); } catch (IOException e) {
			 * e.printStackTrace(); }
			 */
		}

		/*
		 * // Determine the absolute worst accuracy value Rectangle geoSize =
		 * mKmlGeography.getPixelSize(); ImageBackedRealEnvironment ibre =
		 * (ImageBackedRealEnvironment) re; for (int x = geoSize.x; x <
		 * (geoSize.width + geoSize.x); x++) for (int y = geoSize.y; y <
		 * (geoSize.height + geoSize.y); y++) if (mKmlGeography.contains(x, y))
		 * { int pixel = ibre.getValueAt(x, y); int red = (pixel >> 16) & 0xff;
		 * int green = (pixel >> 8) & 0xff; int blue = (pixel) & 0xff;
		 * 
		 * int diff = Math.max(Math.abs(red - 0), Math.abs(red - 255)); diff +=
		 * Math .max(Math.abs(green - 0), Math.abs(green - 255)); diff +=
		 * Math.max(Math.abs(blue - 0), Math.abs(blue - 255));
		 * 
		 * mMaximumAccuracyDifference += diff;
		 * 
		 * }
		 */
	}

	@Override
	public void addReading(int value, GeoLocation loc) {

		Point p = mProjection.getPointAt(loc);

		// Subtract current coverage from the total coverage
		int[] alphaa = new int[1];
		mNetwork.getAlphaRaster().getPixel(p.x, p.y, alphaa);
		double localCoverage = (double) alphaa[0] / 255d;
		mMetricCalc.changeCurrentCoverage(localCoverage * -1d);

		// Subtract the current accuracy difference (we are going to assume this
		// pixel is perfect, and then add back in the imperfection once we know)
		{
			int percPixel = mNetwork.getRGB(p.x, p.y);
			int realPixel = ((ImageBackedRealEnvironment) mRealEnvironment)
					.getValueAt(p.x, p.y);

			int pRed = (percPixel >> 16) & 0xff;
			int pGreen = (percPixel >> 8) & 0xff;
			int pBlue = (percPixel) & 0xff;

			int rRed = (realPixel >> 16) & 0xff;
			int rGreen = (realPixel >> 8) & 0xff;
			int rBlue = (realPixel) & 0xff;

			// How different the real and perceived differ
			double sumOfCurrentPixelDifferences = Math.abs(pRed - rRed);
			sumOfCurrentPixelDifferences += Math.abs(pBlue - rBlue);
			sumOfCurrentPixelDifferences += Math.abs(pGreen - rGreen);

			// The maximum worst difference we could have
			double sumOfWorstPixelDifferences = Math.max(Math.abs(rRed - 0),
					Math.abs(rRed - 255));
			sumOfWorstPixelDifferences += Math.max(Math.abs(rGreen - 0), Math
					.abs(rGreen - 255));
			sumOfWorstPixelDifferences += Math.max(Math.abs(rBlue - 0), Math
					.abs(rBlue - 255));

			// The alpha determines how much we 'trust' the current perceived
			// pixel.
			// If we don't trust it at all, then the current accuracy estimate
			// assumes the worst for this pixel. Therefore, we have to subtract
			// the worst. If we trust it completely, then the current accuracy
			// estimate includes the difference of this pixel and the real
			// network with 0 worst-case assumption, so we have to entirely
			// remove this pixel difference. Following this logic, the alpha is
			// used to weight both the worst and current pixel differences so
			// that we remove the correct amount of each from the estimate
			int alpha = (percPixel >> 24) & 0xff;
			double weight = (double) alpha / 255d;
			// weight == closeness to 1 e.g. full trust
			sumOfCurrentPixelDifferences *= weight;
			// (1-weight) == closeness to 0 e.g. no trust
			sumOfWorstPixelDifferences *= (1d - weight);

			double amountToRemove = sumOfCurrentPixelDifferences
					+ sumOfWorstPixelDifferences;
			amountToRemove *= -1d;
			mMetricCalc.changeCurrentAccuracy(amountToRemove);
		}

		Graphics2D g = (Graphics2D) mNetwork.getGraphics();
		g.setColor(new Color(value));
		g.drawLine(p.x, p.y, p.x, p.y);
		mReadings++;

		// Add back in the coverage
		alphaa = new int[1];
		mNetwork.getAlphaRaster().getPixel(p.x, p.y, alphaa);
		localCoverage = (double) alphaa[0] / 255d;
		mMetricCalc.changeCurrentCoverage(localCoverage);

		// Add back in the difference in accuracy
		{
			int x = p.x;
			int y = p.y;
			int percPixel = mNetwork.getRGB(x, y);
			int realPixel = ((ImageBackedRealEnvironment) mRealEnvironment)
					.getValueAt(x, y);

			int pRed = (percPixel >> 16) & 0xff;
			int pGreen = (percPixel >> 8) & 0xff;
			int pBlue = (percPixel) & 0xff;

			int rRed = (realPixel >> 16) & 0xff;
			int rGreen = (realPixel >> 8) & 0xff;
			int rBlue = (realPixel) & 0xff;

			// How different the real and perceived differ
			double sumOfCurrentPixelDifferences = Math.abs(pRed - rRed);
			sumOfCurrentPixelDifferences += Math.abs(pBlue - rBlue);
			sumOfCurrentPixelDifferences += Math.abs(pGreen - rGreen);

			// The maximum worst difference we could have
			double sumOfWorstPixelDifferences = Math.max(Math.abs(rRed - 0),
					Math.abs(rRed - 255));
			sumOfWorstPixelDifferences += Math.max(Math.abs(rGreen - 0), Math
					.abs(rGreen - 255));
			sumOfWorstPixelDifferences += Math.max(Math.abs(rBlue - 0), Math
					.abs(rBlue - 255));

			// The importance of alpha is explained above
			int alpha = (percPixel >> 24) & 0xff;
			double weight = (double) alpha / 255d;
			sumOfCurrentPixelDifferences *= weight;
			sumOfWorstPixelDifferences *= (1d - weight);

			double amountToAdd = sumOfCurrentPixelDifferences
					+ sumOfWorstPixelDifferences;
			mMetricCalc.changeCurrentAccuracy(amountToAdd);
		}
		
		// double accuracyDifference = mCurrentAccuracyDifference
		// / mMaximumAccuracyDifference;
		// System.out.println("Coverage, Accuracy, Accuracy Diff: "
		// + mCoverageCurrent / mCoverageTotal + ", " + accuracyDifference
		// + ", " + mCurrentAccuracyDifference);
		// System.out.println("Coverage: " + mMetricCalc.getCoverage());
		System.out.println("Accuracy: " + mMetricCalc.getAccuracy());

		if (Main.DEBUG) {
			mDebugImagePanel.setImage(mNetwork);

			try {
				/*
				 * mLog.append(Double.toString(mMaximumAccuracyDifference))
				 * .append(',').append(
				 * Double.toString(mCurrentAccuracyDifference)) .append(',')
				 * .append(Double.toString(accuracyDifference))
				 * .append(',').append(
				 * Double.toString(mMetricCalc.getCoverage())) .append('\n');
				 */mLog.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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

	@Override
	public BufferedImage renderFullImage() {
		return mNetwork;
	}
}
