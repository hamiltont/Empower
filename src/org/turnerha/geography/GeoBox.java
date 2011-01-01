package org.turnerha.geography;

public class GeoBox {

	private GeoLocation mTopRight;
	private GeoLocation mBottomLeft;

	public GeoBox(GeoLocation topRight, GeoLocation bottomLeft) {
		mTopRight = topRight;
		mBottomLeft = bottomLeft;

		// TODO validation of the top right / bottom left. Static methods within
		// the geopoint class that can return information like this would be
		// great
	}

	public GeoLocation getTopRight() {
		return mTopRight;
	}

	public GeoLocation getBottomLeft() {
		return mBottomLeft;
	}
}
