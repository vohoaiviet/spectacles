package com.stromberglabs.imgsrc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Renamer {
	public static void main(String args[]){
		try {
			File f = new File("H:\\descriptors.txt");
			File fout = new File("H:\\covers.csv");
			BufferedReader str = new BufferedReader(new FileReader(f));
			BufferedWriter writer = new BufferedWriter(new FileWriter(fout));
			String line = null;
			while ( (line = str.readLine()) != null ){
				//System.out.println(line);
				if ( line.contains("amazon") || line.contains("training2") ){
					writer.write(line);
				}
			}
			writer.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
}
