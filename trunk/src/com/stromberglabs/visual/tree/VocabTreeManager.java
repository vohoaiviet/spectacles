package com.stromberglabs.visual.tree;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.stromberglabs.cluster.Clusterable;
import com.stromberglabs.cluster.ElkanKMeansClusterer;
import com.stromberglabs.cluster.KClusterer;
import com.stromberglabs.cluster.KMeansClusterer;
import com.stromberglabs.cluster.KMeansForestClusterer;
import com.stromberglabs.cluster.KMeansTreeClusterer;
import com.stromberglabs.tree.KMeansTree;
import com.stromberglabs.util.file.SerializationUtils;
import com.stromberglabs.visual.ip.cache.DirectDBInterestPointCache;
import com.stromberglabs.visual.ip.cache.InterestPointCache;
import com.stromberglabs.visual.ip.dao.InterestPointDAO;
import com.stromberglabs.visual.ip.dao.SIFTInterestPointDAO;
import com.stromberglabs.visual.ip.dao.SURFInterestPointDAO;
import com.stromberglabs.visual.search.ImageWordIndex;

public class VocabTreeManager {
	private static KMeansTree tree;
	
	private static ImageWordIndex index;
	
	public static KMeansTree loadVocabTree(String file) {
		if ( tree == null ){
			tree = (KMeansTree)SerializationUtils.loadObject(file);
		}
		return tree;
	}
	
	public static ImageWordIndex loadIndex(String file){
		if ( index == null ){
			index = (ImageWordIndex)SerializationUtils.loadObject(file);
		}
		return index;
	}
	
	public static KMeansTree makeTree(int numPoints, KClusterer clusterer, int groupId, InterestPointDAO dao){
		return makeSampledTree(numPoints,clusterer,groupId,1,dao);
	}
	
	public static KMeansTree makeSampledTree(int numPoints, KClusterer clusterer, int groupId, int useEvery, InterestPointDAO dao){
		InterestPointCache cache = new DirectDBInterestPointCache(groupId,(long)numPoints,dao);
		List<Clusterable> points = new LinkedList<Clusterable>();
		int count = 0;
		Iterator<Clusterable> itr = cache.getAllPoints();
		while ( itr.hasNext() ){
			Clusterable next = itr.next();
			if ( next != null ){
				if ( count % useEvery == 0 ){
					points.add(next);
				}
				count++;
			}
			if ( count % 100000 == 0 ) System.out.println(count + " points loaded");
		}
		System.out.println(count + " points");
		KMeansTree tree = new KMeansTree(points,10,6,clusterer);
		return tree;
	}

	//part 2 of 3 for creating index, run InterestPointFactory first, IndexBuilder next
	public static void main(String args[]) {
		if ( args.length != 5 ){
			System.out.println("Usage: ant tree -Dargs=\"destinationFile maxPoints clusterType group_id [sift|surf]\"");
			System.out.println("Cluster type: 1 = KMeans, 2 = KMeansForestApprox, 3 = Elkan KMeans, 4 = KMeans Tree");
			System.exit(0);
		}
//		makeAndStoreTree("H:\\trees\\tree_100_1.bin",100,1);
//		makeAndStoreTree("H:\\trees\\tree_1000_1.bin",1000,1);
//		makeAndStoreTree("H:\\trees\\tree_100_2.bin",100,2);
//		makeAndStoreTree("H:\\trees\\tree_200_2.bin",200,2);
//		makeAndStoreTree("H:\\trees\\tree_1000_2.bin",1000,2);
//		makeAndStoreTree("H:\\trees\\tree_2000_2.bin",2000,2);
//		makeAndStoreTree("H:\\trees\\tree_3000_2.bin",3000,2);
//		makeAndStoreTree("H:\\trees\\tree_5000_2.bin",5000,2);
//		makeAndStoreTree("H:\\trees\\tree_3000_1.bin",3000,1);
		String location = args[0];
		int numPoints = Integer.parseInt(args[1]);
		int clusterType = Integer.parseInt(args[2]);
		KClusterer clusterer = null;
		if ( clusterType == 1 ){
			clusterer = new KMeansClusterer();
		} else if ( clusterType == 2 ){
			clusterer = new KMeansForestClusterer();
		} else if ( clusterType == 3 ){
			clusterer = new ElkanKMeansClusterer();
		} else if ( clusterType == 4 ){
			clusterer = new KMeansTreeClusterer();
		}
		int groupId = Integer.parseInt(args[3]);
		InterestPointDAO dao = null;
		if ( "sift".equals(args[4]) ){
			dao = new SIFTInterestPointDAO();
		} else if ( "surf".equals(args[4]) ){
			dao = new SURFInterestPointDAO();
		}
		KMeansTree tree = makeTree(numPoints,clusterer,groupId,dao);
		SerializationUtils.saveObject(tree,new File(location));
	}
}
