package com.stromberglabs.tree.query;

import gnu.trove.map.hash.TIntIntHashMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import com.littletechsecrets.LRUCache;
import com.stromberglabs.cluster.ClusterUtils;
import com.stromberglabs.cluster.Clusterable;
import com.stromberglabs.tree.KMeansTree;
import com.stromberglabs.tree.KMeansTreeNode;
import com.stromberglabs.visual.ip.creator.InterestPointCreator;
import com.stromberglabs.visual.ip.creator.SIFTInterestPointCreator;
import com.stromberglabs.visual.tree.VocabTreeManager;

/**
 * A file based version of the K-Means tree, the meta file contains a list of 
 * word id -> file positions and is headed by an integer containing the number
 * of entries in the file. The file position corresponds to an entry inside
 * the data file, which at each position contains:
 * 
 * WordID Location                 WordID Location                EOL
 * int    double[mDimensionality]  int    double[mDimensionality] Integer.MinValue
 * 
 * @author Andrew
 *
 */
public class QueryTree implements Serializable {
	private static final boolean onlyLeavesAreWords = true;
	
	private static Logger sLogger = Logger.getLogger(QueryTree.class);
	
	private static final long serialVersionUID = 1L;

	private static final String INFO_FILE_NAME = "tree_meta.idx";
	private static final String DATA_FILE_NAME = "tree_data.idx";
	
	private RandomAccessFile mInfoFile;
	private RandomAccessFile mDataFile;
	
	//private TIntLongHashMap mSubNodeLocations = new TIntLongHashMap();
	private long[] mSubNodeLocations;
	
	private TIntIntHashMap mWordCounts = new TIntIntHashMap();
	
	private static int CACHE_SIZE = 100000;
	private LRUCache<Integer,List<Node>> mCache = new LRUCache<Integer, List<Node>>(CACHE_SIZE);
	private int mHits = 0;
	private int mMisses = 0;
	private int mDimensionality = 64;
	
	public QueryTree(KMeansTree tree, File dir){
		try {
			mInfoFile = new RandomAccessFile(new File(dir,INFO_FILE_NAME),"rw");
			mDataFile = new RandomAccessFile(new File(dir,DATA_FILE_NAME),"rw");
			
			Map<Integer,Long> locations = new HashMap<Integer,Long>();
			System.out.println("Tree has " + tree.getAllNodes().size() + " nodes");
			for ( KMeansTreeNode node : tree.getAllNodes() ){
				locations.put(node.getId(),mDataFile.getFilePointer());
				//System.out.println("node " + node.getId() + " is position " + mDataFile.getFilePointer() + " and has " + node.getSubNodes().size() + " sub nodes, is leaf? " + node.isLeafNode());
				for ( KMeansTreeNode subNode : node.getSubNodes() ){
					mDataFile.writeInt(subNode.getId());
					for ( float pos : subNode.getLocation() ){
						mDataFile.writeFloat(pos);
					}
				}
				mDataFile.writeInt(-1);
			}
			
			//Store the dimensionality of the positions
			mDimensionality = tree.getRootNode().getLocation().length;
			mSubNodeLocations = new long[locations.size()];
			mInfoFile.writeInt(mDimensionality);
			mInfoFile.writeInt(locations.size());
			for ( Integer wordId : locations.keySet() ){
				mInfoFile.writeInt(wordId);
				mInfoFile.writeLong(locations.get(wordId));
				//System.out.println("node " + wordId + " is position " + locations.get(wordId));
				mSubNodeLocations[wordId] = locations.get(wordId);
			}
			System.out.println("Wrote out " + locations.keySet().size() + " node locations to the index file");
			
			mInfoFile.writeInt(-1);
			
			mInfoFile.close();
			mDataFile.close();
			
			mInfoFile = new RandomAccessFile(new File(dir,INFO_FILE_NAME),"r");
			mDataFile = new RandomAccessFile(new File(dir,DATA_FILE_NAME),"r");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		mRootNode = new KMeansTreeNode(tree.getRootNode());
	}
	
	public QueryTree(String directory) throws FileNotFoundException, IOException {
		if ( sLogger.isInfoEnabled() )
			sLogger.info("Loading query tree from directory: " + directory);
		mInfoFile = new RandomAccessFile(new File(directory,INFO_FILE_NAME),"r");
		mDimensionality = mInfoFile.readInt();
		int numFiles = mInfoFile.readInt();
		mSubNodeLocations = new long[numFiles];
		int wordId = -1;
		while ( (wordId = mInfoFile.readInt()) != -1 ){
			long position = mInfoFile.readLong();
			mSubNodeLocations[wordId] = position;
		}
		mInfoFile.close();
		if ( sLogger.isInfoEnabled() )
			sLogger.info("Query Tree loaded, contains " + mSubNodeLocations.length + " entries");
		System.out.println("Query Tree loaded, contains " + mSubNodeLocations.length + " entries");
		mDataFile = new RandomAccessFile(new File(directory,DATA_FILE_NAME),"r");
	}
	
	private List<Node> getSubNodes(int wordId){
		if ( mCache.containsKey(wordId) ) {
			mHits++;
			return mCache.get(wordId);
		}
		mMisses++;
		List<Node> nodes = new ArrayList<QueryTree.Node>();
		try {
			long location = mSubNodeLocations[wordId];
			if ( sLogger.isDebugEnabled() )
				sLogger.debug("Seeking to location " + location + " for word " + wordId);
			mDataFile.seek(location);
			int subNodeId = -1;
			while ( (subNodeId = mDataFile.readInt()) != -1 ){
				Node node = new Node();
				node.id = subNodeId;
				node.location = new float[mDimensionality];
				for ( int i = 0; i < mDimensionality; i++ ){
					node.location[i] = mDataFile.readFloat();
				}
				if ( sLogger.isDebugEnabled() )
					sLogger.debug(node.id  + " : " + node.location[0] + "--" + node.location[63]);
				nodes.add(node);
			}
			mCache.put(wordId,nodes);
//			System.out.println("peek at next id: " + mDataFile.readInt());
		} catch ( IOException e ){
			e.printStackTrace();
		}
		return nodes;
	}
	
	/**
	 * "Warms" the cache by loading 75% of the cache by
	 * randomly selecting subnode lists to load
	 */
	public void warmupCache(){
		int warmupSize = Math.min(mSubNodeLocations.length,(int)(CACHE_SIZE * 0.75));
		if ( sLogger.isDebugEnabled() ){
			sLogger.debug("Warming up " + warmupSize + " nodes");
		}
		Random r = new Random(System.currentTimeMillis());
		for ( int i = 0; i < warmupSize; i++ ){
			int node = r.nextInt(getNumWords());
			if ( sLogger.isDebugEnabled() ){
				sLogger.debug("warming up node: " + node);
			}
			getSubNodes(node);
		}
	}
	
	public void addImage(List<Clusterable> imagePoints) {
		for ( Clusterable point : imagePoints ){
			addToTree(point);
		}
	}
	
	private void addToTree(Clusterable point){
		addToTree(0,point);
	}
	
	private void addToTree(int nodeId,Clusterable point){
		List<Node> nodes = getSubNodes(nodeId);
		if ( onlyLeavesAreWords ){
			if ( nodes.size() == 0 ){
				incrementCount(nodeId);
				if ( sLogger.isDebugEnabled() )
					sLogger.debug("incrementing count for node " + nodeId);
			} else {
				int closest = findClosestQueryNode(nodes,point);
				if ( sLogger.isDebugEnabled() )
					sLogger.debug("Next closest node to " + nodeId + " is node " + nodes.get(closest).id);
				addToTree(nodes.get(closest).id,point);
			}
		} else {
			incrementCount(nodeId);
			if ( nodes.size() > 0 ){
				int closest = findClosestQueryNode(nodes,point);
				addToTree(nodes.get(closest).id,point);
			}
		}
	}
	
	private void incrementCount(int nodeId){
		if ( mWordCounts.containsKey(nodeId) ){
			mWordCounts.put(nodeId,mWordCounts.get(nodeId)+1);
		} else {
			mWordCounts.put(nodeId,1);
		}
	}
	
	public void reset() {
		mWordCounts = new TIntIntHashMap();
	}
	
	public int getNumWords(){
		return mSubNodeLocations.length;
	}
	
	public static void main(String[] args){
//		new QueryTree("D:\\test");
//		System.out.println("loaded");
		try {
			InterestPointCreator creator = new SIFTInterestPointCreator();
			List<Clusterable> points = creator.getPoints("D:\\workspace\\ImageFinder\\img\\lena.jpg");
			KMeansTree tree = VocabTreeManager.loadVocabTree("D:\\small\\bestseller_tree.bin");
			tree.resetBreadthList();
			//VocabTreeManager.saveObject(tree,"G:\\tree_10000000_2.bin");
			QueryTree qtree = new QueryTree("D:\\small\\web-index");
//			System.out.println(System.currentTimeMillis());
//			tree.addImage(points);
			System.out.println(System.currentTimeMillis());
			qtree.addImage(points);
			System.out.println(System.currentTimeMillis());
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			Thread.sleep(1000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public TIntIntHashMap getCurrentWordCounts(){
		return mWordCounts;
	}
	
	private class Node implements Clusterable {
		public int id;
		public float[] location;

		public float[] getLocation() {
			return location;
		} 
	}
	
	private static int findClosestQueryNode(List<QueryTree.Node> nodes,Clusterable target){
		double minDistance = Float.MAX_VALUE;
		int index = -1;
		int i = 0;
		for ( Clusterable node : nodes ){
			double distance = ClusterUtils.getEuclideanDistance(node,target);
			if ( distance < minDistance ){
				index = i;
				minDistance = distance;
			}
			i++;
		}
		return index;
	}
	
	public float getCacheHitRatio(){
		return (float)mHits / (float)(mMisses + mHits);
	}
	
	public float getCacheFillRatio(){
		return (float)mCache.size() / (float)CACHE_SIZE;
	}
	
	public int getDimensionality(){
		return mDimensionality;
	}
}
