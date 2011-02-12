package org.turnerha;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class Launcher {

	private static JDialog jd;
	static JTextField mMoveTendenancy = new JTextField("1.0", 3);
	static JTextField mMobility = new JTextField("30", 3);
	static JTextField mTimePerHB = new JTextField("1", 3);
	static JTextField mInputFreq = new JTextField("1.0", 3);
	static ButtonGroup mLocationAccuracy = new ButtonGroup();
	static JComboBox mGeoFile;
	static JComboBox mNetworkFile;

	public static void main(String[] args) {

		jd = new JDialog();
		JPanel mainPanel = new JPanel();

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

		mainPanel.add(new JLabel(
				"Welcome to CPSS - Cell Phone Signal Simulation"));

		JPanel geoPanel = new JPanel(new BorderLayout());
		geoPanel.add(new JLabel("Choose the geographic area"),
				BorderLayout.NORTH);
		File dir = new File("geo-areas");
		String[] children = dir.list();
		mGeoFile = new JComboBox(children);
		geoPanel.add(mGeoFile, BorderLayout.SOUTH);
		mainPanel.add(geoPanel);

		JPanel networkPanel = new JPanel(new BorderLayout());
		networkPanel.add(new JLabel("Choose the network coverage map"),
				BorderLayout.NORTH);
		File dir2 = new File("network-images");
		String[] children2 = dir2.list();
		mNetworkFile = new JComboBox(children2);
		networkPanel.add(mNetworkFile, BorderLayout.SOUTH);
		mainPanel.add(networkPanel);

		JPanel inputs = new JPanel(new GridLayout(0, 2));
		inputs.add(new JLabel("Tendenancy to move (0.0-1.0)"));
		inputs.add(mMoveTendenancy);
		inputs.add(new JLabel("Mobility (meters)"));
		inputs.add(mMobility);
		mMobility.setEnabled(false);
		inputs.add(new JLabel("Time/heartbeat (hours)"));
		inputs.add(mTimePerHB);
		inputs.add(new JLabel("Input Frequency (0.0-1.0)"));
		inputs.add(mInputFreq);
		inputs.add(new JLabel("Location Accuracy"));
		JPanel btns = new JPanel();
		JRadioButton gps = new JRadioButton("GPS");
		JRadioButton network = new JRadioButton("Environment");
		mLocationAccuracy.add(gps);
		mLocationAccuracy.add(network);
		gps.setSelected(true);
		btns.add(gps);
		btns.add(network);
		network.setEnabled(false);
		inputs.add(btns);
		mainPanel.add(inputs);

		JButton startButton = new JButton("Start Simulation");
		startButton.addActionListener(startButtonListener);
		mainPanel.add(startButton);

		jd.setContentPane(mainPanel);
		jd.setSize(500, 350);
		jd.setLocation(400, 200);
		jd.setVisible(true);
	}

	private static ActionListener startButtonListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			jd.dispose();

			final String geoFileName = (String) mGeoFile.getSelectedItem();
			File geoDir = new File("geo-areas");
			File[] geoFiles = geoDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File arg0, String name) {
					if (name.equals(geoFileName))
						return true;
					return false;
				}
			});
			File geoFile = geoFiles[0];

			final String nwFileName = (String) mNetworkFile.getSelectedItem();
			File nwDir = new File("network-images");
			File[] nwFiles = nwDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File arg0, String name) {
					if (name.equals(nwFileName))
						return true;
					return false;
				}
			});
			File nwFile = nwFiles[0];

			double probabilityOfMoving = 1d;
			try {
				probabilityOfMoving = Double.parseDouble(mMoveTendenancy
						.getText());
			} catch (NumberFormatException a) {
			}

			// Not supported yet
			int mobility = 30;

			int timePerHB = 1;
			try {
				timePerHB = Integer.parseInt(mTimePerHB.getText());
			} catch (NumberFormatException a) {
			}

			float inputFreq = 1f;
			try {
				inputFreq = Float.parseFloat(mInputFreq.getText());
			} catch (NumberFormatException a) {
			}

			new Main(geoFile, nwFile, probabilityOfMoving, mobility, timePerHB,
					inputFreq, true);
		}
	};
}
