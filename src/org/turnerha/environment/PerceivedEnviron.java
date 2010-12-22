package org.turnerha.environment;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.jdesktop.swingx.graphics.BlendComposite;
import org.turnerha.geography.KmlGeography;

public class PerceivedEnviron extends Environment {
	BufferedImage mNetworkBlackWhite;

	//BufferedImage mReading;
	
	HashMap<Integer, BufferedImage> mReadingCircles = new HashMap<Integer, BufferedImage>();

	LookupOp mColorize;
	
	/* Create a rescale filter op that makes the image 50% opaque */
	float[] scales = { 1f, 1f, 1f, 0.5f };
	float[] offsets = new float[4];
	RescaleOp mRealpha = new RescaleOp(scales, offsets, null);


	public PerceivedEnviron(Dimension size, KmlGeography m) {

		mNetworkBlackWhite = createCompatibleTranslucentImage(size.width,
				size.height);
		Graphics g = mNetworkBlackWhite.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, size.width, size.height);
		g.dispose();

		//mReading = createFadedCircleImage(10);

		BufferedImage gradient = EnvironUtils.createGradientImage(null, null);

		LookupTable lookupTable = EnvironUtils.createColorLookupTable(gradient, 0.5f);
		mColorize = new LookupOp(lookupTable, null);
	}

	// Does nothing with value right now
	public void addReading(int value, Point p) {

		BufferedImage mReading = mReadingCircles.get(new Integer(value));
		
		if (mReading == null) {
			Color c = new Color(value);
			mReading = createFadedCircleImage(10, c);
			mReadingCircles.put(new Integer(value), mReading);
		}
		
		int circleRadius = mReading.getWidth() / 2;
		float alpha = 1f;

		Graphics2D g = (Graphics2D) mNetworkBlackWhite.getGraphics();

		g.setComposite(BlendComposite.Average.derive(0.1f));
		g.drawImage(mReading, null, p.x - circleRadius, p.y - circleRadius);
	}

	@Override
	public void paint(Graphics g) {

		BufferedImage heatMap = mColorize.filter(mNetworkBlackWhite, null);

			
		Graphics2D g2d = (Graphics2D) g;

		/* Draw the image, applying an alpha filter */
		g2d.drawImage(heatMap, mRealpha, 0, 0);


	}

	public static BufferedImage createFadedCircleImage(int size, Color focusColor) {
		BufferedImage im = createCompatibleTranslucentImage(size, size);
		float radius = size / 2f;

		//RadialGradientPaint gradient = new RadialGradientPaint(radius, radius,
		//		radius, new float[] { 0f, 1f }, new Color[] { focusColor,
		//				new Color(0xffffffff, true) });

		Graphics2D g = (Graphics2D) im.getGraphics();

		//g.setPaint(gradient);
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

}
