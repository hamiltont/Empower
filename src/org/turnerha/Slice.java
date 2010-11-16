package org.turnerha;

import java.awt.Point;
import java.util.List;

public class Slice {

	private List<SmartPhone> mPhones = null;
	private int mRow;
	private int mColumn;
	private Thread mUpdateThread;

	private Runnable mUpdateLoop = new Runnable() {

		@Override
		public void run() {
			while (true) {
				update();
				try {
					backBuffer.completeSlice(new ShallowSlice(Slice.this));
				} catch (InterruptedException e) {
					System.out.print(Thread.currentThread().getName());
					System.out
							.println(" was interrupted. This is the only way to exit for now");
				}
			}
		}
	};

	private ModelBackBuffer backBuffer;

	public Slice(List<SmartPhone> phones, ModelBackBuffer backBuffer,
			int myRow, int myColumn) {
		mPhones = phones;

		mRow = myRow;
		mColumn = myColumn;

		this.backBuffer = backBuffer;

		mUpdateThread = new Thread(mUpdateLoop);
		mUpdateThread.setName("Slice [" + myRow + "," + myColumn + "]");
		mUpdateThread.setDaemon(true);
	}
	
	public void start() {
		mUpdateThread.start();
	}

	private void update() {
		for (SmartPhone phone: mPhones)
			phone.move();
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
}
