package com.stromberglabs.imgsrc.bookthing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;

import com.stromberglabs.imgsrc.amazonbooks.AmazonBookCoverGrabber;
import com.stromberglabs.util.WorkQueue;

public class BookThingISBNFinder {
	private File mSource;
	private File mDestination;
	
	private WorkQueue queue;
	
	private LinkedList<String> isbns;
	
	private AmazonBookCoverGrabber mImageGrabber;
	
	public BookThingISBNFinder(String source,String destinationDir){
		mSource = new File(source);
		mDestination = new File(destinationDir + File.separatorChar + mSource.getName() + "_isbns");
		queue = new WorkQueue(10);
		isbns = new LinkedList<String>();
	}
	
	public BookThingISBNFinder(String source, AmazonBookCoverGrabber grabber){
		mSource = new File(source);
		mImageGrabber = grabber;
		queue = new WorkQueue(10);
	}
	
	public void fillQueue(){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(mSource));
			String line = null;
			int count = 0;
			while (( line = reader.readLine()) != null){
				URL url = new URL(line);
				queue.enqueue(new BookThingISBNWorker(url));
				count++;
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void start(){
		queue.start();
	}
	
	private class BookThingISBNWorker implements Runnable {
		private URL mUrl;
		
		public BookThingISBNWorker(URL url) {
			mUrl = url;
		}
		
		public void run() {
			try {
				HttpURLConnection connection = (HttpURLConnection)mUrl.openConnection();
				connection.setRequestProperty ("User-agent","googlebot - Please don't ban me, just want some isbns, if crawling too quickly, contact me at lvoksphaysbto@mailinator.com");
				if ( connection.getResponseCode() == 200 ){
					BufferedReader str = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					String responseLine = null;
					String isbn = null;
					try {
						while ( (responseLine = str.readLine()) != null ){
							if ( responseLine.contains(">direct<") ){
								//System.out.println(responseLine);
								String[] piecesA = responseLine.split("<a href=\"http://www.amazon.com/exec/obidos/ASIN/");
								String[] piecesB = piecesA[1].split("/ref=nosim/librarythin08-20");
								isbn = piecesB[0];
							}
						}
						if ( isbn != null ){
							if ( mImageGrabber != null ){
								mImageGrabber.addISBN(isbn);
							} else {
								//System.out.println(isbn);
								synchronized (isbns){
									isbns.add(isbn);
								}
								if ( isbns.size() % 100 == 0 ) { System.out.println(isbns.size() + ", " + queue.remainingItems()); }
							}
						}
					} catch (IOException e){
						e.printStackTrace();
					}
				} else {
					System.out.println("Error: " + connection.getResponseCode());
				}
				Thread.sleep(100);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public void writeISBNS(){
		System.out.println("writing out to file");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(mDestination));
			for ( String isbn : isbns ){
				writer.write(isbn);
				writer.newLine();
			}
			writer.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
	public boolean hasItems(){
		synchronized(queue){
			return queue.remainingItems() > 0;
		}
	}
	
	public static void main(String args[]){
		BookThingISBNFinder finder = new BookThingISBNFinder(
				"C:\\workspace\\JOpenSurf\\src\\com\\stromberglabs\\imgsrc\\bookthing\\urls\\priority0_urls",
				"/home/astromberg/JOpenSurf"
			);
		finder.fillQueue();
		finder.start();
		while(finder.hasItems()){
			try {
				Thread.sleep(100);
			} catch(InterruptedException e){
				
			}
		}
		finder.writeISBNS();
		System.exit(0);
	}
}
