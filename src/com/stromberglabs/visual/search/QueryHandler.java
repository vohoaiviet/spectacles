package com.stromberglabs.visual.search;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.stromberglabs.index.WordInvertedIndex;
import com.stromberglabs.cluster.Clusterable;
import com.stromberglabs.tree.query.QueryTree;
import com.stromberglabs.util.SizedPriorityQueue;
import com.stromberglabs.util.StopWatch;
import com.stromberglabs.visual.ip.creator.InterestPointCreator;
import com.stromberglabs.visual.ip.creator.SIFTInterestPointCreator;
import com.stromberglabs.visual.ip.creator.SURFInterestPointCreator;
import com.stromberglabs.visual.ip.dao.InterestPointDAO;
import com.stromberglabs.visual.ip.dao.SIFTInterestPointDAO;
import com.stromberglabs.visual.ip.dao.SURFInterestPointDAO;
import com.stromberglabs.visual.search.scoring.L1Scorer;
import com.stromberglabs.visual.search.scoring.QueryScorer;

/**
 * A class that is designed to do all of the dirty work of loading up the trees, indexes, etc.
 * Then it warms them up and lets you request matches.
 * 
 * @author Andrew
 *
 */
public class QueryHandler {
	private static Logger sLogger = Logger.getLogger(QueryHandler.class);
	private QueryTree mTree;
	private WordInvertedIndex mIndex;
	private QueryScorer mScorer;
	private InterestPointCreator mInterestPointCreator;
	
	private static QueryHandler handler;
	
	public static void initHandler(String directory){
		if ( handler == null ){
			handler = new QueryHandler(directory);
		}
	}
	
	public static QueryHandler getHandler(){
		if ( handler == null ){
			throw new RuntimeException("The QueryHandler is not property initialized");
		}
		return handler;
	}
	
	private QueryHandler(String directory){
		try {
			mTree = new QueryTree(directory);
			if ( sLogger.isInfoEnabled() ){
				sLogger.info("Starting cache warmup");
			}
			StopWatch watch = new StopWatch(true);
			//mTree.warmupCache();
			if ( sLogger.isInfoEnabled() ){
				sLogger.info("Cache warmup complete, took: " + watch.prettyTime());
			}
			if ( mTree.getDimensionality() == 128 ){
				mInterestPointCreator = new SIFTInterestPointCreator();
			} else {
				mInterestPointCreator = new SURFInterestPointCreator();
			}
		} catch ( FileNotFoundException e ){
			sLogger.fatal("The query tree file was not found, please check your configuration",e);
		} catch ( IOException e ){
			sLogger.fatal("The query tree file appears invalid",e);
		}
		try {
			mIndex = new WordInvertedIndex(directory);
		} catch (IOException e) {
			sLogger.fatal("The word index files appear to be invalid",e);
		}
		mScorer = new L1Scorer();
	}
	
	public SizedPriorityQueue<Score> findBest(File file) throws IOException {
		return findBest(file,20);
	}
	
	public SizedPriorityQueue<Score> findBest(File file, int numResults) throws IOException {
		if ( file == null ) throw new IOException("Query file is null");
		StopWatch timer = new StopWatch(true);
		List<Clusterable> points = mInterestPointCreator.getPoints(file.getAbsolutePath());
		if ( sLogger.isInfoEnabled() ){
			sLogger.info(points.size() + " query points created in " + timer.prettyTime());
		}
		timer.reset();
		//TODO: Check to see if this was a giant mistake to add this back in
		mTree.reset();
		mTree.addImage(points);
		if ( sLogger.isInfoEnabled() ){
			sLogger.info("Query points added to tree in " + timer.prettyTime());
		}
		timer.reset();
		SizedPriorityQueue<Score> scores = mScorer.findClosest(mTree.getCurrentWordCounts(),mIndex,numResults);
		if ( sLogger.isInfoEnabled() ){
			sLogger.info("Scores calculated in " + timer.prettyTime());
		}
		System.out.println("hit ratio for query tree: " + mTree.getCacheHitRatio());
		System.out.println("fill ratio is: " + mTree.getCacheFillRatio());
		return scores;
	}
	
	public Map<String,String> getMetaInformation(){
		Map<String, String> info = new HashMap<String, String>();
		info.put("treesize",String.valueOf(mTree.getNumWords()));
		info.put("normalization","L1");
		info.put("numfiles",String.valueOf(mIndex.getNumFiles()));
		return info;
	}
	
	public static void main(String args[]){
		QueryHandler handler = new QueryHandler("H:\\web-index\\");
		System.out.println("Loaded handler");
		try {
			SizedPriorityQueue<Score> scores = handler.findBest(new File("H:\\bookbench\\normal\\0380818191.jpg"));
//			SizedPriorityQueue<Score> scores = handler.findBest(new File("H:\\bookbench\\normal\\013228751X.jpg"));
			while ( scores.size() > 0 ){
				Score topScore = scores.pop();
				System.out.println(topScore.getScore());
				System.out.println(topScore.getTarget());
				System.out.println(topScore.getExplanation());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
