package com.stromberglabs.util.file;

import java.io.File;

public class FileRemover {
	public static void main(String args[]){
		File dir = new File("H:\\ukbench\\query\\");
		int i = 0;
		System.out.println("dir.numfiles = " + dir.listFiles().length);
		for ( File file : dir.listFiles() ){
			if ( i % 4 == 0 ){
				System.out.println("Removing: " + file.getAbsolutePath());
				file.delete();
			}
			i++;
		}
	}
}
