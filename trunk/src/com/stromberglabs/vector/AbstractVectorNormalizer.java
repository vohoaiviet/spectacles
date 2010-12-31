package com.stromberglabs.vector;

import java.util.List;

public abstract class AbstractVectorNormalizer implements VectorNormalizer {

	public float[] getNormalizedVector(float[] vector) {
        float norm = getVectorNorm(vector);
        for ( int i = 0; i < vector.length; i++ ){
            vector[i] = vector[i]/norm;
        }
        return vector;
    }

    public float[] getNormalizedVector(int[] vector) {
        float norm = getVectorNorm(vector);
        float[] newVector = new float[vector.length];
        for ( int i = 0; i < vector.length; i++ ){
            newVector[i] = (float)vector[i]/norm;
        }
        return newVector;
    }
    
    public List<Double> getNormalizedVector(List<Double> vector){
    	System.out.println("THere are " + vector.size() + " num items to normalize");
    	double norm = getVectorNorm(vector);
    	System.out.println("Vector norm is: " + norm);
    	for ( int i = 0; i < vector.size(); i++ ){
    		vector.set(i,vector.get(i)/norm);
    	}
    	System.out.println("Done norming vector");
    	return vector;
    }
}
