package org.turnerha;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyEventDispatcher;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeriesCollection;
import org.turnerha.environment.impl.ImageBackedPerceivedEnvironment;
import org.turnerha.environment.impl.ImageBackedRealEnvironment;
import org.turnerha.misc.PausableTimeSeries;
import org.turnerha.server.Server;

public class Main {

	public static int hoursPerHeartbeat = 1;
	protected static Integer DEFAULT_PHONE_COUNT = 100;
	public static boolean DEBUG = false;

	ModelView mModelView;
	ImageBackedRealEnvironment mRealNetwork;
	Server mServer;

	// GUI elements that need to be updated
	static JLabel sHours;
	static PausableTimeSeries sAccuracy;
	static PausableTimeSeries sCoverage;

	// GUI elements that need to be read from
	static int desiredNodeCount = DEFAULT_PHONE_COUNT;
	static int policy_Collection_constant = 100;
	static int policy_Collection_mobile = 0;
	static int policy_Collection_random = 0;

	// Useful for allowing dialog parent to be set properly
	static JFrame sFrame;

	// Useful for enabling/disabling components
	// This list contains components that should only be enabled when the
	// simulation is paused
	static List<JComponent> enabledWhenPaused = new ArrayList<JComponent>(30);

	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				createAndShowGUI();
			}

		});
		/*
		 * Dimension screen = Util.getRenderingAreaSize();
		 * 
		 * Random r = new Random();
		 * 
		 * // Read in KML file, and create the KmlGeography KmlReader reader =
		 * new KmlReader(geoFileNameKml); Dimension foo = new Dimension(screen);
		 * foo.height -= 25; // TODO figure out why I need this?? KmlGeography
		 * kmlGeography = KmlGeography.init(reader.getPoly(), foo,
		 * reader.mTopRight, reader.mBottomLeft);
		 * 
		 * // Create the server, which internally creates the
		 * PerceivedEnvironment // and the MetricCalculator mServer = new
		 * Server(); new Log(mServer.getMetricCalculator());
		 * 
		 * // Create real network ImageBackedRealEnvironment rn = new
		 * ImageBackedRealEnvironment( networkFileName, screen, 0.5f,
		 * kmlGeography); mRealNetwork = rn;
		 * mServer.getMetricCalculator().updateRealEnvironment(mRealNetwork);
		 * 
		 * // Create a random hashset of real environments to try
		 * HashMap<Integer, File> mEnvironmentalMap = new HashMap<Integer,
		 * File>(2); // for (int i = 0; i < 9; i++) { //
		 * mEnvironmentalMap.put(new Integer( (i+1) * 200), new File( //
		 * "network-images/dynamic-network-images/" + i + ".png")); // }
		 * mEnvironmentalMap.put(new Integer(1500), new File(
		 * "network-images/horizontal-gradient.png")); //
		 * mEnvironmentalMap.put(new Integer(8000), new //
		 * File("network-images/network3.png"));
		 * 
		 * Projection rando = new ProjectionCartesian(kmlGeography.getGeoBox(),
		 * screen);
		 * 
		 * // Build Slices // DataCollectionPolicy policy = new
		 * MobilityBasedDataCollection(r); DataCollectionPolicy policy = new
		 * ConstantDataCollection(); // NodeMovementPolicy mPolicy =
		 * StaticNodeMovementPolicy.getInstance(); NodeMovementPolicy mPolicy =
		 * new ProbabilisticMovementPolicy(0.5, r);
		 * 
		 * Random generator = new Random(); ArrayList<SensorNode> nodes = new
		 * ArrayList<SensorNode>(mPhoneCount);
		 * 
		 * for (@SuppressWarnings("unused") int o : Util.range(mPhoneCount)) {
		 * int x, y; do { x = r.nextInt(screen.width); y =
		 * r.nextInt(screen.height); } while (false == kmlGeography.contains(x,
		 * y));
		 * 
		 * nodes.add(new DefaultSensorNode(new Point(x, y), reader.getPoly(), mServer,
		 * rn, rando, policy, mPolicy, generator)); }
		 * 
		 * // Build the model from the sensor nodes Model model = new
		 * Model(nodes);
		 * 
		 * ModelController controller = new ModelController(mServer
		 * .getMetricCalculator(), mEnvironmentalMap, rn, model);
		 * 
		 * // Add the overall keyboard listener
		 * KeyboardFocusManager.getCurrentKeyboardFocusManager()
		 * .addKeyEventDispatcher(new MyKeyListener(controller));
		 * 
		 * // Setup the UI and start the display JFrame frame = new JFrame();
		 * frame.setSize(screen); frame.setLayout(null);
		 * frame.setTitle("Empower");
		 * 
		 * ModelView view = new ModelView(controller, kmlGeography,
		 * mRealNetwork, mServer.getMetricCalculator(), model, rando);
		 * mModelView = view; view.setBounds(0, 0, screen.width, screen.height);
		 * 
		 * frame.add(view); frame.validate(); frame.setVisible(true);
		 * 
		 * controller.start(view);
		 */
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

			case KeyEvent.VK_P:
				// Means switch to perceived network
				mModelView.setDisplayNetwork(true);
				return true;

			case KeyEvent.VK_R:
				// Means switch to real network
				mModelView.setDisplayNetwork(false);
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

	private static void createAndShowGUI() {
		JFrame frame = new JFrame("Empower Simulation Environment");
		sFrame = frame;
		// TODO popup an 'are you sure' if a simulation is running
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		if (false == frame.getContentPane().getLayout() instanceof BorderLayout)
			frame.getContentPane().setLayout(new BorderLayout());

		Dimension fullScreen = Toolkit.getDefaultToolkit().getScreenSize();
		Container contentPane = frame.getContentPane();
		contentPane.setPreferredSize(fullScreen);

		JPanel left = createLeft();
		left.setPreferredSize(new Dimension((int) Math.max(
				fullScreen.width * 0.35, 300), Short.MAX_VALUE));
		left
				.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2,
						Color.BLACK));
		contentPane.add(left, BorderLayout.LINE_START);

		contentPane.add(createCenter(), BorderLayout.CENTER);

		frame.pack();
		frame.setVisible(true);
	}

	private static JPanel createCenter() {
		Dimension fullScreen = Toolkit.getDefaultToolkit().getScreenSize();
		JPanel center = new JPanel(new BorderLayout());

		JPanel bottom = createBottom();
		bottom.setPreferredSize(new Dimension(Short.MAX_VALUE, (int) Math.max(
				fullScreen.height * 0.40, 300)));
		center.add(bottom, BorderLayout.PAGE_END);

		ModelView mv = ModelView.getInstance();
		// mv.setPreferredSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		mv.setPreferredSize(new Dimension(100, 100));
		mv.setBorder(BorderFactory.createLineBorder(Color.RED, 4));
		mv.setBackground(Color.GRAY);
		center.add(mv, BorderLayout.CENTER);

		return center;
	}

	private static JPanel createBottom() {

		JPanel wrapper = new JPanel(new BorderLayout());

		JPanel controls = createBottom_controls();
		wrapper.add(controls, BorderLayout.PAGE_START);

		final JTabbedPane metrics = new JTabbedPane();
		metrics.addTab("Overview", new JPanel());
		metrics.addTab("Accuracy", createBottom_accuracy());
		metrics.addTab("Coverage", createBottom_coverage());
		metrics.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0,
				Color.BLACK));
		wrapper.add(metrics, BorderLayout.CENTER);

		metrics.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				String title = metrics.getTitleAt(metrics.getSelectedIndex());
				if (title.equals("Accuracy")) {
					sAccuracy.setPaused(false);
					sCoverage.setPaused(true);
				} else if (title.equals("Overview")) {
					sAccuracy.setPaused(true);
					sCoverage.setPaused(true);
				} else if (title.equals("Coverage")) {
					sAccuracy.setPaused(true);
					sCoverage.setPaused(false);
				}

			}
		});

		return wrapper;
	}

	private static ChartPanel createBottom_accuracy() {

		sAccuracy = new PausableTimeSeries("");

		TimeSeriesCollection dataset = new TimeSeriesCollection(sAccuracy);

		JFreeChart c = ChartFactory.createTimeSeriesChart("Accuracy", "",
				"Percent", dataset, false, false, false);
		c.setBackgroundPaint(new JPanel(false).getBackground());
		ChartPanel cp = new ChartPanel(c);
		return cp;
	}

	private static ChartPanel createBottom_coverage() {

		sCoverage = new PausableTimeSeries("");

		TimeSeriesCollection dataset = new TimeSeriesCollection(sCoverage);

		JFreeChart c = ChartFactory.createTimeSeriesChart("Coverage", "",
				"Percent", dataset, false, false, false);
		c.setBackgroundPaint(new JPanel(false).getBackground());
		ChartPanel cp = new ChartPanel(c);
		return cp;
	}

	private static JPanel createBottom_controls() {
		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.LINE_AXIS));
		controls.setBackground(Color.GRAY);
		controls.setPreferredSize(new Dimension(Short.MAX_VALUE, 20));

		controls.add(Box.createHorizontalGlue());

		Dimension preferred = new Dimension(20, Short.MAX_VALUE);

		JButton r = new JButton(">");
		r.setAlignmentX(Component.RIGHT_ALIGNMENT);
		r.setPreferredSize(preferred);
		controls.add(r);

		JButton l = new JButton("<");
		l.setAlignmentX(Component.RIGHT_ALIGNMENT);
		l.setPreferredSize(preferred);
		controls.add(l);

		JButton u = new JButton("^");
		u.setAlignmentX(Component.RIGHT_ALIGNMENT);
		u.setPreferredSize(preferred);
		controls.add(u);

		JButton d = new JButton("v");
		d.setAlignmentX(Component.RIGHT_ALIGNMENT);
		d.setPreferredSize(preferred);
		controls.add(d);

		controls.add(Box.createHorizontalStrut(10));

		JButton in = new JButton("+");
		in.setAlignmentX(Component.RIGHT_ALIGNMENT);
		in.setPreferredSize(preferred);
		controls.add(in);

		JButton out = new JButton("-");
		out.setAlignmentX(Component.RIGHT_ALIGNMENT);
		out.setPreferredSize(preferred);
		controls.add(out);

		controls.add(Box.createHorizontalStrut(15));

		return controls;
	}

	private static JPanel createLeft() {
		JPanel left = new JPanel();
		left.setLayout(new BoxLayout(left, BoxLayout.PAGE_AXIS));

		JPanel params = createParams();
		params.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEmptyBorder(), "Parameters", TitledBorder.LEADING,
				TitledBorder.ABOVE_TOP, new Font(null, Font.BOLD, 20)));
		params.setMaximumSize(new Dimension(Short.MAX_VALUE, params
				.getPreferredSize().height));
		left.add(params);

		JPanel policy = createPolicy();
		policy.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEmptyBorder(), "Policies", TitledBorder.LEADING,
				TitledBorder.ABOVE_TOP, new Font(null, Font.BOLD, 20)));
		policy.setMaximumSize(new Dimension(Short.MAX_VALUE, policy
				.getPreferredSize().height));
		left.add(policy);

		left.add(Box.createVerticalGlue());

		JPanel control = createControl();
		control.setMaximumSize(new Dimension(Short.MAX_VALUE, control
				.getPreferredSize().height));
		left.add(control);

		return left;
	}

	private static JPanel createParams() {
		JPanel params = new JPanel();
		params.setLayout(new BoxLayout(params, BoxLayout.PAGE_AXIS));

		params.add(createParam_kml());

		params.add(createParam_nodeCount());

		params.add(createParam_realNW());

		return params;

	}

	private static JPanel createParam_nodeCount() {
		JPanel nc = new JPanel();
		nc.setLayout(new BoxLayout(nc, BoxLayout.LINE_AXIS));

		nc.add(Box.createHorizontalStrut(30));
		JLabel lab = new JLabel("Number of Sensor Nodes: ");
		nc.add(lab);

		nc.add(Box.createHorizontalGlue());
		final JTextField num = new JTextField(DEFAULT_PHONE_COUNT.toString());
		num.setColumns(10);
		num.setMaximumSize(num.getPreferredSize());
		num.addCaretListener(new CaretListener() {

			@Override
			public void caretUpdate(CaretEvent e) {
				String val = num.getText();
				try {
					int i = Integer.parseInt(val);
					desiredNodeCount = i;
				} catch (NumberFormatException nfe) {
					// TODO show error dialog
				}
			}
		});
		nc.add(num);
		nc.add(Box.createHorizontalStrut(30));
		enabledWhenPaused.add(num);

		return nc;
	}

	private static JPanel createParam_kml() {
		JPanel kml_p = new JPanel();
		kml_p.setLayout(new BoxLayout(kml_p, BoxLayout.LINE_AXIS));

		kml_p.add(Box.createHorizontalStrut(30));

		JLabel kml = new JLabel("KML: ");
		kml_p.add(kml);

		final JLabel filename = new JLabel("No file selected");
		filename.setForeground(Color.RED);
		kml_p.add(filename);

		kml_p.add(Box.createHorizontalGlue());

		JButton choose = new JButton("Edit");
		choose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileFilter() {

					@Override
					public String getDescription() {
						return "*.kml - XML grammar for geographic data";
					}

					@Override
					public boolean accept(File f) {
						if (f.getName().endsWith(".kml"))
							return true;
						return false;
					}
				});

				int result = fc.showOpenDialog(sFrame);

				if (result == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					Model.getInstance()
							.setKml(Model.getInstance().getGeo(file));
					filename.setText(file.getName());
					filename.setForeground(Color.BLACK);
					filename.invalidate();
					// TODO add an else and show an error dialog
				} // TODO add a check for error and show an error dialog
			}
		});
		kml_p.add(choose);
		enabledWhenPaused.add(choose);

		kml_p.add(Box.createHorizontalStrut(30));

		return kml_p;
	}

	private static JPanel createParam_realNW() {
		JPanel kml_p = new JPanel();
		kml_p.setLayout(new BoxLayout(kml_p, BoxLayout.LINE_AXIS));

		kml_p.add(Box.createHorizontalStrut(30));

		JLabel kml = new JLabel("Real Environ: ");
		kml_p.add(kml);

		final JLabel filename = new JLabel("No file selected");
		filename.setForeground(Color.RED);
		kml_p.add(filename);

		kml_p.add(Box.createHorizontalGlue());

		JButton choose = new JButton("Edit");
		choose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileFilter() {

					@Override
					public String getDescription() {
						return "*.png - PNG Image File";
					}

					@Override
					public boolean accept(File f) {
						if (f.getName().endsWith(".png"))
							return true;
						return false;
					}
				});

				int result = fc.showOpenDialog(sFrame);

				if (result == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					ImageBackedRealEnvironment i = new ImageBackedRealEnvironment(
							file, ModelView.getInstance().getRenderingArea(),
							(float) 0.5, Model.getInstance().getKml());
					Model.getInstance().setRealEnvironment(i);
					filename.setText(file.getName());
					filename.setForeground(Color.BLACK);
					filename.invalidate();

					// TODO add an else and show an error dialog
				} // TODO add a check for error and show an error dialog
			}
		});
		kml_p.add(choose);
		enabledWhenPaused.add(choose);

		kml_p.add(Box.createHorizontalStrut(30));

		return kml_p;
	}

	private static JPanel createPolicy() {
		JPanel policy = new JPanel();
		policy.setLayout(new BoxLayout(policy, BoxLayout.PAGE_AXIS));

		int left_padding = 10;
		int right_padding = 10;

		// TODO add all policies and add an 'edit' button to each
		// TODO none of these have a minimum size set, which is ok as long as
		// there is enough room in the left sidebar for all of the preferred
		// sizes to fit. However, if we ever reach a scenario where they dont,
		// then we will either need a scrollbar on the right, or we will need to
		// set a minimum size on the components controlled by the boxlayout. The
		// jpanel children of this JPanel also use boxlayouts, so the same todo
		// applies to them
		JPanel movement = new JPanel();
		Border mBorder = BorderFactory.createCompoundBorder(BorderFactory
				.createTitledBorder(BorderFactory.createEtchedBorder(),
						"Node Movement"), BorderFactory.createEmptyBorder(0,
				left_padding, 0, right_padding));
		movement.setBorder(mBorder);
		movement.setMaximumSize(new Dimension(Short.MAX_VALUE, movement
				.getPreferredSize().height));
		movement.setAlignmentX(Component.CENTER_ALIGNMENT);
		policy.add(movement);

		JPanel collection = createPolicy_collection();
		Border cBorder = BorderFactory.createCompoundBorder(BorderFactory
				.createTitledBorder(BorderFactory.createEtchedBorder(),
						"Data Collection"), BorderFactory.createEmptyBorder(0,
				left_padding, 0, right_padding));
		collection.setBorder(cBorder);
		collection.setMaximumSize(new Dimension(Short.MAX_VALUE, collection
				.getPreferredSize().height));
		collection.setAlignmentX(Component.CENTER_ALIGNMENT);
		policy.add(collection);

		JPanel reporting = new JPanel();
		Border rBorder = BorderFactory.createCompoundBorder(BorderFactory
				.createTitledBorder(BorderFactory.createEtchedBorder(),
						"Data Reporting"), BorderFactory.createEmptyBorder(0,
				left_padding, 0, right_padding));
		reporting.setBorder(rBorder);
		reporting.setMaximumSize(new Dimension(Short.MAX_VALUE, reporting
				.getPreferredSize().height));
		reporting.setAlignmentX(Component.CENTER_ALIGNMENT);
		policy.add(reporting);

		return policy;
	}

	private static JPanel createPolicy_collection() {
		JPanel coll = new JPanel();
		coll.setLayout(new BoxLayout(coll, BoxLayout.PAGE_AXIS));

		JButton constDesc = null;
		JButton constConfig = null;
		JPanel constant = createPolicy_collectionOnePolicy("Constant",
				constDesc, constConfig);
		coll.add(constant);

		JButton mobilDesc = null;
		JButton mobilConfig = null;
		JPanel mobile = createPolicy_collectionOnePolicy("Mobility", mobilDesc,
				mobilConfig);

		coll.add(mobile);

		JButton randDesc = null;
		JButton randConfig = null;
		JPanel random = createPolicy_collectionOnePolicy("Probabilistic",
				randDesc, randConfig);

		coll.add(random);

		return coll;
	}

	private static CaretListener collection_policyCaretListener = new CaretListener() {

		@Override
		public void caretUpdate(CaretEvent e) {
			JTextField src = (JTextField) e.getSource();

			if (src.getText().length() == 0)
				return;

			int val;
			try {
				val = Integer.parseInt(src.getText().trim());
			} catch (NumberFormatException nfe) {
				if (false == src.getText().endsWith("%")) {
					src.setForeground(Color.RED);
					return;
				}

				try {
					String text = src.getText().substring(0,
							src.getText().length() - 1).trim();
					val = Integer.parseInt(text);
				} catch (NumberFormatException nfe2) {
					src.setForeground(Color.RED);
					return;
				}
			}
			System.out.println(val);

			src.setForeground(Color.BLACK);

			String name = src.getName();
			if (name.equals("Probabilistic"))
				policy_Collection_random = val;
			else if (name.equals("Constant"))
				policy_Collection_constant = val;
			else if (name.equals("Mobility"))
				policy_Collection_mobile = val;
			else
				// TODO Show a box or something?
				System.err.println("Unknown Text Field with name '" + name
						+ "'");

		}
	};

	private static JPanel createPolicy_collectionOnePolicy(String title,
			JButton outQuestion, JButton outConfig) {

		// TODO find a better method of forcing button size
		Dimension buttonPref = new Dimension(20, 16);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.add(new JLabel(title));
		panel.add(Box.createHorizontalGlue());
		JTextField textField = new JTextField("0 %", 5);
		if (title.equals("Constant"))
			textField.setText("100 %");
		textField.setName(title);
		textField.addCaretListener(collection_policyCaretListener);
		textField.setHorizontalAlignment(JTextField.TRAILING);
		textField.setMaximumSize(textField.getPreferredSize());
		panel.add(textField);
		enabledWhenPaused.add(textField);
		panel.add(Box.createHorizontalStrut(10));
		outQuestion = new JButton("?");
		outQuestion.setPreferredSize(buttonPref);
		outQuestion.setMaximumSize(buttonPref);
		outQuestion.setMaximumSize(buttonPref);
		panel.add(outQuestion);
		panel.add(Box.createHorizontalStrut(3));
		JButton edit = new JButton("e");
		edit.setPreferredSize(buttonPref);
		edit.setMaximumSize(buttonPref);
		edit.setMinimumSize(buttonPref);
		panel.add(edit);
		enabledWhenPaused.add(edit);

		return panel;
	}

	private static JPanel createControl() {
		JPanel control = new JPanel();
		control.setLayout(new BoxLayout(control, BoxLayout.LINE_AXIS));

		final JButton play = new JButton(
				new ImageIcon("images/play-normal.png"));
		final JButton pause = new JButton(new ImageIcon("images/pause-hot.png"));

		play.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (policy_Collection_constant + policy_Collection_mobile
						+ policy_Collection_random != 100) {
					JOptionPane
							.showMessageDialog(
									sFrame,
									"Data collection policy percentages do not add up to "
											+ "\n100 %, we cannot start the simulation");
					return;
				}

				Model.getInstance().updateNodeCount(desiredNodeCount);
				ModelController.getInstance().start();

				pause.setEnabled(true);
				play.setEnabled(false);

				for (JComponent c : enabledWhenPaused)
					c.setEnabled(false);
			}
		});
		control.add(play);

		pause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ModelController.getInstance().pause();

				pause.setEnabled(false);
				play.setEnabled(true);
				
				for (JComponent c : enabledWhenPaused)
					c.setEnabled(true);
			}
		});
		pause.setEnabled(false);
		control.add(pause);

		control.add(Box.createHorizontalGlue());

		JLabel hours = new JLabel("0 hours");
		sHours = hours;
		hours.setFont(new Font(null, Font.PLAIN, 18));
		hours.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 50));
		control.add(hours);

		return control;
	}
}