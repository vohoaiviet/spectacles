package com.stromberglabs.util.tree;

import java.util.Random;

import com.stromberglabs.util.StopWatch;

import edu.wlu.cs.levy.CG.KDTree;
import edu.wlu.cs.levy.CG.KeyDuplicateException;
import edu.wlu.cs.levy.CG.KeySizeException;

public class KDTreeTest {
	private KDTree<String> mTree;
	private int mNumDimensions;
	private Random mRand;
	
	public KDTreeTest(int numDimensions, int approximateTreeSize){
		mRand = new Random(12345);
		
		mNumDimensions = numDimensions;
		
		mTree = new KDTree<String>(mNumDimensions);
		
		for ( int i = 0; i < approximateTreeSize; i++ ){
			try {
				mTree.insert(generatePoint(),"Point " + i);
			} catch (KeyDuplicateException e){
			} catch (KeySizeException e) {
			}
		}
	}
	
	private double[] generatePoint(){
		double[] point = new double[mNumDimensions];
		for ( int i = 0; i < mNumDimensions; i++ ){
			point[i] = mRand.nextInt(1000);
		}
		return point;
	}
	
	public void executeTestQueries(int numQueries){
		StopWatch timer = new StopWatch();
		for ( int i = 0; i < numQueries; i++ ){
			try {
				//mTree.nearest(generatePoint(),10);

				//List list = mTree.nearestEuclidean(generatePoint(),10);
				//List list = mTree.nearestHamming(generatePoint(),10);
				//System.out.println("size: " + list.size());
				mTree.nearestHamming(generatePoint(),10);
			} catch (KeySizeException e) {
			}
		}
		timer.stop();
		
		System.out.println("It took " + timer.getTime() + " ms to execute " + numQueries + " nearest neighbor queries on a " + mNumDimensions + " dimensional kdtree with size of " + mTree.size());
	}
	
	public static void main(String[] args){
		for ( int i = 1; i <= 64; i++ ){
			KDTreeTest treeTest = new KDTreeTest(i,100000);
			treeTest.executeTestQueries(50);
		}
	}
}
