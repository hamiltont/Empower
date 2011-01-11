package org.turnerha;

import java.awt.Point;
import java.util.HashSet;
import java.util.List;

public class Slice {

	private List<SmartPhone> mPhones = null;

	/**
	 * Replaced every draw. Contains the points on the screen that smartphones
	 * cover
	 */
	private HashSet<Point> mPhonePoints = new HashSet<Point>(0);
	private int mRow;
	private int mColumn;
	private Thread mUpdateThread;

	private Runnable mUpdateLoop = new Runnable() {

		@Override
		public void run() {
			while (true) {
				update();
				try {
					mPhonePoints = new HashSet<Point>(mPhones.size());
					for (SmartPhone phone : mPhones)
						mPhonePoints.add(phone.getLocation());
					
					controller.completeSlice(Slice.this);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};

	private ModelController controller;

	public Slice(List<SmartPhone> phones, ModelController controller,
			int myRow, int myColumn) {
		mPhones = phones;

		mRow = myRow;
		mColumn = myColumn;

		this.controller = controller;

		mUpdateThread = new Thread(mUpdateLoop);
		mUpdateThread.setName("Slice [" + myRow + "," + myColumn + "]");
		mUpdateThread.setDaemon(true);
	}

	public void start() {
		mUpdateThread.start();
	}

	private void update() {

		int size = mPhones.size();
		for (int i = 0; i < size; i++) {
			SmartPhone sp = mPhones.get(i);
			sp.update();

			if (sp.getShouldRemove()) {
				mPhones.remove(i);

				i--;
				size--;
			}
		}
	}

	public List<SmartPhone> getPhones() {
		return mPhones;
	}

	public int getRow() {
		return mRow;
	}

	public int getColumn() {
		return mColumn;
	}

	public HashSet<Point> getPoints() {
		return mPhonePoints;
	}
}
