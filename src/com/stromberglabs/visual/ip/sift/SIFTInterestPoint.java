package com.stromberglabs.visual.ip.sift;

import com.stromberglabs.jopensurf.InterestPoint;

public class SIFTInterestPoint implements InterestPoint {
	private float[] mDescriptor;
	
	public SIFTInterestPoint(float[] location){
		mDescriptor = location;
	}
	
	public float[] getLocation() {
		return mDescriptor;
	}

	public double getDistance(InterestPoint point) {
		double sum = 0;
		if ( point.getLocation() == null || mDescriptor == null ) return Float.MAX_VALUE;
		for ( int i = 0; i < mDescriptor.length; i++ ){
			double diff = mDescriptor[i] - point.getLocation()[i];
			sum += diff*diff; 
		}
		return (double)Math.sqrt(sum);
	}

}
