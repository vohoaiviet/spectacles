package com.stromberglabs.tree;

public interface Word {
	//The word id, also the id of the node in the tree
	public int getId();
	
	//The current number of times an items have been
	//assigned to this node
	public int getCurrentItemCount();
}
