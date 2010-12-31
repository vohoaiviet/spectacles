package com.stromberglabs.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.stromberglabs.util.file.FileFilter;
import com.stromberglabs.util.file.FileFinder;

public class RenameFiles {
	public static void main(String args[]){
		FileFilter jpgFilter = new FileFilter(".*\\.jpg");
		List<FileFilter> filters = new ArrayList<FileFilter>();
		filters.add(jpgFilter);
		FileFinder finder = new FileFinder("H:\\bookbench\\scaled",filters);
		List<File> files = finder.getFiles();
		int i = 1;
		for ( File f : files ){
			try {
				String filename = f.getParent() + File.separator + "book00" + (i >= 10 ? i : "0" + i) + ".jpg";
				System.out.println("Renaming " + f.getAbsolutePath() + " to: " + filename);
				f.renameTo(new File(filename));
			} catch ( Exception e ){
				e.printStackTrace();
			}
			i++;
		}
	}
}
