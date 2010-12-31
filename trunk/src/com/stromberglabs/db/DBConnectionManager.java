package com.stromberglabs.db;

import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.commons.configuration.Configuration;

import com.stromberglabs.util.Config;

/**
 * A single point to request connections from, at a later date this might
 * require pooling and checking them in/out, but for now it just generates
 * a new connection each time.
 * 
 * @author Andrew
 *
 */
public class DBConnectionManager {
	public static Connection getConnection(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Configuration config = Config.getConfiguration();
			return DriverManager.getConnection(config.getString("point.cache.jdbc.url"),
												config.getString("point.cache.jdbc.username"),
												config.getString("point.cache.jdbc.password"));
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
}
