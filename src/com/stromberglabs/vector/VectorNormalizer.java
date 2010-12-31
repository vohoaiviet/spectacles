package com.stromberglabs.vector;

import java.util.List;

public interface VectorNormalizer {
    public float getVectorNorm(float[] vector);
   
    public float getVectorNorm(int[] vector);
    
    public double getVectorNorm(List<Double> vector);
   
    public float[] getNormalizedVector(float[] vector);
   
    public float[] getNormalizedVector(int[] vector);
    
    public List<Double> getNormalizedVector(List<Double> vector);
}