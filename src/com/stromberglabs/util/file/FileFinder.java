package com.stromberglabs.util.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class FileFinder {
	private static final Logger logger = Logger.getLogger(FileFinder.class);
	
	private File mCurrentFolder;
	
	private List<FileFilter> mOrFileFilters = new ArrayList<FileFilter>();
	
	public FileFinder(String folder,List<FileFilter> OrFilters){
		this(new File(folder),OrFilters);
	}
	
	public FileFinder(File folder,List<FileFilter> OrFilters){
		mCurrentFolder = folder;
		mOrFileFilters = OrFilters;
	}
	
	public List<File> getFiles(){
		if ( logger.isDebugEnabled() )
			logger.debug("Finding list of files");
		List<File> allFiles = new ArrayList<File>();
		List<File> currentFiles = getCurrentLevelFiles();
		for ( File file : currentFiles ){
//			System.out.println("Examining file: " + file.getName());
			if  ( file.isDirectory() ){
//				System.out.println("Going into directory: " + file.getAbsolutePath());
				FileFinder subDirFinder = new FileFinder(file.getAbsolutePath(),mOrFileFilters);
				allFiles.addAll(subDirFinder.getFiles());
			} else if ( file.isFile() && filePasses(file) ){
//				System.out.println("Adding file to list: " + file.getName());
				allFiles.add(file);
			}
		}
		if ( logger.isDebugEnabled() )
			logger.debug("Found " + allFiles.size() + " files");
		return allFiles;
	}
	
	private List<File> getCurrentLevelFiles(){
		List<File> files = new ArrayList<File>();
		for ( File file : mCurrentFolder.listFiles()){
			if ( file.canRead() && !file.isHidden() ) files.add(file);
		}
		return files;
	}
	
	private boolean filePasses(File file){
		for ( FileFilter filter : mOrFileFilters ){
			if ( filter.passes(file) ) return true;
		}
		return false;
	}
	
	public static void main(String args[]){
		List<FileFilter> filters = new ArrayList<FileFilter>();
		filters.add(new FileFilter(".*\\.jpg"));
		filters.add(new FileFilter(".*\\.gif"));
		filters.add(new FileFilter(".*\\.png"));
		FileFinder finder = new FileFinder("H:\\benchmarks\\bookbench\\gt\\",filters);
		List<File> files = finder.getFiles();
		for ( File f : files ){
			System.out.println("ll " + f.getName());
		}
		System.out.println("Found: " + files.size() + " files");
	}
}
