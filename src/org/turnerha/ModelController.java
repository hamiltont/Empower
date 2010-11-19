package org.turnerha;

public class ModelController {
	private ShallowSlice[][] mFinishedSlices;
	private ModelProxy proxy;

	public ModelController(ModelProxy proxy, int rows, int columns) {
		mFinishedSlices = new ShallowSlice[rows][columns];
		this.proxy = proxy;
	}

	public synchronized void completeSlice(ShallowSlice slice)
			throws InterruptedException {

		mFinishedSlices[slice.getRow()][slice.getColumn()] = slice;

		if (allSlicesAreReady()) {
			// Swap front and back model pointers
			proxy.modelLock.lock();
			mFinishedSlices = proxy.swapModel(mFinishedSlices);
			proxy.modelLock.unlock();
			
			// Toss out old model memory
			for (int row : Util.range(mFinishedSlices.length))
				for (int col : Util.range(mFinishedSlices[0].length))
					mFinishedSlices[row][col] = null;

			// Let all of the other slices resume working
			notifyAll();
		} else
			// Pause this slice until the others are ready
			wait();

	}

	private boolean allSlicesAreReady() {
		for (int row : Util.range(mFinishedSlices.length))
			for (int col : Util.range(mFinishedSlices[0].length))
				if (mFinishedSlices[row][col] == null)
					return false;

		return true;
	}

}
