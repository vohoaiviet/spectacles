package com.stromberglabs.visual.search;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.stromberglabs.cluster.Clusterable;
import com.stromberglabs.tree.KMeansTree;
import com.stromberglabs.tree.KMeansTreeNode;
import com.stromberglabs.util.SizedPriorityQueue;
import com.stromberglabs.visual.ip.cache.InterestPointCache;

public abstract class AbstractImageWordIndex implements ImageWordIndex {
	private static final long serialVersionUID = 1L;

	private static Logger sLogger = Logger.getLogger(AbstractImageWordIndex.class);
	
	protected Map<Integer,FileList> mWordCounts;
	protected KMeansTree mVocabTree;

	public AbstractImageWordIndex(KMeansTree tree, InterestPointCache c){
		mWordCounts = new HashMap<Integer, FileList>();
		
		mVocabTree = tree;
		
		buildIndex(c);
	}
	
	private void buildIndex(InterestPointCache c){
		if ( sLogger.isDebugEnabled() ){
			sLogger.debug("Number of files to create an index from: " + c.getFiles().size());
		}
		int count = 0;
		for ( String file : c.getFiles() ){
			mVocabTree.reset();//clear out any previous counts in the tree
			List<Clusterable> points = c.getPoints(file);
			mVocabTree.addImage(points);
//			System.out.println("added all " + points.size() + " points to the tree");
			//Keeps track of the normalization vector for this image
			List<KMeansTreeNode> words = getWords();
			double normVector = getNormVector(words);
			double totalWeightedCount = 0;
			double totalPointCount = 0;
			if ( sLogger.isDebugEnabled() ) {
				if ( count % 1000 == 0 ) { sLogger.debug("Currently on file: " + count); }
			}
			count++;
			for ( KMeansTreeNode node : mVocabTree.getAllNodes() ){
				int currentCount = node.getCurrentItemCount();
				if ( useAsIndexWord(node) ){ //note that if you choose to include the leaf nodes, the total count of the FileList is going to be something other than points.size()
					FileList list = null;
					if ( mWordCounts.containsKey(node.getId()) ){
						list = mWordCounts.get(node.getId());
					} else {
						list = new FileList();
					}
					double weightedWord = 0;
					weightedWord = (double)currentCount/normVector;
//					System.out.println(currentCount);
					list.addFile(file,currentCount,weightedWord);
					totalWeightedCount += weightedWord;
					totalPointCount += currentCount;
					mWordCounts.put(node.getId(),list);
				}
			}
//			if ( NORM_TYPE == "L1" ){
//				System.out.println("This should be 1: " + totalWeightedCount); // if using L1 norm
//			} else if ( NORM_TYPE == "L2" ){
//				System.out.println("This should be " + (double)points.size()/L2NormVector + ":" + totalWeightedCount); // if using L2 norm
//			}
//			System.out.println("This should be " + points.size() + ": " + totalPointCount);
//			System.out.println("File " + file + " has a total weighted word count of: " + totalWeightedCount + ", total point count of " + points.size());
			//System.out.println("this was file " + count++ + " - " + System.currentTimeMillis());
		}
		if ( sLogger.isDebugEnabled() ){
			sLogger.debug("creating list weights");
		}
		count = 0;
		for ( String file : c.getFiles() ){
			List<Clusterable> points = c.getPoints(file);
			mVocabTree.addImage(points);
			if ( count % 1000 == 0 ) { System.out.println(count); }
			count++;
		}
		
		double totalFiles = (double)c.getFiles().size();
		for ( Integer word : mWordCounts.keySet() ){
			FileList list = mWordCounts.get(word);
			list.setWeight(Math.log(totalFiles / (double)list.getFileCount()));
		}
		if ( sLogger.isDebugEnabled() ){
			sLogger.debug("done with list weights");
		}
		//getIndexDebuggingInformation();
	}
	
	abstract protected double getNormVector(List<KMeansTreeNode> words);
	
	abstract public String getNormType();
	
	abstract void getIndexDebuggingInformation();
	
	private boolean useAsIndexWord(KMeansTreeNode node){
		return node.isLeafNode() && node.getCurrentItemCount() > 0;
	}
	
	public SizedPriorityQueue<Score> findClosest(List<Clusterable> points){
		return findClosest(points,50);
	}
	
	public SizedPriorityQueue<Score> findClosest(List<Clusterable> points, int trackNumber){
		Map<String,Score> scores = new HashMap<String,Score>();
		mVocabTree.reset();
		mVocabTree.addImage(points);
		//The query image norm vector
		List<KMeansTreeNode> words = getWords();
		double QueryNormVector = getNormVector(words);
		for ( KMeansTreeNode node : mVocabTree.getAllNodes() ){
			if ( useAsIndexWord(node) ){
				if ( mWordCounts.containsKey(node.getId()) ){
					//System.out.println("Examining node " + node.getId());
					FileList list = mWordCounts.get(node.getId());
					double Q = list.getWeight() * (node.getCurrentItemCount() / QueryNormVector);
					for ( String file : list.getFiles() ){
						double D = list.getWeight() * list.getFileWordCount(file);
						//At this point q and d are the normalized vectors we want to add to the current score total
						//double d = list.getFileWordCount(file);
						double scoreToAdd = getQminusD(Q,D);
//						if ( NORM_TYPE == "L2" ){
//							scoreToAdd = q*d;
//						} else  if ( NORM_TYPE == "L1" ){
//							scoreToAdd = Math.abs(q-d) - Math.abs(q) - Math.abs(d);
//						}
						if ( scores.containsKey(file) ){
							Score existingScore = scores.get(file);
							existingScore.addToScore(scoreToAdd);
//							System.out.println("File " + file + " has score " + (existingScore + scoreToAdd));
							//scores.put(file,existingScore + scoreToAdd);
						} else {
							Score score = new Score(file,scoreToAdd);
							scores.put(file,score);
						}
					}
				} else {
					//This word was not present in the tree but was in the query image
				}
			}
		}
		return chooseBestScores(scores,trackNumber);
//		double highest = 0;
//		String bestImage = null;
//		for ( String file : scores.keySet() ){
//			double score = scores.get(file);
//			double actualScore = calcFinalScore(score);
////			if ( NORM_TYPE == "L2" ){
////				actualScore = 2 - 2 * score;
////			} else if ( NORM_TYPE == "L1" ){
////				actualScore = 2 + score;
////			}
//			
//			//System.out.println("File " + file + " had score " + score + " and actual score " + actualScore);
//			if ( actualScore > highest ) {
//				highest = actualScore;
//				bestImage = file;
//			}
//		}
//		
//		return bestImage;
	}
	
	abstract protected SizedPriorityQueue<Score> chooseBestScores(Map<String,Score> scores,int topNum);
	
	abstract public double getQminusD(double Q,double D);

	abstract public double calcFinalScore(Score score);

	public int numWords(){
		return mWordCounts.size();
	}
	
	public Map<Integer,FileList> getLists(){
		return mWordCounts;
	}
	
	public List<KMeansTreeNode> getWords(){
		List<KMeansTreeNode> words = new LinkedList<KMeansTreeNode>();
		for ( KMeansTreeNode node : mVocabTree.getAllNodes() ){
			if ( useAsIndexWord(node) ){
				words.add(node);
			}
		}
		return words;
	}
	
	public Map<Integer,FileList> getItems(){
		return mWordCounts;
	}
	
	public KMeansTree getTree(){
		return mVocabTree;
	}
}
