package com.stromberglabs.visual.search;

import java.util.List;
import java.util.Map;

import com.stromberglabs.tree.KMeansTree;
import com.stromberglabs.tree.KMeansTreeNode;
import com.stromberglabs.util.SizedPriorityQueue;
import com.stromberglabs.visual.ip.cache.FileMapInterestPointCache;

public class L2ImageWordIndex extends AbstractImageWordIndex {

	private static final long serialVersionUID = 1L;

	public L2ImageWordIndex(KMeansTree tree,FileMapInterestPointCache cache) {
		super(tree,cache);
	}

	@Override
	public double calcFinalScore(Score score) {
		return 2 - 2 * score.getScore();
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

	@Override
	void getIndexDebuggingInformation() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getNormType() {
		return "L2";
	}

	@Override
	protected double getNormVector(List<KMeansTreeNode> words) {
		double count = 0;
		for ( KMeansTreeNode node : words ){
			count += node.getCurrentItemCount()*node.getCurrentItemCount();
		}
		return Math.sqrt(count);
	}

	@Override
	public double getQminusD(double Q, double D) {
		return Q*D;
	}

}
