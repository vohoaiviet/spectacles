package com.stromberglabs.visual.search.lire;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import net.semanticmetadata.lire.DocumentBuilder;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

import com.stromberglabs.index.FileIndex;
import com.stromberglabs.util.file.FileFilter;
import com.stromberglabs.util.file.FileFinder;

public abstract class AbstractIndexBuilder {
	public AbstractIndexBuilder(String inputDir,String indexDir) throws CorruptIndexException, LockObtainFailedException, IOException {
		List<FileFilter> filters = new ArrayList<FileFilter>(3);
		filters.add(new FileFilter(".*\\.jpg"));
		filters.add(new FileFilter(".*\\.gif"));
		filters.add(new FileFilter(".*\\.png"));

		FileFinder finder = new FileFinder(inputDir, filters);
		
		DocumentBuilder builder = getDocBuilder();
		
		IndexWriter writer = new IndexWriter(FSDirectory.open(new File(indexDir)), new SimpleAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
		
		for ( File f : finder.getFiles() ){
			Document doc = builder.createDocument(ImageIO.read(f),FileIndex.extractName(f.getName()));
			writer.addDocument(doc);
		}
		
		writer.optimize();
		writer.close();
	}
	
	protected abstract DocumentBuilder getDocBuilder();
}
