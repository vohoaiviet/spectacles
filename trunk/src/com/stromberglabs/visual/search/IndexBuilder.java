package com.stromberglabs.visual.search;

import java.io.File;

import com.stromberglabs.index.WordInvertedIndex;
import com.stromberglabs.tree.KMeansTree;
import com.stromberglabs.tree.query.QueryTree;
import com.stromberglabs.util.file.SerializationUtils;
import com.stromberglabs.visual.ip.cache.DirectDBInterestPointCache;
import com.stromberglabs.visual.ip.cache.InterestPointCache;
import com.stromberglabs.visual.ip.dao.InterestPointDAO;
import com.stromberglabs.visual.ip.dao.SIFTInterestPointDAO;
import com.stromberglabs.visual.ip.dao.SURFInterestPointDAO;
import com.stromberglabs.visual.tree.VocabTreeManager;

/**
 * Just some glue to do all the correct actions for indexing. After this
 * run {@link WordInvertedIndex} to get a web index for searching
 * 
 * @author Andrew
 *
 */
public class IndexBuilder {
	public static void main(String args[]){
		if ( args.length != 5 ){
			System.out.println("Usage: ant index -Dargs=\"vocab_tree_file group_id sift|surf index_obj_file web-index_dir\"");
			System.exit(0);
		}
		InterestPointDAO dao = "sift".equals(args[2]) ? new SIFTInterestPointDAO() : new SURFInterestPointDAO();
		KMeansTree tree = VocabTreeManager.loadVocabTree(args[0]);
		InterestPointCache fullCache = new DirectDBInterestPointCache(Integer.parseInt(args[1]),Integer.MAX_VALUE,dao);
		L1ImageWordIndex index = new L1ImageWordIndex(tree,fullCache);
		System.out.println("Going to write to " + args[3]);
		SerializationUtils.saveObject(index,new File(args[3]));
		tree.resetBreadthList();
		new QueryTree(tree,new File(args[4]));
		System.out.println("index loaded");
		new WordInvertedIndex(index,new File(args[4]));
		System.out.println("web index built");
	}
}
