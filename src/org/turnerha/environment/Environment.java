package org.turnerha.environment;

import java.awt.Dimension;
import java.awt.Graphics;

public interface Environment {

	/** Used to paint the env onto a graphics object */
	public void paint(Graphics g);

	/**
	 * Given an x and y location, return the value at that locale. This is a
	 * precursor to a 'get environment value at location' method that is
	 * independent of how the environment is stored (right now it's stored as an
	 * image, and this method is coupled to that implementation
	 */
	public int getValue(int x, int y);

	/** Returns the size of the real network. For now this is in pixels */
	public Dimension getSize();
}
