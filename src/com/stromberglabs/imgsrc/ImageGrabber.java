package com.stromberglabs.imgsrc;

import com.stromberglabs.imgsrc.amazonbooks.AmazonBookCoverGrabber;
import com.stromberglabs.imgsrc.bookthing.BookThingISBNFinder;

public class ImageGrabber {
	private AmazonBookCoverGrabber mImageGrabber;
	private BookThingISBNFinder mISBNFinder;
	
	public ImageGrabber(String directory,int priority,String destination){
		String source = directory + "/priority" + priority + "_urls";
		mImageGrabber = new AmazonBookCoverGrabber(destination);
		mISBNFinder = new BookThingISBNFinder(source,mImageGrabber);
	}
	
	public void run(){
		mISBNFinder.fillQueue();
		mISBNFinder.start();
		mImageGrabber.start();
	}
	
	public static void main(String[] args){
		ImageGrabber grabber = new ImageGrabber(args[0],Integer.parseInt(args[1]),args[2]);
		grabber.run();
	}
}
