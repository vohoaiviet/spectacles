package com.stromberglabs.tree;

public class ImageVisualWord {
	private int[] mCharacters;//The integer id of the node on the vocab tree
	private int[] mCounts;//The number of times that node is found in the word
	
	
	
	public String toString() {
		String repr = "";
		for ( int i=0; i < mCharacters.length; i++ ){
			repr += mCharacters[i] + " (" + mCounts[i] + "), ";
		}
		return repr;
	}
}
