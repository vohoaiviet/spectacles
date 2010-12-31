package com.stromberglabs.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.stromberglabs.cluster.Cluster;
import com.stromberglabs.cluster.Clusterable;
import com.stromberglabs.cluster.KClusterer;
import com.stromberglabs.util.tree.TreeUtils;

public class KMeansTreeNode implements Clusterable,Serializable {
	private static final long serialVersionUID = 1L;
	
	private static Logger sLogger = Logger.getLogger(KMeansTreeNode.class);
	
	private List<KMeansTreeNode> mSubNodes;
	
	private boolean mIsLeafNode = false;
	private float[] mCenter;//The center of the item
	private int mHeight = 0;//The depth of the node from root

	private int mNumSubItems;//Total number of items with a path through this node, or the "weight"
	private int mCurrentItems;//The current number of items with a path through this node
	
	private int mId = -1;//The unique id for the node in the tree, AKA the "word" of the tree

	public KMeansTreeNode(float[] center, List<Clusterable> items,int branchFactor,int maxHeight,int height, KClusterer clusterer){
		//TODO: Something about this global variable
		mId = KMeansTree.idCount++;
		if ( sLogger.isDebugEnabled() ){
			sLogger.debug("Creating node: " + mId + ", num items to cluster: " + items.size() + ", height: " + height);
		}
		if ( height == maxHeight || items.size() < branchFactor ){
			mIsLeafNode = true;
			mSubNodes = new ArrayList<KMeansTreeNode>(0);
		} else {
			Cluster[] clusters = null;
//			KClusterer clusterer = null;
//			if ( clusterType == 1 ){
//				clusterer = new KMeansClusterer();
//			} else if ( clusterType == 2 ){
//				clusterer = new KMeansTreeClusterer(); 
//			} else if ( clusterType == 3 ){
//				clusterer = new ElkanKMeansClusterer();
//			} else if ( clusterType == 4 ){
//				clusterer = new KMeansForestClusterer();
//			}
			clusters = clusterer.cluster(items,branchFactor);

//			for ( int i = 0; i < clusters.length; i++ ){
//				System.out.println("height: " + height + ", clusters[i].size() = " + clusters[i].getItems().size());
//			}
			mSubNodes = new ArrayList<KMeansTreeNode>(branchFactor);
			for ( Cluster cluster : clusters ){
				if ( cluster.getItems().size() > 0 ){
					KMeansTreeNode node = new KMeansTreeNode(cluster.getClusterMean(),cluster.getItems(),branchFactor,maxHeight,height+1,clusterer);
					mSubNodes.add(node);
				}
			}
		}
		mHeight = height;
		mNumSubItems = items.size();
		mCenter = center;
	}
	
	public boolean isLeafNode(){
		return mIsLeafNode;
	}
	
	public List<KMeansTreeNode> getSubNodes(){
		return mSubNodes;
	}
	
	public float[] getLocation(){
		return mCenter;
	}
	
	public int getNumSubItems(){
		return mNumSubItems;
	}
	
	public int getHeight(){
		return mHeight;
	}
	
	public int getId(){
		return mId;
	}
	
	/**
	 * Methods for setting the query image information
	 */
	
	/**
	 * Adds a clusterable to the current vocab tree for word creation
	 */
	public void addValue(Clusterable c){
		mCurrentItems++;
//		System.out.println(mId + "," + mCurrentItems + "," + mSubNodes.size());
		int index = TreeUtils.findNearestNodeIndex(mSubNodes,c);
		if ( index >= 0 ){
			KMeansTreeNode node = mSubNodes.get(index);
			node.addValue(c);
		}
	}
	
	public int getCurrentItemCount(){
		return mCurrentItems;
	}
	
	public void reset(){
		mCurrentItems = 0;
		for ( KMeansTreeNode node : mSubNodes ){
			node.reset();
		}
	}
	
	@Override
	public String toString() {
		return "KMeansTreeNode [mIsLeafNode=" + mIsLeafNode + ", mCenter="
				+ Arrays.toString(mCenter) + ", mHeight=" + mHeight
				+ ", mNumSubItems=" + mNumSubItems + ", mCurrentItems="
				+ mCurrentItems + ", mId=" + mId + "]";
	}
}