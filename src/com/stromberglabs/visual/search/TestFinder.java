package com.stromberglabs.visual.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.stromberglabs.cluster.Clusterable;
import com.stromberglabs.util.SizedPriorityQueue;
import com.stromberglabs.util.file.FileFilter;
import com.stromberglabs.util.file.FileFinder;
import com.stromberglabs.visual.ip.creator.InterestPointCreator;
import com.stromberglabs.visual.ip.creator.SURFInterestPointCreator;
import com.stromberglabs.visual.ip.dao.SURFInterestPointDAO;
import com.stromberglabs.visual.tree.VocabTreeManager;

public class TestFinder {
	public TestFinder(String queryFolder){
		System.out.println("Loading index...");
		ImageWordIndex index = VocabTreeManager.loadIndex("G:\\index.bin");
		System.out.println("Index loaded, finding files...");
		List<FileFilter> filters = new ArrayList<FileFilter>();
		filters.add(new FileFilter(".*\\.jpg"));
		FileFinder finder = new FileFinder(queryFolder,filters);
		List<File> files = finder.getFiles();
		System.out.println("Found " + files.size() + " files");
		int numRight = 0;
		int numWrong = 0;
		for ( int i = 0; i < files.size(); i++ ){
			try {
				File f = files.get(i);
				InterestPointCreator creator = new SURFInterestPointCreator();
				List<Clusterable> filePoints = creator.getPoints(f.getAbsolutePath());
				SizedPriorityQueue<Score> bestScores = index.findClosest(filePoints);
				
				//System.out.println("For " + f.getAbsolutePath() + " found " + foundFile);
				//System.out.print("For " + f.getAbsolutePath() + ", ");
				//System.out.println("found best: " + bestScores.poll().getTarget());
				File foundF = new File(bestScores.poll().getTarget());
				if ( foundF.getName().equals(f.getName()) ){
					numRight++;
					System.out.println("For " + f.getAbsolutePath() + " found correct best: " + bestScores.poll().getTarget() + ", fp.size = " + filePoints.size());
					//System.out.println("right");
				} else {
					numWrong++;
					System.out.println("For " + f.getAbsolutePath() + " found wrong best: " + bestScores.poll().getTarget() + ", fp.size = " + filePoints.size());
					//System.out.println(f.getName());
					//System.out.println("wrong");
				}
//				while ( bestScores.size() > 0 ){
//					Score nextBest = bestScores.pop();
//					int numMatching = numMatchingPoints(cache.getInterestPoints(nextBest.getTarget()),filePoints);
//					System.out.println(nextBest + " , " + numMatching + ", " + nextBest.getNumScores());
//				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Accuracy: " + ((float)(numRight)/(float)(numRight+numWrong)));
	}
	
	public static void main(String args[]){
		new TestFinder("H:\\bookbench\\gt\\");
		new TestFinder("H:\\bookbench\\scaled\\");
		new TestFinder("H:\\bookbench\\rotated\\");
		new TestFinder("H:\\bookbench\\occluded\\");
		new TestFinder("H:\\bookbench\\affine\\");
	}
}
