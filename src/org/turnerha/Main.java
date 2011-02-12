package org.turnerha;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;

import org.turnerha.environment.MetricCalculator;
import org.turnerha.environment.impl.ImageBackedPerceivedEnvironment;
import org.turnerha.environment.impl.ImageBackedRealEnvironment;
import org.turnerha.geography.KmlGeography;
import org.turnerha.geography.KmlReader;
import org.turnerha.geography.Projection;
import org.turnerha.geography.ProjectionCartesian;
import org.turnerha.policys.collection.ConstantDataCollection;
import org.turnerha.policys.collection.DataCollectionPolicy;

public class Main {

	public static int hoursPerHeartbeat = 1;
	public static int rows = 1; // Do not change this unless you are sure
	public static int columns = 1; // you can share Smart-phones between models
	public static int phonesPerSlice = 1000;
	public static boolean DEBUG = false;

	ModelView mModelView;
	ImageBackedRealEnvironment mRealNetwork;
	ImageBackedPerceivedEnvironment mPerceivedNetwork;


	public Main(File geoFileNameKml, File networkFileName,
			double probabilityOfMoving, int mobilityInMeters,
			int timePerHeartbeat, float inputFrequency, boolean usingGPS) {
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

		// Create the metric calc
		MetricCalculator mc = new MetricCalculator();
		mc.setupCoverage(kmlGeography);

		new Log(mc);

		// Create real network
		ImageBackedRealEnvironment rn = new ImageBackedRealEnvironment(
				networkFileName, screen, 0.5f, kmlGeography);
		mRealNetwork = rn;
		mc.updateRealEnvironment(mRealNetwork);

		// Create perceived Environment
		ImageBackedPerceivedEnvironment pn = new ImageBackedPerceivedEnvironment(
				screen, kmlGeography, mc);
		mPerceivedNetwork = pn;
		mc.updatePerceivedEnvironment(mPerceivedNetwork);

		// calculateAccuracy(rn, pn, kmlGeography);

		// Create ModelFrontBuffer
		ModelProxy proxy = new ModelProxy(rows, columns);

		// Create a random hashset of real environments to try
		HashMap<Integer, File> mEnvironmentalMap = new HashMap<Integer, File>(2);
		//for (int i = 0; i < 9; i++) {
		//	mEnvironmentalMap.put(new Integer( (i+1) * 200), new File(
		//			"network-images/dynamic-network-images/" + i + ".png"));
		//}
		//mEnvironmentalMap.put(new Integer(500), new File("network-images/horizontal-gradient.png"));
		
		//mEnvironmentalMap.put(new Integer(8000), new File("network-images/network3.png"));
		
		ModelController controller = new ModelController(proxy, rows, columns,
				mc, mEnvironmentalMap, rn);

		Projection rando = new ProjectionCartesian(kmlGeography.getGeoBox(),
				screen);

		// Build Slices
		//DataCollectionPolicy policy = new MobilityBasedDataCollection(r);
		DataCollectionPolicy policy = new ConstantDataCollection();
		Slice[][] slices = new Slice[rows][columns];
		Random probOfMoving = new Random(r.nextLong());
		for (int row : Util.range(rows))
			for (int col : Util.range(columns)) {
				ArrayList<SmartPhone> slicePhones = new ArrayList<SmartPhone>();

				for (@SuppressWarnings("unused") int o : Util.range(phonesPerSlice)) {
					int x, y;
					do {
						x = r.nextInt(screen.width);
						y = r.nextInt(screen.height);
					} while (false == kmlGeography.contains(x, y));

					slicePhones.add(new SmartPhone(new Point(x, y), reader
							.getPoly(), pn, rn, probOfMoving.nextDouble(),
							inputFrequency, rando, policy, r));
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
		frame.setTitle("Empower");

		ModelView view = new ModelView(proxy, controller, kmlGeography, pn, mc);
		mModelView = view;
		view.setBounds(0, 0, screen.width, screen.height);
		frame.getRootPane().setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));

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

	public void writeOut(ImageBackedRealEnvironment mR,
			ImageBackedPerceivedEnvironment mP) {

		try {
			ImageIO.write(mR.renderFullImage(), "png", new File("real.png"));
			ImageIO.write(mP.renderFullImage(), "png", new File("perc.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}