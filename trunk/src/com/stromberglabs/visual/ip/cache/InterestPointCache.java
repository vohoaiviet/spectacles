package com.stromberglabs.visual.ip.cache;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.stromberglabs.cluster.Clusterable;

public interface InterestPointCache {
	public static final int ID_COL = 1;
	public static final int GROUP_COL = 2;
	public static final int FILENAME_COL = 3;
	
	public Clusterable getPoint(long pointId);
	
	public String getFilename(long pointId);
	
	public List<Clusterable> getPoints(String file);
	
	public Iterator<Clusterable> getAllPoints();
	
	public Set<String> getFiles();
	
	public List<Long> getIds();
}
