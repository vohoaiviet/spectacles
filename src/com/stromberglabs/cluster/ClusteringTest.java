package com.stromberglabs.cluster;

import java.util.LinkedList;
import java.util.List;

import com.stromberglabs.tree.KMeansTree;
import com.stromberglabs.visual.ip.VirtualInterestPoint;
import com.stromberglabs.visual.ip.cache.DirectDBInterestPointCache;
import com.stromberglabs.visual.ip.dao.SIFTInterestPointDAO;

public class ClusteringTest {
	public ClusteringTest(long numPoints, KClusterer clusterer){
		SIFTInterestPointDAO siftDAO = new SIFTInterestPointDAO();
		DirectDBInterestPointCache cache = new DirectDBInterestPointCache(2,numPoints,siftDAO);
		List<Clusterable> points = new LinkedList<Clusterable>();
		int count = 0;
		for ( Long id : cache.getIds() ){
			points.add(new VirtualInterestPoint(id,cache));
			count++;
		}
		long start = System.currentTimeMillis();
		new KMeansTree(points,10,6,clusterer);
		long time = System.currentTimeMillis() - start;
		System.out.println(numPoints + "," + clusterer + "," + time);
	}
	
	public static void main(String args[]){
		KClusterer[] clusterers = {new KMeansClusterer()};
		for ( KClusterer clusterer : clusterers ){
			for ( int numPoints = 256000; numPoints <= 1000000; numPoints *= 2 ){
				for ( int times = 0; times < 3; times++ ){
					new ClusteringTest(numPoints,clusterer);
				}
			}
		}
	}
}
