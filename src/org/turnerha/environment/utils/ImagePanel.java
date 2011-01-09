package org.turnerha.environment.utils;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ImagePanel extends JPanel {
	private Image mImage;

	public ImagePanel(Image foo) {
		mImage = foo;
	}

	public void setImage(Image foo) {
		mImage = foo;
		repaint();
	}
	
	public void paint(Graphics g) {
		g.drawImage(mImage, 0, 0, getWidth(), getHeight(), null);
	}

}
