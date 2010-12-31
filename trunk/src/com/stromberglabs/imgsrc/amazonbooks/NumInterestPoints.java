package com.stromberglabs.imgsrc.amazonbooks;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.stromberglabs.cluster.Clusterable;
import com.stromberglabs.util.file.FileFilter;
import com.stromberglabs.util.file.FileFinder;
import com.stromberglabs.visual.ip.creator.InterestPointCreator;
import com.stromberglabs.visual.ip.creator.SURFInterestPointCreator;

public class NumInterestPoints {
	public static void main(String args[]){
		List<FileFilter> filters = new LinkedList<FileFilter>();
		filters.add(new FileFilter(".*\\.jpg"));
		FileFinder finder = new FileFinder("H:\\amzn_invalid_cover_isbns\\",filters);
		List<File> files = finder.getFiles();
		InterestPointCreator creator = new SURFInterestPointCreator();
		for ( File file : files ){
			try {
				List<Clusterable> points = creator.getPoints(file.getAbsolutePath());
				System.out.println(file.getAbsolutePath() + "," + points.size());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
