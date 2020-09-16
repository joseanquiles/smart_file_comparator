package com.joseanquiles.sfc.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

	public static  List<String> file2Lines(File file) throws IOException {
		List<String> lines = new ArrayList<String>();
		String line;
		BufferedReader br = new BufferedReader(new FileReader(file));
		while ((line = br.readLine()) != null) {
			lines.add(line);
		}
		br.close();
		return lines;
	}
	
	public static String getFileExtension(String filename) {
		int i = filename.lastIndexOf('.');
		if (i < 0) {
			return "";
		} else {
			return filename.substring(i+1);
		}
	}
	
	public static List<File> exploreDir(File baseDir, List<String> ignoreFiles, List<String> ignoreDirs) {
		List<File> fileList = new ArrayList<File>();
		List<String> ignoreAbsoluteDirs = new ArrayList<>();
		for (int i = 0; i < ignoreDirs.size(); i++) {
			String d = baseDir.getAbsolutePath() + "/" + ignoreDirs.get(i);
			ignoreAbsoluteDirs.add(d);
		}
		exploreDirInternal(baseDir, fileList, ignoreFiles, ignoreAbsoluteDirs);
		return fileList;
	}
	
	public static File transformBasePath(File baseFrom, File baseTo, File filename) {
		String bf = baseFrom.getPath();
		String bt = baseTo.getPath();
		String fn = filename.getPath();
		return new File(fn.replace(bf, bt));
	}
	
	private static boolean ignoreFile(File file, List<String> ignoreFiles) {
		for (int i = 0; i < ignoreFiles.size(); i++) {
			if (file.getName().endsWith(ignoreFiles.get(i))) {
				return true;
			}
		}
		return false;
	}
	
	private static void exploreDirInternal(File dir, List<File> fileList, List<String> ignoreFiles, List<String> ignoreDirs) {
		if (dir.isDirectory()) {
			// check ignored dirs
			for (int i = 0; i < ignoreDirs.size(); i++) {
				File df = new File(ignoreDirs.get(i));
				try {
					if (dir.getCanonicalPath().equals(df.getCanonicalPath())) {
						//System.out.println("*** IGNORE ***" + dir);
						return;
					}					
				} catch (Exception e) {
					// ignore
				}
			}
			File[] files = dir.listFiles();
			for (int i = 0;i < files.length; i++) {
				if (files[i].isFile() && !ignoreFile(files[i], ignoreFiles)) {
					fileList.add(files[i]);
				} else if (files[i].isDirectory()) {
					exploreDirInternal(files[i], fileList, ignoreFiles, ignoreDirs);
				}
			}
		} else if (dir.exists()) {
			fileList.add(dir);
			return;
		}
	}
	
}
