package com.stromberglabs.visual.builder;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import com.stromberglabs.cluster.KClusterer;
import com.stromberglabs.cluster.KMeansClusterer;
import com.stromberglabs.cluster.KMeansForestClusterer;
import com.stromberglabs.db.DBConnectionManager;
import com.stromberglabs.index.WordInvertedIndex;
import com.stromberglabs.tree.KMeansTree;
import com.stromberglabs.tree.query.QueryTree;
import com.stromberglabs.util.Config;
import com.stromberglabs.util.file.SerializationUtils;
import com.stromberglabs.visual.ip.cache.DirectDBInterestPointCache;
import com.stromberglabs.visual.ip.cache.InterestPointCache;
import com.stromberglabs.visual.ip.dao.InterestPointDAO;
import com.stromberglabs.visual.ip.dao.SIFTInterestPointDAO;
import com.stromberglabs.visual.ip.dao.SURFInterestPointDAO;
import com.stromberglabs.visual.ip.dao.ThreadedInterestPointGenerator;
import com.stromberglabs.visual.search.L1ImageWordIndex;
import com.stromberglabs.visual.tree.VocabTreeManager;

/**
 * This class will encapsulate all of the steps required to build a searchable
 * index (compressed) from a folder of images. 
 * 
 * @author Andrew
 *
 */
public class VisualSearchIndexBuilder {
	private static final Logger logger = Logger.getLogger(VisualSearchIndexBuilder.class);
	
	private File mInputFolder;
	private File mIndexFolder;
	private File mTempFolder;
	
	/**
	 * This is so you can inject your own creator, the default is SURF, but there
	 * is a GPLed SIFT creator that I provide as well
	 */
	private InterestPointDAO mDAO;
	private KClusterer mClusterer;
	
	/**
	 * The constructor just validates that all the conditions are met for being able to complete a run on the images
	 * in a particular folder.
	 * 
	 * @param inputFolder - Folder with the images
	 * @param indexFolder - Folder to be used for storing the indexes
	 * @param tempFolder - Scratch folder for temporary files
	 * @throws IOException 
	 * @throws ConfigurationException
	 */
	
	private int mGroupId = -1;
	
	private int mSamplingRate = 1;
	
	public VisualSearchIndexBuilder(String inputFolder, String indexFolder, String tempFolder, InterestPointDAO ipDAO, KClusterer clusterer, int groupId, int samplingRate) throws IOException, ConfigurationException {
		mInputFolder = new File(inputFolder);
		if ( !mInputFolder.exists() || !mInputFolder.canRead() )
			throw new IOException("Unable to read from input folder: " + mInputFolder.getAbsolutePath());
		mIndexFolder = new File(indexFolder);
		if ( !mInputFolder.exists() || !mInputFolder.canWrite() )
			throw new IOException("Invalid index folder: " + mIndexFolder.getAbsolutePath());
		mTempFolder = new File(tempFolder);
		if ( !mTempFolder.exists() || !mTempFolder.canWrite() )
			throw new IOException("Invalid temp folder: " + mTempFolder.getAbsolutePath());
		
		mDAO = ipDAO;
		mClusterer = clusterer;
		
		mGroupId = groupId;
		
		mSamplingRate = samplingRate;
		
		//inits the static config
		Config.getConfiguration();
	}
	
	/**
	 * Creates the actual index. This might take some time.
	 */
	public void run(){
		logger.info("Starting run...");
		
		fetchNextGroupId();
		if ( mGroupId == -1 ) return;
		
		ThreadedInterestPointGenerator factory = new ThreadedInterestPointGenerator(mInputFolder,mGroupId,mDAO);
		factory.start();
		//TODO: This stop might accidentally kill the last couple of points that need to be inserted if the start() polls at an unfortunate time
		factory.stop();
		
		logger.info("Interest points created, moving onto building the vocabulary tree");
		
		File treeFile = new File(mTempFolder,"tree.bin");
		KMeansTree tree = null;
		if ( !treeFile.exists() ){
			tree = VocabTreeManager.makeSampledTree(Integer.MAX_VALUE,mClusterer,mGroupId,mSamplingRate,mDAO);
			tree.resetBreadthList();
			SerializationUtils.saveObject(tree,treeFile);
		} else {
			tree = VocabTreeManager.loadVocabTree(treeFile.getAbsolutePath());
		}
		logger.info("Vocab tree created, moving onto building the index");
		
		InterestPointCache fullCache = new DirectDBInterestPointCache(mGroupId,Integer.MAX_VALUE,mDAO);
		L1ImageWordIndex index = new L1ImageWordIndex(tree,fullCache);
		File indexFile = new File(mTempFolder,"index.bin");
		SerializationUtils.saveObject(index,indexFile);
		
		logger.info("Big index created, moving onto building the queryable index");
		tree.resetBreadthList();
		
		new QueryTree(tree,mIndexFolder);
		System.out.println("index loaded");
		new WordInvertedIndex(index,mIndexFolder);
		
		logger.info("Done with index creation, nuking temp files");
		
		//treeFile.delete();
		//indexFile.delete();
		
		logger.info("All done!");
	}
	
	/**
	 * Fetches the next group id from the db, basically creates a new slate for this run.
	 * @return
	 */
	private void fetchNextGroupId(){
		if ( mGroupId == -1 ){
			java.sql.Connection conn = DBConnectionManager.getConnection();
			ResultSet rs;
			try {
				rs = conn.prepareStatement("select max(group_id) from " + mDAO.getTablePrefix() + "files").executeQuery();
				if ( rs.next() ) {
					mGroupId = rs.getInt(1) + 1;
				} else {
					mGroupId = 0;
				}
			} catch (SQLException e) {
				logger.fatal(e,e);
			}
		}
	}
	
	public static void main(String args[]){
		if ( args.length != 6 && args.length != 7 ){
			System.out.println("Usage: ant builder -Dargs=\"image_input_folder temp_folder output_folder sift|surf kmeans|kmeansforest [group_id] [samplingRate]\"");
			System.exit(0);
		}
		String sourceDirectory = args[0];
		String tempDirectory = args[1];
		String indexDirectory = args[2];
		InterestPointDAO dao = "sift".equals(args[3]) ? new SIFTInterestPointDAO() : new SURFInterestPointDAO();
		KClusterer clusterer = "kmeans".equals(args[4]) ? new KMeansClusterer() : new KMeansForestClusterer();
		int groupId = -1;
		if ( args.length > 5 ){
			groupId = Integer.parseInt(args[5]);
		}
		int samplingRate = 1;
		if ( args.length > 6 ){
			samplingRate = Integer.parseInt(args[6]);
		}
		System.out.println("Run information");
		System.out.println("tempDirectory = " + tempDirectory);
		System.out.println("indexDirectory = " + indexDirectory);
		System.out.println("sourceDirectory = " + sourceDirectory);
		System.out.println("dao = " + dao.getClass());
		System.out.println("clusterer = " + clusterer.getClass());
		System.out.println("groupId = " + groupId);
		System.out.println("samplingRate = " + samplingRate);
		try {
			VisualSearchIndexBuilder builder = new VisualSearchIndexBuilder(sourceDirectory, indexDirectory, tempDirectory, dao, clusterer, groupId, samplingRate);
			builder.run();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}
