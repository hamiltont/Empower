package org.turnerha.network;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.turnerha.map.Map;

public class RealNetwork extends Network {
	BufferedImage mRealNetwork;

	/* Create a rescale filter op that makes the image 50% opaque */
	float[] scales = { 1f, 1f, 1f, 0.5f };
	float[] offsets = new float[4];
	RescaleOp mRealpha;

	/**
	 * 
	 * @param realNetworkFile
	 * @param size
	 *            The size this JPanel should make itself
	 */
	public RealNetwork(File realNetworkFile, Dimension size,
			BufferedImage colorScheme, float alpha, Map map) {
		try {
			BufferedImage temp = ImageIO.read(realNetworkFile);

			mRealNetwork = new BufferedImage(size.width, size.height,
					BufferedImage.TYPE_INT_RGB);
			Graphics g = mRealNetwork.getGraphics();
			g.drawImage(temp, 0, 0, size.width, size.height, 0, 0, temp
					.getWidth(), temp.getHeight(), null);
			g.dispose();

			mRealNetwork = Loader.convertColorScheme(mRealNetwork, colorScheme);

			removePixelsNotInPolys(mRealNetwork, map);

			if (alpha > 0f && alpha < 1.0f)
				scales[3] = alpha;
			mRealpha = new RescaleOp(scales, offsets, null);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void removePixelsNotInPolys(BufferedImage network, Map map) {

		// Iterate over all pixels, check that they are within the polys
		for (int x = 0; x < network.getWidth(); x++)
			for (int y = 0; y < network.getHeight(); y++) {
				if (false == map.contains(x, y))
					network.setRGB(x, y, 0);
			}

	}
	
	public int getRGBat(Point p) {
		return mRealNetwork.getRGB(p.x, p.y);
	}

	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		/* Draw the image, applying the filter */
		g2d.drawImage(mRealNetwork, mRealpha, 0, 0);

		// g.drawImage(mRealNetwork, 0, 0, null);

	}

}
