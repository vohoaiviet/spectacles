package com.stromberglabs.visual.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.stromberglabs.tree.KMeansTree;
import com.stromberglabs.tree.KMeansTreeNode;
import com.stromberglabs.util.SizedPriorityQueue;
import com.stromberglabs.visual.ip.cache.InterestPointCache;
import com.stromberglabs.visual.tree.VocabTreeManager;

public class L1ImageWordIndex extends AbstractImageWordIndex {

	private static final long serialVersionUID = 1L;

	public L1ImageWordIndex(KMeansTree tree,InterestPointCache cache) {
		super(tree,cache);
	}

	@Override
	public double calcFinalScore(Score score) {
		return 2 + score.getScore();
	}

	@Override
	public String getNormType() {
		return "L1";
	}

	@Override
	protected double getNormVector(List<KMeansTreeNode> words) {
		double count = 0;
		for ( KMeansTreeNode node : words ){
			count += node.getCurrentItemCount();
		}
		return count;
	}

	@Override
	public double getQminusD(double Q, double D) {
		return Math.abs(Q-D) - Math.abs(Q) - Math.abs(D);
	}

	@Override
	void getIndexDebuggingInformation() {
		//Every file should have a weight that adds up to 1
		Map<String,Double> totalWeightedCounts = new HashMap<String,Double>();
		for ( FileList list : mWordCounts.values() ){
			for ( String file : list.getFiles() ){
				double count = list.getFileWordCount(file);
				if ( totalWeightedCounts.containsKey(file) ){
					count += totalWeightedCounts.get(file);
				}
				totalWeightedCounts.put(file, count);
			}
		}
		for ( String key : totalWeightedCounts.keySet() ){
			System.out.println(key + ", should be 1: " + totalWeightedCounts.get(key));
		}
	}

	@Override
	protected SizedPriorityQueue<Score> chooseBestScores(Map<String,Score> scores, int topNum) {
		SizedPriorityQueue<Score> bestScores = new SizedPriorityQueue<Score>(topNum,true,new ScoreComparator());
		for ( String file : scores.keySet() ){
			Score score = scores.get(file);
			double finalScore = calcFinalScore(scores.get(file));
			score.setScore(finalScore);
			bestScores.add(score);
		}
		return bestScores;
	}
	
	public static void main(String args[]){
		System.out.println("Loading index");
		L1ImageWordIndex index = (L1ImageWordIndex)VocabTreeManager.loadIndex("G:\\index.bin");
		System.out.println("index loaded");
		Map<Integer,FileList> lists = index.getItems();
		FileList list = lists.get(174162);
		System.out.println("list = " + list.getFileCount() + ", " + list.getTotalCount());
	}
}
