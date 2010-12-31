package com.stromberglabs.imgsrc.amazonbooks;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import com.stromberglabs.util.WorkQueue;

public class AmazonBookCoverGrabber {
	private File mSource;
	private String mOutputDestination;
	private WorkQueue queue;
	
	public AmazonBookCoverGrabber(String destination, String inputFile){
		this(destination);
		mSource = new File(inputFile);
	}
	
	public AmazonBookCoverGrabber(String destination){
		mOutputDestination = destination;
		queue = new WorkQueue(10);
	}
	
	public void fillQueue(){
		try {
			BufferedReader str = new BufferedReader(new FileReader(mSource));
			String line = null;
			while ( (line = str.readLine()) != null ){
				queue.enqueue(new ImageGrabber(line,mOutputDestination));
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		//queue.enqueue(new ImageGrabber());
	}
	
	public void addISBN(String isbn){
		queue.enqueue(new ImageGrabber(isbn,mOutputDestination));
	}
	
	public void start(){
		queue.start();
	}
	
	private class ImageGrabber implements Runnable {
		private String mDestination;
		private String mUrl;
		public ImageGrabber(String isbn, String destination){
			mDestination = destination + "/" + isbn + ".jpg";
			mUrl = "http://images.amazon.com/images/P/" + isbn + ".01.LZZ.jpg";
//			System.out.println(mUrl);
		}
		
		public void run() {
			try {
				BufferedImage img = ImageIO.read(new URL(mUrl));
				File outputfile = new File(mDestination);
				if ( img.getHeight() > 0 && img.getWidth() > 0 ){
					if ( img.getHeight() * img.getWidth() > 22000 ){
						ImageIO.write(img,"jpg",outputfile);
					} else {
						System.out.println("Small: " + mUrl);
					}
				} else {
					System.out.println("Bad: " + mUrl);
				}
			} catch (IOException e){
				System.out.println("Failed: " + mUrl);
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String args[]){
		AmazonBookCoverGrabber grabber = new AmazonBookCoverGrabber("H:\\test\\","H:\\bookbench\\amzn\\isbns.txt");
		grabber.fillQueue();
		grabber.start();
	}
}
