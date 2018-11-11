package com.jack_parsons.circle_detection;

import java.applet.Applet;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import javax.imageio.*;

public class CircleDetectorMain extends Applet {
	private static final long serialVersionUID = 1L; // to make Eclipse happy
	private BufferedImage rawImg, greyImg, edgeImg;

	public void init() {
		try {
			// Load in image
			File file = new File(System.getProperty("user.dir").replace("/bin", "") + "/res/test_circles.png");
			rawImg = ImageIO.read(file);
			greyImg = applyGreyScale(rawImg);
			edgeImg = applyEdgeDetection(greyImg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void paint(Graphics canvas) {
		// Draw image
		canvas.drawImage(edgeImg, 0, 0, null);
	}
	
	private BufferedImage applyEdgeDetection(BufferedImage originalBufferedImage) {
		// Iterate through all the pixels in the original image
		BufferedImage newBufferedImage = new BufferedImage(originalBufferedImage.getWidth(),originalBufferedImage.getHeight(), 1);
		for (int y = 0; y < originalBufferedImage.getHeight(); y++) {
			for (int x = 0; x < originalBufferedImage.getWidth(); x++) {
				int colour = originalBufferedImage.getRGB(x, y);
				int shade = colour & 0b000000000000000011111111;
				int net = 4 * shade;
				for (int[] offset : new int[][]{{0, 1}, {1, 0}, {-1, 0}, {0, -1}}) {
					if (0 <= x + offset[0] && x + offset[0] < originalBufferedImage.getWidth() && 0 <= y + offset[1] && y + offset[1] < originalBufferedImage.getHeight()){
						net -= originalBufferedImage.getRGB(x + offset[0], y + offset[1]) & 0b000000000000000011111111;
					}
				}
				if (net  > 0){
					newBufferedImage.setRGB(x, y, net);
				} else {
					newBufferedImage.setRGB(x, y, 0);
				}
			}
		}
		return newBufferedImage;
	}

	/**
	 * Converts an image into grey-scale
	 * 
	 * @param originalBufferedImage
	 *            The original image
	 * @return The new grey-scale buffered image
	 */
	private BufferedImage applyGreyScale(BufferedImage originalBufferedImage) {
		// Create new buffered image with same dimensions as original image.
		BufferedImage newBufferedImage = new BufferedImage(originalBufferedImage.getWidth(),
				originalBufferedImage.getHeight(), 1);

		// Iterate through all the pixels in the original image
		for (int y = 0; y < originalBufferedImage.getHeight(); y++) {
			for (int x = 0; x < originalBufferedImage.getWidth(); x++) {
				int colour = originalBufferedImage.getRGB(x, y);

				// Extract indivual colours
				int red = (colour & 0b111111110000000000000000) >> 16;
				int green = (colour & 0b000000001111111100000000) >> 8;
				int blue = colour & 0b000000000000000011111111;

				// Calculate the new shade of grey to use
				int greyShade = (red + green + blue) / 3;
				int greyScale = (greyShade << 16) + (greyShade << 8) + greyShade;

				// Update the new image with the grey shade
				newBufferedImage.setRGB(x, y, greyScale);
			}
		}
		return newBufferedImage;
	}
}