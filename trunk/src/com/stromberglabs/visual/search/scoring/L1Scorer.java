package com.stromberglabs.visual.search.scoring;

import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Map;

import com.stromberglabs.util.SizedPriorityQueue;
import com.stromberglabs.visual.search.Score;
import com.stromberglabs.visual.search.ScoreComparator;

public class L1Scorer extends AbstractScorer {

	@Override
	protected double getNormVector(TIntIntHashMap words) {
		double count = 0;
		for ( Integer nodeCount : words.values() ){
			count += nodeCount;
		}
		return count;
	}

	@Override
	public double getQminusD(double Q, double D) {
		return Math.abs(Q-D) - Math.abs(Q) - Math.abs(D);
	}

	@Override
	protected SizedPriorityQueue<Score> chooseBestScores(
			Map<String, Score> scores, int topNum) {
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
	public double calcFinalScore(Score score) {
		return 2 + score.getScore();
	}
}
