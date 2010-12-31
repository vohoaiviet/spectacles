package com.stromberglabs.visual.ip;

import com.stromberglabs.visual.ip.cache.AbstractInterestPoint;
import com.stromberglabs.visual.ip.cache.InterestPointCache;


public class VirtualInterestPoint extends AbstractInterestPoint {
	private long mPointId = -1;
	private InterestPointCache mCache;
	
	public VirtualInterestPoint(long point, InterestPointCache cache){
		mPointId = point;
		mCache = cache;
	}

	public String getFile() {
		return mCache.getFilename(mPointId);
	}

	public long getId() {
		return mPointId;
	}

	public float[] getLocation() {
		return mCache.getPoint(mPointId).getLocation();
	}
}
