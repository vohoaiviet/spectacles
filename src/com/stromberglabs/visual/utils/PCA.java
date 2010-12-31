package com.stromberglabs.visual.utils;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealMatrixImpl;

public class PCA {
	private static final double[][] testMatrix = { {0,1,2,3}, {0,1,2,3} };
	
	public static void main(String args[]){
		RealMatrix X = new RealMatrixImpl(testMatrix);
		
		RealMatrix XPrime = X.transpose();
		RealMatrix SSCP = XPrime.multiply(X);
		
		System.out.println(SSCP.toString());
		
		
	}
}
