package com.stromberglabs.visual.ip.cache;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;
import java.util.Set;

import com.stromberglabs.cluster.Clusterable;
import com.stromberglabs.util.cluster.BasicInterestPoint;

public class BasicPointCache {
	private Map<Long,BasicInterestPoint> mPoints;
	
	public BasicPointCache(int numPoints){
		Connection conn = getConnection();
		
	}
	
	public Clusterable getPoint(long id){
		return mPoints.get(id);
	}
	
	public Set<Long> getIds(){
		return mPoints.keySet();
	}
	
	private Connection getConnection(){
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/imagesearch","root","sunshine");
		} catch (Exception e){
			e.printStackTrace();
		}
		return conn;
	}

}
