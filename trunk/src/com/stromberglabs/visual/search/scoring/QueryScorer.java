package com.stromberglabs.visual.search.scoring;

import gnu.trove.map.hash.TIntIntHashMap;

import com.stromberglabs.index.WordInvertedIndex;
import com.stromberglabs.util.SizedPriorityQueue;
import com.stromberglabs.visual.search.Score;

public interface QueryScorer {
	public SizedPriorityQueue<Score> findClosest(TIntIntHashMap wordCounts,WordInvertedIndex index, int numResults);
}
