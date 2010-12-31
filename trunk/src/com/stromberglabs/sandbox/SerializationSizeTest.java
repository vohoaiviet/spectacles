package com.stromberglabs.sandbox;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Random;

public class SerializationSizeTest {
	private String mFile;
	private double[] mValues;
	
	public SerializationSizeTest(int numValues,String folder){
		Random r = new Random();
		mValues = new double[numValues];
		for ( int i = 0; i < numValues; i++ ){
			mValues[i] = r.nextDouble();
		}
		
		mFile = folder;
		try {
			ObjectOutputStream stream = new ObjectOutputStream( new FileOutputStream(mFile));
			stream.writeObject(mValues);
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]){
		new SerializationSizeTest(60000*400,"C:\\test");
	}
}
