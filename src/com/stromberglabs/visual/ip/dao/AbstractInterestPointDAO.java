package com.stromberglabs.visual.ip.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.stromberglabs.db.DBConnectionManager;
import com.stromberglabs.visual.ip.creator.InterestPointCreator;

public abstract class AbstractInterestPointDAO implements InterestPointDAO {
	private static Logger logger = Logger.getLogger(AbstractInterestPointDAO.class);
	
	private Connection mConnection;
	private PreparedStatement mCountStmt;
	private PreparedStatement mSaveStmt;
	private PreparedStatement mFileSaveStmt;
	
	private InterestPointCreator mCreator;
	
	public AbstractInterestPointDAO(InterestPointCreator creator){
		mCreator = creator;
		mConnection = DBConnectionManager.getConnection();
		try {
			mSaveStmt = mConnection.prepareStatement(getSaveSQL());
			mCountStmt = mConnection.prepareStatement(getCountSQL());
			mFileSaveStmt = mConnection.prepareStatement(getFileSaveSQL());
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	protected abstract String getSaveSQL();
	
	protected abstract String getCountSQL();
	
	protected abstract String getFileSaveSQL();
	
	public void saveInterestPoint(float[] point, String filename, int groupid){
		try {
			mSaveStmt.setInt(1,groupid);
			mSaveStmt.setString(2,filename);
			for ( int i = 0; i < point.length; i++ ){
				mSaveStmt.setFloat(i+3,point[i]);
			}
			mSaveStmt.execute();
		} catch (SQLException e) {
			logger.fatal(e,e);
		}
	}
	
	public void saveFile(String file, int groupId){
		try {
			mFileSaveStmt.setString(1,file);
			mFileSaveStmt.setInt(2,groupId);
			mFileSaveStmt.execute();
		} catch ( SQLException e ){
			logger.fatal(e,e);
		}
	}
	
	public boolean hasInterestPoints(String filename){
		int count = 0;
		try {
			mCountStmt.setString(1,filename);
			ResultSet rs = mCountStmt.executeQuery();

			rs.next();
			count = rs.getInt(1);
			
			DbUtils.close(rs);
		} catch ( SQLException e ) {
			logger.fatal(e,e);
		}
		return count > 0;
	}
	
	public InterestPointCreator getCreator(){
		return mCreator;
	}
	
	public abstract String getTablePrefix();
}
