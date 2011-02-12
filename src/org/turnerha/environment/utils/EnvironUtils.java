package org.turnerha.environment.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.RadialGradientPaint;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

public class EnvironUtils {

	/**
	 * Given size and color, this creates a rectangular image size x size that
	 * is ready to be blended with the perceived network image
	 */
	public static BufferedImage createReadingImage(int size, Color focusColor) {
		BufferedImage im = createCompatibleTranslucentImage(size, size);

		//float radius = size s/ 2f;
		// RadialGradientPaint gradient = new RadialGradientPaint(radius,
		// radius,
		// radius, new float[] { 0f, 1f }, new Color[] { focusColor,
		// new Color(0xffffffff, true) });

		Graphics2D g = (Graphics2D) im.getGraphics();

		// g.setPaint(gradient);
		g.setColor(focusColor);
		g.fillRect(0, 0, size, size);

		return im;
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

}
