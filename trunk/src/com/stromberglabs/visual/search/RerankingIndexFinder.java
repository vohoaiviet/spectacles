package com.stromberglabs.visual.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.stromberglabs.cluster.Clusterable;
import com.stromberglabs.cluster.KMeansClusterer;
import com.stromberglabs.tree.KMeansTree;
import com.stromberglabs.util.SizedPriorityQueue;
import com.stromberglabs.util.file.FileFilter;
import com.stromberglabs.util.file.FileFinder;
import com.stromberglabs.visual.ip.VirtualInterestPoint;
import com.stromberglabs.visual.ip.cache.FileMapInterestPointCache;
import com.stromberglabs.visual.ip.creator.InterestPointCreator;
import com.stromberglabs.visual.ip.creator.SURFInterestPointCreator;
import com.stromberglabs.visual.ip.dao.SURFInterestPointDAO;

public abstract class RerankingIndexFinder {
	private int mMaxHeight = 8;
	private int mBranchFactor = 10;
	private int[] mTestSizes = {100};
	//private int[] mTestSizes = {1000,2000,3000,4000,10200};
	private String mTrainingFilesLocation = "H:\\ukbench\\training2\\";
	private String mQueryFilesLocation = "H:\\ukbench\\query\\";
	
	protected FileMapInterestPointCache mCache;
	
	public RerankingIndexFinder(){
		
	}
	
	public void runTest(){
		System.out.println("TESTING USING BRANCH FACTOR OF " + mBranchFactor + ", HEIGHT OF " + mMaxHeight);
		for ( int numFilesToTest : mTestSizes ){
			int numCorrect = 0;
			int numWrong = 0;

			mCache = new FileMapInterestPointCache(1100000,mTrainingFilesLocation,numFilesToTest/4,0);
			List<Clusterable> points = new ArrayList<Clusterable>(mCache.getIds().size());
			for ( Long id : mCache.getIds() ){
				points.add(new VirtualInterestPoint(id,mCache));
			}
			System.out.println("Done with points, " + points.size());
			KMeansTree tree = new KMeansTree(points,mBranchFactor,mMaxHeight,new KMeansClusterer());
			System.out.println("Done with tree, height: " + tree.getTreeHeight() + ", num nodes: " + tree.getAllNodes().size());
			ImageWordIndex index = new L1ImageWordIndex(tree,mCache);
			System.out.println("Done with index");
			List<FileFilter> filters = new ArrayList<FileFilter>();
			filters.add(new FileFilter(".*\\.jpg"));
			FileFinder finder = new FileFinder(mQueryFilesLocation,filters);
			List<File> files = finder.getFiles();
			for ( int i = 0; i < files.size() && i < (numFilesToTest * 3)/4; i++ ){
				try {
					File f = files.get(i);
					InterestPointCreator creator = new SURFInterestPointCreator();
					List<Clusterable> filePoints = creator.getPoints(f.getAbsolutePath());
					SizedPriorityQueue<Score> bestScores = index.findClosest(filePoints);
					Score topScore = bestScores.poll();
					boolean isCorrect = isCorrectFile(f.getAbsolutePath(),topScore.getTarget());
					double topTwoDiff = bestScores.getAllScores().get(1).getScore() - bestScores.getAllScores().get(0).getScore();
					boolean shouldRerank = topTwoDiff >= 0.133D;
					if ( shouldRerank ){
						Score topRerank = getTopRerankedScore(filePoints,bestScores);
						isCorrect = isCorrectFile(f.getAbsolutePath(),topRerank.getTarget());
					}
					if ( isCorrect ){
						numCorrect++;
					} else {
						numWrong++;
					}
				} catch (IOException e){
					e.printStackTrace();
				}
			}
			float overallAccuracy = numCorrect / (float)(numCorrect + numWrong);
			System.out.println("Combined accuracy: " + overallAccuracy);
		}
	}
	
	private Score getTopRerankedScore(List<Clusterable> currentPoints,SizedPriorityQueue<Score> topScores){
		return rerankScores(currentPoints,topScores).poll();
	}
	
	protected abstract SizedPriorityQueue<Score> rerankScores(List<Clusterable> currentPoints,SizedPriorityQueue<Score> topScores);
	
//	try {
//		//System.out.println("Querying for image: " + f.getAbsolutePath());
//		List<Clusterable> filePoints = InterestPointFactory.getPoints(f.getAbsolutePath());
//		//List<Clusterable> filePoints = queryCache.getInterestPoints(f);
//		SizedPriorityQueue<Score> bestScores = index.findClosest(filePoints);
//		if ( isCorrectFile(f.getAbsolutePath(),bestScores.poll().getTarget())){
//			numCorrect++;
//		} else {
//			numWrong++;
//		}
//		//System.out.println("For file " + f.getAbsolutePath() + " the best image found was " + bestScores.poll().getTarget() + ", is correct? " + isCorrectFile(f.getAbsolutePath(),bestScores.poll().getTarget()));
//	} catch (IOException e) {
//		e.printStackTrace();
//	}

	
	public static void main(String args[]){
		InterestPointReranker finder = new InterestPointReranker();
		finder.runTest();
	}
	//
	//
	public static boolean isCorrectFile(String queryFile, String returnedFile){
		int queryNumber = getImageNumber(queryFile);
		int foundNumber = getImageNumber(returnedFile);
		boolean isGood = (foundNumber < queryNumber && foundNumber + 4 > queryNumber);
		//System.out.println("Is " + returnedFile + " the correct one for " + queryFile + "? " + isGood);
		return isGood;
	}
	
	public static int getImageNumber(String filename){
		int start = filename.indexOf("ukbench",filename.indexOf("ukbench") + 1) + 7;
		int end = start + 5;
		return Integer.parseInt(filename.substring(start,end));
	}
}
