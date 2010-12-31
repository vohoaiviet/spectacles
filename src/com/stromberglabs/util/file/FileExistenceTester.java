package com.stromberglabs.util.file;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.stromberglabs.db.DBConnectionManager;

public class FileExistenceTester {
	public static void main(String args[]){
		Connection conn = DBConnectionManager.getConnection();
		try {
			ResultSet rs = conn.prepareStatement("select filename from files where group_id=2").executeQuery();
			int count = 0;
			while ( rs.next() ){
				File f = new File(rs.getString(1));
				//f = new File("E:\\H\\amazon_book_covers\\" + f.getName());
				if ( ! f.exists() ){
					System.out.println("delete from files where filename='" + rs.getString(1).replaceAll("\\\\","\\\\\\\\") + "';");
					count++;
				}
//				
//				System.out.println(rs.getString(1) + " exists? " + f.exists());
			}
			System.out.println(count);
		} catch ( SQLException e ){
			e.printStackTrace();
		}
	}
}
