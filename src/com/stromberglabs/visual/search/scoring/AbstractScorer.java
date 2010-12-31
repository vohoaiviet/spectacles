package com.stromberglabs.visual.search.scoring;

import gnu.trove.map.hash.TIntIntHashMap;

import java.util.HashMap;
import java.util.Map;

import com.stromberglabs.index.WordInvertedIndex;
import com.stromberglabs.util.SizedPriorityQueue;
import com.stromberglabs.visual.search.FileList;
import com.stromberglabs.visual.search.Score;

public abstract class AbstractScorer implements QueryScorer {
	protected static final boolean EXPLAIN = false;
	
	public SizedPriorityQueue<Score> findClosest(
			TIntIntHashMap words, WordInvertedIndex index,
			int numResults) {
		Map<String,Score> scores = new HashMap<String,Score>();
		//The query image norm vector
		double QueryNormVector = getNormVector(words);
		for ( int wordId : words.keys() ){
			FileList list = index.getFileList(wordId);
//			System.out.println("Examining node " + wordId);
			double Q = list.getWeight() * (words.get(wordId) / QueryNormVector);
			for ( String file : list.getFiles() ){
				double D = list.getWeight() * list.getFileWordCount(file);
				//At this point q and d are the normalized vectors we want to add to the current score total
				//double d = list.getFileWordCount(file);
				double scoreToAdd = getQminusD(Q,D);
				if ( scores.containsKey(file) ){
					Score existingScore = scores.get(file);
					existingScore.addToScore(scoreToAdd);
					if ( EXPLAIN ){
						existingScore.addValueToExplanation(wordId,QueryNormVector,words.get(wordId),list.getWeight(),list.getFileWordCount(file),scoreToAdd);
					}
				} else {
					Score score = new Score(file,scoreToAdd);
					if ( EXPLAIN ){
						score.addValueToExplanation(wordId,QueryNormVector,words.get(wordId),list.getWeight(),list.getFileWordCount(file),scoreToAdd);
					}
					scores.put(file,score);
				}
			}
		}
		return chooseBestScores(scores,numResults);
	}
	
	abstract protected double getNormVector(TIntIntHashMap words);
	
	abstract public double getQminusD(double Q,double D);
	
	abstract protected SizedPriorityQueue<Score> chooseBestScores(Map<String,Score> scores,int topNum);
	
	abstract public double calcFinalScore(Score score);
}
