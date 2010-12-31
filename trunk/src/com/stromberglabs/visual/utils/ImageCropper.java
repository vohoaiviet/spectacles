package com.stromberglabs.visual.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import net.semanticmetadata.lire.imageanalysis.sift.Extractor;
import net.semanticmetadata.lire.imageanalysis.sift.Feature;
import net.semanticmetadata.lire.utils.ImageUtils;

import com.stromberglabs.cluster.Point;

/**
 * Uses SIFT interest points to try and remove the background
 * 
 * @author Andrew
 *
 */
public class ImageCropper {
	/**
	 * The aspect ratio of the original image, X/Y
	 */
	private float mAspectRatio;
	
	/**
	 * The interest point center
	 */
	private Point mCenter;
	
	private List<Feature> mFeatures;
	
	/**
	 * These keep track of the min and max x and y values of
	 * the features
	 */
	
	private int mCroppedUpperLeftX = Integer.MAX_VALUE;
	private int mCroppedUpperLeftY = Integer.MAX_VALUE;

	private int mCroppedLowerRightX = Integer.MIN_VALUE;
	private int mCroppedLowerRightY = Integer.MIN_VALUE;
	
	private BufferedImage mCroppedImage;
	
	public ImageCropper(BufferedImage image){
		mAspectRatio = (float)image.getWidth()/(float)image.getHeight();
		
        Extractor extractor = new Extractor();
    	try {
			mFeatures = extractor.computeSiftFeatures(image);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		float ySum = 0;
		float xSum = 0;
		for ( Feature feature : mFeatures ){
			xSum += feature.location[0];
			ySum += feature.location[1];
			
    		mCroppedUpperLeftX = Math.min((int)feature.location[0],mCroppedUpperLeftX);
			mCroppedUpperLeftY = Math.min((int)feature.location[1],mCroppedUpperLeftY);
			mCroppedLowerRightX = Math.max((int)feature.location[0],mCroppedLowerRightX);
			mCroppedLowerRightY = Math.max((int)feature.location[1],mCroppedLowerRightY);
		}
		mCenter = new Point(xSum/mFeatures.size(),ySum/mFeatures.size());
		
//		int yMaxCropping = Math.max(image.getHeight() - mCroppedLowerRightY,mCroppedUpperLeftY);
//		int xMaxCropping = Math.max(image.getWidth() - mCroppedLowerRightX,mCroppedUpperLeftX);
//		
//		
		int leftWidth = Math.round(mCenter.getX()) - mCroppedUpperLeftX;
		int rightWidth = mCroppedLowerRightX - Math.round(mCenter.getX());
		
		int croppedImageWidth = Math.round((Math.min(rightWidth,leftWidth) + Math.round(Math.min(rightWidth,leftWidth) * 0.10F)) * mAspectRatio);
		
		int topHeight = Math.round(mCenter.getY()) - mCroppedUpperLeftY;
		int bottomHeight = mCroppedLowerRightY - Math.round(mCenter.getY());
		
		int croppedImageHeight = Math.min(topHeight, bottomHeight) + Math.round((Math.min(topHeight, bottomHeight) * 0.10F));
		
		int x = Math.max(Math.round(mCenter.getX()) - croppedImageWidth,0);
		int y = Math.max(Math.round(mCenter.getY()) - croppedImageHeight,0);
		int w = croppedImageWidth * 2;
		int h = croppedImageHeight * 2;
		
		System.out.println("old aspect ratio: " + mAspectRatio);
		System.out.println("new aspect ratio: " + w/(float)h);
		
		mCroppedImage = image.getSubimage(x,y,w,h);
	}
	
	public BufferedImage getCroppedImage(){
		return mCroppedImage;
	}
	
	public static void main(String args[]){
		try {
			BufferedImage img = ImageIO.read(new File("H:\\bookbench\\scaled\\0380818191.jpg"));
			ImageCropper cropper = new ImageCropper(img);
			ViewPoints shower = new ViewPoints(ImageUtils.scaleImage(cropper.getCroppedImage(),500));
			shower.display();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
