package com.joseanquiles.sfc.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.joseanquiles.sfc.configuration.FileComparatorConfiguration;
import com.joseanquiles.sfc.util.FileUtil;

public class SFCEngine {

	private FileComparatorConfiguration config;
	private File leftFile;
	private File rightFile;
	
	public SFCEngine(String configFile, String left, String right, String output) throws Exception {
		
		this.config = new FileComparatorConfiguration(configFile);
		
		this.leftFile = new File(left);
		this.rightFile = new File(right);
		
		if (leftFile.isFile() && rightFile.isDirectory()) {
			throw new Exception(left + " is a file, but " + right + " is a directory. Cannot compare!");
		}
		if (leftFile.isDirectory() && rightFile.isFile()) {
			throw new Exception(left + " is a directory, but " + right + " is a file. Cannot compare!");
		}
		
	}
	
	public void run() {

		List<File> leftFiles;
		List<File> rightFiles;
		
		if (leftFile.isFile()) {
			// files
			leftFiles = new ArrayList<>();
			leftFiles.add(leftFile);
			rightFiles = new ArrayList<>();
			rightFiles.add(rightFile);
		} else {
			// directories
			leftFiles = FileUtil.exploreDir(leftFile, config.getIgnoreFiles(), config.getIgnoreDirs());
			rightFiles = FileUtil.exploreDir(rightFile, config.getIgnoreFiles(), config.getIgnoreDirs());			
		}
		
		
		List<File> deleted = new ArrayList<File>();
		List<File> created = new ArrayList<File>();
		List<File> common1 = new ArrayList<File>();
		List<File> common2 = new ArrayList<File>();
		
		// STEP 1: files in left not in right
		for (int i = 0; i < leftFiles.size(); i++) {
			File f1 = leftFiles.get(i);
			File f2 = FileUtil.transformBasePath(leftFile, rightFile, f1);
			if (!f2.exists()) {
				deleted.add(f1);
			} else {
				common1.add(f1);
				common2.add(f2);
			}
		}

		// STEP 2: files in right not in left
		for (int i = 0; i < rightFiles.size(); i++) {
			File f2 = rightFiles.get(i);
			File f1 = FileUtil.transformBasePath(rightFile, leftFile, f2);
			if (!f1.exists()) {
				created.add(f2);
			}
		}

		// STEP 3: files in both, left and right
		for (int i = 0; i < common1.size(); i++) {
			
		}
		
		PrintList("DELETED: ", deleted);
		PrintList("CREATED: ", created);
		PrintList("COMMON1: ", common1);
		PrintList("COMMON2: ", common1);
		
		// Process common files
		// 1 - filter
		// 2 - compare
				
	}
	
	private static final void PrintList(String msg, List<File> list) {
		System.out.println(msg);
		for (int i = 0; i < list.size(); i++) {
			System.out.println(list.get(i));
		}
	}
	
	public static void main(String[] args) throws Exception {
		String left = "d:\\REPOSITORIOS\\INFA\\0_AT\\srv-nuc-jee\\srv-nuc-jee-SPServiceSPIntTask\\tags\\6.9.0-1-3\\";
		String right = "d:\\REPOSITORIOS\\INFA\\1_CO\\srv-nuc-jee\\srv-nuc-jee-SPServiceSPIntTask\\trunk\\";
		String configFile = "k:\\MisProyectos\\smart_file_comparator\\src\\test\\resources\\config.sample.yaml";
		SFCEngine engine = new SFCEngine(configFile, left, right, null);
		engine.run();
	}
	
}
