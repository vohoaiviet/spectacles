package com.stromberglabs.visual.search;

import java.util.Comparator;

public class ScoreComparator implements Comparator<Score> {
	public int compare(Score o1, Score o2) {
		double diff = o2.getScore() - o1.getScore();
		if ( diff > 0 ){
			return 1;
		} else if ( diff < 0 ) {
			return -1;
		}
		return 0;
	}
}
