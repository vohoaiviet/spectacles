package com.stromberglabs.util.cluster;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.stromberglabs.util.PrintUtils;
import com.stromberglabs.visual.ip.cache.AbstractInterestPoint;
import com.stromberglabs.visual.ip.cache.InterestPointCache;

/**
 * A class for a basic interest point, actually stores the filename, location, and point id in memory
 * 
 * @author Andrew
 *
 */
public class BasicInterestPoint extends AbstractInterestPoint {
	private long mPointId = -1;
	private String mFileName;
	private float mLocation[];

	/**
	 * Not actually sure if I should use this anymore
	 * @param rs
	 * @param id
	 */
	@Deprecated
	public BasicInterestPoint(ResultSet rs, long id) {
		try {
			mLocation = new float[64];
			for (int i = 0; i < 64; i++) {
					mLocation[i] = rs.getFloat(i + 4);
			}
			mPointId = id;
			mFileName = rs.getString(3);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Call this to create a result set from a db query
	 * @param rs
	 */
	public BasicInterestPoint(ResultSet rs){
		try {
			mPointId = rs.getInt(InterestPointCache.ID_COL);
			mFileName = rs.getString(InterestPointCache.FILENAME_COL);
			
			int dimensionality = rs.getMetaData().getColumnCount()-3;
			mLocation = new float[dimensionality];
			for (int i = 0; i < dimensionality; i++) {
					mLocation[i] = rs.getFloat(i + 4);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public BasicInterestPoint(float[] location,String fileName){
		mLocation = location;
		mFileName = fileName;
	}

	public float[] getLocation() {
		return mLocation;
	}

	public String getFile() {
		return mFileName;
	}

	public long getId() {
		return mPointId;
	}
	
	public String toString(){
		return mPointId + "," + mFileName + "," + PrintUtils.printableFloatArrayString(mLocation);
	}
}
