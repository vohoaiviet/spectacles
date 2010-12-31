package com.stromberglabs.visual.search.lire;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.AutoColorCorrelogram;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import com.stromberglabs.index.FileIndex;
import com.stromberglabs.visual.search.QueryHandler;
import com.stromberglabs.visual.search.Score;

public class ColorCorrelogramReranker  {
	IndexReader mReader;
	
	public ColorCorrelogramReranker(String indexDirectory) throws CorruptIndexException, IOException {
		mReader = IndexReader.open(FSDirectory.open(new File(indexDirectory)), true);
	}
	
	public boolean appearsValid(Document query, Document database){
		return false;
	}
	
	public Document findDocument(String identifier) throws CorruptIndexException, IOException{
		for ( int i = 0; i < mReader.maxDoc(); i++ ){
			if ( mReader.isDeleted(i) ) continue;
			if ( mReader.document(i).get(DocumentBuilder.FIELD_NAME_IDENTIFIER).equals(identifier) ){
				return mReader.document(i);
			}
		}
		return null;
	}
	
	public List<Score> filterResults(BufferedImage image, String filename,List<Score> originalScores) throws IOException{
		AutoColorCorrelogram desc = new AutoColorCorrelogram();
		desc.extract(image);
		for ( Score score : originalScores ){
			Document doc = findDocument(score.getTarget());
			if ( doc == null ){
				System.out.println("Unable to find document for " + score.getTarget());
				continue;
			}
			AutoColorCorrelogram db = new AutoColorCorrelogram();
			db.setStringRepresentation(doc.get(DocumentBuilder.FIELD_NAME_AUTOCOLORCORRELOGRAM));
			float distance = desc.getDistance(db);
			System.out.println(doc.get(DocumentBuilder.FIELD_NAME_IDENTIFIER) + "," + distance);
		}
		List<Score> scores = new ArrayList<Score>();
		return scores;
	}
	
	public static void main(String args[]){
		try {
			ColorCorrelogramReranker reranker = new ColorCorrelogramReranker("D:\\small\\web-index\\autocc");
			QueryHandler.initHandler("D:\\small\\web-index\\");
			QueryHandler handler = QueryHandler.getHandler();
			File f = new File("H:\\bookbench\\rotated\\006105691X.jpg");
			List<Score> scores = handler.findBest(f,20).getAllScores();
			reranker.filterResults(ImageIO.read(f),FileIndex.extractName(f.getName()),scores);
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

