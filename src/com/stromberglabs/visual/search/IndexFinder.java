package com.stromberglabs.visual.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.stromberglabs.cluster.Clusterable;
import com.stromberglabs.cluster.KMeansClusterer;
import com.stromberglabs.tree.KMeansTree;
import com.stromberglabs.util.SizedPriorityQueue;
import com.stromberglabs.util.file.FileFilter;
import com.stromberglabs.util.file.FileFinder;
import com.stromberglabs.visual.ip.VirtualInterestPoint;
import com.stromberglabs.visual.ip.cache.FileMapInterestPointCache;
import com.stromberglabs.visual.ip.cache.InterestPointCache;
import com.stromberglabs.visual.ip.creator.InterestPointCreator;
import com.stromberglabs.visual.ip.creator.SURFInterestPointCreator;

public class IndexFinder {
	public static void main(String args[]){
		//InterestPointCache queryCache = new InterestPointCache(1100000,"H:\\ukbench\\query\\",0,0);
		//InterestPointFactory.createInterestPoints("H:\\ukbench\\training2\\");
		System.gc();
		int maxHeight = 8;
		int branchFactor = 10;
		System.out.println("TESTING USING BRANCH FACTOR OF " + branchFactor + ", HEIGHT OF " + maxHeight);
		//int[] testSizes = {100,200,300,400,500,600,700,800,900,1000,2000,3000,4000,10200};
		//int[] testSizes = {100,200,300};
		int[] testSizes = {100,1000,10200};
		//int[] testSizes = {1000,2000,3000,4000};
		//int[] testSizes = {100};
		//int[] testSizes = {10200};
		for ( int numFilesToTest : testSizes ){
			Map<String,Integer> counts = new HashMap<String,Integer>();
			//String file = "H:\\ukbench\\training_small\\ukbench00000.jpg";
			int[] numCorrect = new int[6];
			int[] numWrong = new int[6];
			//String indexLocation = "H:\\ukbench\\training_small\\";
			//String searchLocation = "H:\\ukbench\\query_small\\";
			String indexLocation = "H:\\ukbench\\training2\\";
			String searchLocation = "H:\\ukbench\\query\\";
			System.out.println("Starting cache");
			//InterestPointCache cache = new DirectDBInterestPointCache(2,numFilesToTest);
			InterestPointCache cache = new FileMapInterestPointCache(1100000,indexLocation,numFilesToTest/4,0);
			List<Clusterable> points = new LinkedList<Clusterable>();
			int count = 0;
			for ( Long id : cache.getIds() ){
				points.add(new VirtualInterestPoint(id,cache));
				if ( count % 1000 == 0 ) System.out.println(count);
				count++;
			}
			System.out.println("Done with points, " + points.size());
			KMeansTree tree = new KMeansTree(points,branchFactor,maxHeight,new KMeansClusterer());
			System.out.println("Done with tree, height: " + tree.getTreeHeight() + ", num nodes: " + tree.getAllNodes().size());
			ImageWordIndex index = new L1ImageWordIndex(tree,cache);
			System.out.println("Done with index");
			List<FileFilter> filters = new ArrayList<FileFilter>();
			filters.add(new FileFilter(".*\\.jpg"));
			FileFinder finder = new FileFinder("E:\\H\\ukbench\\query\\",filters);
			List<File> files = finder.getFiles();
			InterestPointCreator creator = new SURFInterestPointCreator();
			for ( int i = 0; i < files.size() && i < (numFilesToTest * 3)/4; i++ ){
				File f = files.get(i);
				if ( i % 100 == 0 && i != 0 ) System.out.println("On file " + i + " of " + ((numFilesToTest * 3)/4) + ", acc: " + ((float)numCorrect[1]/(numCorrect[1]+numWrong[1])));
				try {
					System.out.println("Querying for image: " + f.getAbsolutePath());
					List<Clusterable> filePoints = creator.getPoints(f.getAbsolutePath());
					//List<Clusterable> filePoints = queryCache.getInterestPoints(f);
					SizedPriorityQueue<Score> bestScores = index.findClosest(filePoints);
					if ( bestScores.poll() == null ) continue;
					System.out.println("For file " + f.getAbsolutePath() + " the best image found was " + bestScores.poll().getTarget() + ", is correct? " + isCorrectFile(f.getAbsolutePath(),bestScores.poll().getTarget()));
					if ( bestScores != null && bestScores.size() > 0 ){
						boolean isCorrect[] = new boolean[6];
						if ( isCorrectFile(f.getAbsolutePath(),bestScores.poll().getTarget()) ){
							isCorrect[0] = true;
							numCorrect[0]++;
						} else {
							numWrong[0]++;
						}
						int numChecked = 0;
						double diffs[] = new double[5];
						int matchingWordCounts[] = new int[5];
						boolean corrects[] = new boolean[5];
						double firstScore = bestScores.poll().getScore();
						while ( false && numChecked < 50 && bestScores.size() > 0 ){
							Score next = bestScores.pop();
							//System.out.println(numChecked + ", " + next);
							boolean isCorrectImg = isCorrectFile(f.getAbsolutePath(),next.getTarget());
							if ( isCorrectImg ){
								if ( numChecked < 5 ) isCorrect[1] = true;
								if ( numChecked < 10 ) isCorrect[2] = true;
								if ( numChecked < 15 ) isCorrect[3] = true;
								if ( numChecked < 25 ) isCorrect[4] = true;
								if ( numChecked < 50 ) isCorrect[5] = true;
							}
							if ( numChecked < 5 ){
								//System.out.println("next score : " + next.getScore() + ", first: " + firstScore);
								diffs[numChecked] = next.getScore() - firstScore;
								matchingWordCounts[numChecked] = next.getNumScores();
								corrects[numChecked] = isCorrectImg;
							}
							numChecked++;
						}
						if ( isCorrect[0] ){
							System.out.println(diffs[1]);
//							for ( int r = 0; r < diffs.length; r++ ){
//								//System.out.println(corrects[r] + "," + diffs[r] + "," + matchingWordCounts[r]);
//								System.out.println(diffs[r]);
//							}
						}
						for ( int m = 1; m < isCorrect.length; m++ ){
							if ( isCorrect[m] ){
								numCorrect[m]++;
							} else {
								numWrong[m]++;
							}
						}
					} else {
						numWrong[0]++;
						numWrong[1]++;
						numWrong[2]++;
						numWrong[3]++;
						numWrong[4]++;
						numWrong[5]++;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				//break;
			}
	//		for ( String key : counts.keySet() ){
	//			System.out.println(key + "," + counts.get(key));
	//		}
			for ( int i = 0; i < 6; i++ ){
				System.out.println(numFilesToTest + " files, words checked index: " + i + "," + numCorrect[i] / (float)(numCorrect[i] + numWrong[i]) + " accuracy, " + index.numWords() + " words");				
			}
			System.gc();
		}
	}
	//
	//
	public static boolean isCorrectFile(String queryFile, String returnedFile){
		int queryNumber = getImageNumber(queryFile);
		int foundNumber = getImageNumber(returnedFile);
		boolean isGood = (foundNumber < queryNumber && foundNumber + 4 > queryNumber);
		//System.out.println("Is " + returnedFile + " the correct one for " + queryFile + "? " + isGood);
		return isGood;
	}
	
	public static int getImageNumber(String filename){
		int start = filename.indexOf("ukbench",filename.indexOf("ukbench") + 1) + 7;
		int end = start + 5;
		return Integer.parseInt(filename.substring(start,end));
	}
}

