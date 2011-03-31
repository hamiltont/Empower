package org.turnerha;

import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.turnerha.environment.impl.ImageBackedPerceivedEnvironment;
import org.turnerha.environment.impl.ImageBackedRealEnvironment;
import org.turnerha.geography.KmlGeography;
import org.turnerha.geography.KmlReader;
import org.turnerha.geography.Projection;
import org.turnerha.geography.ProjectionCartesian;
import org.turnerha.policys.collection.ConstantDataCollection;
import org.turnerha.policys.collection.DataCollectionPolicy;
import org.turnerha.policys.movement.NodeMovementPolicy;
import org.turnerha.policys.movement.ProbabilisticMovementPolicy;
import org.turnerha.sensornodes.SensorNode;
import org.turnerha.sensornodes.SmartPhone;
import org.turnerha.server.Server;

public class Main {

	public static int hoursPerHeartbeat = 1;
	public static int rows = 1; // Do not change this unless you are sure
	public static int columns = 1; // you can share Smart-phones between models
	private int mPhoneCount = 1;
	public static boolean DEBUG = false;

	ModelView mModelView;
	ImageBackedRealEnvironment mRealNetwork;
	Server mServer;

	public Main(File geoFileNameKml, File networkFileName,
			double probabilityOfMoving, int mobilityInMeters,
			int timePerHeartbeat, float inputFrequency, boolean usingGPS) {
		Dimension screen = Util.getRenderingAreaSize();

		hoursPerHeartbeat = timePerHeartbeat;

		Random r = new Random();

		// Read in KML file, and create the KmlGeography
		KmlReader reader = new KmlReader(geoFileNameKml);
		Dimension foo = new Dimension(screen);
		foo.height -= 25; // TODO figure out why I need this??
		KmlGeography kmlGeography = KmlGeography.init(reader.getPoly(), foo,
				reader.mTopRight, reader.mBottomLeft);

		// Create the server, which internally creates the PerceivedEnvironment
		// and the MetricCalculator
		mServer = new Server();
		new Log(mServer.getMetricCalculator());

		// Create real network
		ImageBackedRealEnvironment rn = new ImageBackedRealEnvironment(
				networkFileName, screen, 0.5f, kmlGeography);
		mRealNetwork = rn;
		mServer.getMetricCalculator().updateRealEnvironment(mRealNetwork);

		// Create a random hashset of real environments to try
		HashMap<Integer, File> mEnvironmentalMap = new HashMap<Integer, File>(2);
		// for (int i = 0; i < 9; i++) {
		// mEnvironmentalMap.put(new Integer( (i+1) * 200), new File(
		// "network-images/dynamic-network-images/" + i + ".png"));
		// }
		mEnvironmentalMap.put(new Integer(1500), new File(
				"network-images/horizontal-gradient.png"));
		// mEnvironmentalMap.put(new Integer(8000), new
		// File("network-images/network3.png"));

		Projection rando = new ProjectionCartesian(kmlGeography.getGeoBox(),
				screen);

		// Build Slices
		// DataCollectionPolicy policy = new MobilityBasedDataCollection(r);
		DataCollectionPolicy policy = new ConstantDataCollection();
		//NodeMovementPolicy mPolicy = StaticNodeMovementPolicy.getInstance();
		NodeMovementPolicy mPolicy = new ProbabilisticMovementPolicy(0.5, r);

		Random generator = new Random();
		ArrayList<SensorNode> nodes = new ArrayList<SensorNode>(mPhoneCount);

		for (@SuppressWarnings("unused")
		int o : Util.range(mPhoneCount)) {
			int x, y;
			do {
				x = r.nextInt(screen.width);
				y = r.nextInt(screen.height);
			} while (false == kmlGeography.contains(x, y));

			nodes.add(new SmartPhone(new Point(x, y), reader.getPoly(),
					mServer, rn, rando, policy, mPolicy, generator));
		}

		// Build the model from the sensor nodes
		Model model = new Model(nodes);

		ModelController controller = new ModelController(mServer
				.getMetricCalculator(), mEnvironmentalMap, rn, model);

		// Add the overall keyboard listener
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.addKeyEventDispatcher(new MyKeyListener(controller));

		// Setup the UI and start the display
		JFrame frame = new JFrame();
		frame.setSize(screen);
		frame.setLayout(null);
		frame.setTitle("Empower");

		ModelView view = new ModelView(controller, kmlGeography, mRealNetwork,
				mServer.getMetricCalculator(), model, rando);
		mModelView = view;
		view.setBounds(0, 0, screen.width, screen.height);

		frame.add(view);
		frame.validate();
		frame.setVisible(true);

		controller.start(view);
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
				mModelView.setDisplayNetwork(mServer.getPerceivedEnvironment());
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
				writeOut(mRealNetwork,
						(ImageBackedPerceivedEnvironment) mServer
								.getPerceivedEnvironment());
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