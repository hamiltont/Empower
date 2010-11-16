package org.turnerha;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ModelFrontBuffer {

	private ShallowSlice[][] mSlices = null;

	// Plain jane synchronized allows the possibility that swap will be called
	// while the rendering is using the model pointer. This would result in the
	// model pointer then being prepared for new model data e.g. being memory
	// cleared, and all of a sudden the render loop has no data. Essentially the
	// render loop needs to specify that it is using the model currently and it
	// should not be swapped
	public Lock modelLock = new ReentrantLock();

	public ModelFrontBuffer(int rows, int columns) {
	}

	public ShallowSlice[][] getModel() {
		return mSlices;
	}

	public ShallowSlice[][] swapModel(ShallowSlice[][] newModel) {
		ShallowSlice[][] temp = mSlices;
		mSlices = newModel;
		return temp;
	}
}
