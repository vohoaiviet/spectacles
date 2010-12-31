package com.stromberglabs.visual.utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.semanticmetadata.lire.imageanalysis.sift.Extractor;
import net.semanticmetadata.lire.imageanalysis.sift.Feature;
import net.semanticmetadata.lire.utils.ImageUtils;

import com.stromberglabs.jopensurf.SURFInterestPoint;
import com.stromberglabs.jopensurf.Surf;
import com.stromberglabs.jopensurf.SurfCompare;

public class ViewPoints extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final int UNMATCHED_CIRCLE_DIAMETER = 4;
	
	private BufferedImage image;
	private float mImageAXScale = 0;
	private float mImageAYScale = 0;
	private int mImageAWidth = 0;
	private int mImageAHeight = 0;
	
	private List<Feature> mFeatures;
	
	private List<Feature> mCroppedFeatures;
	
	private int mCroppedUpperLeftX = Integer.MAX_VALUE;
	private int mCroppedUpperLeftY = Integer.MAX_VALUE;

	private int mCroppedLowerRightX = Integer.MIN_VALUE;
	private int mCroppedLowerRightY = Integer.MIN_VALUE;
	
	private BufferedImage mCroppedImage;
	
	public ViewPoints(BufferedImage image){
        this.image = image;
        
        Extractor extractor = new Extractor();
    	try {
			mFeatures = extractor.computeSiftFeatures(image);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	mImageAXScale = (float)Math.min(image.getWidth(),800)/(float)image.getWidth();
    	mImageAYScale = (float)Math.min(image.getHeight(),800 * (float)image.getHeight()/(float)image.getWidth())/(float)image.getHeight();
    	
    	mImageAWidth = (int)((float)image.getWidth() * mImageAXScale);
    	mImageAHeight = (int)((float)image.getHeight() * mImageAYScale);
    	
    	for ( Feature feature : mFeatures ){
    		mCroppedUpperLeftX = Math.min((int)feature.location[0],mCroppedUpperLeftX);
			mCroppedUpperLeftY = Math.min((int)feature.location[1],mCroppedUpperLeftY);
			mCroppedLowerRightX = Math.max((int)feature.location[0],mCroppedLowerRightX);
			mCroppedLowerRightY = Math.max((int)feature.location[1],mCroppedLowerRightY);
    	}
    	
//    	this.image = this.image.getSubimage((int)mCroppedUpperLeftX,(int)mCroppedUpperLeftY,(int)(mCroppedLowerRightX-mCroppedUpperLeftX),(int)(mCroppedLowerRightY-mCroppedUpperLeftY));
//    	//this.image = ImageUtils.scaleImage(this.image,500);
//    	try {
//    		mFeatures = extractor.computeSiftFeatures(this.image);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		mCroppedUpperLeftX = Integer.MAX_VALUE;
//		mCroppedUpperLeftY = Integer.MAX_VALUE;
//		mCroppedLowerRightX = Integer.MIN_VALUE;
//		mCroppedLowerRightY = Integer.MIN_VALUE;
//		
//    	for ( Feature feature : mFeatures ){
//    		mCroppedUpperLeftX = Math.min((int)feature.location[0],mCroppedUpperLeftX);
//			mCroppedUpperLeftY = Math.min((int)feature.location[1],mCroppedUpperLeftY);
//			mCroppedLowerRightX = Math.max((int)feature.location[0],mCroppedLowerRightX);
//			mCroppedLowerRightY = Math.max((int)feature.location[1],mCroppedLowerRightY);
//    	}
//		
//    	mImageAXScale = (float)Math.min(this.image.getWidth(),800)/(float)this.image.getWidth();
//    	mImageAYScale = (float)Math.min(this.image.getHeight(),800 * (float)this.image.getHeight()/(float)this.image.getWidth())/(float)this.image.getHeight();
//    	
//    	mImageAWidth = (int)((float)this.image.getWidth() * mImageAXScale);
//    	mImageAHeight = (int)((float)this.image.getHeight() * mImageAYScale);
	}
    /**
     * Drawing an image can allow for more
     * flexibility in processing/editing.
     */
    protected void paintComponent(Graphics g) {
        // Center image in this component.
        g.drawImage(image,0,0,mImageAWidth,mImageAHeight,this);
        
//        //if there is a surf descriptor, go ahead and draw the points
        if ( mFeatures != null ){
        	drawIpoints(g,mFeatures,0,mImageAXScale,mImageAYScale);
        }
    }
    
    private void drawIpoints(Graphics g,List<Feature> points,int offset,float xScale,float yScale){
    	Graphics2D g2d = (Graphics2D)g;
    	g2d.setColor(Color.RED);
    	for ( Feature point : points ){
			int x = (int)(xScale * point.location[0]) + offset;
			int y = (int)(yScale * point.location[1]);
			g2d.drawOval(x-UNMATCHED_CIRCLE_DIAMETER/2,y-UNMATCHED_CIRCLE_DIAMETER/2,UNMATCHED_CIRCLE_DIAMETER,UNMATCHED_CIRCLE_DIAMETER);
    	}
    	System.out.println(mCroppedUpperLeftX + "," + mCroppedUpperLeftY + " - " + mCroppedLowerRightX + "," + mCroppedLowerRightY);
    	g2d.setColor(Color.GREEN);
    	//x offset, y offset, width, height
    	g2d.drawRect((int)(xScale * mCroppedUpperLeftX),(int)(yScale * mCroppedUpperLeftY),(int)(xScale * (mCroppedLowerRightX-mCroppedUpperLeftX)),(int)(yScale * (mCroppedLowerRightY-mCroppedUpperLeftY)));
    }

    public void display(){
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new JScrollPane(this));
        f.setSize(mImageAWidth+50,mImageAHeight+50);
        f.setLocation(0,0);
        f.setVisible(true);
    }
    
    public static void main(String[] args) throws IOException {
        BufferedImage imageA = ImageIO.read(new File("H:\\bookbench\\scaled\\0060502932_brightened.jpg"));
        ViewPoints show = new ViewPoints(ImageUtils.scaleImage(imageA,500));
        show.display();
        //show.matchesInfo();
    }
}

