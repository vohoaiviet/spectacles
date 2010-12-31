package com.stromberglabs.visual.ip.cache;

import com.stromberglabs.jopensurf.InterestPoint;

public abstract class AbstractInterestPoint implements InterestPoint {
	public double getDistance(InterestPoint point) {
		double sum = 0;
		if (point.getLocation() == null || getLocation() == null)
			return Float.MAX_VALUE;
		for (int i = 0; i < getLocation().length; i++) {
			double diff = getLocation()[i] - point.getLocation()[i];
			sum += diff * diff;
		}
		return (double) Math.sqrt(sum);
	}
}
