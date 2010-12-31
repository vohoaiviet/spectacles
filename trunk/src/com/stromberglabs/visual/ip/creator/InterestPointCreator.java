package com.stromberglabs.visual.ip.creator;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import com.stromberglabs.cluster.Clusterable;

public interface InterestPointCreator {
	/**
	 * Gets a list of interest points from an image
	 * 
	 * @param image
	 * @return
	 * @throws IOException
	 */
	public List<Clusterable> getPoints(String image) throws IOException;
	
	public List<Clusterable> getPoints(BufferedImage image) throws IOException;
}
