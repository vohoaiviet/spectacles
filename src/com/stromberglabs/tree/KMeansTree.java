package com.stromberglabs.tree;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.stromberglabs.cluster.Cluster;
import com.stromberglabs.cluster.Clusterable;
import com.stromberglabs.cluster.KClusterer;
import com.stromberglabs.util.tree.TreeUtils;
import com.stromberglabs.visual.tree.VocabTreeManager;

public class KMeansTree implements VocabularyTree,Serializable {
	private static final long serialVersionUID = 1L;
	
	public static int idCount = 0; 

	private int mBranchFactor = 10;
	private int mMaxHeight = 6;
	private int mTreeHeight = 0;
	private int mNumWords = 0;//The total number of descriptors added to the tree for word creation
	private KMeansTreeNode mRootNode;
	private List<KMeansTreeNode> mBreadthWiseList;//A list of all of the nodes from a depth wise search
	
	/**
	 * This creates a new tree from the items
	 * @param items
	 * @param branchFactor
	 * @param leafNodeCapacity
	 */
	public KMeansTree(List<Clusterable> items, int branchFactor, int height, KClusterer clusterer){
		mBranchFactor = branchFactor;
		mMaxHeight = height;
		//System.out.println("Creating root node with " + items.size() + " items");
		//System.out.println("Building root node");
		mRootNode = new KMeansTreeNode(Cluster.getMeanValue(items),items,mBranchFactor,mMaxHeight,0,clusterer);
		mBreadthWiseList = getBreadthWiseList();
//		System.out.println("number of nodes total in the tree: " + mBreadthWiseList.size());
//		for ( KMeansTreeNode node : mBreadthWiseList ){
//			System.out.println("height: " + node.getHeight() + ", number subitems: " + node.getNumSubItems());
//		}
		mTreeHeight = determineTreeDepth();
		
		//System.out.println("The tree height is: " + mTreeHeight);
	}
	
	private int determineTreeDepth(){
		int maxTreeDepth = 0;
		for ( KMeansTreeNode node : mBreadthWiseList ){
			if ( maxTreeDepth < node.getHeight() )
				maxTreeDepth = node.getHeight();
		}
		return maxTreeDepth;
	}
	
	private List<KMeansTreeNode> getBreadthWiseList(){
		List<KMeansTreeNode> nodes = new ArrayList<KMeansTreeNode>(mRootNode.getNumSubItems()+1);
		List<KMeansTreeNode> nodesLeft = new LinkedList<KMeansTreeNode>();
		nodesLeft.add(mRootNode);
		KMeansTreeNode node = nodesLeft.remove(0);
		while ( node != null ){
			//TODO: Remove (but why?)
			nodes.add(node);
			if ( !node.isLeafNode() )
				nodesLeft.addAll(node.getSubNodes());
			if ( nodesLeft.size() > 0 )
				node = nodesLeft.remove(0);
			else
				node = null;
		}
		return nodes;
	}
	
	public void resetBreadthList(){
		mBreadthWiseList = getBreadthWiseList();
	}
	
	public void addImage(List<Clusterable> imagePoints) {
		for ( Clusterable value : imagePoints ){
			addPoint(value);
		}
	}
	
	public void addPoint(Clusterable point){
		mNumWords += 1;
		mRootNode.addValue(point);
	}

	/**
	 * Will return the current word in a depth wise search on the tree
	 */
	public List<Double> getCurrentWords() {
		return TreeUtils.getCurrentWord(mRootNode,mRootNode.getNumSubItems());
	}
	
	public void reset() {
		mNumWords = 0;
		mRootNode.reset();
	}
	
	public int getTreeHeight(){
		return mTreeHeight;
	}
	
	public List<KMeansTreeNode> getAllNodes(){ 
		return mBreadthWiseList;
	}
	
	public KMeansTreeNode getRootNode(){
		return mRootNode;
	}
	
	public static void main(String args[]){
		KMeansTree tree = VocabTreeManager.loadVocabTree("G:\\tree_10000000_2.bin");
		for ( KMeansTreeNode node : tree.getAllNodes() ){
			if ( node.getId() == 174162 ){
				System.out.println(node.getId() + " - " + node.getNumSubItems() + " - " + node.isLeafNode() + ", " + node.getCurrentItemCount());
				for ( KMeansTreeNode subNode : node.getSubNodes() ){
					System.out.println(subNode.getId() + " - " + subNode.getNumSubItems() + " - " + subNode.isLeafNode());
				}
			}
		}
	}
}
