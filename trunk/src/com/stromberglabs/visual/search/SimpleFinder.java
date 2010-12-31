package com.stromberglabs.visual.search;

import java.io.IOException;
import java.util.List;

import com.stromberglabs.cluster.ClusterUtils;
import com.stromberglabs.cluster.Clusterable;
import com.stromberglabs.tree.KMeansTree;
import com.stromberglabs.vector.L2VectorNormalizer;
import com.stromberglabs.vector.VectorNormalizer;
import com.stromberglabs.visual.ip.cache.FileMapInterestPointCache;
import com.stromberglabs.visual.ip.creator.InterestPointCreator;
import com.stromberglabs.visual.ip.creator.SURFInterestPointCreator;
import com.stromberglabs.visual.ip.dao.SURFInterestPointDAO;

/**
 * A really simple finder, takes an example image and does a slow comparison between the vocab trees of both the query and each database image
 * @author Andrew
 *
 */
public class SimpleFinder {
	private FileMapInterestPointCache mInterestPointCache;
	private KMeansTree mVocabTree;
	
	public SimpleFinder(String location){
//		mInterestPointCache = FileMapInterestPointCache.getCache(location);
		//List<Clusterable> items = new ArrayList<Clusterable>(mInterestPointCache.getInterestPointIds().size());
		//for ( Integer id : mInterestPointCache.getInterestPointIds() ){
		//	items.add(new VirtualInterestPoint(id,mInterestPointCache));
		//}
//		mVocabTree = new KMeansTree(items,6,10);
//		mVocabTree = VocabTreeManager.loadVocabTree(location);
//		if ( mVocabTree == null ){
//			System.out.println("Need to create a vocab tree ahead of time");
//			System.exit(0);
//		}
	}
	
	/**
	 * Ghetto find closest algorithm, takes the interest points of an image and searches all of them individually
	 * @param filename
	 * @return
	 */
	public String findClosest(String filename){
		try {
			VectorNormalizer vn = new L2VectorNormalizer();
			InterestPointCreator creator = new SURFInterestPointCreator();
			List<Clusterable> targetImagePoints = creator.getPoints(filename);
			mVocabTree.reset();
			mVocabTree.addImage(targetImagePoints);
			List<Double> targetNodeWeights = mVocabTree.getCurrentWords();
			System.out.println("There are: " + targetImagePoints.size() + " points for the target image: " + filename);
			String closest = "";
			double closestD = Double.MAX_VALUE;
			for ( String file : mInterestPointCache.getFiles() ){
				List<Clusterable> databasePoints = mInterestPointCache.getPoints(file);
				mVocabTree.reset();
				mVocabTree.addImage(databasePoints);
				System.out.println("There are: " + databasePoints.size() + " points for the database image: " + file);
				List<Double> databaseNodeWeights = mVocabTree.getCurrentWords();
				System.out.println("Beginning calculation of sumdifferences");
				double distance = ClusterUtils.sumDifferences(vn.getNormalizedVector(targetNodeWeights),vn.getNormalizedVector(databaseNodeWeights));
				//double distance = ClusterUtils.sumDifferences(targetNodeWeights,databaseNodeWeights);
				System.out.println("The distance between: " + filename + " and " + file + " is : " + distance);
				if ( distance < closestD ){
					closestD = distance;
					closest = file;
				}
			}
			System.out.println("The closest to " + filename + " is: " + closest);
			System.out.println("The distance was: " + closestD);
			return closest;
		} catch ( IOException e ){
			e.printStackTrace();
		}
		//mVocabTree.addImage();
		return null;
	}
	
	public static void main(String args[]){
		SimpleFinder finder = new SimpleFinder("H:\\ukbench\\full\\");
		finder.findClosest("H:\\ukbench\\full\\ukbench00018.jpg");
	}
}
