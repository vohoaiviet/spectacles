package com.stromberglabs.visual.search;

import java.util.List;

import com.stromberglabs.cluster.ClusterUtils;
import com.stromberglabs.cluster.Clusterable;
import com.stromberglabs.util.SizedPriorityQueue;

public class InterestPointReranker extends RerankingIndexFinder {

	@Override
	protected SizedPriorityQueue<Score> rerankScores(List<Clusterable> currentPoints,SizedPriorityQueue<Score> topScores) {
		SizedPriorityQueue<Score> rerankedScores = new SizedPriorityQueue<Score>(5,false,new ScoreComparator());
		for ( int i = 0; i < 5; i++ ){
			Score next = topScores.pop();
			List<Clusterable> nextPoints = mCache.getPoints(next.getTarget());
			int numMatchingPoints = numMatchingPoints(currentPoints,nextPoints);
			rerankedScores.add(new Score(next.getTarget(),(double)numMatchingPoints));
		}
		return rerankedScores;
	}
	
	public static int numMatchingPoints(List<Clusterable> a, List<Clusterable> b){
		int matches = 0;
		for ( Clusterable pointA : a ){
			double d1 = Double.MAX_VALUE, d2 = Double.MAX_VALUE;
			//SURFInterestPoint best = null;
			for ( Clusterable pointB : b ){
				double distance = ClusterUtils.getEuclideanDistance(pointA,pointB);
				if ( distance < d1 ){
					d2 = d1;
					d1 = distance;
					//best = pointB;
				} else if ( distance < d2 ){
					d2 = distance;
				}
			}
			if ( d1/d2 < 0.65D ){
				matches++;
			}
		}
		return matches;
	}
}
