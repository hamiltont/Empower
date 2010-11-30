package org.turnerha;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;

public class Main {

	public static int millisecondsPerHeartbeat = 1000;
	public static int rows = 1;
	public static int columns = 1;
	public static int phonesPerSlice = 200;

	public static void main(String[] args) {
		new Main();
	}

	public Main() {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Random r = new Random();

		System.out.println("Processors Available: "
				+ Runtime.getRuntime().availableProcessors());
		System.out.println("Screen Size: " + screen);
		System.out.println("Total phones:" + rows * columns * phonesPerSlice);

		// Create ModelFrontBuffer
		ModelProxy proxy = new ModelProxy(rows, columns);

		// Create ModelBackBuffer
		ModelController controller = new ModelController(proxy, rows, columns);

		// Build Slices
		ShallowSlice[][] slices = new ShallowSlice[rows][columns];
		for (int row : Util.range(rows))
			for (int col : Util.range(columns)) {
				ArrayList<SmartPhone> slicePhones = new ArrayList<SmartPhone>();

				for (@SuppressWarnings("unused")
				int o : Util.range(phonesPerSlice)) {
					int x = r.nextInt(screen.width);
					int y = r.nextInt(screen.height);
					slicePhones.add(new SmartPhone(new Point(x, y)));
				}

				Slice s = new Slice(slicePhones, controller, row, col);

				// The shallow slice ctor is not thread safe. It's typically run
				// from the thread for that slice, so that's fine. However, for
				// the first copy we run it from the main thread, so we need to
				// do the copy before we start the slice
				slices[row][col] = new ShallowSlice(s);
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

		ModelView view = new ModelView(proxy, controller);
		frame.setLayout(new BorderLayout());
		frame.add(view, BorderLayout.CENTER);

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
			}

			return false;
		}

	}
}