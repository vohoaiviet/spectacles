package com.stromberglabs.visual.search;

import java.util.Comparator;

public class CountComparator implements Comparator<Score> {
	public int compare(Score o1, Score o2) {
		return o2.getNumScores() - o1.getNumScores();
	}
}
