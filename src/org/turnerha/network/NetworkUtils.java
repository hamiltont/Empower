package org.turnerha.network;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.jdesktop.swingx.graphics.BlendComposite;

public class NetworkUtils {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// BufferedImage gradient = ImageIO.read(new File(
		// "network-images/rggradient.png"));

		BufferedImage fadedCircle = createFadedCircleImage(100);
		BufferedImage monoImage = createCompatibleTranslucentImage(400, 350);
		Graphics g = monoImage.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 400, 350);

		BufferedImage circledMonochromeImage = addCircleToMonochromeImage(
				monoImage, fadedCircle, new Point(200, 200));
		circledMonochromeImage = addCircleToMonochromeImage(monoImage,
				fadedCircle, new Point(400, 200));
		circledMonochromeImage = addCircleToMonochromeImage(monoImage,
				fadedCircle, new Point(200, 300));
		circledMonochromeImage = addCircleToMonochromeImage(monoImage,
				fadedCircle, new Point(230, 230));

		BufferedImage gradientImage = createGradientImage(
				new Dimension(256, 1), Color.WHITE, Color.RED, Color.YELLOW,
				Color.GREEN.darker(), Color.CYAN, Color.BLUE, new Color(0, 0,
						0x33));
		LookupTable lookupTable = createColorLookupTable(gradientImage, 0.5f);
		LookupOp colorizeOperation = new LookupOp(lookupTable, null);

		BufferedImage heatMap = colorizeOperation.filter(
				circledMonochromeImage, null);

		try {
			ImageIO.write(fadedCircle, "png", new File("faded-circle.png"));
			ImageIO.write(circledMonochromeImage, "png", new File(
					"circled-mono.png"));
			ImageIO.write(gradientImage, "png", new File("gradient.png"));
			ImageIO.write(heatMap, "png", new File("heatmap.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static BufferedImage createCompatibleTranslucentImage(int width,
			int height) {
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();

		return gc
				.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
	}

	public static BufferedImage createFadedCircleImage(int size) {
		BufferedImage im = createCompatibleTranslucentImage(size, size);
		float radius = size / 2f;

		RadialGradientPaint gradient = new RadialGradientPaint(radius, radius,
				radius, new float[] { 0f, 1f }, new Color[] { Color.BLACK,
						new Color(0xffffffff, true) });

		Graphics2D g = (Graphics2D) im.getGraphics();

		g.setPaint(gradient);
		g.fillRect(0, 0, size, size);

		return im;
	}

	public static BufferedImage addCircleToMonochromeImage(
			BufferedImage monochromeImage, BufferedImage dotImage, Point point) {
		Point p = point;
		int circleRadius = dotImage.getWidth() / 2;
		float alpha = 1f;

		Graphics2D g = (Graphics2D) monochromeImage.getGraphics();

		g.setComposite(BlendComposite.Multiply.derive(alpha));
		g.drawImage(dotImage, null, p.x - circleRadius, p.y - circleRadius);

		// g.dispose();

		return monochromeImage;
	}

	public static BufferedImage createGradientImage(Dimension size,
			Color... colors) {

		if (size == null)
			size = new Dimension(256, 1);
		if (colors == null)
			colors = new Color[] { Color.WHITE, Color.RED, Color.YELLOW,
					Color.GREEN.darker(), Color.CYAN, Color.BLUE,
					new Color(0, 0, 0x33) };

		BufferedImage im = createCompatibleTranslucentImage(size.width,
				size.height);
		Graphics2D g = im.createGraphics();

		float[] fractions = new float[colors.length];
		float step = 1f / colors.length;

		for (int i = 0; i < colors.length; ++i) {
			fractions[i] = i * step;
		}

		LinearGradientPaint gradient = new LinearGradientPaint(0, 0,
				size.width, 1, fractions, colors,
				MultipleGradientPaint.CycleMethod.REPEAT);

		g.setPaint(gradient);
		g.fillRect(0, 0, size.width, size.height);

		g.dispose();

		return im;
	}

	public static LookupTable createColorLookupTable(BufferedImage im,
			float alpha) {
		int tableSize = 256;
		Raster imageRaster = im.getData();
		double sampleStep = 1D * im.getWidth() / tableSize; // Sample pixels
		// evenly
		byte[][] colorTable = new byte[4][tableSize];
		int[] pixel = new int[1]; // Sample pixel
		Color c;

		for (int i = 0; i < tableSize; ++i) {
			imageRaster.getDataElements((int) (i * sampleStep), 0, pixel);

			c = new Color(pixel[0]);

			colorTable[0][i] = (byte) c.getRed();
			colorTable[1][i] = (byte) c.getGreen();
			colorTable[2][i] = (byte) c.getBlue();
			colorTable[3][i] = (byte) (alpha * 0xff);
		}

		LookupTable lookupTable = new ByteLookupTable(0, colorTable);

		return lookupTable;
	}

}
