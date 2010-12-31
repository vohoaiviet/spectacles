package com.stromberglabs.vector;

import java.util.List;

/**
 * Takes a vector and performs L1 Normalization (Manhattan distance)
 * {@link http://mathworld.wolfram.com/VectorNorm.html}
 * @author astromberg
 *
 */
public class L1VectorNormalizer extends AbstractVectorNormalizer {

    public float getVectorNorm(float[] vector) {
        float norm = 0;
        for ( float val : vector ){
            norm += val;
        }
        return norm;
    }

    public float getVectorNorm(int[] vector) {
        float norm = 0;
        for ( int val : vector ){
            norm += val;
        }
        return norm;
    }

    public static void main(String[] args){
        VectorNormalizer normalizer = new L1VectorNormalizer();
       
        float[] vector = {1,2,3,4};
        float norm = normalizer.getVectorNorm(vector);
        System.out.println("norm = " + norm);
        float[] normVector = normalizer.getNormalizedVector(vector);
        System.out.print("{");
        for ( int i = 0; i < normVector.length; i++ ){
            System.out.print(normVector[i]);
            if ( i != normVector.length - 1) System.out.print(",");
        }
        System.out.print("}");
    }

	public double getVectorNorm(List<Double> vector) {
		double norm = 0;
		for ( Double val : vector ){
			norm += val;
		}
		return norm;
	}
}
