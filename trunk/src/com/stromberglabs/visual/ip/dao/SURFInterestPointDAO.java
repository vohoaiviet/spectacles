package com.stromberglabs.visual.ip.dao;

import java.io.IOException;

import com.stromberglabs.visual.ip.creator.SURFInterestPointCreator;

public class SURFInterestPointDAO extends AbstractInterestPointDAO {
	private static String mSaveSql = "insert into descriptors values(NULL,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static String mCountSql = "select count(id) from descriptors where filename = ?";
	private static final String mSaveFileSql = "insert into files values(?,?)";
	
	public SURFInterestPointDAO(){
		super(new SURFInterestPointCreator());
	}
	
	protected String getSaveSQL() {
		return mSaveSql;
	}

	protected String getCountSQL() {
		return mCountSql;
	}
	
	//part 1 of 3 of creating an index, run VocabTreeManager next
	public static void main(String[] args){
		SURFInterestPointDAO factory = new SURFInterestPointDAO();
		try {
			factory.getCreator().getPoints(args[0]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getTablePrefix() {
		return "";
	}

	protected String getFileSaveSQL() {
		return mSaveFileSql;
	}
}
