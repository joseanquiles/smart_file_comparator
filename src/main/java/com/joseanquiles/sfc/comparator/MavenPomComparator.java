package com.joseanquiles.sfc.comparator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.joseanquiles.sfc.util.FileUtil;

import garcia.herrero.elena.model.PomComparation;
import garcia.herrero.elena.service.PomService;

public class MavenPomComparator implements SFCComparator {

	@Override
	public void setParameters(Map<String, String> params) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<String> run(List<String> leftLines, List<String> rightLines) {
		
		List<String> result = new ArrayList<>();

		String leftFile = null;
		String rightFile = null;
		
		try {
		    
			leftFile = FileUtil.writeLinesToTmpFile("left_", leftLines);
			rightFile = FileUtil.writeLinesToTmpFile("right_", rightLines);
		    		
			PomService pomService = new PomService();
		    Map<String, String> sortedDependenciesForPom1 = pomService.getSortedDependencies(new FileReader(leftFile));
		    Map<String, String> sortedDependenciesForPom2 = pomService.getSortedDependencies(new FileReader(rightFile));
	        Map<String, PomComparation> comparePoms = pomService.comparePoms(sortedDependenciesForPom1,
	                sortedDependenciesForPom2);
	        String versions = pomService.getComparatiosAsString(comparePoms);
	        
	        String[] lines = versions.split("\n");
	        for (String line : lines) {
	            String[] words = line.split("\t");
	            if (words.length >= 3 && !"DEPENDENCY".equalsIgnoreCase(words[0].trim())) {
	            	if (!words[1].equals(words[2])) {
	            		result.add(words[0].trim() + " : " + words[1].trim() + " != " + words[2].trim());
	            	}
	            }
	        }			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (leftFile != null) {
				new File(leftFile).delete();
			}
			if (rightFile != null) {
				new File(rightFile).delete();
			}
		}
		
		return result;
	}
	
}
