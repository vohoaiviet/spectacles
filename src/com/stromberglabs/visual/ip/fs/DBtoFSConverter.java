package com.stromberglabs.visual.ip.fs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.stromberglabs.util.cluster.BasicInterestPoint;

public class DBtoFSConverter {
	public DBtoFSConverter(){
		Connection c = getConnection();
		try {
			PreparedStatement stmt = c.prepareStatement("select distinct filename from descriptors where group_id=1",ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			PreparedStatement fstmt = c.prepareStatement("select * from descriptors where filename=? and group_id=1",ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			System.out.println("Looking for filenames");
			ResultSet rs = stmt.executeQuery();
			System.out.println("Found filenames");
			int count = 0;
			while ( rs.next() ){
				String filename = rs.getString(1);
				System.out.println(filename);
				fstmt.setString(1,filename);
				ResultSet frs = fstmt.executeQuery();
				List<BasicInterestPoint> points = new ArrayList<BasicInterestPoint>(); 
				while ( frs.next() ){
					points.add(new BasicInterestPoint(frs));
				}
				System.out.println("Had " + points.size() + " points");
				float[][] values = new float[points.size()][points.get(0).getLocation().length];
				for ( int i = 0; i < points.size(); i++ ){
					values[i] = points.get(i).getLocation();
				}
				File of = new File(points.get(0).getFile());
				File f = new File("E:\\H\\cache_test\\" + of.getName() + ".points");
				try {
					ObjectOutputStream stream = new ObjectOutputStream( new FileOutputStream(f));
					stream.writeObject(values);
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				count++;
				if ( count % 10000 == 0 ) System.out.println(count);
			}
		} catch ( SQLException e){
			e.printStackTrace();
		}
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
	
	public static void main(String args[]){
		new DBtoFSConverter();
	}
}
