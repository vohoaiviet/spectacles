package com.stromberglabs.visual.ip.dao;

import com.stromberglabs.visual.ip.creator.InterestPointCreator;

public interface InterestPointDAO {
	/**
	 * True if there are already interest points for that file
	 */
	public boolean hasInterestPoints(String filename);

	public void saveInterestPoint(float[] point, String filename, int groupId);
	
	public void saveFile(String file, int groupId);
	
	public InterestPointCreator getCreator();
	
	public String getTablePrefix();
}
