package com.stromberglabs.visual.search.lire;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;

public class ColorCorrelogramIndexBuilder extends AbstractIndexBuilder {
	public ColorCorrelogramIndexBuilder(String inputDir,String indexDir) throws CorruptIndexException, LockObtainFailedException, IOException {
		super(inputDir, indexDir);
	}
	
	protected DocumentBuilder getDocBuilder(){
		return DocumentBuilderFactory.getDefaultAutoColorCorrelationDocumentBuilder();
	}
	
	public static void main(String args[]){
		try {
			new ColorCorrelogramIndexBuilder("D:\\bestsellers","D:\\small\\web-index\\autocc");
		} catch ( Exception e ){
			e.printStackTrace();
		}
	}
}
