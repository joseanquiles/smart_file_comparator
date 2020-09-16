package com.joseanquiles.sfc.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.joseanquiles.sfc.configuration.FileComparatorConfiguration;
import com.joseanquiles.sfc.util.FileUtil;

public class SFCEngine {

	private FileComparatorConfiguration config;
	
	public SFCEngine(String configFile, String left, String right, String output) throws Exception {
		
		this.config = new FileComparatorConfiguration(configFile);

		List<File> sourceFiles = FileUtil.exploreDir(source, config.getIgnoreFiles(), config.getIgnoreDirs());
		List<File> revisedFiles = FileUtil.exploreDir(revised, config.getIgnoreFiles(), config.getIgnoreDirs());
		
		List<File> deleted = new ArrayList<File>();
		List<File> created = new ArrayList<File>();
		List<File> common1 = new ArrayList<File>();
		List<File> common2 = new ArrayList<File>();
		
		// STEP 1: files in original not in revised
		for (int i = 0; i < sourceFiles.size(); i++) {
			File f1 = sourceFiles.get(i);
			File f2 = FileUtil.transformBasePath(source, revised, f1);
			if (!f2.exists()) {
				deleted.add(f1);
			} else {
				common1.add(f1);
				common2.add(f2);
			}
		}
		System.out.println("===============================================================");
		System.out.println("DELETED FILES, total " + deleted.size());
		for (int i = 0; i < deleted.size(); i++) {
			System.out.println(deleted.get(i));
		}

		// STEP 2: files in revised not in source
		for (int i = 0; i < revisedFiles.size(); i++) {
			File f2 = revisedFiles.get(i);
			File f1 = FileUtil.transformBasePath(revised, source, f2);
			if (!f1.exists()) {
				created.add(f2);
			}
		}
		System.out.println("===============================================================");
		System.out.println("CREATED FILES, total " + created.size());
		for (int i = 0; i < created.size(); i++) {
			System.out.println(created.get(i));
		}

		// STEP 3: files in both, source and revised
		System.out.println("===============================================================");
		System.out.println("MODIFIED FILES");
		for (int i = 0; i < common1.size(); i++) {
			
			File originalFile = common1.get(i);
			File revisedFile = common2.get(i);
			
			List<SFCFilter> pluginList = config.getPluginsForFile(originalFile);
							
			FileComparator fc = new FileComparator(originalFile, revisedFile);
			List<AbstractDelta<String>> deltas = fc.compare(pluginList);
			if (deltas.size() > 0) {
				System.out.println("===================================================");
				System.out.println("FICHERO:" + common1.get(i));
				if (!nodiff) {
					System.out.println("---------------------");
					for (int j = 0; j < deltas.size(); j++) {
						System.out.println(DeltaUtil.delta2String(deltas.get(j)));
					}
				}
			}
		}
		
		System.out.println("===============================================================");

		
	}
	
}
