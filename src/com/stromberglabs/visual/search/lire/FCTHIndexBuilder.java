package com.stromberglabs.visual.search.lire;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;

public class FCTHIndexBuilder extends AbstractIndexBuilder {
	public FCTHIndexBuilder(String inputDir,String indexDir) throws CorruptIndexException, LockObtainFailedException, IOException {
		super(inputDir, indexDir);
	}
	
	protected DocumentBuilder getDocBuilder(){
		return DocumentBuilderFactory.getFCTHDocumentBuilder();
	}
	
	public static void main(String args[]){
		try {
			new FCTHIndexBuilder("D:\\bestsellers","D:\\small\\web-index\\fcth");
		} catch ( Exception e ){
			e.printStackTrace();
		}
	}
}
