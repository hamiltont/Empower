package org.turnerha.environment;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.turnerha.geography.KmlGeography;

public class RealEnviron extends Environment {
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
	 * 
	 * @param realEnvironmentFile
	 * @param size
	 *            The size this JPanel should make itself
	 * @param colorScheme
	 *            The color scheme that the heatmap will be using. The image of
	 *            the realEnviron will be matched to this color scheme as best
	 *            as possible
	 */
	public RealEnviron(File realEnvironmentFile, Dimension size,
			BufferedImage colorScheme, float alpha, KmlGeography kmlGeography) {

		mSize = size;

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

	public int getRGBat(Point p) {
		return mRealEnviron.getRGB(p.x, p.y);
	}

	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		/* Draw the image, applying the alpha filter */
		g2d.drawImage(mRealEnviron, mRealpha, 0, 0);
	}

}
