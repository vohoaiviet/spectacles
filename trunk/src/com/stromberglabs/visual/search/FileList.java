package com.stromberglabs.visual.search;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

	public class FileList implements Serializable {
		private static final long serialVersionUID = 1L;

		private Map<String,Double> mFiles = new HashMap<String,Double>();
		//private Map<String,Double> mWeightedFiles = new HashMap<String,Double>();
		private double mTotalCount = 0;//Dunno what I will use this for
		private double mWeight = 0;//The weight of this particular node
		
		public void addFile(String file,int count,double weightedCount){
			mTotalCount += count;
			mFiles.put(file,weightedCount);
		}
		
		public boolean containsFile(String file){
			return mFiles.containsKey(file);
		}
		
		public double getFileWordCount(String file){
			return mFiles.get(file);
		}
		
		public Set<String> getFiles(){
			return mFiles.keySet();
		}
		
		public double getTotalCount(){
			return mTotalCount;
		}
		
		public void setTotalCount(double count){
			mTotalCount = count;
		}
		
		public int getFileCount(){
			return mFiles.keySet().size();
		}
		
		public double getWeight(){
			//return 1.0D;
			return mWeight;
		}
		
		public void setWeight(double weight){
			mWeight = weight;
		}
	}
