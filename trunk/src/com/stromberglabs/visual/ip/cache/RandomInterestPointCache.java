package com.stromberglabs.visual.ip.cache;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.littletechsecrets.LRUCache;
import com.stromberglabs.cluster.Clusterable;
import com.stromberglabs.visual.ip.dao.InterestPointDAO;

public class RandomInterestPointCache extends DirectDBInterestPointCache {
	
	private LRUCache<Long,Clusterable> mCache;
	
	public RandomInterestPointCache(int groupId, int maxPoints,InterestPointDAO dao){
		super(groupId,(long)maxPoints,dao);
		mCache = new LRUCache<Long, Clusterable>(maxPoints > 1000000 ? 1000000 : maxPoints);
	}
	
	public Clusterable getPoint(long pointId) {
		if ( mCache.containsKey(pointId) ){
			return mCache.get(pointId);
		}
		Clusterable point = super.getPoint(pointId);
		mCache.put(pointId,point);
		return point;
	}

	public Iterator<Clusterable> getAllPoints() {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> getFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Long> getIds() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
