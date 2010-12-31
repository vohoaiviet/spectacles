package com.stromberglabs.tree;

import java.util.List;

import com.stromberglabs.cluster.Clusterable;

public interface VocabularyTree {
	public List<Double> getCurrentWords();
	
	public void addImage(List<Clusterable> imagePoint);
	
	public void reset();
}
