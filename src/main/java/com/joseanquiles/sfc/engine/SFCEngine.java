package com.joseanquiles.sfc.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.joseanquiles.sfc.comparator.SFCComparator;
import com.joseanquiles.sfc.configuration.FileComparatorConfiguration;
import com.joseanquiles.sfc.filter.SFCFilter;
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
	
	public List<SFCResult> run() {

		List<SFCResult> result = new ArrayList<>();
		
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
				
		// STEP 1: files in left not in right
		for (int i = 0; i < leftFiles.size(); i++) {
			File f1 = leftFiles.get(i);
			File f2 = FileUtil.transformBasePath(leftFile, rightFile, f1);
			if (!f2.exists()) {
				SFCResult r = new SFCResult();
				r.leftFile = f1;
				r.rightFile = null;
				r.status = SFCStatus.LEFT;
				result.add(r);
			}
		}

		// STEP 2: files in right not in left
		for (int i = 0; i < rightFiles.size(); i++) {
			File f2 = rightFiles.get(i);
			File f1 = FileUtil.transformBasePath(rightFile, leftFile, f2);
			if (!f1.exists()) {
				SFCResult r = new SFCResult();
				r.leftFile = null;
				r.rightFile = f2;
				r.status = SFCStatus.RIGHT;
				result.add(r);
			}
		}

		// STEP 3: files in both, left and right
		List<File> common1 = new ArrayList<File>();
		List<File> common2 = new ArrayList<File>();
		for (int i = 0; i < leftFiles.size(); i++) {
			File f1 = leftFiles.get(i);
			File f2 = FileUtil.transformBasePath(leftFile, rightFile, f1);
			if (f1.exists() && f2.exists()) {
				common1.add(f1);
				common2.add(f2);
			}			
		}
						
		// Process common files
		// 1 - filter
		// 2 - compare
		
		for (int i = 0; i < common1.size(); i++) {
			
			try {
				File f1 = common1.get(i);
				File f2 = common2.get(i);

				// read files
				List<String> f1Lines = FileUtil.file2Lines(f1);
				List<String> f2Lines = FileUtil.file2Lines(f2);
				
				// FILTERS
				
				List<SFCFilter> f1Filters = this.config.getFiltersForFile(f1);
				List<SFCFilter> f2Filters = this.config.getFiltersForFile(f2);
				
				for (int f = 0; f < f1Filters.size(); f++) {
					f1Lines = f1Filters.get(f).run(f1Lines);
					f2Lines = f2Filters.get(f).run(f2Lines);
				}

				// COMPARATORS
				
				List<SFCComparator> f1Comparators = this.config.getComparatorsForFile(f1);
				
				SFCResult r = new SFCResult();
				r.leftFile = f1;
				r.rightFile = f2;
				for (int c = 0; c < f1Comparators.size(); c++) {
					List<String> diffs = f1Comparators.get(c).run(f1Lines, f2Lines);
					if (diffs.size() > 0) {
						r.status = SFCStatus.DIFFERENT;
						for (int d = 0; d < diffs.size(); d++) {
							r.differences.add(diffs.get(d));
						}
					}
				}
				if (r.status == SFCStatus.DIFFERENT) {
					result.add(r);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		return result;
				
	}
	
	public static void main(String[] args) throws Exception {
		String left = "./src/test/resources/test1/left";
		String right = "./src/test/resources/test1/right";
		String configFile = "./src/test/resources/test1/sfc.config.yaml";
		SFCEngine engine = new SFCEngine(configFile, left, right, null);
		List<SFCResult> result = engine.run();
		for (int i = 0; i < result.size(); i++) {
			System.out.println(result.get(i));
		}
	}
	
}
