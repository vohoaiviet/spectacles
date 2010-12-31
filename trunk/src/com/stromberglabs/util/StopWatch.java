package com.stromberglabs.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class StopWatch {
	private static Map<String,StopWatch> sWatches;
	static {
		sWatches = new HashMap<String, StopWatch>();
	}
	
	public static StopWatch getWatch(String watch){
		StopWatch w = null;
		if ( sWatches.containsKey(watch) ){
			w = sWatches.get(watch); 
		} else {
			w = new StopWatch();
			sWatches.put(watch,w);
		}
		return w;
	}
	
	private long mStartTime;
	private boolean mIsRunning = false;
	private long mElapsedTime = 0;
	
	public StopWatch(){
		start();
	}
	
	public StopWatch(boolean started){
		if ( started )
			mStartTime = System.currentTimeMillis();
		mIsRunning = started;
	}
	
	public String prettyTime(){
		long time = getTime();
		int hours = (int)time / 3600000;
		int remainder = (int)time % 3600000;
		int minutes = remainder / 60000;
		remainder = remainder % 60000;
		float seconds = (float)remainder / 1000;
		String elapsedTime = "";
		if ( hours > 0 ) elapsedTime += hours + "h ";
		if ( minutes > 0 ) elapsedTime += minutes + "m ";
		if ( seconds > 0 ) elapsedTime += seconds + "s";
		return elapsedTime;
	}
	
	public long getTime(){
		if ( mIsRunning )
			return System.currentTimeMillis() - mStartTime + mElapsedTime;
		else
			return mElapsedTime;
	}
	
	public void stop(){
		if ( !mIsRunning ) return;
		mElapsedTime += System.currentTimeMillis() - mStartTime;
		mIsRunning = false;
	}
	
	public void start(){
		if ( mIsRunning ) return;
		mStartTime = System.currentTimeMillis();
		mIsRunning = true;
	}
	
	public void reset(){
		mElapsedTime = 0;
		mStartTime = System.currentTimeMillis();
	}
}
