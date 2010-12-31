package com.stromberglabs.index;

import gnu.trove.map.hash.TIntLongHashMap;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.stromberglabs.visual.search.FileList;
import com.stromberglabs.visual.search.L1ImageWordIndex;

/**
 * Two files:
 * Info File contains a list of:
 * 	WordID Position WordID Position WordID Position
 *  Long   Long     Long   Long     Long   Long
 *  
 *  This file is read in and stored in a map so that when a particular
 *  word is requested from the file, it can jump to that position in the
 *  file and read off the word's file list structure
 *  
 *  IDEALLY: 
 *  Data File is of the format:
 *   Weight FileID Count FileID Count FileID ... FileID Count EOL
 *   Double Long   Int   Long   Int   Long   ... Long   Int   -1
 *
 *	ACTUALLY:
 *  Data File is of the format:
 *   Weight  TotalCount  FileID  Count   FileID  Count   FileID  Count   FileID  EOL
 *   Double  Double      Long    Double  Long    Double  Long    Double  Long    -1
 *  
 *  So for getting a word's list, it looks up it's start position, then reads
 *  off a float for the weight, then reads off sequential longs and ints for
 *  the list of files and their counts on a particular word
 *  
 * @author Andrew
 *
 */
public class WordIndex {
	private static final String DATA_FILE_NAME = "word_data.idx";
	private static final String INFO_FILE_NAME = "word_meta.idx";
	
	private static Logger sLogger = Logger.getLogger(WordIndex.class);
	
	RandomAccessFile mDataFile;
	RandomAccessFile mInfoFile;
	
	private TIntLongHashMap mFileListLocations = new TIntLongHashMap();
	
	public WordIndex(L1ImageWordIndex objidx, Map<String,Long> mapping, File directory) throws IOException {
		System.out.println("mapping has " + mapping.keySet().size() + " keys");
		File f = new File(directory,DATA_FILE_NAME);
		mDataFile = new RandomAccessFile(f,"rw");
		f = new File(directory,INFO_FILE_NAME);
		mInfoFile = new RandomAccessFile(f,"rw");
		
		for ( Integer wordId : objidx.getItems().keySet() ){
			mFileListLocations.put(wordId,mDataFile.getFilePointer());
			FileList list = objidx.getItems().get(wordId);
			writeFileList(list,mapping);
		}
		
		int count = 0;
		for ( int wordId : mFileListLocations.keys() ){
			mInfoFile.writeInt(wordId);
			mInfoFile.writeLong(mFileListLocations.get(wordId));
			count++;
		}
		mInfoFile.writeInt(-1);
		System.out.println("Wrote out " + mFileListLocations.size() + " locations");
		
		mDataFile.close();
		mInfoFile.close();
		
		f = new File(directory,DATA_FILE_NAME);
		mDataFile = new RandomAccessFile(f,"r");
		f = new File(directory,INFO_FILE_NAME);
		mInfoFile = new RandomAccessFile(f,"r");
	}
	
	private void writeFileList(FileList list, Map<String,Long> mapping) throws IOException{
		mDataFile.writeDouble(list.getWeight());
		mDataFile.writeDouble(list.getTotalCount());
//		System.out.println("weight = " + list.getWeight() + ", totalCount = " + list.getTotalCount());
		for ( String file : list.getFiles() ){
			//look up it's id
			String name = FileIndex.extractName(file);
			Long id = mapping.get(name);
			double count = list.getFileWordCount(file);
//			System.out.println("file " + name + " has id " + id + " and count " + count);
			mDataFile.writeLong(id);
			mDataFile.writeDouble(count);
		}
		mDataFile.writeLong(-1);
	}
	
	public WordIndex(String dir) throws IOException {
		mDataFile = new RandomAccessFile(new File(dir,DATA_FILE_NAME),"r");
//		System.out.println("mDataFile = " + mDataFile);
		mInfoFile = new RandomAccessFile(new File(dir,INFO_FILE_NAME),"r");
		int wordId = -1;
		try {
			while ( (wordId = mInfoFile.readInt()) != -1 ){
				long location = mInfoFile.readLong();
				mFileListLocations.put(wordId,location);
			}
		} catch ( EOFException e ){
			e.printStackTrace();
			//ignore, just means we hit the end of the file
		}
		System.out.println("Read in " + mFileListLocations.size() + " file locations");
//		for ( int word : mFileListLocations.keySet() ){
//			System.out.println("looking up word " + word + " it has position: " + mFileListLocations.get(word));
//			Document doc = getDocument(word);
//			System.out.println("Word has weight: " + doc.mWeight + " and count: " + doc.mTotalCount);
//			for ( TermFrequency frequency : doc.mFrequencies ){
//				System.out.println("File " + frequency.mFile + " has count " + frequency.mCount);
//			}
//		}
	}
	
	public Document getDocument(int id){
		List<TermFrequency> words = new ArrayList<WordIndex.TermFrequency>();
		double totalCount = 1;
		double weight = 1;
		try {
//			System.out.println("mFileListLocations.get(" + id + ") = " + mFileListLocations.get(id));
			if ( sLogger.isDebugEnabled() ){
				sLogger.debug("Requesting word id: " + id + " from word index, found at location: " + mFileListLocations.get(id));
			}
			if ( !mFileListLocations.containsKey(id) ){
				return new Document(words,0,0);
			}
			mDataFile.seek(mFileListLocations.get(id));
			weight = mDataFile.readDouble();
			totalCount = mDataFile.readDouble();
			long fileId = mDataFile.readLong();
			double count = mDataFile.readDouble();
			while ( fileId != -1 && count != -1 ){
				words.add(new TermFrequency(fileId,count));
				fileId = mDataFile.readLong();
				count = mDataFile.readDouble();
			}
		} catch ( EOFException e ){
			System.out.println("Ate it on word " + id);
		} catch ( IOException e ){
			e.printStackTrace();
		}
		return new Document(words,weight,totalCount);
	}
	
	public class Document {
		public double mTotalCount;
		public List<TermFrequency> mFrequencies;
		public double mWeight;
		public Document(List<TermFrequency> words,double weight,double totalCount){
			mFrequencies = words;
			mWeight = weight;
			mTotalCount = totalCount;
		}
	}
	
	public class TermFrequency {
		public long mFile;
		public double mCount;
		public TermFrequency(long file, double count){
			mFile = file;
			mCount = count;
		}
	}
	
	public static void main(String args[]){
		try {
			new WordIndex("D:\\test");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
