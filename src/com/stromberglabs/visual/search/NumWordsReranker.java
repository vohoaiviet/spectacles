package com.stromberglabs.visual.search;

import java.util.List;

import com.stromberglabs.cluster.Clusterable;
import com.stromberglabs.util.SizedPriorityQueue;

public class NumWordsReranker extends RerankingIndexFinder {

	@Override
	protected SizedPriorityQueue<Score> rerankScores(List<Clusterable> currentPoints,SizedPriorityQueue<Score> topScores) {
		SizedPriorityQueue<Score> rerankedScores = new SizedPriorityQueue<Score>(5,false,new CountComparator());
		for ( int i = 0; i < 5; i++ ){
			rerankedScores.add(topScores.pop());
		}
		return rerankedScores;
	}

}
