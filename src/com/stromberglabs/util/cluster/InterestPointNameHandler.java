package com.stromberglabs.util.cluster;

import java.util.HashMap;
import java.util.Map;

public class InterestPointNameHandler {
	private static InterestPointNameHandler mHandler;
	
	private Map<Integer,String> mIdToName;
	private Map<String,Integer> mNameToId;
	
	private int mCurrentId = 0;
	
	private InterestPointNameHandler(){
		mIdToName = new HashMap<Integer, String>();
		mNameToId = new HashMap<String, Integer>();
	}
	
	public static InterestPointNameHandler getHandler(){
		if ( mHandler == null ) mHandler = new InterestPointNameHandler();
		return mHandler;
	}
	
	public void addFile(String filename){
		if ( mNameToId.containsKey(filename) ) return;
		mIdToName.put(mCurrentId,filename);
		mNameToId.put(filename,mCurrentId);
		mCurrentId++;
	}
	
	public int getId(String filename){
		return mNameToId.get(filename);
	}
	
	public String getName(int id){
		return mIdToName.get(id);
	}
	
	public boolean inMap(String filename){
		return mNameToId.containsKey(filename);
	}
}
