package com.stromberglabs.visual.accuracy;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

import com.stromberglabs.index.FileIndex;
import com.stromberglabs.util.SizedPriorityQueue;
import com.stromberglabs.visual.dataset.DataSet;
import com.stromberglabs.visual.dataset.DataSet.MatchQuality;
import com.stromberglabs.visual.dataset.ISBNDataSet;
import com.stromberglabs.visual.search.QueryHandler;
import com.stromberglabs.visual.search.Score;

public class AccuracyTest {
	private DataSet mData;
	private QueryHandler mHandler;
	private int[] mCounts;
	
	public AccuracyTest(DataSet set, QueryHandler handler, int[] resultCounts){
		mData = set;
		mHandler = handler;
		mCounts = resultCounts;
	}
	
	public void runTest(){
		int totalItems = 0;
		int totalCorrect[] = new int[mCounts.length];
		int maxFiles = NumberUtils.max(mCounts);
		for ( File queryFile : mData.getQueryFiles() ){
			try {
				SizedPriorityQueue<Score> results = mHandler.findBest(queryFile,maxFiles);
				List<Score> scores = results.getAllScores();
				int matchPosition = Integer.MAX_VALUE;
				String isbn = FileIndex.extractName(queryFile.getAbsolutePath());
				for ( int i = 0; i < scores.size(); i++ ){
					if ( mData.getMatchQuality(isbn, results.pop().getTarget()) == MatchQuality.EXACT ){
						System.out.println("Query: " + isbn + " matched at position: " + i);
						matchPosition = i;
						break;
					}
				}
				for ( int i = 0; i < mCounts.length; i++ ){
					if ( matchPosition <= mCounts[i] ){
						totalCorrect[i]++;
					}
				}
				totalItems++;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for ( int i = 0; i < mCounts.length; i++ ){
			System.out.println("recall @ " + mCounts[i] + " = " + totalCorrect[i] + "/" + totalItems);
		}
	}
	
	public static void main(String args[]){
		//QueryHandler.initHandler("H:\\test");
		QueryHandler.initHandler("H:\\indexes\\run_1");
		QueryHandler handler = QueryHandler.getHandler();
		DataSet set = new ISBNDataSet("H:\\benchmarks\\bookbench\\normal");
		AccuracyTest test = new AccuracyTest(set, handler, new int[]{1,5,10,20});
		test.runTest();
	}
}