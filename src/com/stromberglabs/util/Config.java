package com.stromberglabs.util;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

public class Config {
	private static final Logger logger = Logger.getLogger(Config.class);
		
	public static Configuration config;
	
	public static Configuration getConfiguration() {
		if ( config == null ){
			try {
				config = new PropertiesConfiguration("conf/conf.properties");
			} catch (Exception e) {
				logger.fatal(e,e);
				throw new RuntimeException("Invalid config file");
			}
		}
		return config;
	}
	
	public static void main(String args[]){
		Config.getConfiguration();
	}
}
