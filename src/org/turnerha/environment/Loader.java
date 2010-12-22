package org.turnerha.environment;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Loader {

	/**
	 * @param args
	 */
	/*public static void main(String[] args) {

		BufferedImage colorImage;
		try {
			colorImage = ImageIO.read(new File("network-images/network1.png"));

			BufferedImage gradient = EnvironUtils.createGradientImage(new Dimension(
					256, 1), Color.WHITE, Color.RED, Color.YELLOW, Color.GREEN
					.darker(), Color.CYAN, Color.BLUE, new Color(0, 0, 0x33));

			ImageIO.write(convertColorScheme(colorImage, gradient), "png",
					new File("hm-nw.png"));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

	/**
	 * Converts an arbitrary image into the color scheme we are using for the
	 * heatmaps. Does so by flattening the image into gray and then expanding
	 * into color. This is obviously not a very good method as a lot of detail is
	 * lost in the finer transitions, but it works for now.
	 */
	public static BufferedImage convertColorScheme(BufferedImage colorImage,
			BufferedImage gradient) {

		BufferedImage image = new BufferedImage(colorImage.getWidth(),
				colorImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		Graphics g = image.getGraphics();
		g.drawImage(colorImage, 0, 0, null);
		g.dispose();

		BufferedImage rgbGrayscale = new BufferedImage(colorImage.getWidth(),
				colorImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
		g = rgbGrayscale.getGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();

		LookupTable lookupTable = EnvironUtils.createColorLookupTable(gradient, 1f);
		LookupOp colorizeOperation = new LookupOp(lookupTable, null);

		BufferedImage colored = colorizeOperation.filter(rgbGrayscale, null);
		return colored;
	}

}
