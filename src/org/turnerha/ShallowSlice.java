package org.turnerha;

import java.awt.Point;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* Deep copy of the essential information necessary for rendering a slice */
public class ShallowSlice {

	private HashSet<Point> points_;
	private int mRow, mCol;

	public ShallowSlice(Slice fullSlice) {
		List<SmartPhone> phones = fullSlice.getPhones();

		points_ = new HashSet<Point>(phones.size() / 2);
		for (SmartPhone phone : phones)
			if (false == points_.contains(phone.getLocation()))
				points_.add(new Point(phone.getLocation()));
		
		mRow = fullSlice.getRow();
		mCol = fullSlice.getColumn();
	}

	public Set<Point> getPoints() {
		return points_;
	}
	
	public int getRow() {
		return mRow;
	}
	
	public int getColumn() {
		return mCol;
	}
}
