package com.stromberglabs.visual.dataset;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.stromberglabs.util.file.FileFilter;
import com.stromberglabs.util.file.FileFinder;

/**
 * Easiest possible implementation of a dataset, just returns EXACT if the
 * ISBNs of the query and the result match.
 * 
 * @author Andrew
 *
 */
public class ISBNDataSet implements DataSet {
	List<File> mFiles = new ArrayList<File>();
	
	public ISBNDataSet(String directory){
		List<FileFilter> filters = new ArrayList<FileFilter>();
		filters.add(new FileFilter(".*\\.jpg"));
		filters.add(new FileFilter(".*\\.gif"));
		filters.add(new FileFilter(".*\\.png"));
		FileFinder finder = new FileFinder(directory,filters);
		mFiles = finder.getFiles();
	}
	
	public MatchQuality getMatchQuality(String query, String result) {
		return ( query.equals(result) ? MatchQuality.EXACT : MatchQuality.BAD);
	}

	public List<File> getQueryFiles() {
		return mFiles;
	}

}
