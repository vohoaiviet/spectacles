package com.stromberglabs.visual.search;

import java.io.Serializable;

import com.stromberglabs.visual.search.scoring.Explain;

public class Score implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String mTarget;
	private double mScore;
	private double mRerankedScore;
	private int mNumScores;
	private Explain mExplain = new Explain();
	
	public Score(String target){
		mScore = 0;
		mNumScores = 0;
	}
	
	public Score(String target, double score){
		mTarget = target;
		mScore = score;
		mNumScores = 1;
	}
	
	public String getTarget(){
		return mTarget;
	}
	
	public double getScore(){
		return mScore;
	}
	
	public void addToScore(double score){
		mScore += score;
		mNumScores++;
	}
	
	public void setScore(double score){
		 mScore = score;
	}
	
	public double getRerankedScore(){
		return mRerankedScore;
	}
	
	public void setRerankedScore(double newScore){
		mRerankedScore = newScore;
	}
	
	public int getNumScores(){
		return mNumScores;
	}
	
	public Explain getExplanation(){
		return mExplain;
	}
	
	public void addValueToExplanation(int wordId, double norm, int numQueryWordOcc, double wordWeight, double normedTargetCount,double scoreAdded){
		mExplain.addTerm(wordId, norm, numQueryWordOcc, wordWeight, normedTargetCount,scoreAdded);
	}

	public String toString(){
		return mTarget + " - " + mScore + " - " + mNumScores;
	}
}
