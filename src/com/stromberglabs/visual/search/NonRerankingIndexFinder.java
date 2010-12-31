package com.stromberglabs.visual.search;

import java.util.List;

import com.stromberglabs.cluster.Clusterable;
import com.stromberglabs.util.SizedPriorityQueue;

public class NonRerankingIndexFinder extends RerankingIndexFinder {

	@Override
	protected SizedPriorityQueue<Score> rerankScores(
			List<Clusterable> currentPoints, SizedPriorityQueue<Score> topScores) {
		return topScores;
	}

}
