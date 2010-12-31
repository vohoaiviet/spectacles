package com.stromberglabs.vector;

import java.util.List;

/**
 * Take a vector and performs L2 Normalization (Euclidean) on it
 * {@link http://mathworld.wolfram.com/VectorNorm.html}
 * @author astromberg
 *
 */
public class L2VectorNormalizer extends AbstractVectorNormalizer {

    public float getVectorNorm(float[] vector) {
        double sumSquares = 0;
        for ( float val : vector ){
            sumSquares += val * val;
        }
        return (float)Math.sqrt(sumSquares);
    }

    public float getVectorNorm(int[] vector) {
        double sumSquares = 0;
        for ( float val : vector ){
            sumSquares += val * val;
        }
        return (float)Math.sqrt(sumSquares);
    }
    
	public double getVectorNorm(List<Double> vector) {
		double sumSquares = 0;
		for ( Double val : vector ){
			sumSquares += val * val;
		}
		return Math.sqrt(sumSquares);
	}
	
    public static void main(String[] args){
        VectorNormalizer normalizer = new L2VectorNormalizer();
       
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
}
