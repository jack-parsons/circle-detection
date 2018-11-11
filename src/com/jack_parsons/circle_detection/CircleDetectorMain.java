package com.jack_parsons.circle_detection;

import java.applet.Applet;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.awt.Image;

import javax.imageio.*;

public class CircleDetectorMain extends Applet {
	private static final long serialVersionUID = 1L; // to make Eclipse happy
	private BufferedImage rawImg, greyImg, edgeImg, scaledImg;

	public void init() {
		try {
			// Load in image
			File file = new File(System.getProperty("user.dir").replace("/bin", "") + "/res/test_circles.png");
			rawImg = ImageIO.read(file);
			float scale = Math.min((float)300/rawImg.getHeight(), (float)300/rawImg.getWidth());
			scaledImg = new BufferedImage((int)(rawImg.getWidth() * scale), (int)(rawImg.getHeight() * scale), 1);
			scaledImg.createGraphics().drawImage(rawImg.getScaledInstance((int)(rawImg.getWidth() * scale), (int)(rawImg.getHeight() * scale), Image.SCALE_FAST), 0, 0, null);
			greyImg = applyGreyScale(scaledImg);
			edgeImg = applyEdgeDetection(greyImg);
			for (int[] pos : applyCircleDetection(0.45, 10, 150, edgeImg)) {
//				System.out.printf("%d %d\n", pos[0], pos[1]);
				edgeImg.setRGB(pos[0], pos[1], 255 << 16);
				for (int x = pos[0] - pos[2]; x < pos[0] + pos[2]; x ++) {
					if (0 <= pos[1] - pos[2] && pos[1] - pos[2] < edgeImg.getHeight() && 0 < x && x < edgeImg.getWidth()){
						edgeImg.setRGB(x, pos[1] - pos[2], 255 << 8);
					}
					if (0 <= pos[1] + pos[2] && pos[1] + pos[2] < edgeImg.getHeight() && 0 < x && x < edgeImg.getWidth()){
						edgeImg.setRGB(x, pos[1] + pos[2], 255 << 8);
					}
				}
				for (int y = pos[1] - pos[2]; y < pos[1] + pos[2]; y ++) {
					if (0 <= pos[0] - pos[2] && pos[0] - pos[2] < edgeImg.getWidth() && 0 < y && y < edgeImg.getHeight()){
						edgeImg.setRGB(pos[0] - pos[2], y, 255 << 8);
					}
					if (0 <= pos[0] + pos[2] && pos[0] + pos[2] < edgeImg.getWidth() && 0 < y && y < edgeImg.getHeight()){
						edgeImg.setRGB(pos[0] + pos[2], y, 255 << 8);
					}
				}
			}
			System.out.println("Completed");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void paint(Graphics canvas) {
		// Draw image
		canvas.drawImage(edgeImg, 0, 0, null);
	}

	private ArrayList<int[]> applyCircleDetection(double d, int minRadius, int maxRadius, BufferedImage originalBufferedImage) {
		ArrayList<int[]> circleLocations = new ArrayList<int[]>();
		
		// Loop through all the possible sized radii
		for (int radius = minRadius; radius < maxRadius; radius ++) {
			for (int y = radius; y < originalBufferedImage.getHeight()-radius; y ++) {
				for (int x = radius; x < originalBufferedImage.getWidth()-radius; x++) {
					int votes = 0;
					int maxVotes = 0;
					for (float angle = 0; angle < Math.PI; angle += 0.1) {
						int actualX = (int)Math.round(x + radius * Math.cos(angle));
						int actualY = (int)Math.round(y + radius * Math.sin(angle));
						if (0 <= actualX && actualX < originalBufferedImage.getWidth() && 0 <= actualY && actualY < originalBufferedImage.getHeight()){
							votes += originalBufferedImage.getRGB(actualX, actualY) & 0b000000000000000011111111;
							maxVotes += 255;
						}
					}
					if (votes / (float)maxVotes > d) {
						circleLocations.add(new int[]{x, y, radius});
					}
				}
			}
		}
		
		return circleLocations;
	}

	private BufferedImage applyEdgeDetection(BufferedImage originalBufferedImage) {
		BufferedImage newBufferedImage = new BufferedImage(originalBufferedImage.getWidth(),
				originalBufferedImage.getHeight(), 1);

		// Iterate through all the pixels in the original image
		for (int y = 0; y < originalBufferedImage.getHeight(); y++) {
			for (int x = 0; x < originalBufferedImage.getWidth(); x++) {
				int colour = originalBufferedImage.getRGB(x, y);
				int shade = colour & 0b000000000000000011111111;
				int net = 4 * shade;
				for (int[] offset : new int[][] { { 0, 1 }, { 1, 0 }, { -1, 0 }, { 0, -1 } }) {
					if (0 <= x + offset[0] && x + offset[0] < originalBufferedImage.getWidth() && 0 <= y + offset[1]
							&& y + offset[1] < originalBufferedImage.getHeight()) {
						net -= originalBufferedImage.getRGB(x + offset[0], y + offset[1]) & 0b000000000000000011111111;
					}
				}
				if (net > 0) {
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