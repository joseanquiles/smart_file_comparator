package com.joseanquiles.sfc.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
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
		String bf = getDirectory(baseFrom);
		String bt = getDirectory(baseTo);
		String fn = filename.getPath();
		return new File(fn.replace(bf, bt));
	}

	public static String writeLinesToTmpFile(String prefix, List<String> lines) throws IOException {
	    // create temporal file
	    File file = File.createTempFile(prefix, ".tmp");

	    FileOutputStream fos = new FileOutputStream(file);
	    
	    for (int i = 0; i < lines.size(); i++) {
	    	fos.write(lines.get(i).getBytes());
	    	fos.write("\n".getBytes());
	    }
	    
	    fos.close();
	    
	    return file.toString();
	}

	private static String getDirectory(File f) {
		if (f.isFile()) {
			return f.getParent();
		} else {
			return f.getPath();
		}
	}
	
	private static boolean ignoreFile(File file, List<String> ignoreFiles) {
		for (int i = 0; i < ignoreFiles.size(); i++) {
			if (file.getName().matches(ignoreFiles.get(i))) {
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
	
	public static void main(String[] args) {
		File f1 = new File("D:/a/b/c/file.txt");
		File f2 = new File("E:/a/x/g/file.txt");
		File d1 = new File("D:/a/b/c/");
		File d2 = new File("E:/a/x/g");
		System.out.println("PARENT OF FILE: " + f1.getParent() + "," + f2.getParent());
		System.out.println("PATH OF FILE: " + f1.getPath() + "," + f2.getPath());
		System.out.println("PARENT OF DIR: " + d1.getParent() + "," + d2.getParent());
		System.out.println("PATH OF DIR: " + d1.getPath() + "," + d2.getPath());
		
		File t = FileUtil.transformBasePath(d1,  d2, f1);
		System.out.println("TRANSFORMED: " + f1 + " -> " + t);
		t = FileUtil.transformBasePath(f1,  f2, f1);
		System.out.println("TRANSFORMED: " + f1 + " -> " + t);
		
		List<String> ignoreFiles = new ArrayList<>();
		ignoreFiles.add(".*\\.class$");
		ignoreFiles.add(".*\\.docx$");
		ignoreFiles.add(".*\\.doc$");
		ignoreFiles.add("\\.classpath$");
		ignoreFiles.add("\\.project$");
		
		System.out.println(ignoreFile(new File("D:/a/b/c/a.doc"), ignoreFiles));
		System.out.println(ignoreFile(new File("D:/a/b/c/.classpath"), ignoreFiles));
		System.out.println(ignoreFile(new File("D:/a/b/c/.project"), ignoreFiles));
		System.out.println(ignoreFile(new File("D:/a/b/c/d/test.java"), ignoreFiles));
	}
	
}
