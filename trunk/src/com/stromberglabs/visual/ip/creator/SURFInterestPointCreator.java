package com.stromberglabs.visual.ip.creator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import com.stromberglabs.cluster.Clusterable;
import com.stromberglabs.jopensurf.SURFInterestPoint;
import com.stromberglabs.jopensurf.Surf;
import com.stromberglabs.util.cluster.BasicInterestPoint;

public class SURFInterestPointCreator implements InterestPointCreator {
	public List<Clusterable> getPoints(String image) throws IOException {
		return getPoints(ImageIO.read(new File(image)));
	}
		
	public List<Clusterable> getPoints(BufferedImage img) throws IOException {
		return getPoints(img,"");
	}
	
	public List<Clusterable> getPoints(BufferedImage img, String image) throws IOException {
		Surf surf = new Surf(img);
		List<Clusterable> values = new LinkedList<Clusterable>();
		for (SURFInterestPoint point : surf.getFreeOrientedInterestPoints()) {
			values.add(new BasicInterestPoint(point.getDescriptor(), image));
		}
		return values;
	}
}
