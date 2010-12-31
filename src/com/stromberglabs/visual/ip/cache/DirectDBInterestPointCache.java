package com.stromberglabs.visual.ip.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.littletechsecrets.LRUCache;
import com.stromberglabs.cluster.Clusterable;
import com.stromberglabs.db.DBConnectionManager;
import com.stromberglabs.util.cluster.BasicInterestPoint;
import com.stromberglabs.visual.ip.dao.InterestPointDAO;

public class DirectDBInterestPointCache implements InterestPointCache {
	private static final String mAllDescriptorsFileLimited = "select * from descriptors where group_id=? order by id asc limit ?";
	
	private static PreparedStatement mAllStmt;
	private static PreparedStatement mAllLimitedStmt;
	private static PreparedStatement mAllFileLimitedStmt;
	private static PreparedStatement mSelectSingleStmt;
	private static PreparedStatement mSelectSingleByFileStmt;
	private static PreparedStatement mSelectIdsStmt;
	private static PreparedStatement mSelectIdsStmtLimited;
	private static PreparedStatement mSelectFilesStmt;
	
	private Connection mConnection;
	
	private LRUCache<Long,BasicInterestPoint> mCache;
	
	private int mGroupId = -1;
	
	private int mMaxFiles;
	private long mMaxPoints;
	private String mTablePrefix = "";
	
	public DirectDBInterestPointCache(int groupId,int maxFiles,InterestPointDAO dao){
		mGroupId = groupId;
		mMaxFiles = maxFiles;
		mCache = new LRUCache<Long, BasicInterestPoint>(200000);
		mTablePrefix = dao.getTablePrefix();
		initConnection();
	}
	
	public DirectDBInterestPointCache(int groupId,long maxPoints,InterestPointDAO dao){
		mGroupId = groupId;
		mMaxPoints = maxPoints;
		mCache = new LRUCache<Long, BasicInterestPoint>(20000000);
		mTablePrefix = dao.getTablePrefix();
		initConnection();
	}
	/**
	 * Gives you an streaming iterator to the complete list of points
	 * with this group id
	 */
	public Iterator<Clusterable> getAllPoints(){
		try {
			PreparedStatement stmt = null;
			if ( mMaxPoints > 0 ){
				stmt = mAllLimitedStmt;
				stmt.setLong(2,mMaxPoints);
			} else {
				stmt = mAllStmt;
			}
			stmt.setInt(1,mGroupId);
			ResultSet rs = stmt.executeQuery();
			return new InterestPointIterator(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private Connection initConnection(){
		mConnection =  DBConnectionManager.getConnection();
		try {
			mAllStmt = mConnection.prepareStatement(getAllDesciptorsSQL(),ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			mAllStmt.setFetchSize(Integer.MIN_VALUE);
			mSelectSingleStmt = mConnection.prepareStatement(getOneByIDSQL(),ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			mSelectSingleStmt.setFetchSize(Integer.MIN_VALUE);
			mSelectSingleByFileStmt = mConnection.prepareStatement(getOneByNameSQL());
			mSelectSingleByFileStmt.setFetchSize(Integer.MIN_VALUE);
			mSelectIdsStmt = mConnection.prepareStatement(getSelectIDSQL(),ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			mSelectIdsStmt.setFetchSize(Integer.MIN_VALUE);
			mSelectFilesStmt = mConnection.prepareStatement(getSelectFilesSQL(),ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			mSelectFilesStmt.setFetchSize(Integer.MIN_VALUE);
			mAllLimitedStmt = mConnection.prepareStatement(getLimitedDescriptorsSQL(),ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			mAllLimitedStmt.setFetchSize(Integer.MIN_VALUE);
			mSelectIdsStmtLimited = mConnection.prepareStatement(getSelectIdsLimited(),ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			mSelectIdsStmtLimited.setFetchSize(Integer.MIN_VALUE);
		} catch ( SQLException e ){
			e.printStackTrace();
		}
		return mConnection;
	}
	
	private String getAllDesciptorsSQL(){
		return "select * from " + mTablePrefix + "descriptors where group_id=? order by id asc";
	}
	
	private String getOneByIDSQL(){
		return "select * from " + mTablePrefix + "descriptors where id=?";
	}
	
	private String getOneByNameSQL(){
		return "select * from " + mTablePrefix + "descriptors where filename=?";
	}
	
	private String getSelectIDSQL(){
		return "select id from " + mTablePrefix + "descriptors where group_id=?";
	}
	
	private String getLimitedDescriptorsSQL(){
		return "select * from " + mTablePrefix + "descriptors where group_id=? order by id asc limit ?";
	}
	
	private String getSelectFilesSQL(){
		return "select filename from " + mTablePrefix + "files where group_id=? limit ?";
	}
	
	private String getSelectIdsLimited(){
		return "select id from " + mTablePrefix + "descriptors where group_id=? order by id asc limit ?";
	}
	
	public String getFilename(long pointId) {
		try {
			mSelectSingleStmt.setLong(1,pointId);
			ResultSet rs = mSelectSingleStmt.executeQuery();
			if ( rs.next() ){
				return rs.getString(InterestPointCache.FILENAME_COL);
			}
		} catch ( SQLException e ){
			e.printStackTrace();
		}
		return null;
	}

	public Set<String> getFiles() {
		try {
			mSelectFilesStmt.setInt(1,mGroupId);
			mSelectFilesStmt.setLong(2,mMaxFiles);
			ResultSet rs = mSelectFilesStmt.executeQuery();
			Set<String> files = new HashSet<String>();
			while ( rs.next() ){
				files.add(rs.getString(1));
			}
			System.out.println(files.size());
			return files;
		} catch ( SQLException e ){
			e.printStackTrace();
		}
		return null;
	}

	public List<Long> getIds() {
		try {
			PreparedStatement stmt = null;
			if ( mMaxPoints > 0 ){
				stmt = mSelectIdsStmtLimited;
				stmt.setLong(2,mMaxPoints);
			} else {
				stmt = mSelectIdsStmt;
			}
			stmt.setInt(1,mGroupId);
			ResultSet rs = stmt.executeQuery();
			List<Long> ids = new ArrayList<Long>();
			while ( rs.next() ){
				ids.add(rs.getLong(InterestPointCache.ID_COL));
			}
			return ids;
		} catch ( SQLException e ){
			e.printStackTrace();
		}
		return null;
	}

	public Clusterable getPoint(long pointId) {
		if ( mCache.containsKey(pointId) ){
			return mCache.get(pointId);
		}
		try {
			mSelectSingleStmt.setLong(1,pointId);
			ResultSet rs = mSelectSingleStmt.executeQuery();
			if ( rs.next() ){
				BasicInterestPoint c = new BasicInterestPoint(rs);
				rs.close();
				mCache.put(pointId,c);
				return c;
			}
		} catch ( SQLException e ){
			e.printStackTrace();
		}
		return null;
	}

	public List<Clusterable> getPoints(String file) {
		try {
			mSelectSingleByFileStmt.setString(1,file);
			ResultSet rs = mSelectSingleByFileStmt.executeQuery();
			List<Clusterable> points = new LinkedList<Clusterable>();
			while ( rs.next() ){
				BasicInterestPoint point = new BasicInterestPoint(rs);
				points.add(point);
			}
			return points;
		} catch ( SQLException e ){
			e.printStackTrace();
		}
		return null;
	}
	
	private class InterestPointIterator implements Iterator<Clusterable>{
		private ResultSet rs;
		
		public InterestPointIterator(ResultSet rs){
			this.rs = rs;
		}
		
		public boolean hasNext() {
			try {
				return !rs.isAfterLast();
			} catch ( SQLException e ){
				e.printStackTrace();
			}
			return false;
		}

		public Clusterable next() {
			try {
				rs.next();
				if ( rs.isAfterLast() ){
					return null;
				}
				return new BasicInterestPoint(rs);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		public void remove() {
			throw new RuntimeException("Shouldn't be removing here");
		}
	}
	
	//TODO: Use it or lose it
	@SuppressWarnings("unused")	
	private class IdIterator implements Iterator<Long>{
		private ResultSet rs;
		
		public IdIterator(ResultSet rs){
			this.rs = rs;
		}
		
		public boolean hasNext() {
			try {
				return rs.isAfterLast();
			} catch (SQLException e){
				e.printStackTrace();
			}
			return false;
		}

		public Long next() {
			try {
				return rs.getLong(InterestPointCache.ID_COL);
			} catch (SQLException e){
				e.printStackTrace();
			}
			return null;
		}

		public void remove() {
			throw new RuntimeException("Shouldn't be removing here");
		}
	}
}
