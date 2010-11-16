package org.turnerha;

public class ModelBackBuffer {
	private ShallowSlice[][] mFinishedSlices;
	private ModelFrontBuffer frontBuffer;

	public ModelBackBuffer(ModelFrontBuffer frontBuffer, int rows, int columns) {
		mFinishedSlices = new ShallowSlice[rows][columns];
		this.frontBuffer = frontBuffer;
	}

	public synchronized void completeSlice(ShallowSlice slice)
			throws InterruptedException {

		mFinishedSlices[slice.getRow()][slice.getColumn()] = slice;

		if (allSlicesAreReady()) {
			// Swap front and back model pointers
			frontBuffer.modelLock.lock();
			mFinishedSlices = frontBuffer.swapModel(mFinishedSlices);
			frontBuffer.modelLock.unlock();
			
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
