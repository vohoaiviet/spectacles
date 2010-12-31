package com.stromberglabs.visual.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

public class MultiIndexFinder {
	private int mNumTrees;
	private List<KMeansTree> mTrees;
	private List<ImageWordIndex> mIndexes;
	
	private FileMapInterestPointCache mTreeCache;
	private FileMapInterestPointCache mIndexCache;
	
	public MultiIndexFinder(String sourceDirectory, int numTrees, int maxFiles){
		mNumTrees = numTrees;
		mTrees = new LinkedList<KMeansTree>();
		mIndexes = new LinkedList<ImageWordIndex>();
		//InterestPointFactory.createInterestPoints(sourceDirectory);
		//System.out.println("initting cache");
		//mCache = new FileMapInterestPointCache(1100000,sourceDirectory,maxFiles,0);
		
		List<Clusterable> points = new ArrayList<Clusterable>(mTreeCache.getIds().size());
		for ( long id : mTreeCache.getIds() ){
			points.add(new VirtualInterestPoint(id,mTreeCache));
		}
		for ( int i = 0; i < mNumTrees; i++ ){
			//System.out.println("Building tree " + i);
			mTrees.add(new KMeansTree(points,6,10,new KMeansClusterer()));
			//System.out.println("Building index " + i);
			mIndexes.add(new L1ImageWordIndex(mTrees.get(i),mIndexCache));
		}
	}
	
	public String findBestMatch(String file, int topNResults){
		String lowest = "";
		try {
			List<SizedPriorityQueue<Score>> queues = new ArrayList<SizedPriorityQueue<Score>>(mIndexes.size());
			InterestPointCreator creator = new SURFInterestPointCreator();
			List<Clusterable> points = creator.getPoints(file);
			for ( ImageWordIndex index : mIndexes ){
				queues.add(index.findClosest(points,topNResults));
			}
			Map<String,Double> scores = new HashMap<String,Double>();
			Map<String,Integer> counts = new HashMap<String,Integer>();
			for ( SizedPriorityQueue<Score> queue : queues ){
				double rank = 0;
				for ( Score score : queue.getAllScores() ){
					if ( scores.containsKey(score.getTarget()) ){
						scores.put(score.getTarget(),scores.get(score.getTarget())+score.getScore());
						counts.put(score.getTarget(),counts.get(score.getTarget())+1);
					} else {
						scores.put(score.getTarget(),score.getScore());
						counts.put(score.getTarget(),1);
					}
					rank++;
				}
			}
			double lowestScore = Double.MAX_VALUE;
			int minNumTrees = 3;
			if ( mNumTrees > 5 && mNumTrees <= 7 )
				minNumTrees = 4;
			else if ( mNumTrees > 7 && mNumTrees <= 9 )
				minNumTrees = 6;
			else if ( mNumTrees > 9 && mNumTrees <= 11 )
				minNumTrees = 7;
			for ( String f : scores.keySet() ){
				//System.out.println(f + "," + scores.get(f));
				double score = (scores.get(f)/counts.get(f));
				if ( counts.get(f) >= minNumTrees &&
						score < lowestScore ){
					lowest = f;
					lowestScore = score;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lowest;
	}
	
	public static void main(String args[]){
		//int testSize = 1000;
		String indexLocation = "H:\\ukbench\\training2\\";
		String searchLocation = "H:\\ukbench\\query\\";
		
//		int[] testSizes = {2000};
//		int[] numTreesTests = {3,4,5,6,7,8,9,10};
//		int[] examineTopNResultsTests = {5,10,20};
		int[] testSizes = {10200};
		int[] numTreesTests = {1};//3,4,5,10};
		int[] examineTopNResultsTests = {5,20};
		for ( int testSize : testSizes ){
			for ( int numTrees : numTreesTests ){
				for ( int numResults : examineTopNResultsTests ){
					System.out.println(testSize + ", " + numTrees + ", " + numResults);
					MultiIndexFinder finder = new MultiIndexFinder(indexLocation,numTrees,testSize/4);
					
					List<FileFilter> filters = new ArrayList<FileFilter>();
					filters.add(new FileFilter(".*\\.jpg"));
					FileFinder ffinder = new FileFinder(searchLocation,filters);
					List<File> files = ffinder.getFiles();
					int numRight = 0;
					int numWrong = 0;
					int count = 0;
					for ( File file : files ){
						String foundFile = finder.findBestMatch(file.getAbsolutePath(),numResults);
						//System.out.println("for " + file.getAbsolutePath() + ", found " + foundFile);
						if ( !"".equals(foundFile) && IndexFinder.isCorrectFile(file.getAbsolutePath(),foundFile) ){
							numRight++;
						} else {
							numWrong++;
						}
						count++;
						if ( count >= (testSize *3)/4 ) break;
					}
					System.out.println(numRight/(float)(numRight+numWrong));
					System.gc();
				}
				System.gc();
			}
		}
	}
}
