package org.turnerha;

import java.util.ArrayList;
import java.util.List;

public class Util {

	public static List<Integer> range(int upper) {
		ArrayList<Integer> result = new ArrayList<Integer>(upper);
		for (int i = 0; i < upper; i++)
			result.add(new Integer(i));
		return result;
	}

}
