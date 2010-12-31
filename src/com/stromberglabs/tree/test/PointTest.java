package com.stromberglabs.tree.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.stromberglabs.cluster.ClusterUtils;
import com.stromberglabs.cluster.Clusterable;
import com.stromberglabs.cluster.KMeansTreeClusterer;
import com.stromberglabs.tree.KMeansTree;
import com.stromberglabs.tree.KMeansTreeNode;
import com.stromberglabs.visual.ip.VirtualInterestPoint;
import com.stromberglabs.visual.ip.cache.FileMapInterestPointCache;
import com.stromberglabs.visual.ip.cache.InterestPointCache;

public class PointTest {
	KMeansTree a = null;
	KMeansTree b = null;
	
	public PointTest() {
		System.out.println("initting cache, " + System.currentTimeMillis());
		InterestPointCache cache = new FileMapInterestPointCache(300000,"H:\\ukbench\\training2\\",2000,-1);
		List<Clusterable> pointsA = new ArrayList<Clusterable>();
		System.out.println("listing out points, " + System.currentTimeMillis());
		for ( long id : cache.getIds() ){
			pointsA.add(new VirtualInterestPoint(id, cache));
		}
		System.out.println("building tree a, " + System.currentTimeMillis());
		a = new KMeansTree(pointsA,10,6,new KMeansTreeClusterer());
		pointsA = null;
		cache = null;
		
		InterestPointCache cache2 = new FileMapInterestPointCache(300000,"H:\\ukbench\\training2\\",10000,-1);
		List<Clusterable> pointsB = new ArrayList<Clusterable>();
		System.out.println("listing out points, " + System.currentTimeMillis());
		for ( long id : cache2.getIds() ){
			pointsB.add(new VirtualInterestPoint(id, cache2));
		}
		
		System.out.println("building tree b, " + System.currentTimeMillis());
		b = new KMeansTree(pointsB,10,6,new KMeansTreeClusterer());
		pointsB = null;
		cache2 = null;

		comparePoints();
	}
	
	private void comparePoints(){
		List<KMeansTreeNode> aNodes = a.getAllNodes();
		List<KMeansTreeNode> bNodes = b.getAllNodes();
		//Mapping from index of B to index of A
		Map<Integer,Integer> closest = new HashMap<Integer,Integer>();
		double totalDistances = 0;
		double distanceCount = 0;
		System.out.println(aNodes.size());
		System.out.println(bNodes.size());
		for ( int i = 0; i < aNodes.size(); i++ ){
			int currClosest = -1;
			double currDistance = Double.MAX_VALUE;
			for ( int j = 0; j < bNodes.size(); j++ ){
				double distance = ClusterUtils.getEuclideanDistance(aNodes.get(i),bNodes.get(j));
				if ( distance < currDistance && !closest.containsKey(j) ){
					currClosest = j;
					currDistance = distance;
				}
			}
			if ( currDistance != Double.MAX_VALUE ){
				//System.out.println(i + " is closest to " + currClosest + ", distance = " + currDistance);
				totalDistances += currDistance;
				distanceCount += 1;
			}
			//System.out.println(currDistance);
			closest.put(currClosest,i);
		}
		System.out.println("mean distance: " + totalDistances/distanceCount);
		System.out.println("All done");
	}
	
	public static void main(String args[]){
		new PointTest();
	}
}
