package com.stromberglabs.visual.dataset;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.stromberglabs.index.FileIndex;
import com.stromberglabs.util.file.FileFilter;
import com.stromberglabs.util.file.FileFinder;

public class UKBenchDataSet implements DataSet {
	List<File> mFiles = new ArrayList<File>();
	
	public UKBenchDataSet(String directory){
		List<FileFilter> filters = new ArrayList<FileFilter>();
		filters.add(new FileFilter(".*\\.jpg"));
		filters.add(new FileFilter(".*\\.gif"));
		filters.add(new FileFilter(".*\\.png"));
		FileFinder finder = new FileFinder(directory,filters);
		for ( File f : finder.getFiles() ){
			String name = FileIndex.extractName(f.getAbsolutePath()).substring(7);
			if ( Integer.parseInt(name) % 4 == 0 ){
				System.out.println("Adding: " + f.getAbsolutePath());
				mFiles.add(f);
			}
		}
	}

	public MatchQuality getMatchQuality(String query, String result) {
		int resultNum = Integer.parseInt(result.substring(7));
		int queryNum = Integer.parseInt(query.substring(7));
		boolean matches =  queryNum >= resultNum && (queryNum + 3) <= resultNum;
		return (matches ? MatchQuality.EXACT : MatchQuality.BAD);
	}

	public List<File> getQueryFiles() {
		return mFiles;
	}
	
	public static void main(String args[]){
		UKBenchDataSet ds = new UKBenchDataSet("H:\\benchmarks\\ukbench\\full");
	}
}
