package com.stromberglabs.index;

import gnu.trove.map.hash.TLongLongHashMap;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.stromberglabs.visual.search.FileList;
import com.stromberglabs.visual.search.L1ImageWordIndex;

public class FileIndex {
	private static final String DATA_FILE_NAME = "file_data.idx";
	private static final String INFO_FILE_NAME = "file_meta.idx";
	
	RandomAccessFile mDataFile;
	RandomAccessFile mInfoFile;
	
	//contains the id and file offsets of each file name
	private TLongLongHashMap mFileNameLocations = new TLongLongHashMap();
	
	public FileIndex(L1ImageWordIndex idx, File directory) throws IOException {
		File f = new File(directory,DATA_FILE_NAME);
		mDataFile = new RandomAccessFile(f,"rw");
		f = new File(directory,INFO_FILE_NAME);
		mInfoFile = new RandomAccessFile(f,"rw");
		
		//build out the files
		HashSet<String> files = new HashSet<String>();
		long id = 0;
		for ( FileList list : idx.getItems().values() ){
			for ( String file : list.getFiles() ){
				String fName = extractName(file);
				if ( files.contains(fName) ){
					continue;
				}
				//System.out.println("Writing out file: " + fName);
				mFileNameLocations.put(id++,mDataFile.getFilePointer());
				mDataFile.writeBytes(fName + '\n');
				files.add(fName);
				if ( id % 1000 == 0 ) {System.out.println("id = " + id);}
			}
		}
		
		for ( long fileId : mFileNameLocations.keys() ){
			mInfoFile.writeLong(fileId);
			mInfoFile.writeLong(mFileNameLocations.get(fileId));
		}
		mInfoFile.writeLong(-1);
		
		mInfoFile.close();
		mDataFile.close();
		
		mDataFile = new RandomAccessFile(new File(directory,DATA_FILE_NAME),"r");
		mInfoFile = new RandomAccessFile(new File(directory,INFO_FILE_NAME),"r");
	}

	public FileIndex(String directory) throws IOException {
		mDataFile = new RandomAccessFile(new File(directory,DATA_FILE_NAME),"r");
		mInfoFile = new RandomAccessFile(new File(directory,INFO_FILE_NAME),"r");
		try {
			long id = -1;
			while ( (id = mInfoFile.readLong()) != -1 ){
				long location = mInfoFile.readLong();
				mFileNameLocations.put(id,location);
			}
		} catch ( EOFException e ){
			e.printStackTrace();
			//ignore, just means we hit the end of the file
		}
//		System.out.println("mFileNameLocations.get(0099472422) = " + mFileNameLocations.get("0099472422"));
//		System.out.println("mFileNameLocations has " + mFileNameLocations.keySet().size() + " mappings");
	}
	
	public String getFilename(long fileId) throws IOException {
		mDataFile.seek(mFileNameLocations.get(fileId));
		return mDataFile.readLine();
	}
	
	/**
	 * ONLY USE THIS FOR CONSTRUCTION OF A WordIndex, it's expensive
	 * It builds a map of file name -> file id
	 * !!Do not confuse the file ID with the word ID!!
	 * @return
	 */
	public Map<String,Long> generateFileToIdMap(){
		Map<String,Long> mapping = new HashMap<String, Long>();
		for ( long id : mFileNameLocations.keys() ){
			try {
				String file = getFilename(id);
				if ( mapping.containsKey(file) ){
					System.out.println("For some reason I Have " + file + " in there twice");
				}
				mapping.put(getFilename(id),id);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return mapping;
	}
	
	public int getNumFiles(){
		return mFileNameLocations.size();
	}
	
	public static String extractName(String file){
		File f = new File(file);
		String name = f.getName();
		if ( name.contains(".") )
			return name.split("\\.")[0];
		return name;
	}
	
	public static void main(String args[]){
		try {
			FileIndex idx = new FileIndex("D:\\test");
			Map<String,Long> mapping = idx.generateFileToIdMap();
			for ( String key : mapping.keySet()){
				System.out.println(mapping.get(key) + " => " + key);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
