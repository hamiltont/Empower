package org.turnerha;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import org.turnerha.geography.KmlGeography;
import org.turnerha.geography.Projection;
import org.turnerha.sensornodes.SensorNode;

public class Util {

	public static List<Integer> range(int upper) {
		ArrayList<Integer> result = new ArrayList<Integer>(upper);
		for (int i = 0; i < upper; i++)
			result.add(new Integer(i));
		return result;
	}

	/**
	 * Returns the size of the area used to render the KML geography and the
	 * {@link SensorNode}s moving around. This {@link Dimension}, is used in
	 * conjunction with the latitude longitude range of the {@link KmlGeography}
	 * to determine the appropriate {@link Projection}
	 * 
	 * @return
	 */
	public static Dimension getRenderingAreaSize() {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		screen.height -= 20;
		return screen;
	}

}
