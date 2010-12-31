package com.stromberglabs.visual.ip.dao;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.stromberglabs.cluster.Clusterable;
import com.stromberglabs.index.FileIndex;
import com.stromberglabs.util.WorkQueue;
import com.stromberglabs.util.file.FileFilter;
import com.stromberglabs.util.file.FileFinder;

public class ThreadedInterestPointGenerator {
	private static final Logger logger = Logger.getLogger(ThreadedInterestPointGenerator.class);
	
	private WorkQueue mQueue = new WorkQueue(10);
	private InterestPointDAO mIPDAO;
	
	public ThreadedInterestPointGenerator(File rootDirectory, int groupId, InterestPointDAO dao){
		List<File> files = findFiles(rootDirectory);
		for ( File f : files )
			mQueue.enqueue(new InterestPointExtractor(f.getAbsolutePath(),groupId));
		mIPDAO = dao;
	}
	
	public void start(){
		mQueue.start();
		int remaining = mQueue.remainingItems();
		int duplicateRemainingCount = 0;
		while ( mQueue.remainingItems() > 0 ){
			logger.debug(mQueue.remainingItems());
			if ( remaining == mQueue.remainingItems() ){
				duplicateRemainingCount++;
				if ( duplicateRemainingCount > 20 ){
					logger.info("Breaking out of interest point generation because was stuck on the remaining count of: " + remaining);
					break;
				}
			} else {
				remaining = mQueue.remainingItems();
				duplicateRemainingCount = 0;
			}
			try {
				Thread.sleep(1000*10);
			} catch (InterruptedException e) {
				logger.fatal(e,e);
			}
		}
	}
	
	public void stop(){
		mQueue.stop();
	}
	
	private static List<File> findFiles(File folder) {
		// Get the list of all files in the directory
		List<FileFilter> filters = new ArrayList<FileFilter>(3);
		filters.add(new FileFilter(".*\\.jpg"));
		filters.add(new FileFilter(".*\\.gif"));
		filters.add(new FileFilter(".*\\.png"));

		FileFinder finder = new FileFinder(folder, filters);
		return finder.getFiles();
	}
	
	private class InterestPointExtractor implements Runnable {
		private String mFile;
		private int mGroupId;

		public InterestPointExtractor(String file, int groupId) {
			mFile = file;
			mGroupId = groupId;
		}
		
		public void run(){
			String file = FileIndex.extractName(mFile);
			if ( logger.isDebugEnabled() )
				logger.debug("Examining file: " + file + ", " + mFile);
//			try {
//				Class.forName("com.mysql.jdbc.Driver");
//				mConnection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/imagesearch","root","sunshine");
//				mSaveStmt = mConnection.prepareStatement(mSaveSql);
//				mCountStmt = mConnection.prepareStatement(mCountSql);
//			} catch (Exception e){
//				e.printStackTrace();
//			}
			synchronized ( mIPDAO ){
				if ( !mIPDAO.hasInterestPoints(file) ){
					try {
						List<Clusterable> points = mIPDAO.getCreator().getPoints(mFile);
						System.out.println(points.size() + " interest points");
						for ( Clusterable point : points )
							mIPDAO.saveInterestPoint(point.getLocation(),file,mGroupId);
						mIPDAO.saveFile(file,mGroupId);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					logger.debug("Skipping file " + mFile + " because it already has points");
				}
			}
		}
	}
	
	public static void main(String args[]){
		if ( args.length != 3 ){
			System.out.println("Usage: ant pointgeneration -Dargs=\"image_director group_id sift|surf\"");
			System.exit(0);
		}
		InterestPointDAO dao = null;
		if ( args[2].equals("sift") ){
			dao = new SIFTInterestPointDAO();
		} else if ( args[2].equals("surf") ) {
			dao = new SURFInterestPointDAO();
		}
		ThreadedInterestPointGenerator factory = new ThreadedInterestPointGenerator(new File(args[0]),Integer.parseInt(args[1]),dao);
		factory.start();
	}
}
