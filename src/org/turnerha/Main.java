package org.turnerha;

import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.turnerha.environment.MetricCalculator;
import org.turnerha.environment.impl.ImageBackedPerceivedEnvironment;
import org.turnerha.environment.impl.ImageBackedRealEnvironment;
import org.turnerha.environment.utils.EnvironUtils;
import org.turnerha.geography.KmlGeography;
import org.turnerha.geography.KmlReader;
import org.turnerha.geography.Projection;
import org.turnerha.geography.ProjectionCartesian;

public class Main {

	public static int hoursPerHeartbeat = 1;
	public static int rows = 1;    // Do not change this unless you are sure
	public static int columns = 1; // you can share Smart-phones between models
	public static int phonesPerSlice = 500;
	public static boolean DEBUG = false;

	ModelView mModelView;
	ImageBackedRealEnvironment mRealNetwork;
	ImageBackedPerceivedEnvironment mPerceivedNetwork;

	private KmlGeography mKmlGeography;

	public Main(File geoFileNameKml, File networkFileName,
			float moveTendenancy, int mobilityInMeters, int timePerHeartbeat,
			float inputFrequency, boolean usingGPS) {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		screen.height -= 20;

		hoursPerHeartbeat = timePerHeartbeat;

		Random r = new Random();

		// Read in KML and create map - it has no dependencies
		KmlReader reader = new KmlReader(geoFileNameKml);
		Dimension foo = new Dimension(screen);
		foo.height -= 25;
		KmlGeography kmlGeography = new KmlGeography(reader.getPoly(), foo,
				reader.mTopRight, reader.mBottomLeft);
		mKmlGeography = kmlGeography;

		// Create the metric calc
		MetricCalculator mc = new MetricCalculator();
		mc.setupCoverage(kmlGeography);
		
		new Log(mc);

		// Create real network
		BufferedImage colorScheme = EnvironUtils
				.createGradientImage(null, null);
		ImageBackedRealEnvironment rn = new ImageBackedRealEnvironment(
				networkFileName, screen, colorScheme, 0.5f, kmlGeography);
		mRealNetwork = rn;
		mc.updateRealEnvironment(mRealNetwork);
		mc.setupAccuracy(kmlGeography, rn);

		// Create perceived Environment
		ImageBackedPerceivedEnvironment pn = new ImageBackedPerceivedEnvironment(
				screen, kmlGeography, mc);
		mPerceivedNetwork = pn;
		mc.updatePerceivedEnvironment(mPerceivedNetwork);

		// calculateAccuracy(rn, pn, kmlGeography);

		// Create ModelFrontBuffer
		ModelProxy proxy = new ModelProxy(rows, columns);

		// Create ModelBackBuffer
		ModelController controller = new ModelController(proxy, rows, columns,
				mc);

		Projection rando = new ProjectionCartesian(kmlGeography.getGeoBox(),
				screen);

		// Build Slices
		Slice[][] slices = new Slice[rows][columns];
		for (int row : Util.range(rows))
			for (int col : Util.range(columns)) {
				ArrayList<SmartPhone> slicePhones = new ArrayList<SmartPhone>();

				for (int o : Util.range(phonesPerSlice)) {
					int x, y;
					do {
						x = r.nextInt(screen.width);
						y = r.nextInt(screen.height);
					} while (false == kmlGeography.contains(x, y));

					slicePhones.add(new SmartPhone(new Point(x, y), reader
							.getPoly(), pn, rn, moveTendenancy, inputFrequency,
							rando));
				}

				Slice s = new Slice(slicePhones, controller, row, col);
				s.start();
			}

		// Add Slices to front buffer for first read
		proxy.swapModel(slices);

		// Add the overall keyboard listener
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.addKeyEventDispatcher(new MyKeyListener(controller));

		// Setup the UI and start the display
		JFrame frame = new JFrame();
		frame.setSize(screen);
		frame.setLayout(null);

		ModelView view = new ModelView(proxy, controller, kmlGeography, pn, rn);
		mModelView = view;
		view.setBounds(0, 0, screen.width, screen.height);

		frame.add(view);
		frame.validate();
		frame.setVisible(true);
	}

	private class MyKeyListener implements KeyEventDispatcher {
		private ModelController mc;

		private static final int speedFactor = 1;

		public MyKeyListener(ModelController controller) {
			mc = controller;
		}

		@Override
		public boolean dispatchKeyEvent(KeyEvent e) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_PLUS:
			case KeyEvent.VK_EQUALS:
				// Plus means speed system up, which means decrease sleep time
				if (mc.sleepTime.get() <= 0)
					return true;

				mc.sleepTime.addAndGet(speedFactor * -1);
				return true;

			case KeyEvent.VK_MINUS:
			case KeyEvent.VK_UNDERSCORE:
				// Plus means slow system down, which means increase sleep time

				mc.sleepTime.addAndGet(speedFactor);
				return true;

			case KeyEvent.VK_P:
				// Means switch to perceived network
				mModelView.setDisplayNetwork(mPerceivedNetwork);
				return true;

			case KeyEvent.VK_R:
				// Means switch to real network
				mModelView.setDisplayNetwork(mRealNetwork);
				return true;

			case KeyEvent.VK_ESCAPE:
				// Means quit
				Log.close();
				System.exit(0);

			case KeyEvent.VK_A:
				// Calculate accuracy and write out
				// calculateAccuracy(mRealNetwork, mPerceivedNetwork,
				// mKmlGeography);
				return true;

			case KeyEvent.VK_O:
				writeOut(mRealNetwork, mPerceivedNetwork);
				return true;

			}

			return false;
		}

	}

	private static double maxDifference = 0;

	/*
	 * private static void calculateAccuracy(ImageBackedRealEnvironment re,
	 * ImageBackedPerceivedEnvironment pe, KmlGeography kml) { Dimension size =
	 * re.getSize();
	 * 
	 * // Change all white pixels to black BufferedImage colorCorrectedEnviron =
	 * pe.generateForAccuracyCheck();
	 * 
	 * // Tally up the differences b/w real and perceived double
	 * currentDifference = 0; for (int x = 0; x < size.width; x++) for (int y =
	 * 0; y < size.height; y++) {
	 * 
	 * if (false == kml.contains(x, y)) continue;
	 * 
	 * int r = re.getValue(x, y); int p = colorCorrectedEnviron.getRGB(x, y);
	 * currentDifference += findDistance(r, p); }
	 * System.out.println("Perceived Difference is " + currentDifference);
	 * 
	 * // Find the maximum difference if (maxDifference == 0) maxDifference =
	 * currentDifference; System.out.println("Max Difference is " +
	 * maxDifference);
	 * 
	 * // Find the total accuracy by calculating the amount that is correct //
	 * over the total amount double accuracy = (maxDifference -
	 * currentDifference) / maxDifference; System.out.println("Accuracy is " +
	 * accuracy); }
	 */

	private static double findDistance(int pixel, int pixel1) {
		int red = (pixel >> 16) & 0xff;
		int green = (pixel >> 8) & 0xff;
		int blue = (pixel) & 0xff;

		int red1 = (pixel1 >> 16) & 0xff;
		int green1 = (pixel1 >> 8) & 0xff;
		int blue1 = (pixel1) & 0xff;

		double sum = Math.pow(red - red1, 2);
		sum += Math.pow(green - green1, 2);
		sum += Math.pow(blue - blue1, 2);

		sum = Math.sqrt(sum);

		return sum;
	}

	public void writeOut(ImageBackedRealEnvironment mR,
			ImageBackedPerceivedEnvironment mP) {

		try {
			ImageIO.write(mR.renderFullImage(), "png", new File("real.png"));
			ImageIO.write(mP.renderFullImage(), "png", new File("perc.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String ArgbToString(int pixel) {
		StringBuilder sb = new StringBuilder("[");
		int alpha = (pixel >> 24) & 0xff;
		int red = (pixel >> 16) & 0xff;
		int green = (pixel >> 8) & 0xff;
		int blue = (pixel) & 0xff;

		sb.append(alpha).append(',').append(red).append(',').append(green)
				.append(',').append(blue).append(',').append(pixel).append(']');
		return sb.toString();
	}
}