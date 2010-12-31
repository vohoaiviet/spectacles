package com.stromberglabs.util.tree;

import java.util.LinkedList;
import java.util.List;

import com.stromberglabs.cluster.ClusterUtils;
import com.stromberglabs.cluster.Clusterable;
import com.stromberglabs.tree.KMeansTreeNode;

public class TreeUtils {
	public static int findNearestNodeIndex(List<KMeansTreeNode> nodes,Clusterable target){
		double minDistance = Float.MAX_VALUE;
		int index = -1;
		int i = 0;
		for ( KMeansTreeNode node : nodes ){
			double distance = ClusterUtils.getEuclideanDistance(node,target);
			if ( distance < minDistance ){
				index = i;
				minDistance = distance;
			}
			i++;
		}
		return index;
	}
	
	public static KMeansTreeNode findNearestNode(List<KMeansTreeNode> nodes, KMeansTreeNode targetNode){
		int index = findNearestNodeIndex(nodes,targetNode);
		if ( index >= 0 )
			return nodes.get(index);
		return null;
	}
	
	/**
	 * @param node
	 * @param totalNodeCount
	 * @return
	 */
	public static List<Double> getCurrentWord(KMeansTreeNode node,int totalNodeCount){
		List<Double> values = new LinkedList<Double>();
		double weight = Math.log((double)node.getNumSubItems()/(double)totalNodeCount);
		double val = weight * (double)node.getCurrentItemCount();
		values.add(val);
		for ( KMeansTreeNode subNode : node.getSubNodes() ){
			values.addAll(getCurrentWord(subNode,totalNodeCount));
		}
		return values;
	}
}
