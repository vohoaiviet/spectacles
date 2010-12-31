package com.stromberglabs.visual.ip.cache;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.littletechsecrets.LRUCache;
import com.stromberglabs.cluster.Clusterable;
import com.stromberglabs.util.cluster.BasicInterestPoint;

public class FileMapInterestPointCache implements InterestPointCache {
	//TODO: Remove or use this
	@SuppressWarnings("unused")
	private static FileMapInterestPointCache mCache;
	
	private Connection mConnection;
	private String mPointsStmt = "select id,filename from descriptors where filename=?";
	private String mFileStmt = "select filename from files where filename like ? limit ?";
	//TODO: Remove or use this
	@SuppressWarnings("unused")
	private String mAllNthStmt = "select id,filename from (select *,@a:=@a+1 as rc where filename like ?) where rc%?=0";
	private String mSingleStmt = "select * from descriptors where id=?";
	
	//The collection caches interest points by file name up to a max number of points
	private int mPointCacheSize;
	private Map<Long,BasicInterestPoint> mPointCache;
	private Map<String,List<Long>> mFileToId;
	private Map<Long,String> mIdToFile;
	
	private long mNumHits = 0;
	private long mNumMisses = 0;
	
	//TODO: Remove or use this
	@SuppressWarnings("unused")
	private int mSkipNImages = 0;
	private long mMaxImages = 0;
	
	private String mCacheRoot;
	
	public FileMapInterestPointCache(int cacheSize, String cacheRoot, long maxImages, int skipNImages){
		mPointCacheSize = cacheSize;
		mPointCache = new LRUCache<Long,BasicInterestPoint>(mPointCacheSize);
		mFileToId = new HashMap<String,List<Long>>();//File to a list of it's id's
		mIdToFile = new HashMap<Long,String>();//The descriptor id to file name map
		mCacheRoot = cacheRoot;
		mSkipNImages = skipNImages;
		mMaxImages = maxImages;
		initConnection();
		initFileToIdCache();
	}
	
	private Connection initConnection(){
		if ( mConnection == null ){
			try {
				Class.forName("com.mysql.jdbc.Driver");
				mConnection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/imagesearch","root","sunshine");
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		return mConnection;
	}
	
	private void initFileToIdCache(){
		try {
			PreparedStatement fileStmt = mConnection.prepareStatement(mFileStmt);
			fileStmt.setString(1,mCacheRoot.replaceAll("\\\\","\\\\\\\\") + "%");
			fileStmt.setLong(2,mMaxImages > 0 ? mMaxImages : Long.MAX_VALUE);
			ResultSet rs = fileStmt.executeQuery();
			int count = 0;
			while ( rs.next() ){
				PreparedStatement stmt = mConnection.prepareStatement(mPointsStmt,java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_READ_ONLY);
				stmt.setString(1,rs.getString(1));
				ResultSet frs = stmt.executeQuery();
				while ( frs.next() ){
					long id = frs.getLong(1);
					String filename = frs.getString(2);
					mIdToFile.put(id,filename);
					List<Long> ids = null;
					if ( mFileToId.containsKey(filename) ){
						ids = mFileToId.get(filename);
					} else {
						ids = new ArrayList<Long>();
						mFileToId.put(filename,ids);
					}
					ids.add(id);
					count++;
				}
				frs.close();
				stmt.close();
			}
			System.out.println(count + " points");
			rs.close();
			fileStmt.close();
//			
//			PreparedStatement stmt = mConnection.prepareStatement(mAllStmt,java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_READ_ONLY);
//			if ( mMaxImages > 0 ) {
//				stmt = mConnection.prepareStatement(mPartialStmt,java.sql.ResultSet.TYPE_FORWARD_ONLY,java.sql.ResultSet.CONCUR_READ_ONLY);
//				stmt.setLong(2,mMaxImages);
//			}
//			stmt.setFetchSize(Integer.MIN_VALUE);
//			if ( mSkipNImages > 0 ){
//				stmt = mConnection.prepareStatement(mAllNthStmt);
//				stmt.setInt(2,mSkipNImages);
//			}
//			stmt.setString(1,mCacheRoot.replaceAll("\\\\","\\\\\\\\") + "%");
//			//System.out.println(stmt.toString());
//			ResultSet rs = stmt.executeQuery();
//			while ( rs.next() ){
//				long id = rs.getLong(1);
//				String filename = rs.getString(2);
//				if ( mMaxImages > 0 && mFileToId.keySet().size() >= mMaxImages ) break;
//				mIdToFile.put(id,filename);
//				//System.out.println("caching file " + filename);
//				List<Long> ids = null;
//				if ( mFileToId.containsKey(filename) ){
//					ids = mFileToId.get(filename);
//				} else {
//					ids = new ArrayList<Long>();
//					mFileToId.put(filename,ids);
//				}
//				ids.add(id);
//			}
//			//System.out.println("There are " + mFileToId.keySet().size() + " images");
//			rs.close();
//			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	public Clusterable getPoint(long pointId) {
//		if ( ((mNumHits + mNumMisses) % 1000000) == 0 ){
//			info();
//		}
		//if ( mNumHits % 1000000 == 0)
		//	System.out.println("Requested point: " + pointId + ", is in cache? " + mPointCache.containsKey(pointId) + ", number of hits: " + mNumHits + ", number of misses: " + mNumMisses + ", miss percent: " + ((double)mNumMisses/(double)(mNumHits+mNumMisses))*100d);
		if ( mPointCache.containsKey(pointId) ){
			mNumHits++;
			return mPointCache.get(pointId);
		}
		mNumMisses++;
		BasicInterestPoint point = null;
		try {
			PreparedStatement stmt = mConnection.prepareStatement(mSingleStmt);
			stmt.setLong(1,pointId);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			point = new BasicInterestPoint(rs,pointId);
			//System.out.println("Retrieved interest point: " + PrintUtils.printableFloatArrayString(point.getLocation()));
			rs.close();
			stmt.close();
		} catch ( SQLException e ){
			e.printStackTrace();
		}
		mPointCache.put(pointId,point);
		return point;
	}
	
	public String getFilename(long id){
		return mIdToFile.get(id);
	}
	
	public List<Clusterable> getPoints(File file) throws IOException {
		String filePath = file.getAbsolutePath();
		return getPoints(filePath);
	}
	
	public List<Clusterable> getPoints(String file){
		List<Clusterable> points = new ArrayList<Clusterable>();
		List<Long> ids = mFileToId.get(file);
		for ( Long id : ids ){
			points.add(getPoint(id));
		}
		return points;
	}
	
	public Set<String> getFiles(){
		return mFileToId.keySet();
	}
	
	public List<Long> getIds(){
		List<Long> mKeys = new ArrayList<Long>(mIdToFile.size());
		mKeys.addAll(mIdToFile.keySet());
		return mKeys;
	}
	
	public static void main(String args[]){
//		String cacheRoot = "H:\\ukbench\\training_small";
//		InterestPointCache cache = InterestPointCache.getCache(cacheRoot);
//		List<Clusterable> mPoints = new ArrayList<Clusterable>(cache.getInterestPointIds().size());
//		System.out.println("JVM Usage: " + Runtime.getRuntime().totalMemory());
//		for ( Integer id : InterestPointCache.getCache(cacheRoot).getInterestPointIds() ){
//			mPoints.add(new VirtualInterestPoint(id,InterestPointCache.getCache(cacheRoot)));
//		}
//		System.out.println("JVM Usage: " + Runtime.getRuntime().totalMemory());
//		KMeansTree tree = new KMeansTree(mPoints,6,10);
//		System.out.println("Tree height: " + tree.getTreeHeight());
//		System.out.println("Number of points: " + cache.getInterestPointIds().size());
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e){
//			
//		}
	}
	
	//TODO: Remove or use this
	@SuppressWarnings("unused")
	private void info(){
		System.out.format("Fill percent: %1.3f%%, Miss Rate: %1.3f%%",(mPointCache.size()/(float)mPointCacheSize)*100f,((double)mNumMisses/(double)(mNumHits+mNumMisses))*100d);
		System.out.println();
	}

	public Iterator<Clusterable> getAllPoints() {
		// TODO Auto-generated method stub
		return null;
	}
	
	//TODO: Remove or use this
	@SuppressWarnings("unused")
	private class FileMapCacheIterator implements Iterator<Clusterable> {
		private Iterator<Long> mIterator;
		private FileMapInterestPointCache mCache;
		
		public FileMapCacheIterator(FileMapInterestPointCache cache){
			mIterator = cache.getIds().iterator();
			mCache = cache;
		}
		
		public boolean hasNext() {
			return mIterator.hasNext();
		}

		public Clusterable next() {
			return mCache.getPoint(mIterator.next());
		}

		public void remove() {
			throw new RuntimeException("You shouldn't be calling remove on this iterator");
		}
		
	}
}
