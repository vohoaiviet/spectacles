package com.stromberglabs.visual.search.scoring;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Explain implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private List<Term> mTerms = new ArrayList<Term>();
	private List<Double> mScores = new ArrayList<Double>();
	
	public void addTerm(int wordId, double norm, int numQueryWordOcc, double wordWeight, double normedTargetCount, double actualScoreAdded){
		mTerms.add(new Term(wordId, norm, numQueryWordOcc, wordWeight, normedTargetCount));
		mScores.add(actualScoreAdded);
	}
	
	private class Term {
		private int mWordId;
		private double mQueryNorm;
		private double mOverallWordWeight;
		private int mNumQueryWordOcc;
		private double mNormedTargetOccurrences;
		
		public Term(int wordId, double norm, int numQueryWordOcc, double wordWeight, double normedTargetCount){
			mWordId = wordId;
			mQueryNorm = norm;
			mNumQueryWordOcc = numQueryWordOcc;
			mOverallWordWeight = wordWeight;
			mNormedTargetOccurrences = normedTargetCount;
		}
		
		public String toString(){
			String Q = mOverallWordWeight + " * (" + mNumQueryWordOcc + "/" + mQueryNorm + ")";
			String D = mOverallWordWeight + " * " + mNormedTargetOccurrences;
			return "[" + mWordId + "]|(" + Q + ") - (" + D + ")| - |" + Q + "| - |" + D + "|"; 
		}
	}
	
	public String toString(){
		String display = "";
		for ( int i = 0; i < mTerms.size(); i++ ){
			display += mTerms.get(i);
			if ( i != mTerms.size() - 1 ) display += " + ";
		}
		display += " => ";
		for ( int i = 0; i < mScores.size(); i++ ){
			display += mScores.get(i);
			if ( i != mTerms.size() - 1 ) display += " + ";
		}
		return display;
	}
}
