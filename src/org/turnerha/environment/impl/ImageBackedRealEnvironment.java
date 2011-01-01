package org.turnerha.environment.impl;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.turnerha.environment.RealEnvironment;
import org.turnerha.environment.utils.Loader;
import org.turnerha.geography.GeoBox;
import org.turnerha.geography.GeoLocation;
import org.turnerha.geography.KmlGeography;
import org.turnerha.geography.Projection;
import org.turnerha.geography.ProjectionCartesian;

public class ImageBackedRealEnvironment implements RealEnvironment {
	BufferedImage mRealEnviron;

	/** A rescale filter operation that makes the image somewhat transparent */
	RescaleOp mRealpha;

	/** The desired pixel size of the real environment */
	private Dimension mSize;

	/** Contains a 1x256 image that specifies the valid 265 colors */
	private BufferedImage mColorScheme;

	/** Contains the KML data of the geographic location we are simulating */
	private KmlGeography mKmlGeography;

	/**
	 * The projection between the pixel values in the backing image model data
	 * and real-world latitude longitude
	 */
	private ProjectionCartesian mProjection;

	/**
	 * 
	 * @param realEnvironmentFile
	 * @param size
	 *            The size this JPanel should make itself
	 * @param colorScheme
	 *            The color scheme that the heatmap will be using. The image of
	 *            the realEnviron will be matched to this color scheme as best
	 *            as possible
	 */
	public ImageBackedRealEnvironment(File realEnvironmentFile, Dimension size,
			BufferedImage colorScheme, float alpha, KmlGeography kmlGeography) {

		mSize = size;

		mProjection = new ProjectionCartesian(kmlGeography.getGeoBox(), size);

		// Setup the alpha operation
		float[] scales = { 1f, 1f, 1f, 0.5f };
		float[] offsets = new float[4];
		if (alpha > 0f && alpha < 1.0f)
			scales[3] = alpha;
		mRealpha = new RescaleOp(scales, offsets, null);

		mColorScheme = colorScheme;

		mKmlGeography = kmlGeography;

		loadNewEnvironment(realEnvironmentFile);
	}

	public void loadNewEnvironment(String filename) {
		File f = new File("network-images/network3.png");
		loadNewEnvironment(f);
	}

	public void loadNewEnvironment(File realNetworkFile) {
		try {
			// Load the image
			BufferedImage temp = ImageIO.read(realNetworkFile);

			// Scale the image
			mRealEnviron = new BufferedImage(mSize.width, mSize.height,
					BufferedImage.TYPE_INT_RGB);
			Graphics g = mRealEnviron.getGraphics();
			g.drawImage(temp, 0, 0, mSize.width, mSize.height, 0, 0, temp
					.getWidth(), temp.getHeight(), null);
			g.dispose();

			// Convert the color scheme
			mRealEnviron = Loader
					.convertColorScheme(mRealEnviron, mColorScheme);

			// Clear any pixels not contained within the defined geo
			removePixelsNotInGeography(mRealEnviron, mKmlGeography);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void removePixelsNotInGeography(BufferedImage environment,
			KmlGeography kmlGeography) {

		// Iterate over all pixels, check that they are within the polys
		for (int x = 0; x < environment.getWidth(); x++)
			for (int y = 0; y < environment.getHeight(); y++) {
				if (false == kmlGeography.contains(x, y))
					environment.setRGB(x, y, 0);
			}

	}

	@Override
	public void paintInto(Graphics g, Projection proj) {
		Graphics2D g2d = (Graphics2D) g;

		/* Draw the image, applying the alpha filter */
		g2d.drawImage(mRealEnviron, mRealpha, 0, 0);
	}

	@Override
	public GeoBox getSize() {
		int w = mRealEnviron.getWidth();
		int h = mRealEnviron.getHeight();

		return mProjection.getGeoBoxOf(new Rectangle(w, h));	
	}

	@Override
	public int getValueAt(GeoLocation location) {
		Point p = mProjection.getPointAt(location);
		return mRealEnviron.getRGB(p.x, p.y);
	}

}
