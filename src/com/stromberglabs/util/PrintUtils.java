package com.stromberglabs.util;


public class PrintUtils {
	public static String printableFloatArrayString(float[] values){
		String str = "[";
		for ( float val : values ){
			str += val + ",";
		}
		return str.substring(0,str.length() - 1) + "]";
	}
	
	public static String printableFloatArrayString(Double[] values){
		String str = "[";
		for ( Double val : values ){
			str += String.valueOf(val) + ",";
		}
		return str.substring(0,str.length() - 1) + "]";
	}
}