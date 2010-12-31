package com.stromberglabs.visual.utils;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.tomgibara.CannyEdgeDetector;

public class EdgeView extends JPanel {
	private BufferedImage mImage;
	private float mImageAXScale = 0;
	private float mImageAYScale = 0;
	private int mImageAWidth = 0;
	private int mImageAHeight = 0;
	
	public EdgeView(BufferedImage image){
		CannyEdgeDetector detector = new CannyEdgeDetector();
		
    	mImageAXScale = (float)Math.min(image.getWidth(),800)/(float)image.getWidth();
    	mImageAYScale = (float)Math.min(image.getHeight(),800 * (float)image.getHeight()/(float)image.getWidth())/(float)image.getHeight();
    	
    	mImageAWidth = (int)((float)image.getWidth() * mImageAXScale);
    	mImageAHeight = (int)((float)image.getHeight() * mImageAYScale);
		
		detector.setSourceImage(image);
		detector.setHighThreshold(10.0F);
		detector.setLowThreshold(3.0F);
		detector.process();
		mImage = detector.getEdgesImage();
	}
	
    public void display(){
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new JScrollPane(this));
        f.setSize(mImageAWidth+50,mImageAHeight+50);
        f.setLocation(0,0);
        f.setVisible(true);
    }
    
    protected void paintComponent(Graphics g) {
        // Center image in this component.
        g.drawImage(mImage,0,0,mImageAWidth,mImageAHeight,this);
    }
	
    public static void main(String[] args) throws IOException {
        BufferedImage imageA = ImageIO.read(new File(args[0]));
        EdgeView show = new EdgeView(imageA);
        show.display();
        //show.matchesInfo();
    }
}
