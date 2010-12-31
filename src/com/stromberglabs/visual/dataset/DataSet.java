package com.stromberglabs.visual.dataset;

import java.io.File;
import java.util.List;

/**
 * A dataset will be able to tell you if a query image is related to a one of the results
 * 
 * @author Andrew
 *
 */
public interface DataSet {
	/*
	 * EXACT = Result is perfect match for query image
	 * PARTIAL = Result has some relevance for the query image
	 * BAD = Result has no relevance for the query image
	 */
	public static enum MatchQuality { EXACT, PARTIAL, BAD };
	
	public MatchQuality getMatchQuality(String query, String result);
	
	public List<File> getQueryFiles();
}
